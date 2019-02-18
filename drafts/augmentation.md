

## Augmentation details
Depending on what function your application is offering and for what stages it will be called, there are different augmentation options. This section will break down all the augmentation options (for payment flows specifically) and for what stages and functions they may be useful. The next section will look at specific use cases and advise on how to model your service for that use case.

### Pre transaction-processing augmentations
The majority of the relevant augmentations happen before the `TRANSACTION_PROCESSING` stage where a payment service determines the outcome of the transaction. After that point, all flow services can do is add references. This section will cover the augmentation options for the `PRE_TRANSACTION` and `POST_CARD_READING` stages. For both these stages, any augmentations are done via the `PreTransactionModel`.

<div class="callout callout--warning">
  <p>Note that `PRE_FLOW` is not covered here as it's intended for quite specific use cases.</p>
</div>

#### Adding additional amounts
There are various scenarios for which adding additional amounts is relevant, be it for tipping, fees, donations, etc. The `PreTransactionModel` contains two methods for doing this;
- `setAdditionalAmount(String identifier, long amount)`
- `setAdditionalAmountAsBaseFraction(String identifier, float fraction)`

The first method allows you to specify the amount value in its sub-unit form, whereas the second method allows you to add an amount as a _fraction_ of the request _base amount value_. This is useful for a variety of cases where the amount is a percentage of the base amount, such as fees, tipping, etc.

In both cases, the amount is identified via a string identifier. See [Additional Amounts]({{ site.baseurl }}/technical/additional-amounts) for details.

#### Adding a basket
The client/POS application may have provided a basket in the initial request. To provide sensible separation between what app/service added what items, flow services can add _new_ baskets, but not add more items to the existing baskets (discounts excepted). This can be done with the `PreTransactionModel.addNewBasket(Basket basket)` method.

This is the recommended approach to add any form of "upsells" or extras, as it adds visibility of what specifically has been added, allowing receipts apps to show it clearly on the receipts.

<div class="callout callout--warning">
  <p>Note that the request amounts will be automatically updated to reflect this new basket.</p>
</div>

#### Adding/updating customer details
A flow service can either add or update customer details. If the initial `Payment` does not contain any customer data, the flow service can create a new `Customer` model and add to the transaction. If however a `Customer` model already exists, a flow service can add
- Tokens
- Customer details as additional data

Either case is done via `PreTransactionModel.addOrUpdateCustomerDetails(Customer customer)`. If adding, the `Customer` parameter is created by your service. If updating, you use the `Customer` model from the request, update it and then pass it in here.

#### Paying amounts
A flow service can pay off a portion or all of the requested amounts. The most common scenario for this is via loyalty rewards or points. This can be done via `PreTransactionModel.setAmountsPaid(Amounts amountsPaid, String paymentMethod)`. The amounts must not exceed requested amounts and the payment method must be set. See [Payment Methods]({{ site.baseurl }}/technical/payment-methods) for defined options.

This method is recommended when the payment has no relation to any basket items. If however the payment is provided as discounts to certain items in the basket, like offering a free coffee, then the recommended approach is to apply discounts to baskets which is covered in the next section.

#### Applying discounts to baskets
If your service provides discounts or rewards based on basket items, then this function will allow your service to apply discounts as items in the same basket. As an example, if the basket contains an item "Latte" with a cost of $3.50, you can via this method add a "free coffee reward" that would look something like "Reward: Free Latte" with a negative amount of $-3.50 to negate the cost of the original item.

See `PreTransactionModel.applyDiscountsToBasket(String basketId, List<BasketItem> basketItems, String paymentMethod)` for more info.

#### Adding request data
Any arbitrary data that may be required or useful for other flow services (or the POS app) can be added via the `PreTransactionModel.addRequestData(String key, T... values)` method.

### Adding references after transaction processing
The `PostTransactionModel` offers a `addReferences(String key, T... values)` method to provide references back to the POS app for the transaction.

