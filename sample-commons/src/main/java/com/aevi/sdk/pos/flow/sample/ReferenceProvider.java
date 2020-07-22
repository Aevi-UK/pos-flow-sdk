package com.aevi.sdk.pos.flow.sample;

import android.util.Log;

import com.aevi.sdk.flow.constants.AdditionalDataKeys;
import com.aevi.sdk.flow.constants.AmountIdentifiers;
import com.aevi.sdk.flow.constants.CardDataKeys;
import com.aevi.sdk.flow.model.AdditionalData;
import com.aevi.sdk.flow.models.TaxInfo;
import com.aevi.sdk.pos.flow.model.Card;
import com.aevi.sdk.pos.flow.model.TransactionRequest;
import com.aevi.sdk.pos.flow.model.events.Receipt;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

import static com.aevi.sdk.flow.constants.AdditionalDataKeys.DATA_KEY_TRANSACTION_ID;
import static com.aevi.sdk.flow.constants.PaymentDataKeys.*;
import static com.aevi.sdk.flow.constants.PaymentMethods.PAYMENT_METHOD_CARD;
import static com.aevi.sdk.flow.constants.ReceiptKeys.*;
import static com.aevi.sdk.flow.constants.ReferenceKeys.*;
import static com.aevi.sdk.pos.flow.sample.AmountFormatter.formatAmount;


final class ReceiptData {

    String acquirerResponseCode = "111222";
    String accountBalance = "4322.43";
    String additionalData = "AVAIL BAL: 4322.43";
    String amount;
    String authorizationCode = "789456";
    String avsResponseCode = "Z";
    String batchNumber = "99";
    String cardAID;
    String cardBrand;
    String cardholderName = "John Doe";
    String clerkId = "A44";
    String cvdResponseCode = "N";
    String cvm = "Signature";
    String currency;
    String deviceId = "99998877";
    String entryMode;
    String emvApplicationLabel;
    String hostTerminalId = IdProvider.getTerminalId();
    String language = "en";
    String merchantId = IdProvider.getMerchantId();
    String merchantReferenceNumber = "Merch ref #1";
    String partiallyAuthorized = "false";
    String paymentMethod;
    String receiptType;
    String responseMessage = "Approved";
    String rrn = "W123ADE32";
    String signatureRequired = "false";
    String stan = "242424";
    String surchargeAmount;
    String taxAmount;
    String terminalId = IdProvider.getTerminalId();
    String tipAmount;
    String tipLinePrint = "false";
    String traceNumber = "a43d3f4g";
    String transactionDate;
    String transactionId;
    String transactionMode = "deferred";
    String transactionTime;
    String transactionType;
    String truncatedPAN;
    String tsi = "0000";
    String tvr = "0000000000";

    transient SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("MM/dd/yyyy");
    transient SimpleDateFormat TIME_FORMAT = new SimpleDateFormat("HH:mm:ss");

    ReceiptData(TransactionRequest request, String type, String txId) {
        this.transactionId = txId;

        this.paymentMethod = PAYMENT_METHOD_CARD;

        this.currency = request.getAmounts().getCurrency();
        this.amount = formatAmount(currency, request.getAmounts().getTotalAmountValue(), false);

        this.surchargeAmount = formatAmount(currency, request.getAmounts().getAdditionalAmountValue(AmountIdentifiers.AMOUNT_SURCHARGE), false);
        this.tipAmount = formatAmount(currency, request.getAmounts().getAdditionalAmountValue(AmountIdentifiers.AMOUNT_TIP), false);

        TaxInfo taxInfo = request.getAdditionalData().getValue(AdditionalDataKeys.DATA_KEY_TAX_INFO, TaxInfo.class);
        if (taxInfo != null) {
            this.taxAmount = formatAmount(currency, Math.round(taxInfo.getTaxAmount()), false);
        }

        Card card = CardProducer.getCard(request);

        this.cardAID = card.getAdditionalData().getStringValue(CardDataKeys.CARD_DATA_AID, "A000000003101001");
        this.cardBrand = card.getAdditionalData().getStringValue(CardDataKeys.CARD_DATA_BRAND, "Visa");
        this.entryMode = card.getAdditionalData().getStringValue(CardDataKeys.CARD_DATA_ENTRY_METHOD, "Chip");
        this.emvApplicationLabel = card.getAdditionalData().getStringValue(CardDataKeys.CARD_DATA_NETWORK, "Visa Credit");
        this.truncatedPAN = card.getMaskedPan();
        this.cardholderName = card.getCardholderName();

        this.transactionType = request.getFlowType();
        this.transactionDate = DATE_FORMAT.format(new Date());
        this.transactionTime = TIME_FORMAT.format(new Date());
        this.receiptType = type;

    }
}

public final class ReferenceProvider {

    private static final Gson GSON = new GsonBuilder().create();

    public static AdditionalData getReferences(TransactionRequest request) {
        AdditionalData additionalData = new AdditionalData();
        String txId = UUID.randomUUID().toString();
        additionalData.addData(DATA_KEY_TRANSACTION_ID, txId);
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

        String customerReceiptData = GSON.toJson(new ReceiptData(request, "customer", txId));
        Log.d("RFP", "Customer receipt data: " + customerReceiptData);
        Receipt customerReceipt = new Receipt("customer", "Customer Receipt Text Text Text");
        customerReceipt.setReceiptData("json", customerReceiptData);
        additionalData.addData(RECEIPT_CUSTOMER, customerReceipt);

        String merchReceiptData = GSON.toJson(new ReceiptData(request, "merchant", txId));
        Log.d("RFP", "Merch receipt data: " + merchReceiptData);
        Receipt merchReceipt = new Receipt("merchant", "Merchant Receipt Text Text Text");
        merchReceipt.setReceiptData("json", merchReceiptData);
        additionalData.addData(RECEIPT_MERCHANT, merchReceipt);

        return additionalData;
    }
}
