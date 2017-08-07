package com.expedia.vm.traveler

import com.expedia.bookings.widget.traveler.FrequentFlyerCard
import rx.subjects.BehaviorSubject

class FlightTravelerFrequentFlyerItemViewModel {
    //TODO: update traveler with FFN info
    //TODO: set up VM for input fields
    private lateinit var frequentFlyerCard: FrequentFlyerCard
    val frequentFlyerProgramObservable = BehaviorSubject.create<String>()
    val frequentFlyerNumberObservable = BehaviorSubject.create<String>()

    fun bind(frequentFlyerCard: FrequentFlyerCard) {
        this.frequentFlyerCard = frequentFlyerCard
    }

    fun getTitle(): String {
        return frequentFlyerCard.airlineName
    }
}