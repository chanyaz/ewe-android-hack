package com.expedia.vm

import com.expedia.bookings.data.Money
import com.expedia.bookings.data.pos.PointOfSale
import com.expedia.bookings.data.pos.PointOfSaleId
import rx.subjects.BehaviorSubject
import rx.subjects.PublishSubject

abstract class BaseTotalPriceWidgetViewModel(isSlidable: Boolean) {
    val total = PublishSubject.create<Money>()
    val savings = PublishSubject.create<Money>()
    val pricePerPerson = PublishSubject.create<Money>()
    val priceAvailableObservable = PublishSubject.create<Boolean>()

    val pricePerPersonObservable = BehaviorSubject.create<String>()
    val totalPriceObservable = BehaviorSubject.create<String>()
    val savingsPriceObservable = BehaviorSubject.create<String>("")
    val bundleTextLabelObservable = BehaviorSubject.create<String>()
    val perPersonTextLabelObservable = BehaviorSubject.create<Boolean>()
    val bundleTotalIncludesObservable = BehaviorSubject.create<String>()
    val contentDescriptionObservable = BehaviorSubject.create<String>()
    val costBreakdownEnabledObservable = BehaviorSubject.create<Boolean>()

    abstract fun getAccessibleContentDescription(isCostBreakdownShown: Boolean = false,
                                                 isSlidable: Boolean = false, isExpanded: Boolean = false) : String

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

        return if (PointOfSale.getPointOfSale().pointOfSaleId == PointOfSaleId.JAPAN) Money.F_NO_DECIMAL
        else Money.F_ALWAYS_TWO_PLACES_AFTER_DECIMAL
    }

    fun getMoneyFormatFlagForInteger(): Int {

        return if (PointOfSale.getPointOfSale().pointOfSaleId == PointOfSaleId.JAPAN) Money.F_NO_DECIMAL
        else Money.F_NO_DECIMAL_IF_INTEGER_ELSE_TWO_PLACES_AFTER_DECIMAL
    }

}