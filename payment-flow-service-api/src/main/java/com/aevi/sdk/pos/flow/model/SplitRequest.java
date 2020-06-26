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
import com.aevi.sdk.flow.model.DeviceAudience;
import com.aevi.util.json.JsonConverter;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class SplitRequest extends BaseModel {

    private final Payment sourcePayment;
    private final Amounts accumulatedRequestTotals;
    private final List<Transaction> transactions;
    private DeviceAudience deviceAudience;

    // Default constructor for deserialisation
    SplitRequest() {
        this(new Payment(), new Amounts(0, "XXX"), new ArrayList<>());
    }

    /**
     * Initialise the split request.
     *
     * @param sourcePayment            The source payment used for initiation
     * @param accumulatedRequestTotals The up-to-date request totals, used for calculations for remaining amounts
     * @param transactions             The list of transactions
     */
    public SplitRequest(Payment sourcePayment, Amounts accumulatedRequestTotals, List<Transaction> transactions) {
        super(UUID.randomUUID().toString());
        this.sourcePayment = sourcePayment;
        this.accumulatedRequestTotals = accumulatedRequestTotals;
        this.transactions = transactions;
    }

    /**
     * Get the source Request, as provide by the calling VAA.
     *
     * @return The source {@link Payment}
     */
    @NonNull
    public Payment getSourcePayment() {
        return sourcePayment;
    }

    /**
     * Get the list of transactions (consists of TransactionRequest and TransactionResponse pairs)
     *
     * Note that this list will be empty before any transactions have taken place.
     *
     * @return The list of {@link Transaction} objects
     */
    @NonNull
    public List<Transaction> getTransactions() {
        return transactions;
    }

    /**
     * Checks whether there are any previously completed transactions.
     *
     * @return True if there are previous transactions, false otherwise
     */
    public boolean hasPreviousTransactions() {
        return !transactions.isEmpty();
    }

    /**
     * Convenience method to get the last completed transaction.
     *
     * @return The last completed {@link Transaction} or null if there are none
     */
    @Nullable
    public Transaction getLastTransaction() {
        if (transactions.isEmpty()) {
            return null;
        }
        return transactions.get(transactions.size() - 1);
    }

    /**
     * Get the total accumulated requested amounts.
     *
     * The return value here takes into account any additional request amounts added by flow services as part of the transactions.
     *
     * @return The total request {@link Amounts} for processing
     */
    @NonNull
    public Amounts getRequestedAmounts() {
        return accumulatedRequestTotals;
    }

    /**
     * Get the remaining amounts to process for this {@link Payment} in order to meet the {@link #getRequestedAmounts()}.
     *
     * @return The remaining {@link Amounts} to process
     */
    @NonNull
    public Amounts getRemainingAmounts() {
        if (transactions.isEmpty()) {
            return getRequestedAmounts();
        }
        Amounts processed = getProcessedAmounts();
        return Amounts.subtractAmounts(getRequestedAmounts(), processed, false);
    }

    /**
     * Get the processed amounts at this point in time.
     *
     * This will be zero-based if no amounts have been processed yet.
     *
     * Note that it is possible in some cases that this will be larger than {@link #getRequestedAmounts()}.
     *
     * @return The processed {@link Amounts} at this point in time
     */
    @NonNull
    public Amounts getProcessedAmounts() {
        Amounts processed = new Amounts(0, getRequestedAmounts().getCurrency());
        if (transactions.isEmpty()) {
            return processed;
        }
        for (Transaction transaction : transactions) {
            processed = Amounts.addAmounts(processed, transaction.getProcessedAmounts());
        }
        return processed;
    }

    /**
     * For internal use
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

    @Override
    public String toJson() {
        return JsonConverter.serialize(this);
    }

    public static SplitRequest fromJson(String json) {
        return JsonConverter.deserialize(json, SplitRequest.class);
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
        } else if (!super.equals(o)) {
            return false;
        }

        SplitRequest that = (SplitRequest) o;
        return Objects.equals(sourcePayment, that.sourcePayment) &&
                Objects.equals(accumulatedRequestTotals, that.accumulatedRequestTotals) &&
                Objects.equals(transactions, that.transactions) &&
                deviceAudience == that.deviceAudience;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), sourcePayment, accumulatedRequestTotals, transactions, deviceAudience);
    }

    @Override
    public String toString() {
        return "SplitRequest{" +
                "sourcePayment=" + sourcePayment +
                ", accumulatedRequestTotals=" + accumulatedRequestTotals +
                ", transactions=" + transactions +
                ", deviceAudience=" + deviceAudience +
                '}';
    }
}
