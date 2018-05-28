package com.expedia.vm

import com.expedia.bookings.data.Money
import com.expedia.bookings.data.pos.PointOfSale
import com.expedia.bookings.data.pos.PointOfSaleId
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject

abstract class BaseTotalPriceWidgetViewModel(isSlidable: Boolean) {
    val total = PublishSubject.create<Money>()
    val savings = PublishSubject.create<Money>()
    val referenceTotalPrice = PublishSubject.create<Money>()
    val shouldShowSavings = PublishSubject.create<Boolean>()
    val betterSavingsObservable = PublishSubject.create<Boolean>()
    val pricePerPerson = PublishSubject.create<Money>()
    val priceAvailableObservable = PublishSubject.create<Boolean>()

    val pricePerPersonObservable = BehaviorSubject.create<String>()
    val totalPriceObservable = BehaviorSubject.create<String>()
    val savingsPriceObservable = BehaviorSubject.createDefault<String>("")
    val bundleTextLabelObservable = BehaviorSubject.create<String>()
    val perPersonTextLabelObservable = BehaviorSubject.create<Boolean>()
    val bundleTotalIncludesObservable = BehaviorSubject.create<String>()
    val contentDescriptionObservable = BehaviorSubject.create<String>()
    val costBreakdownEnabledObservable = BehaviorSubject.create<Boolean>()

    val priceWidgetClick = PublishSubject.create<PriceWidgetEvent>()

    enum class PriceWidgetEvent {
        SAVINGS_STRIP_CLICK,
        SAVINGS_BUTTON_CLICK,
        INFO_ICON_CLICK,
        BUNDLE_PRICE_CLICK,
        BUNDLE_WIDGET_CLICK
    }

    abstract fun getAccessibleContentDescription(isCostBreakdownShown: Boolean = false,
                                                 isSlidable: Boolean = false, isExpanded: Boolean = false): String

    abstract fun shouldShowTotalPriceLoadingProgress(): Boolean

    init {
        total.subscribe { total ->
            totalPriceObservable.onNext(total.getFormattedMoneyFromAmountAndCurrencyCode(getMoneyFormatFlagForInteger()))
            contentDescriptionObservable.onNext(getAccessibleContentDescription(false, isSlidable))
        }
    }

    fun setPriceValues(totalPrice: Money, tripSavings: Money) {
        total.onNext(totalPrice)
        savings.onNext(tripSavings)
    }

    fun getMoneyFormatFlag(): Int {

        val pointOfSale = PointOfSale.getPointOfSale().pointOfSaleId
        return if (pointOfSale == PointOfSaleId.JAPAN || pointOfSale == PointOfSaleId.SOUTH_KOREA) Money.F_NO_DECIMAL_IF_INTEGER_ELSE_TWO_PLACES_AFTER_DECIMAL
        else Money.F_ALWAYS_TWO_PLACES_AFTER_DECIMAL
    }

    fun getMoneyFormatFlagForInteger(): Int {

        return Money.F_NO_DECIMAL_IF_INTEGER_ELSE_TWO_PLACES_AFTER_DECIMAL
    }

    fun setBundleTotalPrice(totalPrice: Money) {
        totalPriceObservable.onNext(totalPrice.getFormattedMoneyFromAmountAndCurrencyCode(getMoneyFormatFlagForInteger()))
    }
}
