package com.aevi.sdk.pos.flow.model;

import com.aevi.util.json.JsonConverter;
import com.google.gson.JsonParseException;

import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class PaymentTest {

    private Payment defaultPayment;

    @Before
    public void setup() {
        defaultPayment = getValidRequest(1000, "GBP", "sale");
    }

    @Test
    public void canBuildValidRequest() {
        Payment payment = getValidRequest(1000, "GBP", "sale");

        assertValues(payment, 1000, "GBP", "sale");
    }

    @Test
    public void canSerialiseAndDeserialize() {
        Payment payment = getValidRequest(1000, "GBP", "sale");

        Payment result = Payment.fromJson(payment.toJson());

        System.out.println(payment.toJson());

        assertValues(result, 1000, "GBP", "sale");
        assertThat(payment).isEqualTo(result);
    }

    @Test(expected = JsonParseException.class)
    public void invalidJsonWillThrow() {
        Payment.fromJson("{}ahjj");
    }

    @Test
    public void checkTwoPaymentRequestsAreEquivalent() {
        Payment payment1 = getValidRequest(1000, "GBP", "sale");
        Payment payment2 = getValidRequest(1000, "GBP", "sale");

        assertThat(payment1.equivalent(payment2)).isTrue();
        assertThat(payment1.equals(payment2)).isFalse();
    }

    private Payment toFromJson(Payment payment) {
        return JsonConverter.deserialize(payment.toJson(), Payment.class);
    }

    private void assertValues(Payment payment, long amount, String currency, String type) {
        assertThat(payment).isNotNull();
        assertThat(payment.getAmounts()).isEqualTo(new Amounts(amount, currency));
        assertThat(payment.getAmounts().getCurrency()).isEqualTo(currency);
        assertThat(payment.getFlowType()).isEqualTo(type);
    }

    private void assertArrayOfOptions(Payment result, String key, String... types) {
        String[] resultTypes = (String[]) result.getAdditionalData().getValue(key);
        assertThat(resultTypes).isNotEmpty();
        assertThat(resultTypes.length).isEqualTo(types.length);
        assertThat(resultTypes).containsExactly(types);
    }

    private void setupArrayOfOptions(Payment payment, String key, String... types) {
        payment.getAdditionalData().addData(key, types);
    }

    private Payment getValidRequest(long amount, String currency, String type) {
        return new PaymentBuilder().withPaymentFlow(type).withAmounts(new Amounts(amount, currency)).build();
    }
}
