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

package com.aevi.sdk.pos.flow.sample;


import android.support.annotation.NonNull;

import com.aevi.sdk.flow.constants.CardEntryMethods;
import com.aevi.sdk.flow.model.AdditionalData;
import com.aevi.sdk.flow.model.Customer;
import com.aevi.sdk.pos.flow.model.Card;
import com.aevi.sdk.pos.flow.model.CardBuilder;
import com.aevi.sdk.pos.flow.model.TransactionRequest;

import static com.aevi.sdk.flow.constants.CardDataKeys.*;
import static com.aevi.sdk.flow.constants.CardNetworks.*;

public class CardProducer {

    private static final String VISA_PAN = CardBuilder.maskPan("4612345678123456");
    private static final String MASTERCARD_PAN = CardBuilder.maskPan("5312345678123456");
    private static final String AMEX_PAN = CardBuilder.maskPan("3712345678123456");
    private static final String EXPIRY_DATE = "2506";

    private static final String VISA_AID = "A0000000031010";
    private static final String MASTERCARD_AID = "A0000000041010";
    private static final String AMEX_AID = "A0000000250000";
    private static final String[] LANGUAGES = new String[]{"en", "de", "fr"};

    private static final String WE_BUILT_THIS_CARD_KEY = "cardProducedInSample"; // We built this card on rock'n'roll...

    @NonNull
    public static Card getCard(TransactionRequest transactionRequest) {
        // If the card details passed on via the request is from our app in the card reading step, we use that again
        if (CardProducer.cardWasProducedHere(transactionRequest.getCard())) {
            return transactionRequest.getCard();
        }
        // If not, let's just return the default card
        return CardProducer.getDefaultCard();
    }

    public static Card getDefaultCard() {
        return buildCard(CARD_NETWORK_VISA, true, false, false);
    }

    public static Card buildCard(String cardNetwork, boolean includeToken, boolean includeCardholder, boolean includeEmvDetails) {
        String maskedPan = getPanForNetwork(cardNetwork);
        Customer customer = CustomerProducer.getDefaultCustomer("cardProducer");
        String cardholderName = includeCardholder ? customer.getFullName() : null;

        CardBuilder cardBuilder = new CardBuilder()
                .withMaskedPan(maskedPan)
                .withExpiryDate(EXPIRY_DATE)
                .withCardholderName(cardholderName)
                .withAdditionalData(getCardAdditionalData(includeEmvDetails, cardNetwork));

        if (includeToken) {
            cardBuilder.withCardToken(customer.getTokens().get(0));
        }

        cardBuilder.addAdditionalData(WE_BUILT_THIS_CARD_KEY, true);

        return cardBuilder.build();
    }

    public static boolean cardWasProducedHere(Card card) {
        return card != null && card.getAdditionalData().hasData(WE_BUILT_THIS_CARD_KEY);
    }

    private static String getPanForNetwork(String cardNetwork) {
        switch (cardNetwork) {
            case CARD_NETWORK_VISA:
                return VISA_PAN;
            case CARD_NETWORK_MASTERCARD:
                return MASTERCARD_PAN;
            case CARD_NETWORK_AMEX:
                return AMEX_PAN;
            default:
                return VISA_PAN;
        }
    }

    private static String getAidForNetwork(String cardNetwork) {
        cardNetwork = cardNetwork.toLowerCase();
        switch (cardNetwork) {
            case CARD_NETWORK_VISA:
                return VISA_AID;
            case CARD_NETWORK_MASTERCARD:
                return MASTERCARD_AID;
            case CARD_NETWORK_AMEX:
                return AMEX_AID;
            default:
                return VISA_AID;
        }
    }

    private static AdditionalData getCardAdditionalData(boolean withEmv, String cardNetwork) {
        AdditionalData additionalData = new AdditionalData();
        if (withEmv) {
            additionalData.addData(CARD_DATA_BRAND, "EUROWAG");
            additionalData.addData(CARD_DATA_AID, getAidForNetwork(cardNetwork));
            additionalData.addData(CARD_DATA_LANGUAGES, LANGUAGES);
        }
        additionalData.addData(CARD_DATA_NETWORK, cardNetwork);
        additionalData.addData(CARD_DATA_ENTRY_METHOD, CardEntryMethods.CARD_ENTRY_METHOD_INSERT);
        return additionalData;
    }
}
