package com.expedia.vm.packages

import android.content.Context
import com.expedia.bookings.R
import com.expedia.bookings.data.Money
import com.squareup.phrase.Phrase
import rx.Observable
import rx.subjects.BehaviorSubject
import rx.subjects.PublishSubject

class BundlePriceViewModel(val context: Context) {
    val setTextObservable = PublishSubject.create<Pair<String, String>>()
    val total = PublishSubject.create<Money>()
    val savings = PublishSubject.create<Money>()

    val totalPriceObservable = BehaviorSubject.create<String>()
    val savingsPriceObservable = BehaviorSubject.create<String>()
    val bundleTextLabelObservable = BehaviorSubject.create<String>()
    val perPersonTextLabelObservable = BehaviorSubject.create<Boolean>()
    val bundleTotalIncludesObservable = BehaviorSubject.create<String>()

    init {
        setTextObservable.subscribe { bundle ->
            totalPriceObservable.onNext(bundle.first)
            savingsPriceObservable.onNext(bundle.second)
        }

        Observable.combineLatest(total, savings, { total, savings ->
            var packageSavings = Phrase.from(context, R.string.bundle_total_savings_TEMPLATE)
                    .put("savings", savings.formattedMoney)
                    .format().toString()
            setTextObservable.onNext(Pair(total.formattedMoney, packageSavings))
        }).subscribe()
    }
}