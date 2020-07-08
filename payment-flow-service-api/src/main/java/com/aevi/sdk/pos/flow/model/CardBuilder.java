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
import com.aevi.sdk.flow.model.Token;

import java.util.Arrays;

import static com.aevi.sdk.flow.util.Preconditions.checkArgument;

/**
 * Builder class for {@link Card}.
 */
public class CardBuilder {

    private static final int MIN_PAN_LENGTH = 16;
    private static final byte X_CHAR = 0x58;
    private static final int START_PAN_OFFSET = 6;
    private static final int END_PAN_OFFSET = 4;

    private String maskedPan;
    private String cardholderName;
    private String expiryDate;
    private Token cardToken;
    private AdditionalData additionalData = new AdditionalData();

    /**
     * Set the PAN of the card presented masked according to PCI DSS standards.
     *
     * See {@link #maskPan(String)} for a helper method to mask a clear text PAN.
     *
     * @param maskedPan The PAN masked according to PCI DSS standards.
     * @return This builder
     */
    @NonNull
    public CardBuilder withMaskedPan(String maskedPan) {
        this.maskedPan = maskedPan;
        return this;
    }

    /**
     * Set the card holder name of the card presented.
     *
     * @param cardholderName The card holder name.
     * @return This builder
     */
    @NonNull
    public CardBuilder withCardholderName(String cardholderName) {
        this.cardholderName = cardholderName;
        return this;
    }

    /**
     * Set the card expiry date in the format YYMM (such as 2006 for June 2020).
     *
     * @param expiryDate The expiry date in the format YYMM.
     * @return This builder
     */
    @NonNull
    public CardBuilder withExpiryDate(String expiryDate) {
        if (expiryDate != null && expiryDate.length() != 4) {
            throw new IllegalArgumentException("Expiry date must be in format YYMM");
        }
        this.expiryDate = expiryDate;
        return this;
    }

    /**
     * Set the card token if any was generated for the card presented.
     *
     * @param cardToken The card token
     * @return This builder
     */
    public CardBuilder withCardToken(Token cardToken) {
        this.cardToken = cardToken;
        return this;
    }

    /**
     * Add additional data as an {@link AdditionalData} object.
     *
     * Examples could be aid, service code, card entry method, etc.
     *
     * This will clear any additional data previously added with {@link #addAdditionalData(String, Object[])}
     *
     * @param additionalData Additional data
     * @return This builder
     */
    @NonNull
    public CardBuilder withAdditionalData(AdditionalData additionalData) {
        this.additionalData = additionalData;
        return this;
    }

    /**
     * @deprecated Use {{@link #addAdditionalData(String, Object[])}}
     */
    @Deprecated
    @NonNull
    public <T> CardBuilder withAdditionalData(String key, T... values) {
        return addAdditionalData(key, values);
    }

    /**
     * Convenience wrapper for adding additional data.
     *
     * See {@link AdditionalData#addData(String, Object[])} for more info.
     *
     * @param key    The key to use for this data
     * @param values An array of values for this data
     * @param <T>    The type of object this data is an array of
     * @return This builder
     */
    @NonNull
    public <T> CardBuilder addAdditionalData(String key, T... values) {
        additionalData.addData(key, values);
        return this;
    }

    /**
     * Builds an instance of {@link Card} based on the parameters set in this builder.
     *
     * @return A {@link Card} instance
     */
    @NonNull
    public Card build() {
        return new Card(maskedPan, cardholderName, expiryDate, cardToken, additionalData);
    }

    /**
     * Masks PANs of at least 16 digits in length according to PCI DSS:
     *
     * All digits except first six digits and last four digits
     * are masked using the "X" character.
     *
     * This PAN masking is SRED compliant.
     *
     * @param pan PAN to be masked
     * @return masked PAN
     * @throws IllegalArgumentException if PAN is null or less than 16 digits
     */
    @NonNull
    public static String maskPan(String pan) {
        checkArgument(pan != null && pan.length() >= MIN_PAN_LENGTH, "PAN must be >= " + MIN_PAN_LENGTH + " in length");
        byte[] bytes = pan.getBytes();
        Arrays.fill(bytes, START_PAN_OFFSET, bytes.length - END_PAN_OFFSET, X_CHAR);
        return new String(bytes);
    }
}