package com.expedia.vm

import com.expedia.bookings.data.Money
import rx.subjects.BehaviorSubject
import rx.subjects.PublishSubject

abstract class BaseTotalPriceWidgetViewModel(isSlidable: Boolean) {
    val total = PublishSubject.create<Money>()
    val savings = PublishSubject.create<Money>()
    val pricePerPerson = PublishSubject.create<Money>()
    val showTotalBundlePrice = PublishSubject.create<Boolean>()

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

    init {
        total.subscribe { total ->
            totalPriceObservable.onNext(total.getFormattedMoneyFromAmountAndCurrencyCode(Money.F_NO_DECIMAL_IF_INTEGER_ELSE_TWO_PLACES_AFTER_DECIMAL))
            contentDescriptionObservable.onNext(getAccessibleContentDescription(false, isSlidable))
        }
    }

    fun setPriceValues(totalPrice: Money, tripSavings: Money) {
        total.onNext(totalPrice)
        savings.onNext(tripSavings)
    }
}