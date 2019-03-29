package com.aevi.sdk.pos.flow.model;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class AmountsTest {

    @Test
    public void checkTotalIsCorrect() {
        Amounts amounts = new Amounts(1000L, "GBP");
        amounts.addAdditionalAmount("one", 1500L);
        amounts.addAdditionalAmount("two", 2000L);
        assertThat(amounts.getBaseAmountValue()).isEqualTo(1000L);
        assertThat(amounts.getBaseAmount()).isEqualTo(new Amount(1000L, "GBP"));
        assertThat(amounts.getTotalAmountValue()).isEqualTo(4500L);
        assertThat(amounts.getTotalAmount()).isEqualTo(new Amount(4500L, "GBP"));
    }

    @Test
    public void checkCurrencyIsCorrect() {
        Amounts amounts = new Amounts(1000L, "GBP");

        assertThat(amounts.getCurrency()).isEqualTo("GBP");
    }

    @Test
    public void checkAddAmounts() throws Exception {
        Amounts amount1 = new Amounts(1000L, "GBP");
        amount1.addAdditionalAmount("BOB", 150L);
        amount1.addAdditionalAmount("susan", 300L);
        Amounts amount2 = new Amounts(350L, "GBP");
        amount2.addAdditionalAmount("bob", 250L);
        amount2.addAdditionalAmount("harry", 120L);

        Amounts result = Amounts.addAmounts(amount1, amount2);
        assertThat(result.getCurrency()).isEqualTo("GBP");
        assertThat(result.getBaseAmountValue()).isEqualTo(1350L);
        assertThat(result.getTotalAmountValue()).isEqualTo(2170L);
        assertThat(result.getAdditionalAmountValue("bOb")).isEqualTo(400L);
        assertThat(result.getAdditionalAmountValue("susan")).isEqualTo(300L);
        assertThat(result.getAdditionalAmountValue("harry")).isEqualTo(120L);
    }

    @Test
    public void checkCanAddPercentageAmounts() throws Exception {
        Amounts amounts = new Amounts(1000L, "GBP");
        amounts.addAdditionalAmountAsBaseFraction("charity", 0.5f);

        assertThat(amounts.getAdditionalAmountValue("charity")).isEqualTo(500L);
        assertThat(amounts.getTotalAmountValue()).isEqualTo(1500L);
    }

    @Test(expected = IllegalArgumentException.class)
    public void checkCantUseNegativePercentage() throws Exception {
        Amounts amounts = new Amounts(1000L, "GBP");
        amounts.addAdditionalAmountAsBaseFraction("charity", -0.5f);
    }

    @Test(expected = IllegalArgumentException.class)
    public void checkCantAddMoreThan100Percent() throws Exception {
        Amounts amounts = new Amounts(1000L, "GBP");
        amounts.addAdditionalAmountAsBaseFraction("charity", 1.5f);
    }

    @Test(expected = IllegalArgumentException.class)
    public void checkCantAddAmountsDifferentCurrency() throws Exception {
        Amounts.addAmounts(new Amounts(10L, "GBP"), new Amounts(10L, "USD"));
    }

    @Test
    public void checkSubtractAmounts() throws Exception {
        Amounts amount1 = new Amounts(1000L, "GBP");
        amount1.addAdditionalAmount("bob", 450L);
        amount1.addAdditionalAmount("susan", 300L);
        Amounts amount2 = new Amounts(350L, "GBP");
        amount2.addAdditionalAmount("BOB", 250L);
        amount2.addAdditionalAmount("harry", 120L);

        Amounts result = Amounts.subtractAmounts(amount1, amount2, true);
        assertThat(result.getCurrency()).isEqualTo("GBP");
        assertThat(result.getBaseAmountValue()).isEqualTo(650L);
        assertThat(result.getTotalAmountValue()).isEqualTo(1150L);
        assertThat(result.getAdditionalAmounts()).containsKeys("bob", "susan");
        assertThat(result.getAdditionalAmountValue("bOb")).isEqualTo(200L);
        assertThat(result.getAdditionalAmountValue("susan")).isEqualTo(300L);
        assertThat(result.getAdditionalAmountValue("harry")).isEqualTo(0L);
    }

    @Test
    public void checkSubtractAmountsRemovesZeroBasedWhenRequested() throws Exception {
        Amounts amount1 = new Amounts(1000L, "GBP");
        amount1.addAdditionalAmount("bob", 450L);
        Amounts amount2 = new Amounts(500L, "GBP");
        amount2.addAdditionalAmount("bob", 450L);
        amount2.addAdditionalAmount("harry", 120L);

        Amounts result = Amounts.subtractAmounts(amount1, amount2, false);
        assertThat(result.getCurrency()).isEqualTo("GBP");
        assertThat(result.getBaseAmountValue()).isEqualTo(500L);
        assertThat(result.getAdditionalAmounts()).doesNotContainKeys("bob", "harry");
    }

    @Test(expected = IllegalArgumentException.class)
    public void checkCantSubtractAmountsDifferentCurrency() throws Exception {
        Amounts.subtractAmounts(new Amounts(10L, "GBP"), new Amounts(10L, "USD"), false);
    }

    @Test
    public void checkCanGetTotalExcludingAmounts() throws Exception {
        Amounts amounts = new Amounts(1000L, "GBP");
        amounts.addAdditionalAmount("one", 1500L);
        amounts.addAdditionalAmount("two", 2000L);
        amounts.addAdditionalAmount("three", 3000L);
        assertThat(amounts.getBaseAmountValue()).isEqualTo(1000L);
        assertThat(amounts.getTotalAmountValue()).isEqualTo(7500L);
        assertThat(amounts.getTotalExcludingAmounts("one", "two")).isEqualTo(4000L);
    }

    @Test
    public void checkAdditionalAmountsAreCaseInsensitive() throws Exception {
        Amounts amounts = new Amounts(1000L, "GBP");
        amounts.addAdditionalAmount("ONE", 2500L);
        amounts.addAdditionalAmount("OnE", 3000L);
        amounts.addAdditionalAmount("one", 1500L);
        amounts.addAdditionalAmount("two", 2000L);

        assertThat(amounts.getAdditionalAmounts().keySet()).hasSize(2);

        assertThat(amounts.hasAdditionalAmount("one")).isTrue();
        assertThat(amounts.hasAdditionalAmount("oNe")).isTrue();
        assertThat(amounts.hasAdditionalAmount("ONE")).isTrue();

        assertThat(amounts.getAdditionalAmount("one").getValue()).isEqualTo(1500L);
        assertThat(amounts.getAdditionalAmount("ONE").getValue()).isEqualTo(1500L);
        assertThat(amounts.getAdditionalAmount("oNe").getValue()).isEqualTo(1500L);
        assertThat(amounts.getAdditionalAmountValue("ONE")).isEqualTo(1500L);
    }
}
