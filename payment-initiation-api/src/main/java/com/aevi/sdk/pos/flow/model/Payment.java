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
import android.util.Log;

import com.aevi.sdk.flow.model.AdditionalData;
import com.aevi.sdk.flow.model.BaseModel;
import com.aevi.sdk.flow.model.Customer;
import com.aevi.sdk.flow.model.Token;
import com.aevi.util.json.JsonConverter;

import java.util.Objects;
import java.util.UUID;

import static com.aevi.sdk.flow.util.Preconditions.checkArgument;

/**
 * Payment model containing relevant information for initiating payments involving amounts.
 *
 * For other type of requests (such as reversals), please see {@link com.aevi.sdk.flow.model.Request}
 */
public class Payment extends BaseModel {

    private final String flowType;
    private final String flowName;
    private final Amounts amounts;
    private final String paymentMethod;
    private final Basket basket;
    private final Customer customer;
    private final boolean splitEnabled;
    private final Token cardToken;
    private final AdditionalData additionalData;
    private final boolean isExternalId;
    private final String source;
    private final String deviceId;

    // Default constructor for deserialisation
    Payment() {
        super("N/A");
        this.flowType = "";
        this.flowName = null;
        this.isExternalId = false;
        this.amounts = new Amounts();
        this.paymentMethod = null;
        this.basket = null;
        this.customer = null;
        this.splitEnabled = false;
        this.cardToken = null;
        this.additionalData = new AdditionalData();
        this.source = "";
        this.deviceId = null;
    }

    /**
     * Internal constructor for use from the Builder.
     */
    Payment(String flowType, String flowName, Amounts amounts, Basket basket, Customer customer, boolean splitEnabled, Token cardToken,
            AdditionalData additionalData, String source, String deviceId, String paymentMethod) {
        super(UUID.randomUUID().toString());
        Log.i(Payment.class.getSimpleName(), "Created Payment with (internal) id: " + getId());
        this.flowType = flowType;
        this.flowName = flowName;
        this.isExternalId = false;
        this.amounts = amounts != null ? amounts : new Amounts();
        this.basket = basket;
        this.paymentMethod = paymentMethod;
        this.customer = customer;
        this.splitEnabled = splitEnabled;
        this.cardToken = cardToken;
        this.additionalData = additionalData != null ? additionalData : new AdditionalData();
        this.source = source;
        this.deviceId = deviceId;
        validateArguments();
    }

    /**
     * Package level constructor, allowing an internal client to set an external id to use for this payment.
     *
     * This is useful when a request from a different SDK/API is translated to our Payment model.
     */
    Payment(String id, String requestSource, String flowType, String flowName, Amounts amounts, Basket basket, Customer customer,
            boolean splitEnabled, Token cardToken, AdditionalData additionalData, String deviceId, String paymentMethod) {
        super(id);
        Log.i(Payment.class.getSimpleName(), "Created Payment with (external) id: " + id);
        this.source = requestSource;
        this.isExternalId = true;
        this.flowType = flowType;
        this.flowName = flowName;
        this.amounts = amounts != null ? amounts : new Amounts();
        this.basket = basket;
        this.paymentMethod = paymentMethod;
        this.customer = customer;
        this.splitEnabled = splitEnabled;
        this.cardToken = cardToken;
        this.additionalData = additionalData != null ? additionalData : new AdditionalData();
        this.deviceId = deviceId;
        validateArguments();
    }

    private void validateArguments() {
        checkArgument(getId() != null, "Id cannot be null");
        checkArgument(flowType != null, "Flow type cannot be null");
        checkArgument(amounts != null, "Amounts cannot be null (but can be zero)");
    }

    /**
     * Get the flow type (aka transaction type) associated with this payment.
     *
     * The flow type is either set by the client or derived from the chosen flow this payment will processed by.
     *
     * In cases where only the flow type is set for the payment, FPS will look up matching flows from it and assign a flow name.
     *
     * @return The flow type
     */
    @NonNull
    public String getFlowType() {
        return flowType;
    }

