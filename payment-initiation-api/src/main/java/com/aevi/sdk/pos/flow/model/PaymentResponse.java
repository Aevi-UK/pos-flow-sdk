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
import com.aevi.sdk.flow.model.BaseModel;
import com.aevi.util.json.JsonConverter;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Response for a previously made {@link Payment}.
 *
 * A payment can lead to a single or multiple transactions and it is up to the client to ensure this is taken into account.
 *
 * This class contains convenience methods to get overall information for the transaction processing.
 */
public class PaymentResponse extends BaseModel {

    /**
     * The outcome of a payment.
     */
    public enum Outcome {
        /**
         * The payment was fulfilled, meaning the processed amounts equals or exceeds the requested amounts.
         *
         * The {@link #getTotalAmountsProcessed()} may be more than requested if flow services added amounts or baskets. The total request amounts
         * can be checked via #
         */
        FULFILLED,
        /**
         * The payment was partially fulfilled, meaning that the {@link #getTotalAmountsProcessed()} is less than the total requested amounts
         * at the point when the payment service processes the transaction. Note that a flow service may have added amounts or baskets in between
         * the initiation of the flow and the payment processing step.
         *
         * The most common scenarios for this are partial auth from the payment host or flow cancellation before completion.
         *
         * See {@link FailureReason} for any failure details.
         */
        PARTIALLY_FULFILLED,
        /**
         * The payment failed and no money was charged.
         *
         * See {@link FailureReason} for the reason of failure.
         */
        FAILED
    }

    /**
     * The reason for failure, if any.
     */
    public enum FailureReason {
        /**
         * No failure occurred
         */
        NONE,
        /**
         * The payment was cancelled before completing
         */
        CANCELLED,
        /**
         * The payment was rejected either due to incorrect parameters or due to payment app not being able to process it at this time
         */
        REJECTED,
        /**
         * The payment was declined, meaning that all transactions processed were declined
         */
        DECLINED,
        /**
         * The payment timed out before completing
         */
        TIMEOUT,
        /**
         * An error occurred that meant processing could not complete
         */
        ERROR
    }

    private Payment originatingPayment;

    protected Outcome outcome;
    protected FailureReason failureReason;
    protected String failureMessage;
    protected boolean allTransactionsApproved = true;
    protected Amounts totalAmountsRequested;
    protected Amounts totalAmountsProcessed;
    protected List<Transaction> transactions;
    protected long creationDateTimeMs;
    protected FlowAppInfo executedPreFlowApp;
    protected FlowAppInfo executedPostFlowApp;

    // Default constructor for deserialisation
    PaymentResponse() {
        super("N/A");
        transactions = new CopyOnWriteArrayList<>();
        totalAmountsProcessed = new Amounts();
    }

    protected PaymentResponse(Payment payment) {
        super(payment.getId());
        originatingPayment = payment;
        totalAmountsProcessed = new Amounts(0, payment.getAmounts().getCurrency());
        transactions = new CopyOnWriteArrayList<>();
    }

    /**
     * This can be called to check whether the payment was fulfilled via multiple split transactions or not.
     *
     * If true, the {@link #getTransactions()} method will return a list with a size greater than 1.
     *
     * @return True if split, false otherwise
     */
    public boolean isSplit() {
        return transactions.size() > 1;
    }

    /**
     * Get the initial {@link Payment} that this response is for.
     *
     * @return The {@link Payment} that initiated the flow
     */
    @NonNull
    public Payment getOriginatingPayment() {
        return originatingPayment;
    }

    /**
     * Get the overall outcome of the payment.
     *
     * This will indicate whether the payment was fulfilled (requested amount processed), partially fulfilled (part of requested amount processed)
     * or failed (no amount processed).
     *
     * @return The overall outcome.
     */
    @NonNull
    public Outcome getOutcome() {
        return outcome;
    }

    /**
     * For {@link Outcome#PARTIALLY_FULFILLED} and {@link Outcome#FAILED}, this will return the reason for why the payment was not fully fulfilled.
     *
     * Note that there are some cases of {@link Outcome#PARTIALLY_FULFILLED} where this will be set to {@link FailureReason#NONE}. An example
     * of this is when the payment app approves an amount less than requested (partial auth), which is not a failure from a transactional point of view.
     *
     * @return The reason for failure.
     */
    @NonNull
    public FailureReason getFailureReason() {
        return failureReason;
    }

    /**
     * When a {@link FailureReason} has been set, this field can be set to provide additional information on what went wrong.
     *
     * @return The failure message. May be null.
     */
    @Nullable
    public String getFailureMessage() {
        return failureMessage;
    }

