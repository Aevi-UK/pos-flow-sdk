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
import android.util.Log;
import com.aevi.sdk.flow.model.AdditionalData;
import com.aevi.sdk.flow.model.BaseModel;
import com.aevi.util.json.JsonConverter;

import java.util.*;

import static com.aevi.sdk.pos.flow.model.RoundingStrategy.NEAREST;

/**
 * Represents a basket consisting of one or multiple {@link BasketItem}.
 *
 * Baskets are uniquely identified via a randomly generated id, and also contains a name as a readable identifier.
 *
 * Basket items are kept in a list sorted by most recently added first.
 *
 * Basket items are uniquely identified by an id, meaning it is possible that there is more than one item with the same label.
 * It is up to the client to manage this correctly.
 *
 * Note that as basket items are immutable, any update to items (such as merging or changing quantity) leads to new instances being created. For the
 * latest up to date item, always fetch via {@link #getItemById(String)}.
 */
@SuppressWarnings({"UnusedReturnValue", "WeakerAccess"})
public class Basket extends BaseModel {

    private final String basketName;
    private final List<BasketItem> displayItems;
    private final AdditionalData additionalBasketData;
    private boolean primaryBasket;
    private RoundingStrategy roundingStrategy = NEAREST;

    /**
     * Initialise an empty basket.
     *
     * @param basketName The name of the basket
     */
    public Basket(String basketName) {
        this(basketName, new ArrayList<>());
    }

    /**
     * Initialise a basket from the provided var-args items, maintaining the same order as they are specified in.
     *
     * @param basketName  The name of the basket
     * @param basketItems The var-args list of basket items to initialise the basket with
     */
    public Basket(String basketName, BasketItem... basketItems) {
        this(basketName, Arrays.asList(basketItems));
    }

    /**
     * Initialise a basket based on the provided basket items.
     *
     * @param basketName  The name of the basket
     * @param basketItems The list of basket items to initialise the basket with
     */
    public Basket(String basketName, List<BasketItem> basketItems) {
        this(UUID.randomUUID().toString(), basketName, basketItems);
    }

    /**
     * Internal constructor for initialising basket.
     *
     * @param id          The id of the basket
     * @param basketName  The name of the basket
     * @param basketItems The list of basket items to initialise the basket with
     */
    Basket(String id, String basketName, List<BasketItem> basketItems) {
        super(id);
        this.basketName = basketName != null ? basketName : "N/A";
        this.displayItems = new ArrayList<>(basketItems);
        this.additionalBasketData = new AdditionalData();
    }

    /**
     * Set the rounding strategy for calculating the basket total to the appropriate whole sub-unit.
     *
     * By default, {@link RoundingStrategy#NEAREST} will be used.
     *
     * @param roundingStrategy The {@link RoundingStrategy} to use for calculating basket total
     */
    public void setRoundingStrategy(RoundingStrategy roundingStrategy) {
        this.roundingStrategy = roundingStrategy;
    }

    /**
     * Get the name of the basket.
     *
     * @return Name of the basket
     */
    @NonNull
    public String getBasketName() {
        return basketName;
    }

    /**
     * Check whether this basket is the primary basket for the current flow.
     *
     * The primary basket is generally provided by the client application initiating the flow (i.e the POS app).
     *
     * @return True if this basket is the primary basket, false otherwise
     */
    public boolean isPrimaryBasket() {
        return primaryBasket;
    }

    /**
     * Retrieve the list of basket items for reading and/or modifying.
     *
     * This returns the list that backs the basket - any changes made in the list will directly affect the basket.
     *
     * The list is sorted by most recent first.
     *
     * @return The list of basket items
     */
    @NonNull
    public List<BasketItem> getBasketItems() {
        return displayItems;
    }

    /**
     * Add one or multiple basket items to the *front* of the basket either as a new item or via merging with an existing one with the same id.
     *
     * If there is an existing item with the same id in the basket, then the item quantity of the two items will be added together and stored.
     *
     * Note that due to how items may be merged, the instance passed into this method is not necessarily the same instance that is stored.
     *
     * @param items The item(s) to add
     */
    public void addItems(BasketItem... items) {
        for (BasketItem item : items) {
            BasketItem existingItem = getItemById(item.getId());
            if (existingItem != null) {
                replaceItem(existingItem, item.getQuantity(), true);
            } else {
                displayItems.add(0, item);
            }
        }
    }

