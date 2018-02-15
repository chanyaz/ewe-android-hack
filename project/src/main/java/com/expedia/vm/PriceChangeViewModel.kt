package com.expedia.vm

import android.content.Context
import android.graphics.drawable.Drawable
import android.support.v4.content.ContextCompat
import com.expedia.bookings.extensions.ObservableOld
import com.expedia.bookings.R
import com.expedia.bookings.data.Money
import io.reactivex.subjects.BehaviorSubject

open class PriceChangeViewModel(context: Context) {
    val originalPrice = BehaviorSubject.create<Money>()
    val newPrice = BehaviorSubject.create<Money>()

    val priceChangeText = BehaviorSubject.create<String>()
    val priceChangeDrawable = BehaviorSubject.create<Drawable>()
    val priceChangeVisibility = BehaviorSubject.create<Boolean>()

    init {
        ObservableOld.zip(originalPrice, newPrice, { originalPrice, newPrice ->
            if (originalPrice != null) {
                if (newPrice.amount > originalPrice.amount) {
                    priceChangeDrawable.onNext(ContextCompat.getDrawable(context, R.drawable.warning_triangle_icon))
                    priceChangeText.onNext(context.getString(R.string.price_changed_from_TEMPLATE,
                            originalPrice.getFormattedMoneyFromAmountAndCurrencyCode(Money.F_NO_DECIMAL_IF_INTEGER_ELSE_TWO_PLACES_AFTER_DECIMAL)))
                } else if (newPrice < originalPrice) {
                    priceChangeDrawable.onNext(ContextCompat.getDrawable(context, R.drawable.price_change_decrease))
                    priceChangeText.onNext(context.getString(R.string.price_dropped_from_TEMPLATE,
                            originalPrice.getFormattedMoneyFromAmountAndCurrencyCode(Money.F_NO_DECIMAL_IF_INTEGER_ELSE_TWO_PLACES_AFTER_DECIMAL)))
                } else {
                    // API could return price change error with no difference in price (see: hotel_price_change_checkout.json)
                    priceChangeDrawable.onNext(ContextCompat.getDrawable(context, R.drawable.price_change_decrease))
                    priceChangeText.onNext(context.getString(R.string.price_changed_from_TEMPLATE,
                            originalPrice.getFormattedMoneyFromAmountAndCurrencyCode(Money.F_NO_DECIMAL_IF_INTEGER_ELSE_TWO_PLACES_AFTER_DECIMAL)))
                }
            }
        }).subscribe()
    }
}
