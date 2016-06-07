package com.expedia.vm.packages

import android.content.Context
import com.expedia.bookings.R
import com.expedia.bookings.data.Money
import com.squareup.phrase.Phrase
import rx.Observable
import rx.subjects.BehaviorSubject
import rx.subjects.PublishSubject

class BundlePriceViewModel(val context: Context, val isSlidable: Boolean = false) {
    val setTextObservable = PublishSubject.create<Pair<String, String>>()
    val total = PublishSubject.create<Money>()
    val savings = PublishSubject.create<Money>()

    val totalPriceObservable = BehaviorSubject.create<String>()
    val savingsPriceObservable = BehaviorSubject.create<String>()
    val bundleTextLabelObservable = BehaviorSubject.create<String>()
    val perPersonTextLabelObservable = BehaviorSubject.create<Boolean>()
    val bundleTotalIncludesObservable = BehaviorSubject.create<String>()
    val contentDescriptionObservable = BehaviorSubject.create<String>()

    init {
        setTextObservable.subscribe { bundle ->
            totalPriceObservable.onNext(bundle.first)
            savingsPriceObservable.onNext(bundle.second)
            contentDescriptionObservable.onNext(getAccessibleContentDescription(isSlidable))
        }

        Observable.combineLatest(total, savings, { total, savings ->
            var packageSavings = Phrase.from(context, R.string.bundle_total_savings_TEMPLATE)
                    .put("savings", savings.formattedMoney)
                    .format().toString()
            setTextObservable.onNext(Pair(total.formattedMoney, packageSavings))
            contentDescriptionObservable.onNext(getAccessibleContentDescription())
        }).subscribe()
    }

    fun getAccessibleContentDescription(isSlidable: Boolean = false, isExpanded: Boolean = false): String {
        var stringID: Int
        if (isSlidable) {
            if (isExpanded)
                return context.getString(R.string.bundle_overview_price_widget_button_close)
            else
                stringID = R.string.bundle_overview_price_widget_button_open
        }
        else {
            stringID = R.string.bundle_overview_price_widget
        }
        return Phrase.from(context, stringID)
                .put("totalprice", totalPriceObservable.value)
                .put("savings", savingsPriceObservable.value)
                .format().toString()
    }
}