    /**
     * Check whether the basket has an item with the provided id.
     *
     * Optionally, a min quantity parameter can be passed in to filter results against. If an item with the id is found and the min quantity is set,
     * this method will only return true if the item has at least the min quantity.
     *
     * @param id          The id to match against
     * @param minQuantity Optional param to specify a minimum quantity criteria
     * @return True if there is an item with matching id, false otherwise
     */
    public boolean hasItemWithId(String id, int... minQuantity) {
        int minQuantityValue = minQuantity.length > 0 ? minQuantity[0] : -1;
        for (BasketItem item : displayItems) {
            if (item.getId().equals(id) && item.getQuantity() >= minQuantityValue) {
                return true;
            }
        }
        return false;
    }

    /**
     * Check whether the basket has an item with the provided label.
     *
     * Note that there may be more than one item with the same label.
     *
     * @param label The label to match against
     * @return True if there is an item with matching label, false otherwise.
     */
    public boolean hasItemWithLabel(String label) {
        for (BasketItem item : displayItems) {
            if (item.getLabel().equals(label)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Get an item based on its id.
     *
     * @param id The id to match against
     * @return The item if found, or null
     */
    @Nullable
    public BasketItem getItemById(String id) {
        for (BasketItem item : displayItems) {
            if (item.getId().equals(id)) {
                return item;
            }
        }
        return null;
    }

    /**
     * Get an item that matches the provided label.
     *
     * Note that there may be more than one item with the same label, in which case it returns the first one found.
     *
     * @param label The label to match against
     * @return The item if found, or null
     */
    @Nullable
    public BasketItem getItemByLabel(String label) {
        for (BasketItem item : displayItems) {
            if (item.getLabel().equals(label)) {
                return item;
            }
        }
        return null;
    }

    /**
     * Get all items that belongs to the provided category.
     *
     * @param category The category to filter against
     * @return A list of items belonging to the provided category. May be empty.
     */
    @NonNull
    public List<BasketItem> getBasketItemsByCategory(String category) {
        List<BasketItem> items = new ArrayList<>();
        for (BasketItem displayItem : displayItems) {
            String itemCategory = displayItem.getCategory();
            if (itemCategory != null && itemCategory.equals(category)) {
                items.add(displayItem);
            }
        }
        return items;
    }

    /**
     * Increment the basket item quantity of the item with the provided id.
     *
     * Note that if no such item exists, this is a no-op.
     *
     * @param itemId    The basket item id to increment the quantity of
     * @param increment The increment value (positive)
     * @return The item with updated quantity or null if no item with id found
     */
    @Nullable
    public BasketItem incrementItemQuantity(String itemId, int increment) {
        BasketItem item = getItemById(itemId);
        if (item != null) {
            return replaceItem(item, increment, true);
        }
        return null;
    }

    /**
     * Decrement the basket item quantity of the item with the provided id.
     *
     * The default behaviour is that items that are decremented to zero are removed from the basket. This can be overriden by the retainIfZero param.
     *
     * Note that if no such item exists, this is a no-op.
     *
     * @param itemId       The basket item id to decrement the quantity of
     * @param decrement    The decrement value (positive)
     * @param retainIfZero If set to true, the item will be kept in the basket despite the quantity being zero
     * @return The item with the updated quantity or null if no item with id found or it is removed
     */
    @Nullable
    public BasketItem decrementItemQuantity(String itemId, int decrement, Boolean... retainIfZero) {
        BasketItem item = getItemById(itemId);
        if (item != null) {
            boolean retain = retainIfZero.length > 0 && retainIfZero[0];
            return replaceItem(item, -decrement, retain);
        }
        return null;
    }

    /**
     * Explicitly set the quantity for a basket item.
     *
     * Note that if no such item exists, this is a no-op.
     *
     * The quantity must be zero or larger. Negative values are ignored.
     *
     * @param itemId      The item id
     * @param newQuantity The new quantity for the basket item (must be positive)
     * @return The item with the updated quantity or null if no item with id found
     */
    @Nullable
    public BasketItem setItemQuantity(String itemId, int newQuantity) {
        if (newQuantity >= 0) {
            BasketItem item = getItemById(itemId);
            if (item != null) {
                return replaceItem(item, newQuantity - item.getQuantity(), true);
            }
        }
        return null;
    }

    /**
     * Remove the item with the provided id.
     *
     * @param itemId The id of the item to remove
     * @return The basket item that was removed, or null
     */
    @Nullable
    public BasketItem removeItem(String itemId) {
        BasketItem item = getItemById(itemId);
        if (item != null) {
            displayItems.remove(item);
            return item;
        }
        return null;
    }

    /**
     * Clear the basket of all items.
     */
    public void clearItems() {
        displayItems.clear();
    }

    /**
     * Get the number of unique items in the basket, incl any zero-quantity items.
     *
     * See {@link #getTotalNumberOfItems()} for retrieving the total number of items.
     *
     * @return The number of unique items in the basket
     */
    public int getNumberOfUniqueItems() {
        return displayItems.size();
    }

    /**
     * Get the total number of items, taking into account the quantity of each individual item. This excludes zero-quantity items.
     *
     * See {@link #getNumberOfUniqueItems()} for retrieving the number of unique items.
     *
     * @return The total number of items
     */
    @JsonConverter.ExposeMethod(value = "totalNumberOfItems")
    public int getTotalNumberOfItems() {
        int total = 0;
        for (BasketItem displayItem : displayItems) {
            total += displayItem.getQuantity();
        }
        return total;
    }

    /**
     * Get the total basket value, inclusive of tax.
     *
     * This value is calculated as the sum of each item base amount with modifiers applied and then rounded as per the strategy set
     * via {@link #setRoundingStrategy(RoundingStrategy)}.
     *
     * @return The total basket value
     */
    @JsonConverter.ExposeMethod(value = "totalBasketValue")
    public long getTotalBasketValue() {
        double total = 0;
        for (BasketItem displayItem : displayItems) {
            total += displayItem.getTotalFractionalAmount();
        }
        if (roundingStrategy != null) {
            switch (roundingStrategy) {
                case DOWN:
                    return (long) total;
                case UP:
                    return (long) Math.ceil(total);
                case NEAREST:
                default:
                    return Math.round(total);
            }
        }
        return Math.round(total);
    }

    /**
     * Add additional data to this basket.
     *
     * See {@link AdditionalData#addData(String, Object[])} for more info.
     *
     * @param key    The key to use for this data
     * @param values An array of values for this data
     * @param <T>    The type of object this data is an array of
     */
    @SafeVarargs
    public final <T> void addAdditionalData(String key, T... values) {
        additionalBasketData.addData(key, values);
    }

    /**
     * Get any additional data set for this basket.
     *
     * @return The additional basket data
     */
    @NonNull
    public AdditionalData getAdditionalBasketData() {
        return additionalBasketData;
    }

    private BasketItem replaceItem(BasketItem existingItem, int quantityOffset, boolean retainIfZero) {
        BasketItem newItem = new BasketItemBuilder(existingItem).offsetQuantityBy(quantityOffset).build();
        if (newItem.getQuantity() == 0 && !retainIfZero) {
            displayItems.remove(existingItem);
        } else {
            displayItems.set(displayItems.indexOf(existingItem), newItem);
        }
        return newItem;
    }

    /**
     * For internal use.
     *
     * @param primaryBasket True or false
     */
    public void setPrimaryBasket(boolean primaryBasket) {
        this.primaryBasket = primaryBasket;
    }

    @Override
    public String toJson() {
        return JsonConverter.serialize(this);
    }

    @Override
    public String toString() {
        return "Basket{" + "basketName='" + basketName + '\'' + '}';
    }

    /**
     * Helper function to log all the items in the basket to Android logcat.
     */
    public void logBasketEntries() {
        Log.i("Basket", "Items in basket: " + basketName);
        for (BasketItem displayItem : displayItems) {
            Log.i("BasketItem", displayItem.toString());
        }
    }

    @Override
    public boolean equals(Object o) {
        return doEquals(o, false);
    }

    @Override
    public boolean equivalent(Object o) {
        return doEquals(o, true);
    }

    private boolean doEquals(Object o, boolean equiv) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        if (equiv && !super.doEquivalent(o)) {
            return false;
        } else if (!equiv && !super.equals(o)) {
            return false;
        }

        Basket basket = (Basket) o;
        return Objects.equals(basketName, basket.basketName) &&
                Objects.equals(displayItems, basket.displayItems) &&
                Objects.equals(additionalBasketData, basket.additionalBasketData);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), basketName, displayItems, additionalBasketData);
    }

    /**
     * Clone the provided basket and return a new identical instance.
     *
     * @param basket The basket to clone
     * @return A cloned basket
     */
    public static Basket clone(Basket basket) {
        return new Basket(basket.getId(), basket.getBasketName(), basket.getBasketItems());
    }
}
