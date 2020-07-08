package com.aevi.sdk.pos.flow.sample;

import com.aevi.sdk.flow.model.AdditionalData;

import java.util.UUID;

import static com.aevi.sdk.flow.constants.AdditionalDataKeys.DATA_KEY_TRANSACTION_ID;
import static com.aevi.sdk.flow.constants.CardDataKeys.*;
import static com.aevi.sdk.flow.constants.PaymentDataKeys.*;
import static com.aevi.sdk.flow.constants.ReferenceKeys.*;

public final class ReferenceProvider {

    public static AdditionalData getReferences() {
        AdditionalData additionalData = new AdditionalData();
        additionalData.addData(DATA_KEY_TRANSACTION_ID, UUID.randomUUID().toString());
        additionalData.addData(REFERENCE_KEY_MERCHANT_ID, IdProvider.getMerchantId());
        additionalData.addData(REFERENCE_KEY_MERCHANT_NAME, IdProvider.getMerchantName());
        additionalData.addData(REFERENCE_KEY_TERMINAL_ID, IdProvider.getTerminalId());
        additionalData.addData(REFERENCE_KEY_TRANSACTION_DATE_TIME, String.valueOf(System.currentTimeMillis()));
        additionalData.addData(REFERENCE_KEY_STAN, "242424");
        additionalData.addData(REFERENCE_KEY_DEVICE_ID, "99998877");
        additionalData.addData(REFERENCE_KEY_TRACK_1, "1111111111111111");
        additionalData.addData(REFERENCE_KEY_TRACK_2, "2222222222222222");
        additionalData.addData(REFERENCE_KEY_TRACK_3, "3333333333333333");
        additionalData.addData(REFERENCE_KEY_ACQUIRER_RESPONSE_CODE, "111222");
        additionalData.addData(REFERENCE_KEY_AUTH_CODE, "789456");
        additionalData.addData(REFERENCE_KEY_BATCH_NUMBER, "99");
        additionalData.addData(DATA_KEY_MERCHANT_REFERENCE, "Merch ref #1");
        additionalData.addData(DATA_KEY_MERCHANT_SECOND_REFERENCE, "Merch ref #2");
        return additionalData;
    }


}
