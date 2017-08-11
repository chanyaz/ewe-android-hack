package com.expedia.vm.traveler

import com.expedia.bookings.data.Traveler
import com.expedia.bookings.widget.traveler.FrequentFlyerCard
import rx.subjects.PublishSubject

class FlightTravelerFrequentFlyerItemViewModel(var traveler: Traveler) {
    private lateinit var frequentFlyerCard: FrequentFlyerCard
    val ffnProgramNumberViewModel = FrequentFlyerProgramNumberViewModel(traveler, "")
    val ffnProgramNumberSubject = PublishSubject.create<String>()

    val frequentFlyerProgramObservable = PublishSubject.create<String>()
    val frequentFlyerNumberObservable = PublishSubject.create<String>()

    fun bind(frequentFlyerCard: FrequentFlyerCard) {
        this.frequentFlyerCard = frequentFlyerCard
        ffnProgramNumberViewModel.airlineKey = frequentFlyerCard.airlineCode
        updateTraveler(traveler)
    }

    fun getTitle(): String {
        return frequentFlyerCard.airlineName
    }

    fun updateTraveler(traveler: Traveler) {
        this.traveler = traveler
        ffnProgramNumberViewModel.traveler = traveler

        val frequentFlyerProgramNumber = traveler.frequentFlyerMemberships[(frequentFlyerCard.airlineCode)]?.membershipNumber ?: ""
        ffnProgramNumberSubject.onNext(frequentFlyerProgramNumber)
    }
}