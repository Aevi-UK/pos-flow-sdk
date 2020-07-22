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

import java.math.BigDecimal;
import java.util.Currency;

public class AmountFormatter {

    public static String formatAmount(String currencyCode, long amountValue) {
        return formatAmount(currencyCode, amountValue, true);
    }

    /**
     * Format amount to a readable format, taking currency sub-unit fractions into account
     */
    public static String formatAmount(String currencyCode, long amountValue, boolean withSymbol) {
        if (currencyCode != null) {
            String symbol;
            int subUnitFractions;
            try {
                Currency currency = Currency.getInstance(currencyCode);
                symbol = currency.getSymbol();
                subUnitFractions = currency.getDefaultFractionDigits();
            } catch (Throwable t) {
                symbol = currencyCode;
                subUnitFractions = 2;
            }
            if (withSymbol) {
                return symbol + BigDecimal.valueOf(amountValue).movePointLeft(subUnitFractions).toString();
            } else {
                return BigDecimal.valueOf(amountValue).movePointLeft(subUnitFractions).toString();
            }
        } else {
            return "0.00";
        }
    }
}
