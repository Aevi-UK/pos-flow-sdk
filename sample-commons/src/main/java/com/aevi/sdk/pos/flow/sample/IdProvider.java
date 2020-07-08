package com.aevi.sdk.pos.flow.sample;

public final class IdProvider {

    private IdProvider() {

    }

    public static String getTerminalId() {
        return "12345678";
    }

    public static String getMerchantId() {
        return "87654321";
    }

    public static String getMerchantName() {
        return "Sample Merchant";
    }
}
