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
import com.aevi.sdk.flow.model.BaseModel;
import com.aevi.sdk.flow.model.Customer;
import com.aevi.sdk.flow.model.DeviceAudience;
import com.aevi.util.json.JsonConverter;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static com.aevi.sdk.flow.util.Preconditions.checkArgument;

/**
 * Request for an individual transaction to be processed by a payment app / service.
 */
public class TransactionRequest extends BaseModel {

    private final String transactionId;
    private final String flowType;
    private final Amounts amounts;
    private final List<Basket> baskets;
    private final String paymentMethod;
    private final Customer customer;
    private final String flowStage;
    private final AdditionalData additionalData;
    private final Card card;
    private DeviceAudience deviceAudience;
    private String targetPaymentAppComponent;

    // Default constructor for deserialisation
    TransactionRequest() {
        this("N/A", "N/A", "", "", new Amounts(), null, null, new AdditionalData(), null, null);
    }

    /**
     * Construct a new instance.
     *
     * @param id             The request id (unique for this request)
     * @param transactionId  The transaction id representing the overall transaction this request is generated for
     * @param flowType       The flow type
     * @param flowStage      The current flow stage
     * @param amounts        The amounts to process
     * @param baskets        The baskets
     * @param customer       The customer details
     * @param additionalData The additional data
     * @param card           The card details from the VAA or from the payment card reading step
     * @param paymentMethod  The payment method for this transaction
     */
    public TransactionRequest(String id, String transactionId, String flowType, String flowStage, Amounts amounts, List<Basket> baskets,
                              Customer customer, AdditionalData additionalData, Card card, String paymentMethod) {
        super(id);
        this.transactionId = transactionId;
        this.flowStage = flowStage;
        this.flowType = flowType;
        this.amounts = amounts != null ? amounts : new Amounts(0, "XXX");
        this.baskets = baskets;
        this.paymentMethod = paymentMethod;
        this.customer = customer;
        this.additionalData = additionalData != null ? additionalData : new AdditionalData();
        this.card = card != null ? card : Card.getEmptyCard();
        this.deviceAudience = DeviceAudience.MERCHANT;
        checkArgument(id != null && !id.isEmpty(), "Id must be set");
        checkArgument(flowType != null, "Flow type must be set");
    }

    /**
     * Get the unique id for this particular {@link TransactionRequest}.
     *
     * This id is generated uniquely for each request within a {@link Transaction}, meaning it should NOT be used to identify the current transaction.
     *
     * Please see {@link #getTransactionId()} for an id that represents the {@link Transaction}.
     *
     * @return The unique id for this particular request
     */
    @NonNull
    @Override
    public String getId() {
        return super.getId();
    }

    /**
     * Get the id for the {@link Transaction} this request originates from.
     *
     * This id will be consistent for all requests and for all stages within the transaction, and can be used by applications that are called
     * in multiple stages to identify it as the same transaction for any saved state, etc.
     *
     * To retrieve the unique id for this particular request, please see {@link #getId()}.
     *
     * @return The transaction id that is consistent for all requests within a {@link Transaction}
     */
    @NonNull
    public String getTransactionId() {
        return transactionId;
    }

    /**
     * Get the flow type (aka transaction type).
     *
     * Can not be null.
     *
     * @return The flow type.
     */
    @NonNull
    public String getFlowType() {
        return flowType;
    }

    /**
     * Get the amounts to process.
     *
     * Can be zero with a currency of "XXX" in certain situations.
     *
     * @return The amounts
     */
    @NonNull
    public Amounts getAmounts() {
        return amounts;
    }

    /**
     * Check whether this transaction has a primary basket or not.
     *
     * The primary basket is generally provided by the client application initiating the flow (i.e the POS app), whereas secondary baskets may
     * be added by flow services.
     *
     * See {@link #getPrimaryBasket()} for retrieving the basket if one exists.
     *
     * @return True if this transaction has a primary basket, false otherwise
     */
    public boolean hasPrimaryBasket() {
        return baskets != null && !baskets.isEmpty() && baskets.get(0).isPrimaryBasket();
    }

