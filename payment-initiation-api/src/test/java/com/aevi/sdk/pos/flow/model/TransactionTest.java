package com.aevi.sdk.pos.flow.model;


import org.assertj.core.api.Assertions;
import org.junit.Ignore;
import org.junit.Test;

import static org.assertj.core.api.Java6Assertions.assertThat;

@Ignore
public class TransactionTest {

    private Transaction transaction;

    @Test
    public void remainingAmountShouldEqualTotalForNoResponses() throws Exception {
        transaction = new Transaction(new Amounts(1000, "GBP"), null, null, null);

        assertThat(transaction.getRemainingAmounts()).isEqualTo(transaction.getRequestedAmounts());
    }

    @Test
    public void remainingAmountShouldTakeProcessedAmountsIntoAccount() throws Exception {
        transaction = new Transaction(new Amounts(1000, "GBP"), null, null, null);
        transaction.addTransactionResponse(getResponse(new Amounts(200, "GBP")));
        transaction.addTransactionResponse(getResponse(new Amounts(100, "GBP")));
        transaction.addTransactionResponse(getResponse(new Amounts(50, "GBP")));

        Amounts expectedRemaining = new Amounts(650, "GBP");
        assertThat(transaction.getRemainingAmounts()).isEqualTo(expectedRemaining);
    }

    @Test
    public void processedAmountsShouldReturnSumOfResponsesProcessed() throws Exception {
        transaction = new Transaction(new Amounts(1000, "GBP"), null, null, null);
        transaction.addTransactionResponse(getResponse(new Amounts(200, "GBP")));
        transaction.addTransactionResponse(getResponse(new Amounts(100, "GBP")));
        transaction.addTransactionResponse(getResponse(new Amounts(50, "GBP")));

        Amounts expectedProcessed = new Amounts(350, "GBP");
        assertThat(transaction.getProcessedAmounts()).isEqualTo(expectedProcessed);
    }

    @Test
    public void checkTwoTransactionsAreEquivalentButNotEqual() {
        Transaction transaction1 = new Transaction(new Amounts(1000, "GBP"), null, null, null);
        Transaction transaction2 = new Transaction(new Amounts(1000, "GBP"), null, null, null);

        Assertions.assertThat(transaction1.equivalent(transaction2)).isTrue();
        Assertions.assertThat(transaction1.equals(transaction2)).isFalse();

    }

    private static TransactionResponse getResponse(Amounts amounts) {
        return new TransactionResponse("", null, null, null, amounts, null, null, null);
    }
}
