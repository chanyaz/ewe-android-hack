package com.expedia.vm.traveler

import com.expedia.bookings.data.Traveler
import com.expedia.bookings.data.flights.FrequentFlyerPlansTripResponse
import com.expedia.bookings.widget.traveler.FrequentFlyerCard
import rx.subjects.PublishSubject
import java.util.LinkedHashMap
import java.util.ArrayList

class FlightTravelerFrequentFlyerItemViewModel(var traveler: Traveler) {
    private lateinit var frequentFlyerCard: FrequentFlyerCard
    var allFrequentFlyerPlans: LinkedHashMap<String, FrequentFlyerPlansTripResponse> = LinkedHashMap()
    var enrolledPlans: LinkedHashMap<String, FrequentFlyerPlansTripResponse> = LinkedHashMap()
    var allAirlineCodes: ArrayList<String> = ArrayList()

    val frequentFlyerProgramObservable = PublishSubject.create<String>()
    val frequentFlyerNumberObservable = PublishSubject.create<String>()
    val frequentFlyerProgramNumberViewModel = FrequentFlyerProgramNumberViewModel(traveler, "")

    fun bind(frequentFlyerCard: FrequentFlyerCard) {
        this.frequentFlyerCard = frequentFlyerCard
        frequentFlyerProgramNumberViewModel.airlineKey = frequentFlyerCard.airlineCode
        updateTraveler(traveler)
    }

    fun getTitle(): String {
        return frequentFlyerCard.airlineName
    }

    fun updateTraveler(traveler: Traveler) {
        this.traveler = traveler
        frequentFlyerProgramNumberViewModel.traveler = traveler

        val frequentFlyerProgramNumber = traveler.frequentFlyerMemberships[frequentFlyerCard.airlineCode]?.membershipNumber ?: ""
        val frequentFlyerProgramName = allFrequentFlyerPlans[frequentFlyerCard.airlineCode]?.frequentFlyerPlanName
        frequentFlyerNumberObservable.onNext(frequentFlyerProgramNumber)
        frequentFlyerProgramObservable.onNext(frequentFlyerProgramName)
    }
}