    /**
     * Get the name of the flow that will be processing this payment.
     *
     * This may be null if the client has not specified it.
     *
     * @return The name of the flow to execute
     */
    @Nullable
    public String getFlowName() {
        return flowName;
    }

    /**
     * Get the amounts associated with this payment.
     *
     * @return The amounts
     */
    @NonNull
    public Amounts getAmounts() {
        return amounts;
    }

    /**
     * Get the basket for this payment.
     *
     * @return The basket if set, or null
     */
    @Nullable
    public Basket getBasket() {
        return basket;
    }

    /**
     * Get the payment method to use for transaction processing for this payment.
     *
     * Note that flow services may still make use of other payment methods for other stages of the flow.
     *
     * @return The payment method if set, or null
     */
    @Nullable
    public String getPaymentMethod() {
        return paymentMethod;
    }

    /**
     * Get the customer details for this payment.
     *
     * @return The customer details, or null
     */
    @Nullable
    public Customer getCustomer() {
        return customer;
    }

    /**
     * Whether or not this payment can/should be split into multiple transactions.
     *
     * If set, it is up to the acquirer environment if and how the payment is split into
     * multiple transactions.
     *
     * If not set, it is still possible that split will be enabled during the payment flow.
     *
     * @return True if split is enabled, false otherwise.
     */
    public boolean isSplitEnabled() {
        return splitEnabled;
    }

    /**
     * Get any card token to be used for this transaction.
     *
     * @return The card {@link Token} to be used
     */
    @Nullable
    public Token getCardToken() {
        return cardToken;
    }

    /**
     * Get the extra data set for this payment.
     *
     * This contains optional and bespoke parameters.
     *
     * @return The additional data (may be empty).
     */
    @NonNull
    public AdditionalData getAdditionalData() {
        return additionalData;
    }

    /**
     * Get information on whether the id of this payment was generated internally or externally.
     *
     * For most cases, the id is generated internally, but for cases where requests from another API/SDK
     * is translated to our payment model, the id from that external request will be set and this flag set to true.
     *
     * @return True if external, false if internal
     */
    public boolean isExternalId() {
        return isExternalId;
    }

    /**
     * Get the source of this payment.
     *
     * @return The source of this payment.
     */
    @NonNull
    public String getSource() {
        return source;
    }

    /**
     * Get the id of the device that should be used for customer interactions, if any.
     *
     * @return The device id to use for this payment (may be null)
     */
    @Nullable
    public String getDeviceId() {
        return deviceId;
    }

    @Override
    public String toString() {
        return "Payment{" +
                "flowType='" + flowType + '\'' +
                ", flowName='" + flowName + '\'' +
                ", amounts=" + amounts +
                ", paymentMethod='" + paymentMethod + '\'' +
                ", basket=" + basket +
                ", customer=" + customer +
                ", splitEnabled=" + splitEnabled +
                ", cardToken=" + cardToken +
                ", additionalData=" + additionalData +
                ", isExternalId=" + isExternalId +
                ", source='" + source + '\'' +
                ", deviceId='" + deviceId + '\'' +
                "} " + super.toString();
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

        Payment payment = (Payment) o;
        return splitEnabled == payment.splitEnabled &&
                isExternalId == payment.isExternalId &&
                Objects.equals(flowType, payment.flowType) &&
                Objects.equals(flowName, payment.flowName) &&
                Objects.equals(amounts, payment.amounts) &&
                Objects.equals(paymentMethod, payment.paymentMethod) &&
                Objects.equals(basket, payment.basket) &&
                Objects.equals(customer, payment.customer) &&
                Objects.equals(cardToken, payment.cardToken) &&
                Objects.equals(additionalData, payment.additionalData) &&
                Objects.equals(source, payment.source) &&
                Objects.equals(deviceId, payment.deviceId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), flowType, flowName, amounts, paymentMethod, basket, customer, splitEnabled, cardToken, additionalData,
                isExternalId, source, deviceId);
    }

    @Override
    public String toJson() {
        return JsonConverter.serialize(this);
    }

    public static Payment fromJson(String json) {
        return JsonConverter.deserialize(json, Payment.class);
    }

}
