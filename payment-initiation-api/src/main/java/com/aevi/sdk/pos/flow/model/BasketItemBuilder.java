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

import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;

/**
 * Builder to create {@link BasketItem} instances.
 */
public class BasketItemBuilder {

    private String id;
    private int quantity = 1;
    private String label;
    private String category;
    private long amount;
    private Map<String, String> references;

    /**
     * Initialise the builder with a default random id.
     */
    public BasketItemBuilder() {
        generateRandomId();
    }

    /**
     * Initialise the builder from the provided basket item.
     *
     * @param copyFrom The item to copy from
     */
    public BasketItemBuilder(BasketItem copyFrom) {
        this.id = copyFrom.getId();
        this.quantity = copyFrom.getQuantity();
        this.label = copyFrom.getLabel();
        this.category = copyFrom.getCategory();
        this.amount = copyFrom.getIndividualAmount();
        this.references = copyFrom.getReferences();
    }

    /**
     * Generate a new random id (UUID) for this item.
     *
     * Note that the builder is initialised with a random id, meaning this only has to be called to generate a new random id.
     *
     * @return This builder
     */
    @NonNull
    public BasketItemBuilder generateRandomId() {
        this.id = UUID.randomUUID().toString();
        return this;
    }

    /**
     * Set the id for this item, overriding the default generated random id.
     *
     * @param id The id
     * @return This builder
     */
    @NonNull
    public BasketItemBuilder withId(String id) {
        this.id = id;
        return this;
    }

    /**
     * Set the initial quantity for this item.
     *
     * Defaults to 1 if not set.
     *
     * @param quantity The item quantity
     * @return This builder
     */
    @NonNull
    public BasketItemBuilder withQuantity(int quantity) {
        this.quantity = quantity;
        return this;
    }

    /**
     * Increment the quantity by one.
     *
     * @return This builder
     */
    @NonNull
    public BasketItemBuilder incrementQuantity() {
        this.quantity++;
        return this;
    }

    /**
     * Decrements the item quantity by one as long as the current quantity is greater than zero.
     *
     * If the quantity is already zero, this method has no effect.
     *
     * @return This builder
     */
    @NonNull
    public BasketItemBuilder decrementQuantity() {
        this.quantity--;
        return this;
    }

    /**
     * Modify the current quantity with the provided offset.
     *
     * This effectively does quantity += offset, and can be used to increase or decrease the quantity.
     *
     * @param offset The value to modify the current quantity with.
     * @return This builder
     */
    @NonNull
    public BasketItemBuilder offsetQuantityBy(int offset) {
        this.quantity += offset;
        return this;
    }

    /**
     * Set the label for this item.
     *
     * @param label The label
     * @return This builder
     */
    @NonNull
    public BasketItemBuilder withLabel(String label) {
        this.label = label;
        return this;
    }

    /**
     * Set the category for this item (such as "drinks" or "vegetables")
     *
     * @param category The category the item belongs to
     * @return This builder
     */
    @NonNull
    public BasketItemBuilder withCategory(String category) {
        this.category = category;
        return this;
    }

    /**
     * Set the item amount value.
     *
     * Note that the amount value can be negative to represent discounts, etc.
     *
     * @param amount The item amount value
     * @return This builder
     */
    @NonNull
    public BasketItemBuilder withAmount(long amount) {
        this.amount = amount;
        return this;
    }

    /**
     * Add a custom / additional reference to this basket item.
     *
     * This can be used to add further information about the basket item that is not covered by the primary fields.
     *
     * @param key   The reference key
     * @param value The reference value
     * @return This builder
     */
    public BasketItemBuilder withReference(String key, String value) {
        if (references == null) {
            references = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        }
        references.put(key, value);
        return this;
    }

    /**
     * Build the instance with a default quantity of 1 (if not set).
     *
     * @return A {@link BasketItem} instance
     */
    @NonNull
    public BasketItem build() {
        if (quantity < 0) {
            throw new IllegalArgumentException("Basket item must have a quantity of zero or more");
        }
        if (id == null || id.isEmpty()) {
            throw new IllegalArgumentException("A basket item must have an id");
        }
        if (label == null || label.isEmpty()) {
            throw new IllegalArgumentException("A basket item must have a label");
        }
        return new BasketItem(id, label, category, amount, quantity, references);
    }
}
