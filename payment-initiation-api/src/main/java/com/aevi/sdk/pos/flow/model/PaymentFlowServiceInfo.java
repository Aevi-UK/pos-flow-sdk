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

package com.aevi.sdk.pos.flow.model;


import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import com.aevi.sdk.flow.model.AdditionalData;
import com.aevi.sdk.flow.model.BaseServiceInfo;
import com.aevi.sdk.flow.util.ComparisonUtil;
import com.aevi.util.json.JsonConverter;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Represents the capabilities of a flow service.
 *
 * See {@link BaseServiceInfo} for inherited information.
 *
 * Use flow-service-api FlowServiceInfoBuilder to construct an instance.
 */
public class PaymentFlowServiceInfo extends BaseServiceInfo {

    private final boolean canAdjustAmounts;
    private final boolean canPayAmounts;
    private final String defaultCurrency;
    private final Set<String> supportedCurrencies;
    private final Set<String> paymentMethods;

    // Default constructor for deserialisation
    PaymentFlowServiceInfo() {
        canAdjustAmounts = false;
        canPayAmounts = false;
        defaultCurrency = "";
        supportedCurrencies = new HashSet<>();
        paymentMethods = new HashSet<>();
    }

    public PaymentFlowServiceInfo(String id, String packageName, String vendor, String serviceVersion, String apiVersion, String displayName,
                                  boolean hasAccessibilityMode, Set<String> supportedFlowTypes, Set<String> supportedRequestTypes,
                                  Set<String> supportedDataKeys, boolean canAdjustAmounts, boolean canPayAmounts,
                                  String defaultCurrency, Set<String> supportedCurrencies, Set<String> paymentMethods,
                                  AdditionalData additionalInfo) {
        super(id, packageName, vendor, serviceVersion, apiVersion, displayName, hasAccessibilityMode, supportedFlowTypes,
              supportedRequestTypes, supportedDataKeys, additionalInfo);
        this.canAdjustAmounts = canAdjustAmounts;
        this.canPayAmounts = canPayAmounts;
        this.defaultCurrency = defaultCurrency;
        this.paymentMethods = paymentMethods != null ? paymentMethods : new HashSet<>();
        this.supportedCurrencies = supportedCurrencies != null ? supportedCurrencies : new HashSet<>();
    }

    /**
     * Gets the set of payment methods supported by the service.
     *
     * See reference values in the documentation for possible values.
     *
     * May be empty.
     *
     * @return Set of supported payment methods
     */
    @NonNull
    public Set<String> getPaymentMethods() {
        return paymentMethods;
    }

    /**
     * Check whether this service supports the given payment method.
     *
     * @param paymentMethod The payment method to check if supported
     * @return True if supported, false otherwise
     */
    public boolean supportsPaymentMethod(String paymentMethod) {
        return paymentMethods.size() > 0 && ComparisonUtil.stringCollectionContainsIgnoreCase(paymentMethods, paymentMethod);
    }

    /**
     * Gets the set of currency codes supported by the service.
     *
     * May be empty.
     *
     * @return Set of String objects indicating the 3-letter ISO 4217 currencies supported by the service.
     */
    @NonNull
    public Set<String> getSupportedCurrencies() {
        return supportedCurrencies;
    }

    /**
     * Check whether this service supports the given currency.
     *
     * @param currency The currency to check if supported
     * @return True if supported, false otherwise
     */
    public boolean supportsCurrency(String currency) {
        return supportedCurrencies.size() > 0 && ComparisonUtil.stringCollectionContainsIgnoreCase(supportedCurrencies, currency);
    }

    /**
     * Check whether this service can adjust the request amounts.
     *
     * This is typically used to add a charge/fee, or for charity, or to split a request.
     *
     * @return True if the service can adjust request amounts, false otherwise
     */
    public boolean canAdjustAmounts() {
        return canAdjustAmounts;
    }

    /**
     * Check whether this service can pay amounts via non-payment card means, such as loyalty points.
     *
     * @return True if the service can pay amounts, false otherwise.
     */
    public boolean canPayAmounts() {
        return canPayAmounts;
    }

    /**
     * Gets the default 3-letter ISO 4217 currency code of the service.
     *
     * @return The default currency of the service.
     */
    @Nullable
    public String getDefaultCurrency() {
        return defaultCurrency;
    }

    @Override
    public boolean equals(Object o) {
        return doEquals(o, false);
    }

    @Override
    public boolean equivalent(Object o) {
        return doEquals(o, true);
    }

    private boolean doEquals(Object o, boolean equiv) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        if (equiv && !super.doEquivalent(o)) {
            return false;
        } else if (!equiv && !super.equals(o)) {
            return false;
        }

        PaymentFlowServiceInfo that = (PaymentFlowServiceInfo) o;
        return canAdjustAmounts == that.canAdjustAmounts &&
                canPayAmounts == that.canPayAmounts &&
                Objects.equals(defaultCurrency, that.defaultCurrency) &&
                Objects.equals(supportedCurrencies, that.supportedCurrencies) &&
                Objects.equals(paymentMethods, that.paymentMethods);
    }

    @Override
    public int hashCode() {

        return Objects.hash(super.hashCode(), canAdjustAmounts, canPayAmounts, defaultCurrency, supportedCurrencies, paymentMethods);
    }

    @Override
    public String toJson() {
        return JsonConverter.serialize(this);
    }

    public static PaymentFlowServiceInfo fromJson(String json) {
        return JsonConverter.deserialize(json, PaymentFlowServiceInfo.class);
    }
}
