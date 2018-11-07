/*
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.aevi.sdk.pos.flow.paymentinitiationsample.ui.fragment;


import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.widget.Toast;
import butterknife.BindView;
import butterknife.OnClick;
import butterknife.OnItemSelected;
import com.aevi.android.rxmessenger.MessageException;
import com.aevi.sdk.flow.constants.AdditionalDataKeys;
import com.aevi.sdk.flow.model.Request;
import com.aevi.sdk.flow.model.Response;
import com.aevi.sdk.flow.model.config.FlowConfig;
import com.aevi.sdk.pos.flow.PaymentApi;
import com.aevi.sdk.pos.flow.PaymentClient;
import com.aevi.sdk.pos.flow.model.Amounts;
import com.aevi.sdk.pos.flow.model.PaymentResponse;
import com.aevi.sdk.pos.flow.model.TransactionResponse;
import com.aevi.sdk.pos.flow.model.config.PaymentSettings;
import com.aevi.sdk.pos.flow.paymentinitiationsample.R;
import com.aevi.sdk.pos.flow.paymentinitiationsample.model.SampleContext;
import com.aevi.sdk.pos.flow.paymentinitiationsample.ui.GenericResultActivity;
import com.aevi.sdk.pos.flow.paymentinitiationsample.ui.RequestInitiationActivity;
import com.aevi.sdk.pos.flow.sample.CustomerProducer;
import com.aevi.sdk.pos.flow.sample.ui.ModelDisplay;
import com.aevi.ui.library.BaseObservableFragment;
import com.aevi.ui.library.DropDownHelper;
import com.aevi.ui.library.recycler.DropDownSpinner;
import io.reactivex.disposables.Disposable;

import java.util.List;

import static android.content.Intent.*;
import static com.aevi.sdk.flow.constants.FlowTypes.FLOW_TYPE_CASH_RECEIPT_DELIVERY;
import static com.aevi.sdk.flow.constants.FlowTypes.FLOW_TYPE_REVERSAL;
import static com.aevi.sdk.flow.constants.PaymentMethods.PAYMENT_METHOD_CASH;
import static com.aevi.sdk.flow.constants.ReceiptKeys.*;

public class GenericRequestFragment extends BaseObservableFragment {

    private static final String SHOW_LOYALTY_POINTS_REQUEST = "showLoyaltyPointsBalance";
    private static final String UNSUPPORTED_FLOW = "unsupportedFlowType";

    @BindView(R.id.request_flow_spinner)
    DropDownSpinner requestFlowSpinner;

    private String selectedApiRequestFlow;
    private ModelDisplay modelDisplay;
    private Request request;

    private PaymentClient paymentClient;
    private PaymentSettings paymentSettings;
    private Disposable initiateDisposable;

    @Override
    public int getLayoutResource() {
        return R.layout.fragment_generic_request;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        modelDisplay = ((RequestInitiationActivity) getActivity()).getModelDisplay();
        final DropDownHelper dropDownHelper = new DropDownHelper(getActivity());
        paymentClient = PaymentApi.getPaymentClient(getContext());

        PaymentClient paymentClient = SampleContext.getInstance(getActivity()).getPaymentClient();
        paymentClient.getPaymentSettings()
                .subscribe(paymentSettings -> {
                    GenericRequestFragment.this.paymentSettings = paymentSettings;
                    List<String> flowTypes = paymentSettings.getFlowConfigurations().getFlowTypes(FlowConfig.REQUEST_CLASS_GENERIC);
                    flowTypes.add(UNSUPPORTED_FLOW); // For illustration of what happens if you initiate a request with unsupported flow
                    dropDownHelper.setupDropDown(requestFlowSpinner, flowTypes, false);
                }, throwable -> dropDownHelper.setupDropDown(requestFlowSpinner, R.array.request_flows));
    }

    @OnItemSelected(R.id.request_flow_spinner)
    public void onRequestTypeSelection(int position) {
        selectedApiRequestFlow = (String) requestFlowSpinner.getAdapter().getItem(position);
        this.request = createRequest();
        if (request != null && modelDisplay != null) {
            modelDisplay.showRequest(request);
        }
    }

    @OnClick(R.id.send)
    public void onProcessRequest() {
        if (request != null) {
            Intent intent = new Intent(getContext(), GenericResultActivity.class);
            intent.addFlags(FLAG_ACTIVITY_NEW_TASK | FLAG_ACTIVITY_CLEAR_TOP | FLAG_ACTIVITY_REORDER_TO_FRONT | FLAG_ACTIVITY_NO_ANIMATION);
            initiateDisposable = paymentClient.initiateRequest(request)
                    .subscribe(response -> {
                        if (isAdded()) {
                            intent.putExtra(GenericResultActivity.GENERIC_RESPONSE_KEY, response.toJson());
                            startActivity(intent);
                        }
                    }, throwable -> {
                        Response response;
                        if (throwable instanceof MessageException) {
                            response = new Response(request, false, ((MessageException) throwable).getCode()
                                    + " : " + throwable.getMessage());
                        } else {
                            response = new Response(request, false, throwable.getMessage());
                        }
                        if (isAdded()) {
                            intent.putExtra(GenericResultActivity.GENERIC_RESPONSE_KEY, response.toJson());
                            startActivity(intent);
                        }
                    });
        }
    }

    private Request createRequest() {
        if (paymentSettings == null) {
            return null; // Wait for settings to come back first
        }
        Request request = new Request(selectedApiRequestFlow);
        PaymentResponse lastResponse = SampleContext.getInstance(getContext()).getLastReceivedPaymentResponse();

        // Some types require additional information
        switch (request.getRequestType()) {
            case FLOW_TYPE_REVERSAL:
                if (lastResponse == null || lastResponse.getTransactions().isEmpty() || !lastResponse.getTransactions().get(0).hasResponses()) {
                    Toast.makeText(getContext(), "Please complete a successful payment before using this request type", Toast.LENGTH_SHORT).show();
                    return null;
                }
                request.addAdditionalData(AdditionalDataKeys.DATA_KEY_TRANSACTION_ID,
                                          lastResponse.getTransactions().get(0).getLastResponse().getId());
                break;
            case FLOW_TYPE_CASH_RECEIPT_DELIVERY:
                Amounts cashAmounts = new Amounts(15000, "EUR");
                String paymentMethod = PAYMENT_METHOD_CASH;
                String outcome = TransactionResponse.Outcome.APPROVED.name();
                request.addAdditionalData(RECEIPT_AMOUNTS, cashAmounts);
                request.addAdditionalData(RECEIPT_PAYMENT_METHOD, paymentMethod);
                request.addAdditionalData(RECEIPT_OUTCOME, outcome);
                break;
            case SHOW_LOYALTY_POINTS_REQUEST:
                request.addAdditionalData("customer", CustomerProducer.getDefaultCustomer("Payment Initiation Sample"));
                break;
            default:
                // No extra data required
                break;
        }
        return request;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (initiateDisposable != null) {
            initiateDisposable.dispose();
            initiateDisposable = null;
        }
    }
}
