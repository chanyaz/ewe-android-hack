package com.expedia.bookings.widget

import com.expedia.bookings.data.rail.responses.RailSearchResponse
import rx.subjects.BehaviorSubject
import rx.subjects.PublishSubject

public class RailViewModel() {

    //Inputs
    val legOptionObservable = PublishSubject.create<RailSearchResponse.LegOption>()

    //Outputs
    val legIdObservable = BehaviorSubject.create<String>()
    val priceObservable = BehaviorSubject.create<String>()
    val operatorObservable = BehaviorSubject.create<String>()
    val durationObservable = BehaviorSubject.create<Int>()

    init {
        legOptionObservable.subscribe {
            legIdObservable.onNext(it.legOptionId)
            priceObservable.onNext(it.formattedPrice())
            operatorObservable.onNext(it.allOperators())
            durationObservable.onNext(it.durationInMinutes)
        }
    }
}