    /**
     * Get the primary basket for this transaction, if any is available.
     *
     * The primary basket is generally provided by the client application initiating the flow (i.e the POS app), whereas secondary baskets may
     * be added by flow services.
     *
     * See {@link #hasPrimaryBasket()} to check whether one exists or not.
     *
     * @return The primary basket, or null if none is available
     */
    @Nullable
    public Basket getPrimaryBasket() {
        if (hasPrimaryBasket()) {
            return baskets.get(0);
        }
        return null;
    }

    /**
     * Get the baskets for this transaction.
     *
     * This may consist of baskets added by the client initiation app as well as flow services in the flow.
     *
     * See {@link #getPrimaryBasket()} for retrieving the primary basket specifically, if any is available.
     *
     * @return The baskets
     */
    @NonNull
    public List<Basket> getBaskets() {
        return baskets != null ? baskets : new ArrayList<>();
    }

    /**
     * Get the payment method to use for transaction processing.
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
     * Get the customer details for this transaction.
     *
     * @return The customer details, or null
     */
    @Nullable
    public Customer getCustomer() {
        return customer;
    }

    /**
     * Get the additional data
     *
     * @return The data. May be empty.
     */
    @NonNull
    public AdditionalData getAdditionalData() {
        return additionalData;
    }

    /**
     * Get the stage the payment is at, which will determine what data is available and what response data can be set.
     *
     * @return The current flow stage this request was generated for
     */
    @NonNull
    public String getFlowStage() {
        return flowStage;
    }

    /**
     * For internal use.
     *
     * @param deviceAudience Device audience
     */
    public void setDeviceAudience(DeviceAudience deviceAudience) {
        this.deviceAudience = deviceAudience;
    }

    /**
     * Get the type of audience (merchant or customer) that will be interacting with the device your app is running on.
     *
     * This information can be used to customise your application for the audience appropriately.
     *
     * @return The type of audience (merchant or customer) of the device this app is running on
     */
    @NonNull
    public DeviceAudience getDeviceAudience() {
        return deviceAudience;
    }

    /**
     * Get the card details provided from the VAA or from the payment card reading step.
     *
     * Note that in some cases, only the token will be available.
     *
     * @return The card details (may be empty)
     */
    @NonNull
    public Card getCard() {
        return card;
    }

    @Override
    public String toJson() {
        return JsonConverter.serialize(this);
    }

    public static TransactionRequest fromJson(String json) {
        return JsonConverter.deserialize(json, TransactionRequest.class);
    }

    /**
     * For internal use.
     *
     * @return String
     */
    @Nullable
    public String getTargetPaymentAppComponent() {
        return targetPaymentAppComponent;
    }

    /**
     * For internal use.
     *
     * @param targetPaymentAppComponent String
     */
    public void setTargetPaymentAppComponent(String targetPaymentAppComponent) {
        if (targetPaymentAppComponent != null) {
            this.targetPaymentAppComponent = targetPaymentAppComponent;
        }
    }

    @Override
    public String toString() {
        return "TransactionRequest{" +
                "transactionId='" + transactionId + '\'' +
                ", flowType='" + flowType + '\'' +
                ", amounts=" + amounts +
                ", baskets=" + baskets +
                ", customer=" + customer +
                ", flowStage='" + flowStage + '\'' +
                ", additionalData=" + additionalData +
                ", card=" + card +
                ", deviceAudience=" + deviceAudience +
                ", targetPaymentAppComponent='" + targetPaymentAppComponent + '\'' +
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

        TransactionRequest that = (TransactionRequest) o;
        return Objects.equals(transactionId, that.transactionId) &&
                Objects.equals(flowType, that.flowType) &&
                Objects.equals(amounts, that.amounts) &&
                Objects.equals(baskets, that.baskets) &&
                Objects.equals(customer, that.customer) &&
                Objects.equals(flowStage, that.flowStage) &&
                Objects.equals(additionalData, that.additionalData) &&
                Objects.equals(card, that.card) &&
                deviceAudience == that.deviceAudience &&
                Objects.equals(targetPaymentAppComponent, that.targetPaymentAppComponent);
    }

    @Override
    public int hashCode() {

        return Objects.hash(super.hashCode(), transactionId, flowType, amounts, baskets, customer, flowStage, additionalData, card, deviceAudience,
                            targetPaymentAppComponent);
    }
}