    /**
     * Convenience method to check whether all transactions that were processed
     * due to the originating payment was approved or not.
     *
     * Note - the payment can still have been fulfilled despite failed transactions, so this method
     * should NOT be used to check the outcome of the payment - use getOutcome() for that.
     *
     * @return True if all transactions were approved, false if some were declined or had errors.
     */
    public boolean isAllTransactionsApproved() {
        return !transactions.isEmpty() && allTransactionsApproved;
    }

    /**
     * Get the total amounts requested at the end of the flow, including any amounts / baskets added by flow services as part of it.
     *
     * The request amounts provided in the {@link Payment} are input to the flow where other applications may add further amounts.
     *
     * The amounts returned here are guaranteed to be equal to or greater than the initially requested amounts.
     *
     * @return The total request amounts at the end of the flow
     */
    @NonNull
    public Amounts getTotalAmountsRequested() {
        return totalAmountsRequested;
    }

    /**
     * Get the total amounts processed for the transactions that were carried out.
     *
     * This may not match the initially requested amounts. Note that {@link #getOutcome()} can be used to determine whether
     * a transaction was fulfilled, partially fulfilled or failed. This method is a convenience method to get the amount totals and not intended
     * to be used to investigate the outcome of the payment.
     *
     * Note that this may contain empty amounts or have amounts greater than the requested amounts.
     *
     * @return The total processed amounts. May be zero.
     */
    @NonNull
    public Amounts getTotalAmountsProcessed() {
        return totalAmountsProcessed;
    }

    /**
     * Get the list of the customer transactions.
     *
     * This list will usually only contain one item, but can contain multiple items in split transaction scenarios.
     *
     * {{@link #isAllTransactionsApproved()}} can be used to check whether all transactions were approved.
     *
     * @return The list of {@link Transaction} items.
     */
    @NonNull
    public List<Transaction> getTransactions() {
        return transactions;
    }

    /**
     * Get the creation date/time of this response in milliseconds since epoch.
     *
     * @return The creation date/time of this response in milliseconds since epoch
     */
    public long getCreationDateTimeMs() {
        return creationDateTimeMs;
    }

    /**
     * Get the pre-flow app that was executed for this transaction, if any.
     *
     * @return The pre-flow app info, or null.
     */
    @Nullable
    public FlowAppInfo getExecutedPreFlowApp() {
        return executedPreFlowApp;
    }

    /**
     * Get the post-flow app that was executed for this transaction, if any.
     *
     * @return The post-flow app info, or null.
     */
    @Nullable
    public FlowAppInfo getExecutedPostFlowApp() {
        return executedPostFlowApp;
    }

    @Override
    public String toJson() {
        return JsonConverter.serialize(this);
    }

    public static PaymentResponse fromJson(String json) {
        return JsonConverter.deserialize(json, PaymentResponse.class);
    }

    @Override
    public String toString() {
        return "PaymentResponse{" +
                "originatingPayment=" + originatingPayment +
                ", outcome=" + outcome +
                ", failureReason=" + failureReason +
                ", failureMessage='" + failureMessage + '\'' +
                ", allTransactionsApproved=" + allTransactionsApproved +
                ", totalAmountsRequested=" + totalAmountsRequested +
                ", totalAmountsProcessed=" + totalAmountsProcessed +
                ", transactions=" + transactions +
                ", creationDateTimeMs=" + creationDateTimeMs +
                ", executedPreFlowApp=" + executedPreFlowApp +
                ", executedPostFlowApp=" + executedPostFlowApp +
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

        PaymentResponse that = (PaymentResponse) o;
        return allTransactionsApproved == that.allTransactionsApproved &&
                creationDateTimeMs == that.creationDateTimeMs &&
                Objects.equals(originatingPayment, that.originatingPayment) &&
                outcome == that.outcome &&
                failureReason == that.failureReason &&
                Objects.equals(failureMessage, that.failureMessage) &&
                Objects.equals(totalAmountsRequested, that.totalAmountsRequested) &&
                Objects.equals(totalAmountsProcessed, that.totalAmountsProcessed) &&
                Objects.equals(transactions, that.transactions) &&
                Objects.equals(executedPreFlowApp, that.executedPreFlowApp) &&
                Objects.equals(executedPostFlowApp, that.executedPostFlowApp);
    }

    @Override
    public int hashCode() {

        return Objects
                .hash(super.hashCode(), originatingPayment, outcome, failureReason, failureMessage, allTransactionsApproved, totalAmountsRequested,
                      totalAmountsProcessed, transactions, creationDateTimeMs, executedPreFlowApp, executedPostFlowApp);
    }
}
