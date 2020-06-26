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
import com.aevi.util.json.JsonConverter;

import java.util.Objects;

/**
 * A transaction response representing the outcome of processing a {@link TransactionRequest}.
 */
public class TransactionResponse extends BaseModel {

    static final String DEFAULT_PAYMENT_METHOD = "card";

    public enum Outcome {
        APPROVED,
        DECLINED
    }

    private final Card card;
    private final Outcome outcome;
    private final String outcomeMessage;
    private final Amounts amounts;
    private final String responseCode;
    private final String paymentMethod;
    private final AdditionalData references;

    private String flowServiceId;
    private String flowStage;

    // Default constructor for deserialisation
    TransactionResponse() {
        this("", null, Outcome.DECLINED, "", null, null, new AdditionalData(), null);
    }

    TransactionResponse(String id, Card card, Outcome outcome, String outcomeMessage, Amounts amounts, String responseCode,
                        AdditionalData references, String paymentMethod) {
        super(id);
        this.card = card != null ? card : Card.getEmptyCard();
        this.outcome = outcome;
        this.outcomeMessage = outcomeMessage;
        this.amounts = amounts;
        this.responseCode = responseCode;
        this.references = references != null ? references : new AdditionalData();
        this.paymentMethod = paymentMethod != null ? paymentMethod : DEFAULT_PAYMENT_METHOD;
    }

    /**
     * Get the outcome of this transaction - approved or declined.
     *
     * Note that for declined outcomes in particular, depending on where and how the transaction was declined, either {@link #getOutcomeMessage()}
     * or {@link #getResponseCode()} will be set to indicate the reason.
     *
     * @return The outcome
     */
    @NonNull
    public Outcome getOutcome() {
        return outcome;
    }

    /**
     * Get the outcome message to describe the reason for the outcome in more detail.
     *
     * This is particularly useful where a payment may have been declined "offline" at any point in the payment flow, such as for payment app errors.
     *
     * This is not to be confused with {@link #getResponseCode()} below, which is typically tied to the payment host/gateway processing the txn.
     *
     * @return The outcome message describing the outcome reason.
     */
    @Nullable
    public String getOutcomeMessage() {
        return outcomeMessage;
    }

    /**
     * Get the response code for this transaction.
     *
     * The range of values for this field is payment gateway / acquirer / host protocol specific.
     * See online documentation for more information.
     *
     * An example would be ISO-8583 2-alphanumeric character response codes.
     *
     * @return The bespoke response code
     */
    @Nullable
    public String getResponseCode() {
        return responseCode;
    }

    /**
     * Get the payment type used for this transaction.
     *
     * Note - defaults to "card".
     *
     * @return The payment type used.
     */
    @NonNull
    public String getPaymentMethod() {
        return paymentMethod;
    }

    /**
     * Get the card details for the card used for this transaction.
     *
     * Note that all fields in this model are optional.
     *
     * @return The card details (may be empty)
     */
    @NonNull
    public Card getCard() {
        return card;
    }

    /**
     * Get the amounts processed for this transaction, if any.
     *
     * @return The amounts processed
     */
    @Nullable
    public Amounts getAmountsProcessed() {
        return amounts;
    }

    /**
     * Add an extra/optional reference to pass back to the calling application.
     *
     * Note that if the key already exists in the references, this will be ignored!
     *
     * See {@link AdditionalData#addData(String, Object[])} for more info.
     *
     * @param key    The key to use for this reference
     * @param values An array of values for this reference
     * @param <T>    The type of object this reference is an array of
     */
    @SafeVarargs
    public final <T> void addReference(String key, T... values) {
        if (!references.hasData(key)) {
            references.addData(key, values);
        }
    }

    /**
     * Add extra references to pass back to the calling application.
     *
     * Note that if the key already exists in the references, this will be ignored!
     *
     * @param fromReferences The references to copy in
     */
    public void addReferences(AdditionalData fromReferences) {
        references.addData(fromReferences, false);
    }

    /**
     * Get any extra, optional references that the payment app / service or post-transaction apps have set.
     *
     * @return The {@link AdditionalData} containing reference data
     */
    @NonNull
    public AdditionalData getReferences() {
        return references;
    }

    /**
     * Get the id of the flow service that generated this response.
     *
     * Note that this may be null for cases where a response is auto-generated (due to errors, etc)
     *
     * @return The flow service id, if set.
     */
    @Nullable
    public String getFlowServiceId() {
        return flowServiceId;
    }

    /**
     * For internal use.
     *
     * @param flowServiceId Flow service id
     */
    public void setFlowServiceId(String flowServiceId) {
        this.flowServiceId = flowServiceId;
    }

    /**
     * Get the flow stage this response was generated in.
     *
     * @return The flow stage this response was generated in, if set
     */
    @Nullable
    public String getFlowStage() {
        return flowStage;
    }

    /**
     * For internal use.
     *
     * @param flowStage The flow stage
     */
    public void setFlowStage(String flowStage) {
        this.flowStage = flowStage;
    }

    @Override
    public String toJson() {
        return JsonConverter.serialize(this);
    }

    public static TransactionResponse fromJson(String json) {
        return JsonConverter.deserialize(json, TransactionResponse.class);
    }

    @Override
    public String toString() {
        return "TransactionResponse{" +
                "card=" + card +
                ", outcome=" + outcome +
                ", outcomeMessage='" + outcomeMessage + '\'' +
                ", amounts=" + amounts +
                ", responseCode='" + responseCode + '\'' +
                ", paymentMethod='" + paymentMethod + '\'' +
                ", references=" + references +
                ", flowServiceId='" + flowServiceId + '\'' +
                ", flowStage='" + flowStage + '\'' +
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

        TransactionResponse that = (TransactionResponse) o;
        return Objects.equals(card, that.card) &&
                outcome == that.outcome &&
                Objects.equals(outcomeMessage, that.outcomeMessage) &&
                Objects.equals(amounts, that.amounts) &&
                Objects.equals(responseCode, that.responseCode) &&
                Objects.equals(paymentMethod, that.paymentMethod) &&
                Objects.equals(references, that.references) &&
                Objects.equals(flowServiceId, that.flowServiceId) &&
                Objects.equals(flowStage, that.flowStage);
    }

    @Override
    public int hashCode() {

        return Objects
                .hash(super.hashCode(), card, outcome, outcomeMessage, amounts, responseCode, paymentMethod, references, flowServiceId, flowStage);
    }
}

