package com.aevi.sdk.pos.flow.model;

import org.junit.Test;

import static org.assertj.core.api.Java6Assertions.assertThat;

public class BasketItemBuilderTest {

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowExceptionForNegativeQuantity() throws Exception {
        new BasketItemBuilder().withLabel("bla").withQuantity(-1).build();
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowExceptionIfIdIsNull() throws Exception {
        new BasketItemBuilder().withId(null).withQuantity(1).withLabel("bla").build();
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowExceptionIfLabelNull() throws Exception {
        new BasketItemBuilder().withQuantity(1).build();
    }

    @Test
    public void shouldInitialiseWithDefaultRandomId() throws Exception {
        BasketItem basketItem = new BasketItemBuilder().withLabel("banana").build();
        assertThat(basketItem.getId()).isNotEmpty();
    }

    @Test
    public void shouldCreateCorrectBasketItem() throws Exception {
        BasketItem basketItem =
                new BasketItemBuilder().withId("123").withLabel("banana").withQuantity(2).withCategory("fruit").withAmount(200).build();
        assertThat(basketItem.getId()).isEqualTo("123");
        assertThat(basketItem.getLabel()).isEqualTo("banana");
        assertThat(basketItem.getCategory()).isEqualTo("fruit");
        assertThat(basketItem.getQuantity()).isEqualTo(2);
        assertThat(basketItem.getIndividualAmount()).isEqualTo(200);
        assertThat(basketItem.getTotalAmount()).isEqualTo(400);
    }

    @Test
    public void shouldAllowCreationFromExistingItem() throws Exception {
        BasketItem basketItem =
                new BasketItemBuilder().withId("123").withLabel("banana").withQuantity(2).withCategory("fruit").withAmount(200).build();
        BasketItem newItem = new BasketItemBuilder(basketItem).withQuantity(4).build();

        assertThat(newItem.getId()).isEqualTo("123");
        assertThat(newItem.getLabel()).isEqualTo("banana");
        assertThat(newItem.getCategory()).isEqualTo("fruit");
        assertThat(newItem.getQuantity()).isEqualTo(4);
        assertThat(newItem.getIndividualAmount()).isEqualTo(200);
        assertThat(newItem.getTotalAmount()).isEqualTo(800);
    }

    @Test
    public void shouldAllowIncrementItemQuantity() throws Exception {
        BasketItem basketItem =
                new BasketItemBuilder().withId("123").withLabel("banana").withQuantity(2).withCategory("fruit").withAmount(200).build();
        BasketItem newItem = new BasketItemBuilder(basketItem).incrementQuantity().build();
        assertThat(newItem.getQuantity()).isEqualTo(3);
    }

    @Test
    public void shouldAllowDecrementItemQuantity() throws Exception {
        BasketItem basketItem =
                new BasketItemBuilder().withId("123").withLabel("banana").withQuantity(2).withCategory("fruit").withAmount(200).build();
        BasketItem newItem = new BasketItemBuilder(basketItem).decrementQuantity().build();
        assertThat(newItem.getQuantity()).isEqualTo(1);
    }

    @Test
    public void shouldAllowOffsetPositiveItemQuantity() throws Exception {
        BasketItem basketItem =
                new BasketItemBuilder().withId("123").withLabel("banana").withQuantity(2).withCategory("fruit").withAmount(200).build();
        BasketItem newItem = new BasketItemBuilder(basketItem).offsetQuantityBy(2).build();
        assertThat(newItem.getQuantity()).isEqualTo(4);
    }

    @Test
    public void shouldAllowOffsetNegativeItemQuantity() throws Exception {
        BasketItem basketItem =
                new BasketItemBuilder().withId("123").withLabel("banana").withQuantity(2).withCategory("fruit").withAmount(200).build();
        BasketItem newItem = new BasketItemBuilder(basketItem).offsetQuantityBy(-1).build();
        assertThat(newItem.getQuantity()).isEqualTo(1);
    }

    @Test
    public void referencesShouldBeCaseInsensitive() throws Exception {
        BasketItemBuilder builder =
                new BasketItemBuilder().withId("123").withLabel("banana").withQuantity(2).withCategory("fruit").withAmount(200);
        builder.withReference("bleep", "blarp");
        builder.withReference("BLEEP", "blarp");
        builder.withReference("blEEp", "blarp");

        BasketItem item = builder.build();

        assertThat(item.getReferences()).hasSize(1);
        assertThat(item.getReference("bleep")).isEqualTo("blarp");
        assertThat(item.getReference("BLEEP")).isEqualTo("blarp");
    }

}
