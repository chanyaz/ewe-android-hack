package com.expedia.vm.flights

import io.reactivex.subjects.BehaviorSubject

class FlightConfirmationCardViewModel(flightTitle: String, flightSubtitle: String, flightUrl: String, flightDeparetureDateTitle: String) {
    val titleSubject = BehaviorSubject.create<String>()
    val subtitleSubject = BehaviorSubject.create<String>()
    val urlSubject = BehaviorSubject.create<String>()
    val departureDateTitleSubject = BehaviorSubject.create<String>()

    init {
        titleSubject.onNext(flightTitle)
        subtitleSubject.onNext(flightSubtitle)
        urlSubject.onNext(flightUrl)
        departureDateTitleSubject.onNext(flightDeparetureDateTitle)
    }
}