package com.expedia.vm.traveler

import com.expedia.bookings.widget.traveler.FrequentFlyerCard
import rx.subjects.PublishSubject

class FlightTravelerFrequentFlyerItemViewModel {
    //TODO: update traveler with FFN info
    //TODO: set up VM for input fields
    private lateinit var frequentFlyerCard: FrequentFlyerCard
    val frequentFlyerProgramObservable = PublishSubject.create<String>()
    val frequentFlyerNumberObservable = PublishSubject.create<String>()

    fun bind(frequentFlyerCard: FrequentFlyerCard) {
        this.frequentFlyerCard = frequentFlyerCard
    }

    fun getTitle(): String {
        return frequentFlyerCard.airlineName
    }
}