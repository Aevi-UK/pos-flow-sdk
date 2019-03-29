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
import com.aevi.util.json.JsonConverter;
import com.aevi.util.json.Jsonable;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import static com.aevi.sdk.flow.util.Preconditions.checkArgument;

/**
 * Representation of all the amounts relevant for a transaction.
 *
 * See {@link Amount} for representation of a single amount value with associated currency.
 */
public class Amounts implements Jsonable {

    private final long baseAmount;
    private final Map<String, Long> additionalAmounts;
    private final String currency;

    private double currencyExchangeRate;
    private String originalCurrency;

    // Default constructor for deserialisation
    Amounts() {
        baseAmount = 0;
        additionalAmounts = createMap(null);
        currency = "XXX";
    }

    /**
     * Initialise with base amount (inclusive of tax) and currency.
     *
     * Additional amounts can be set via {@link #addAdditionalAmount(String, long)}.
     *
     * @param baseAmount The base amount, inclusive of tax, in subunit form (cents, pence, etc)
     * @param currency   The ISO-4217 currency code
     */
    public Amounts(long baseAmount, String currency) {
        this(baseAmount, currency, null);
    }

    /**
     * Initialise with another Amounts object and copy over the values.
     *
     * @param from The amounts to copy from
     */
    public Amounts(Amounts from) {
        this(from.baseAmount, from.currency, from.additionalAmounts);
        currencyExchangeRate = from.currencyExchangeRate;
        originalCurrency = from.originalCurrency;
    }

    /**
     * Initialise with base amount (inclusive of tax), currency and additional amounts map.
     *
     * @param baseAmount        The base amount, inclusive of tax, in subunit form (cents, pence, etc)
     * @param currency          The ISO-4217 currency code
     * @param additionalAmounts The additional amounts
     */
    public Amounts(long baseAmount, String currency, Map<String, Long> additionalAmounts) {
        checkArgument(baseAmount >= 0 && currency != null && currency.length() == 3, "Base amount and currency must be set correctly");
        this.baseAmount = baseAmount;
        this.currency = currency;
        this.additionalAmounts = createMap(additionalAmounts);
    }

    /**
     * Add an additional amount to complement the base amount.
     *
     * The additional amounts are represented via a string identifier and the amount value in subunit form.
     *
     * Examples of identifiers are "tip" and "cashback".
     *
     * @param identifier The string identifier for the amount
     * @param amount     The amount value
     */
    public void addAdditionalAmount(String identifier, long amount) {
        checkArgument(identifier != null && amount >= 0, "Identifier must be set and value must be >= 0");
        additionalAmounts.put(identifier, amount);
    }

    /**
     * Add an additional amount as a fraction of the base amount.
     *
     * This is useful for cases where a fee, charity contribution, etc is calculated as a fraction or percentage of the base amount value.
     *
     * @param identifier The string identifier for the amount
     * @param fraction   The fraction of the base amount, ranging from 0.0 to 1.0f (0% to 100%)
     */
    public void addAdditionalAmountAsBaseFraction(String identifier, float fraction) {
        if (fraction < 0.0f || fraction > 1.0f) {
            throw new IllegalArgumentException("Fraction must be between 0.0 and 1.0");
        }
        addAdditionalAmount(identifier, (long) (baseAmount * fraction));
    }

    /**
     * Get the currency for these amounts.
     *
     * @return The ISO-4217 currency code
     */
    @NonNull
    public String getCurrency() {
        return currency;
    }

    /**
     * Get the base amount in subunit form (cents, pence, etc).
     *
     * The base amount is inclusive of any tax.
     *
     * Note that base amount can be 0.
     *
     * @return The base amount in subunit form
     */
    public long getBaseAmountValue() {
        return baseAmount;
    }

    /**
     * Get an {@link Amount} representation of the base amount with associated currency.
     *
     * The base amount is inclusive of any tax.
     *
     * @return Base {@link Amount}
     */
    @NonNull
    public Amount getBaseAmount() {
        return new Amount(baseAmount, currency);
    }

