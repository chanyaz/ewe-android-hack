package com.expedia.vm

import android.content.Context
import android.graphics.drawable.Drawable
import android.support.v4.content.ContextCompat
import com.expedia.bookings.R
import com.expedia.bookings.data.LineOfBusiness
import com.expedia.bookings.data.Money
import com.expedia.bookings.tracking.FlightsV2Tracking
import com.expedia.bookings.tracking.PackagesTracking
import com.expedia.bookings.utils.Constants
import rx.Observable
import rx.subjects.BehaviorSubject
import java.math.BigDecimal

open class PriceChangeViewModel(context: Context, lob: LineOfBusiness) {
    val originalPrice = BehaviorSubject.create<Money>()
    val newPrice = BehaviorSubject.create<Money>()

    val priceChangeText = BehaviorSubject.create<String>()
    val priceChangeDrawable = BehaviorSubject.create<Drawable>()
    val priceChangeVisibility = BehaviorSubject.create<Boolean>()

    init {
        Observable.zip(originalPrice, newPrice, { originalPrice, newPrice ->
            val hasPriceChange = originalPrice != null
            if (hasPriceChange) {
                if (newPrice.amount > originalPrice?.amount) {
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
                val priceDiff = newPrice.amount.toInt() - originalPrice.amount.toInt()
                var diffPercentage: Int = 0
                if (priceDiff.toInt() != 0) {
                    diffPercentage = (priceDiff * 100) / originalPrice.amount.toInt()
                }
                if (lob == LineOfBusiness.PACKAGES) {
                    PackagesTracking().trackPriceChange(diffPercentage)
                } else if (lob == LineOfBusiness.FLIGHTS_V2) {
                    FlightsV2Tracking.trackFlightPriceChange(diffPercentage)
                }
            }
            priceChangeVisibility.onNext(hasPriceChange && showPriceChange(newPrice.amount, originalPrice.amount))
        }).subscribe()
    }

    fun showPriceChange(newprice: BigDecimal, originalprice: BigDecimal): Boolean {
        if (newprice.compareTo(originalprice) == 1) {
            return true
        }
        val ratio = newprice.toDouble()/originalprice.toDouble()
        val isChangeBigEnoughToShow = (1.0 - ratio.toDouble()) >= Constants.PRICE_CHANGE_NOTIFY_CUTOFF
        return isChangeBigEnoughToShow
    }
}