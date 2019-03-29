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
import android.support.annotation.Nullable;
import com.aevi.util.json.JsonConverter;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Represents an immutable basket item with associated id, label, category, quantity and amount.
 *
 * A basket item may have a positive or negative amount, representing both purchases and applied discounts.
 *
 * Please create via {@link BasketItemBuilder}.
 */
public class BasketItem {

    private final String id;
    private final String label;
    private final String category;
    private final long amount;
    private final int quantity;
    private final Map<String, String> references;

    // Default constructor for deserialisation
    BasketItem() {
        this("", "", null, 0, 0, null);
    }

    /**
     * Create a new basket item with label, category, amount (inclusive of tax) and quantity.
     *
     * @param id         The identifier (SKU or similar) for this item
     * @param label      The label of the item to show to merchants/customers, such as "Red onion"
     * @param category   The category the item belongs to, such as "vegetables" or "dairy"
     * @param amount     The purchase amount for this (individual) item, inclusive of tax
     * @param quantity   The number of this type of basket item (default is 1, below 0 will produce an exception)
     * @param references Custom references for this basket item
     */
    BasketItem(String id, String label, String category, long amount, int quantity, Map<String, String> references) {
        this.id = id;
        this.label = label;
        this.category = category;
        this.amount = amount;
        this.quantity = quantity;
        this.references = references;
    }

    /**
     * Get the id of this item.
     *
     * @return The id of the item.
     */
    @NonNull
    public String getId() {
        return id;
    }

    /**
     * Get the label for this item.
     *
     * @return The label
     */
    @NonNull
    public String getLabel() {
        return label;
    }

    /**
     * Get the category for this item, if any.
     *
     * @return The category
     */
    @Nullable
    public String getCategory() {
        return category;
    }

    /**
     * Get the cost (amount) for a single item of this type, inclusive of tax.
     *
     * Note that the amount may be negative in the case of discounts, etc.
     *
     * @return The cost (amount) for a single item of this type
     */
    public long getIndividualAmount() {
        return amount;
    }

    /**
     * Get the total cost (amount) for the items of this type, inclusive of tax.
     *
     * Note that the amount may be negative in the case of discounts, etc.
     *
     * @return The total cost (amount) for the items of this type.
     */
    @JsonConverter.ExposeMethod(value = "totalAmount")
    public long getTotalAmount() {
        return amount * quantity;
    }

    /**
     * Get the item quantity.
     *
     * @return The item quantity
     */
    public int getQuantity() {
        return quantity;
    }

    /**
     * Check whether this basket item has any custom references.
     *
     * @return True if there are references, false otherwise
     */
    public boolean hasReferences() {
        return references != null && !references.isEmpty();
    }

    /**
     * Get the value for a reference by its key.
     *
     * @param referenceKey The key of the reference
     * @return The reference value, or null if no such reference exists
     */
    @Nullable
    public String getReference(String referenceKey) {
        if (references != null) {
            return references.get(referenceKey);
        }
        return null;
    }

    /**
     * Get all item references.
     *
     * Note that as a basket item is immutable, this is returning an unmodifiable map.
     *
     * @return An unmodifiable copy of the item reference map
     */
    @NonNull
    public Map<String, String> getReferences() {
        return Collections.unmodifiableMap(references != null ? references : new HashMap<>());
    }

    @Override
    public String toString() {
        return "BasketItem{" +
                "id='" + id + '\'' +
                ", label='" + label + '\'' +
                ", category='" + category + '\'' +
                ", amount=" + amount +
                ", quantity=" + quantity +
                ", references=" + references +
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
        BasketItem that = (BasketItem) o;
        return amount == that.amount &&
                quantity == that.quantity &&
                Objects.equals(id, that.id) &&
                Objects.equals(label, that.label) &&
                Objects.equals(category, that.category) &&
                referenceEquals(references, that.references);
    }

    private boolean referenceEquals(Map<String, String> refsOne, Map<String, String> refsTwo) {
        if ((refsOne == null || refsOne.isEmpty()) && (refsTwo == null || refsTwo.isEmpty())) {
            return true;
        }
        return Objects.equals(refsOne, refsTwo);
    }

    @Override
    public int hashCode() {

        return Objects.hash(id, label, category, amount, quantity, references);
    }
}