    /**
     * Check whether there is an additional amount with the provided identifier defined.
     *
     * @param identifier The identifier
     * @return True if one is defined, false otherwise
     */
    public boolean hasAdditionalAmount(String identifier) {
        return additionalAmounts.containsKey(identifier);
    }

    /**
     * Get the additional amount value for the provided identifier.
     *
     * If none is set, 0 will be returned.
     *
     * @param identifier The identifier
     * @return The amount value
     */
    public long getAdditionalAmountValue(String identifier) {
        if (additionalAmounts.containsKey(identifier)) {
            return additionalAmounts.get(identifier);
        }
        return 0;
    }

    /**
     * Get an {@link Amount} representation of the additional amount with associated currency.
     *
     * @param identifier The identifier
     * @return The additional {@link Amount}
     */
    @NonNull
    public Amount getAdditionalAmount(String identifier) {
        return new Amount(getAdditionalAmountValue(identifier), currency);
    }

    /**
     * Get the map of all the additional amounts set.
     *
     * @return The map of identifier keys mapped to amount values
     */
    @NonNull
    public Map<String, Long> getAdditionalAmounts() {
        return additionalAmounts;
    }

    /**
     * Get the total amount (base + additional amounts) in subunit form.
     *
     * @return The total amount
     */
    @JsonConverter.ExposeMethod(value = "totalAmount")
    public long getTotalAmountValue() {
        long total = baseAmount;
        for (String key : additionalAmounts.keySet()) {
            total += additionalAmounts.get(key);
        }
        return total;
    }

    /**
     * Get the total amount (base + additional amounts) in subunit form, excluding any additionals as defined by their provided identifiers.
     *
     * This is useful for scenarios where some additional amounts are supported natively in an environment (such as tip and cashback), but others
     * (like charity donations) are not and should be appended to the base amount for that environment.
     *
     * @param amountIdentifiers The identifiers of the amounts to exclude from the calculation
     * @return The total amount value, excluding the amounts as provider via the identifiers
     */
    public long getTotalExcludingAmounts(String... amountIdentifiers) {
        long total = baseAmount;
        List<String> identifierList = Arrays.asList(amountIdentifiers);
        for (String key : additionalAmounts.keySet()) {
            if (!identifierList.contains(key)) {
                total += additionalAmounts.get(key);
            }
        }
        return total;
    }

    /**
     * Get an {@link Amount} representation of the total amount with associated currency.
     *
     * @return Total {@link Amount}
     */
    @NonNull
    public Amount getTotalAmount() {
        return new Amount(getTotalAmountValue(), currency);
    }

    /**
     * Get the currency exchange rate associated with these amounts, if relevant.
     *
     * @return The currency exchange rate (from original currency to new currency)
     */
    public double getCurrencyExchangeRate() {
        return currencyExchangeRate;
    }

    void setCurrencyExchangeRate(double currencyExchangeRate) {
        this.currencyExchangeRate = currencyExchangeRate;
    }

    /**
     * Get the original currency for these amounts.
     *
     * This will only be set if the currency has been changed.
     *
     * @return The original currency
     */
    @NonNull
    public String getOriginalCurrency() {
        return originalCurrency;
    }

    void setOriginalCurrency(String originalCurrency) {
        this.originalCurrency = originalCurrency;
    }

