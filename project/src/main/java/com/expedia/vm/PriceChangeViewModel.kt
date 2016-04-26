package com.expedia.vm

import android.content.Context
import android.graphics.drawable.Drawable
import android.support.v4.content.ContextCompat
import com.expedia.bookings.R
import com.expedia.bookings.data.LineOfBusiness
import com.expedia.bookings.data.Money
import com.expedia.bookings.tracking.PackagesTracking
import rx.Observable
import rx.subjects.BehaviorSubject

class PriceChangeViewModel(context: Context, lob: LineOfBusiness) {
    val originalPackagePrice = BehaviorSubject.create<Money>()
    val packagePrice = BehaviorSubject.create<Money>()

    val priceChangeText = BehaviorSubject.create<String>()
    val priceChangeDrawable = BehaviorSubject.create<Drawable>()
    val priceChangeVisibility = BehaviorSubject.create<Boolean>()

    init {
        Observable.zip(originalPackagePrice, packagePrice, { originalPrice, newPrice ->
            val hasPriceChange = originalPrice != null
            var priceDiff: Int

            if (hasPriceChange) {
                if (newPrice.amount > originalPrice?.amount) {
                    priceChangeDrawable.onNext(ContextCompat.getDrawable(context, R.drawable.price_change_increase))
                    priceChangeText.onNext(context.getString(R.string.price_changed_from_TEMPLATE, originalPrice?.formattedMoney))
                } else if (newPrice < originalPrice) {
                    priceChangeDrawable.onNext(ContextCompat.getDrawable(context, R.drawable.price_change_decrease))
                    priceChangeText.onNext(context.getString(R.string.price_dropped_from_TEMPLATE, originalPrice?.formattedMoney))
                } else {
                    // API could return price change error with no difference in price (see: hotel_price_change_checkout.json)
                    priceChangeDrawable.onNext(ContextCompat.getDrawable(context, R.drawable.price_change_decrease))
                    priceChangeText.onNext(context.getString(R.string.price_changed_from_TEMPLATE, originalPrice?.formattedMoney))
                }
                priceDiff = newPrice.amount.toInt() - originalPrice.amount.toInt()
                if (lob == LineOfBusiness.PACKAGES) {
                    var diffPercentage: Int = 0
                    if (priceDiff.toInt() != 0) {
                        diffPercentage = (priceDiff * 100) / originalPrice.amount.toInt()
                    }
                    PackagesTracking().trackPriceChange(diffPercentage)
                }
            }
            priceChangeVisibility.onNext(hasPriceChange)

        }).subscribe()

    }
}