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

package com.aevi.sdk.pos.flow.paymentservicesample;


import android.support.annotation.NonNull;
import android.util.Log;

import com.aevi.sdk.pos.flow.model.Merchant;
import com.aevi.sdk.pos.flow.model.PaymentFlowServiceInfo;
import com.aevi.sdk.pos.flow.model.PaymentFlowServiceInfoBuilder;
import com.aevi.sdk.pos.flow.provider.BasePaymentFlowServiceInfoProvider;
import com.aevi.sdk.pos.flow.sample.IdProvider;

import static com.aevi.sdk.flow.constants.AppMessageTypes.FLOW_EVENT;
import static com.aevi.sdk.flow.constants.FlowTypes.*;
import static com.aevi.sdk.flow.constants.ServiceInfoErrors.*;

public class PaymentServiceInfoProvider extends BasePaymentFlowServiceInfoProvider {

    private static final String TAG = PaymentServiceInfoProvider.class.getSimpleName();

    @Override
    protected PaymentFlowServiceInfo getPaymentFlowServiceInfo() {
        String[] supportedCurrencies = getContext().getResources().getStringArray(R.array.supported_currencies);
        String[] supportedPaymentMethods = getContext().getResources().getStringArray(R.array.payment_methods);

        return new PaymentFlowServiceInfoBuilder()
                .withVendor("AEVI")
                .withDisplayName("Payment Service Sample")
                .withCanPayAmounts(true, supportedPaymentMethods)
                .withSupportedCurrencies(supportedCurrencies)
                .withDefaultCurrency(supportedCurrencies[0])
                .withSupportedFlowTypes(FLOW_TYPE_SALE, FLOW_TYPE_REFUND, FLOW_TYPE_REVERSAL, FLOW_TYPE_PRE_AUTHORISATION, FLOW_TYPE_PRE_AUTH_COMPLETION, FLOW_TYPE_TOKENISATION, FLOW_TYPE_BATCH_CLOSURE)
                .withCustomRequestTypes(FLOW_EVENT, "handshake") // handshake is specific to Chase/xPap
                .withMerchants(new Merchant(IdProvider.getMerchantId(), IdProvider.getMerchantName()))
                .withManualEntrySupport(false)
                .withSupportsAccessibilityMode(false)
                .build(getContext());
    }

    @Override
    protected boolean onServiceInfoError(@NonNull String errorType, @NonNull String errorMessage) {
        switch (errorType) {
            case RETRIEVAL_TIME_OUT:
                Log.d(TAG, "Retrieval of service info timed out");
                break;
            case INVALID_STAGE_DEFINITIONS:
                Log.d(TAG, "Problems with stage definitions: " + errorMessage);
                break;
            case INVALID_SERVICE_INFO:
                Log.d(TAG, "Invalid service info: " + errorMessage);
                break;
        }
        return true;
    }
}
