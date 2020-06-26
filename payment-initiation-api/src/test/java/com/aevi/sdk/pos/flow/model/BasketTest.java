package com.aevi.sdk.pos.flow.model;


import com.aevi.util.json.JsonConverter;

import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public class BasketTest {

    private static final BasketItem defaultItemOne =
            new BasketItem(UUID.randomUUID().toString(), "LabelOne", null, 1000, 1000, 2, null, null, null, null);
    private static final BasketItem defaultItemTwo =
            new BasketItem(UUID.randomUUID().toString(), "LabelTwo", null, 400, 400, 1, null, null, null, null);
    private Basket sourceBasket;

    @Before
    public void setUp() throws Exception {
        sourceBasket = new Basket("test");
    }

    @Test
    public void canAddItems() throws Exception {
        sourceBasket.addItems(defaultItemOne, defaultItemTwo);

        assertThat(sourceBasket.getNumberOfUniqueItems()).isEqualTo(2);
        assertThat(sourceBasket.hasItemWithId(defaultItemOne.getId())).isTrue();
        assertThat(sourceBasket.hasItemWithId(defaultItemTwo.getId())).isTrue();
    }

    @Test
    public void canMergeItemsWithSameId() throws Exception {
        int singleItemQuantity = defaultItemOne.getQuantity();
        sourceBasket.addItems(defaultItemOne);

        assertThat(sourceBasket.getItemById(defaultItemOne.getId()).getQuantity()).isEqualTo(singleItemQuantity);

        sourceBasket.addItems(defaultItemOne);

        assertThat(sourceBasket.getItemById(defaultItemOne.getId()).getQuantity()).isEqualTo(singleItemQuantity * 2);
    }

    @Test
    public void returnsListInRecentFirstOrder() throws Exception {
        sourceBasket.addItems(defaultItemOne, defaultItemTwo);

        assertThat(sourceBasket.getBasketItems()).containsExactly(defaultItemTwo, defaultItemOne);
    }

    @Test
    public void listOrderIsRetainedAfterUpdatingQuantity() throws Exception {
        sourceBasket.addItems(defaultItemOne, defaultItemTwo);

        sourceBasket.incrementItemQuantity(defaultItemOne.getId(), 1);
        sourceBasket.decrementItemQuantity(defaultItemOne.getId(), 1);

        assertThat(sourceBasket.getBasketItems()).containsExactly(defaultItemTwo, defaultItemOne);
    }

    @Test
    public void canHaveMultipleItemsSameLabel() throws Exception {
        sourceBasket.addItems(defaultItemOne);
        BasketItem anotherItem = new BasketItem("123", defaultItemOne.getLabel(), null, 500, 500, 1, null, null, null, null);
        sourceBasket.addItems(anotherItem);

        assertThat(sourceBasket.getNumberOfUniqueItems()).isEqualTo(2);
        assertThat(sourceBasket.getItemById(defaultItemOne.getId()).getLabel()).isEqualTo(sourceBasket.getItemById(anotherItem.getId()).getLabel());
    }

    @Test
    public void canSerialiseAndDeserialise() {
        sourceBasket.addItems(defaultItemOne, defaultItemTwo);

        String json = sourceBasket.toJson();
        Basket deserialised = JsonConverter.deserialize(json, Basket.class);

        assertThat(deserialised).isEqualTo(sourceBasket);
        String json2 = deserialised.toJson();
        assertThat(json2).isEqualTo(json);
    }

    @Test
    public void canSpecifyMinQuantityForHasItemWithId() throws Exception {
        sourceBasket.addItems(defaultItemOne, defaultItemTwo);

        assertThat(sourceBasket.hasItemWithId(defaultItemOne.getId())).isTrue();
        assertThat(sourceBasket.hasItemWithId(defaultItemTwo.getId())).isTrue();
        assertThat(sourceBasket.hasItemWithId(defaultItemOne.getId(), defaultItemOne.getQuantity())).isTrue();
        assertThat(sourceBasket.hasItemWithId(defaultItemOne.getId(), defaultItemOne.getQuantity() + 1)).isFalse();
    }

    @Test
    public void canFindBasketItemById() throws Exception {
        sourceBasket.addItems(defaultItemOne, defaultItemTwo);

        BasketItem item = sourceBasket.getItemById(defaultItemOne.getId());
        assertThat(item).isEqualTo(defaultItemOne);
    }

    @Test
    public void canFindBasketItemByLabel() {
        sourceBasket.addItems(defaultItemOne, defaultItemTwo);

        BasketItem item = sourceBasket.getItemByLabel(defaultItemOne.getLabel());
        assertThat(item).isEqualTo(defaultItemOne);
    }

    @Test
    public void canIncrementBasketItemQuantity() {
        sourceBasket.addItems(defaultItemOne, defaultItemTwo);

        int beforeQuantity = defaultItemOne.getQuantity();
        sourceBasket.incrementItemQuantity(defaultItemOne.getId(), 1);

        assertThat(sourceBasket.getItemById(defaultItemOne.getId()).getQuantity()).isEqualTo(beforeQuantity + 1);
    }

    @Test
    public void canDecrementBasketItemQuantity() throws Exception {
        sourceBasket.addItems(defaultItemOne, defaultItemTwo);

        int beforeQuantity = defaultItemOne.getQuantity();
        sourceBasket.decrementItemQuantity(defaultItemOne.getId(), 1);

        assertThat(sourceBasket.getItemById(defaultItemOne.getId()).getQuantity()).isEqualTo(beforeQuantity - 1);
    }

    @Test
    public void cantDecrementPastZeroAndItemRetainedAsZeroQuantity() throws Exception {
        sourceBasket.addItems(defaultItemOne, defaultItemTwo);

        sourceBasket.decrementItemQuantity(defaultItemTwo.getId(), 1, true);

        assertThat(sourceBasket.getItemById(defaultItemTwo.getId()).getQuantity()).isEqualTo(0);
    }

    @Test
    public void itemIsRemovedAfterDecrementingToZeroIfRetainNotSet() throws Exception {
        sourceBasket.addItems(defaultItemOne, defaultItemTwo);

        sourceBasket.decrementItemQuantity(defaultItemTwo.getId(), 1);

        assertThat(sourceBasket.hasItemWithId(defaultItemTwo.getId())).isFalse();
    }

    @Test
    public void canSetBasketItemQuantity() throws Exception {
        sourceBasket.addItems(defaultItemOne, defaultItemTwo);

        sourceBasket.setItemQuantity(defaultItemOne.getId(), 10);

        assertThat(sourceBasket.getItemById(defaultItemOne.getId()).getQuantity()).isEqualTo(10);
    }

    @Test
    public void canGetTotalNumberOfItems() throws Exception {
        sourceBasket.addItems(defaultItemOne, defaultItemTwo);

        assertThat(sourceBasket.getTotalNumberOfItems()).isEqualTo(defaultItemOne.getQuantity() + defaultItemTwo.getQuantity());
    }

    @Test
    public void canGetNumberOfUniqueItems() throws Exception {
        sourceBasket.addItems(defaultItemOne, defaultItemTwo);

        assertThat(sourceBasket.getNumberOfUniqueItems()).isEqualTo(2);
    }

    @Test
    public void canClearItems() throws Exception {
        sourceBasket.addItems(defaultItemOne, defaultItemTwo);

        sourceBasket.clearItems();

        assertThat(sourceBasket.getTotalNumberOfItems()).isZero();
    }

    @Test
    public void canRemoveItem() throws Exception {
        sourceBasket.addItems(defaultItemOne, defaultItemTwo);

        sourceBasket.removeItem(defaultItemOne.getId());

        assertThat(sourceBasket.getTotalNumberOfItems()).isEqualTo(1);
        assertThat(sourceBasket.hasItemWithId(defaultItemOne.getId())).isFalse();
    }

    @Test
    public void canGetTotalBasketValue() throws Exception {
        sourceBasket.addItems(defaultItemOne, defaultItemTwo);

        assertThat(sourceBasket.getTotalBasketValue()).isEqualTo(defaultItemOne.getTotalAmount() + defaultItemTwo.getTotalAmount());
    }

    @Test
    public void canGetItemsByCategory() throws Exception {
        sourceBasket.addItems(new BasketItem("123", "Coke", "Drinks", 1000, 1000, 1, null, null, null, null),
                new BasketItem("456", "Fanta", "Drinks", 1000, 1000, 1, null, null, null, null),
                new BasketItem("789", "Pork", "Meat", 1000, 1000, 1, null, null, null, null));

        List<BasketItem> drinks = sourceBasket.getBasketItemsByCategory("Drinks");
        assertThat(drinks).hasSize(2).containsExactlyInAnyOrder(sourceBasket.getItemById("123"), sourceBasket.getItemById("456"));
    }

    @Test
    public void totalValueHandlesNegativeItemsCorrectly() throws Exception {
        sourceBasket.addItems(new BasketItem("123", "Coke", "Drinks", 1000, 1000, 1, null, null, null, null),
                new BasketItem("456", "Fanta", "Drinks", -500, -500, 1, null, null, null, null));

        long total = sourceBasket.getTotalBasketValue();
        assertThat(total).isEqualTo(500);
    }

    @Test
    public void totalValueHandlesNegativeItemsOnlyCorrectly() throws Exception {
        sourceBasket.addItems(new BasketItem("456", "Fanta", "Drinks", -500, -500, 1, null, null, null, null));

        long total = sourceBasket.getTotalBasketValue();
        assertThat(total).isEqualTo(-500);
    }

    @Test
    public void totalShouldCalculateFromAmountFieldIfNoModifiers() throws Exception {
        sourceBasket.addItems(new BasketItem("123", "Coke", "Drinks", 1000, 0, 1, null, null, null, null),
                new BasketItem("456", "Fanta", "Drinks", 500, 0, 1, null, null, null, null));

        assertThat(sourceBasket.getTotalBasketValue()).isEqualTo(1500);
    }

    @Test
    public void checkTwoBasketsAreEquivalent() {
        Basket basket1 = new Basket("basket");
        basket1.addItems(defaultItemOne, defaultItemTwo);
        Basket basket2 = new Basket("basket");
        basket2.addItems(defaultItemOne, defaultItemTwo);

        assertThat(basket1.equivalent(basket2)).isTrue();
    }

    @Test
    public void totalShouldCalculateFromBaseAndModifiersAndRound() throws Exception {
        BasketItem basketItem1 = new BasketItemBuilder()
                .withLabel("vanilla")
                .withQuantity(2)
                .withBaseAmountAndModifiers(500, new BasketItemModifierBuilder("tax1", "tax").withFractionalAmount(202.05f).build(),
                        new BasketItemModifierBuilder("tax2", "tax").withPercentage(24.56f).build()).build();

        BasketItem basketItem2 = new BasketItemBuilder()
                .withLabel("ice")
                .withQuantity(1)
                .withBaseAmountAndModifiers(150, new BasketItemModifierBuilder("tax4", "tax").withFractionalAmount(-5.5f).build(),
                        new BasketItemModifierBuilder("tax5", "tax").withPercentage(-25.003f).build()).build();

        sourceBasket.addItems(basketItem1, basketItem2);

        sourceBasket.setRoundingStrategy(RoundingStrategy.NEAREST);
        assertThat(sourceBasket.getTotalBasketValue()).isEqualTo(1757);

        sourceBasket.setRoundingStrategy(RoundingStrategy.DOWN);
        assertThat(sourceBasket.getTotalBasketValue()).isEqualTo(1756);

        sourceBasket.setRoundingStrategy(RoundingStrategy.UP);
        assertThat(sourceBasket.getTotalBasketValue()).isEqualTo(1757);
    }
}
