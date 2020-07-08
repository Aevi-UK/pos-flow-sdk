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

import com.aevi.sdk.flow.model.AdditionalData;

import static com.aevi.sdk.flow.util.Preconditions.*;
import static com.aevi.sdk.pos.flow.model.TransactionResponse.Outcome;

/**
 * Builder to create a {@link TransactionResponse} instance.
 *
 * At a minimum, the transaction outcome must be set via one of the {@link #approve()} or {@link #decline(String)} calls.
 */
public final class TransactionResponseBuilder {

    private String id;
    private Card card;
    private Outcome outcome;
    private String outcomeMessage;
    private Amounts amounts;
    private String responseCode;
    private String paymentMethod = TransactionResponse.DEFAULT_PAYMENT_METHOD;
    private AdditionalData references = new AdditionalData();

    /**
     * Initialise this builder with {@link TransactionRequest#getId()} for the request that your service received.
     *
     * @param requestId The id of the TransactionRequest passed to the flow service.
     */
    public TransactionResponseBuilder(String requestId) {
        this.id = requestId;
    }

    /**
     * Initialise this builder with an existing transaction response.
     *
     * @param transactionResponse The response to use as a base for all values
     * @return This builder
     */
    @NonNull
    public TransactionResponseBuilder withDefaultsFrom(TransactionResponse transactionResponse) {
        id = transactionResponse.getId();
        card = transactionResponse.getCard();
        outcome = transactionResponse.getOutcome();
        outcomeMessage = transactionResponse.getOutcomeMessage();
        amounts = transactionResponse.getAmountsProcessed();
        responseCode = transactionResponse.getResponseCode();
        paymentMethod = transactionResponse.getPaymentMethod();
        references = transactionResponse.getReferences();
        return this;
    }

    /**
     * Set the transaction as approved with no processed amounts (for scenarios where no amounts were charged/processed).
     *
     * For cases where amounts were processed, see {@link #approve(Amounts, String...)}.
     *
     * @return This builder
     */
    @NonNull
    public TransactionResponseBuilder approve() {
        this.outcome = Outcome.APPROVED;
        this.amounts = null;
        return this;
    }

    /**
     * Set the transaction as approved with processed amount details and optionally specify the payment method used (defaults to "card").
     *
     * Important - if the transaction is approved and the full amounts have been charged, it is strongly recommended that you use the request amounts
     * from the {@link TransactionRequest} to set the amounts here to ensure the correct amounts breakdown.
     *
     * If the outcome is a partial auth, it is at the discretion of the payment application how to set the amounts but it is advisable that the
     * request amounts breakdown is kept intact when possible.
     *
     * For cases where no amounts were processed, see {@link #approve()}.
     *
     * @param processedAmounts The processed amounts (may be zero, but must not be null)
     * @param paymentMethod    Optional payment method used (defaults to "card")
     * @return This builder
     */
    @NonNull
    public TransactionResponseBuilder approve(Amounts processedAmounts, String... paymentMethod) {
        checkNotNull(processedAmounts, "Processed amounts must be set");
        this.outcome = Outcome.APPROVED;
        this.amounts = processedAmounts;
        if (paymentMethod.length > 0) {
            this.paymentMethod = paymentMethod[0];
        }
        return this;
    }

    /**
     * Set the transaction as declined and provide a decline message detailing the reason for it.
     *
     * Note that it is also advisable that a response code is set where possible via {@link #withResponseCode(String)}.
     *
     * @param declineMessage The message detailing why the transaction was declined
     * @return This builder
     */
    @NonNull
    public TransactionResponseBuilder decline(String declineMessage) {
        checkNotNull(declineMessage, "Decline message must be set");
        this.outcome = Outcome.DECLINED;
        this.outcomeMessage = declineMessage;
        this.amounts = null;
        return this;
    }

    /**
     * Set the card used for the transaction.
     *
     * @param card The card details
     * @return This builder
     */
    @NonNull
    public TransactionResponseBuilder withCard(Card card) {
        this.card = card;
        return this;
    }

    /**
     * Set an outcome message to further clarify the reason for the outcome.
     *
     * This is mandatory to set for declined outcomes via the {@link #decline(String)} call, but can optionally be set for approved scenarios too.
     *
     * This is not to be confused with {@link #withResponseCode(String)} below, which is tied to the payment backend system used.
     *
     * @param outcomeMessage The outcome message.
     * @return This builder
     */
    @NonNull
    public TransactionResponseBuilder withOutcomeMessage(String outcomeMessage) {
        this.outcomeMessage = outcomeMessage;
        return this;
    }

    /**
     * Set the response code for this transaction.
     *
     * The range of values for this field is payment gateway / acquirer / host protocol specific.
     * See online documentation for more information.
     *
     * An example would be ISO-8583 2-alphanumeric character response codes.
     *
     * @param responseCode The response code
     * @return This builder
     */
    @NonNull
    public TransactionResponseBuilder withResponseCode(String responseCode) {
        this.responseCode = responseCode;
        return this;
    }

    /**
     * @deprecated Use {@link #addReference(String, Object[])}
     */
    @Deprecated
    @NonNull
    public <T> TransactionResponseBuilder withReference(String key, T... values) {
        return addReference(key, values);
    }

    /**
     * Add an extra/optional reference to pass back to the calling application.
     *
     * Examples of this might be the merchant id used, date time of the transaction, etc.
     *
     * See {@link AdditionalData#addData(String, Object[])} for more info.
     *
     * @param key    The key to use for this option
     * @param values An array of values for this option
     * @param <T>    The type of object this option is an array of
     * @return This builder
     */
    @NonNull
    public <T> TransactionResponseBuilder addReference(String key, T... values) {
        references.addData(key, values);
        return this;
    }

    /**
     * Add extra/optional references to pass back to the calling application.
     *
     * Examples of this might be the merchant id used, date time of the transaction, etc.
     *
     * This will clear any previous references added using {@link #addReference(String, Object[])}
     *
     * @param references The map of references
     * @return This builder.
     */
    @NonNull
    public TransactionResponseBuilder withReferences(AdditionalData references) {
        if (references != null) {
            this.references = references;
        }
        return this;
    }

    /**
     * Returns true if the data setup in the builder so far will build a valid {@link TransactionResponse}
     *
     * @return True if this builder has been setup correctly i.e. an outcome has been set
     */
    public boolean isValid() {
        return id != null && !id.isEmpty() && outcome != null;
    }

    /**
     * Build an instance of {@link TransactionResponse} with the provided parameters.
     *
     * @return The TransactionResponse instance
     */
    @NonNull
    public TransactionResponse build() {
        checkArgument(id != null && !id.isEmpty(), "Request id must be set");
        checkArgument(outcome != null, "Outcome must be set");
        return new TransactionResponse(id, card, outcome, outcomeMessage, amounts, responseCode, references, paymentMethod);
    }
}


