package com.expedia.vm.traveler

import com.expedia.bookings.data.Traveler
import com.expedia.bookings.widget.traveler.FrequentFlyerCard
import rx.subjects.BehaviorSubject

class FlightTravelerFrequentFlyerItemViewModel(var traveler: Traveler) {
    //TODO: update traveler with FFN info
    //TODO: set up VM for input fields
    private lateinit var frequentFlyerCard: FrequentFlyerCard
    val ffnProgramNumberViewModel = FrequentFlyerProgramNumberViewModel(traveler, "")
    val ffnProgramNumberSubject = BehaviorSubject.create<String>()

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

        val frequentFlyerProgramNumber = traveler.frequentFlyerMemberships.get(frequentFlyerCard.airlineCode)?.membershipNumber
        if (frequentFlyerProgramNumber?.isNotEmpty() ?: false) {
            ffnProgramNumberSubject.onNext(frequentFlyerProgramNumber)
        } else {
            ffnProgramNumberSubject.onNext("")
        }

    }
}