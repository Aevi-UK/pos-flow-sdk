package com.aevi.flowservicesample;


import com.aevi.flowservicesample.service.GenericRequestService;
import com.aevi.flowservicesample.settings.ServiceStateHandler;
import com.aevi.sdk.flow.constants.FinancialRequestTypes;
import com.aevi.sdk.flow.constants.PaymentMethods;
import com.aevi.sdk.flow.constants.TransactionTypes;
import com.aevi.sdk.flow.model.FlowServiceInfo;
import com.aevi.sdk.pos.flow.model.FlowServiceInfoBuilder;
import com.aevi.sdk.pos.flow.model.PaymentStage;
import com.aevi.sdk.pos.flow.service.BaseFlowServiceInfoProvider;

import java.util.ArrayList;
import java.util.List;

import static com.aevi.sdk.pos.flow.model.PaymentStage.*;

public class FlowServiceInfoProvider extends BaseFlowServiceInfoProvider {

    @Override
    protected FlowServiceInfo getFlowServiceInfo() {
        return new FlowServiceInfoBuilder()
                .withVendor("AEVI")
                .withDisplayName("Flow Service Sample")
                .withStages(getEnabledStages())
                .withCapabilities("sample")
                .withCanAdjustAmounts(true)
                .withCanPayAmounts(true, PaymentMethods.LOYALTY_POINTS, PaymentMethods.GIFT_CARD, PaymentMethods.CASH)
                .withSupportedTransactionTypes(TransactionTypes.SALE)
                .withSupportedRequestTypes(FinancialRequestTypes.PAYMENT, GenericRequestService.SHOW_LOYALTY_POINTS_REQUEST)
                .build(getContext());
    }

    private String[] getEnabledStages() {
        List<String> stages = addEnabledStages(PRE_FLOW, SPLIT, PRE_TRANSACTION, POST_CARD_READING, POST_TRANSACTION, POST_FLOW);
        String[] stagesArray = new String[stages.size()];
        return stages.toArray(stagesArray);
    }

    private List<String> addEnabledStages(PaymentStage... paymentStages) {
        List<String> stages = new ArrayList<>();
        for (PaymentStage paymentStage : paymentStages) {
            if (ServiceStateHandler.isStageEnabled(getContext(), paymentStage)) {
                stages.add(paymentStage.name());
            }
        }
        return stages;
    }
}