    @Override
    public String toString() {
        return "Amounts{" +
                "baseAmount=" + baseAmount +
                ", additionalAmounts=" + additionalAmounts +
                ", currency='" + currency + '\'' +
                ", currencyExchangeRate=" + currencyExchangeRate +
                ", originalCurrency='" + originalCurrency + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Amounts amounts = (Amounts) o;

        if (baseAmount != amounts.baseAmount) {
            return false;
        }
        if (Double.compare(amounts.currencyExchangeRate, currencyExchangeRate) != 0) {
            return false;
        }
        if (additionalAmounts != null ? !additionalAmounts.equals(amounts.additionalAmounts) : amounts.additionalAmounts != null) {
            return false;
        }
        if (currency != null ? !currency.equals(amounts.currency) : amounts.currency != null) {
            return false;
        }
        return originalCurrency != null ? originalCurrency.equals(amounts.originalCurrency) : amounts.originalCurrency == null;
    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        result = (int) (baseAmount ^ (baseAmount >>> 32));
        result = 31 * result + (additionalAmounts != null ? additionalAmounts.hashCode() : 0);
        result = 31 * result + (currency != null ? currency.hashCode() : 0);
        temp = Double.doubleToLongBits(currencyExchangeRate);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        result = 31 * result + (originalCurrency != null ? originalCurrency.hashCode() : 0);
        return result;
    }

    /**
     * Add together two amounts and get a new Amounts instance back with the result.
     *
     * @param a1 The first amount to add with
     * @param a2 The second amount to add with
     * @return The combined result
     */
    @NonNull
    public static Amounts addAmounts(Amounts a1, Amounts a2) {
        if (a1 == null || a2 == null || !a1.getCurrency().equals(a2.getCurrency())) {
            throw new IllegalArgumentException("Invalid amounts or trying to combine different currencies");
        }
        long newBaseAmount = a1.getBaseAmountValue() + a2.getBaseAmountValue();
        Map<String, Long> newAdditionals = createMap(a1.getAdditionalAmounts());
        Map<String, Long> a2Additionals = a2.getAdditionalAmounts();
        for (String a2Key : a2Additionals.keySet()) {
            if (newAdditionals.containsKey(a2Key)) {
                newAdditionals.put(a2Key, newAdditionals.get(a2Key) + a2Additionals.get(a2Key));
            } else {
                newAdditionals.put(a2Key, a2Additionals.get(a2Key));
            }
        }

        return new Amounts(newBaseAmount, a1.getCurrency(), newAdditionals);
    }

    /**
     * Subtract one amounts from another.
     *
     * Note that the result Amounts will only contain additionalAmounts defined in a1, the one being subtracted from.
     * If an amount is set in a2 only, it will not be added to the resulting Amounts.
     *
     * @param a1                        The amounts to subtract a2 from
     * @param a2                        The amounts of which a1 will be reduced by
     * @param keepZeroAmountAdditionals Whether or not to keep additional amounts with a value of zero
     * @return The reduced amounts
     */
    @NonNull
    public static Amounts subtractAmounts(Amounts a1, Amounts a2, boolean keepZeroAmountAdditionals) {
        if (a1 == null || a2 == null || !a1.getCurrency().equals(a2.getCurrency())) {
            throw new IllegalArgumentException("Invalid amounts or trying to combine different currencies");
        }
        long remainingBase = Math.max(a1.getBaseAmountValue() - a2.getBaseAmountValue(), 0);
        Map<String, Long> newAdditionals = createMap(a1.getAdditionalAmounts());
        Map<String, Long> a2Additionals = a2.getAdditionalAmounts();
        for (String a2Key : a2Additionals.keySet()) {
            if (newAdditionals.containsKey(a2Key)) {
                long additionalRemainder = Math.max(newAdditionals.get(a2Key) - a2Additionals.get(a2Key), 0);
                if (additionalRemainder > 0 || keepZeroAmountAdditionals) {
                    newAdditionals.put(a2Key, additionalRemainder);
                } else {
                    newAdditionals.remove(a2Key);
                }
            }
        }

        return new Amounts(remainingBase, a1.getCurrency(), newAdditionals);
    }

    private static Map<String, Long> createMap(Map<String, Long> source) {
        Map<String, Long> map = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        if (source != null) {
            map.putAll(source);
        }
        return map;
    }

    @Override
    public String toJson() {
        return JsonConverter.serialize(this);
    }
}
