package com.expedia.vm.traveler

import com.expedia.bookings.data.Traveler
import com.expedia.bookings.data.flights.FlightCreateTripResponse
import com.expedia.bookings.data.flights.FrequentFlyerPlansTripResponse
import com.expedia.bookings.widget.traveler.FrequentFlyerCard
import rx.subjects.BehaviorSubject
import rx.subjects.PublishSubject
import java.util.TreeMap
import kotlin.properties.Delegates

class FlightTravelerFrequentFlyerItemViewModel(var traveler: Traveler) {
    private lateinit var frequentFlyerCard: FrequentFlyerCard
    var allFrequentFlyerPlans: TreeMap<String, FrequentFlyerPlansTripResponse> = TreeMap()
    var enrolledPlans: TreeMap<String, FrequentFlyerPlansTripResponse> = TreeMap()
    var allAirlineNames: ArrayList<String> = ArrayList()
    val ffnProgramNumberViewModel = FrequentFlyerProgramNumberViewModel(traveler, "")
    val ffnProgramNumberSubject = PublishSubject.create<String>()

    val frequentFlyerProgramObservable = BehaviorSubject.create<String>()
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
        val frequentFlyerProgramName = allFrequentFlyerPlans[frequentFlyerCard.airlineCode]?.frequentFlyerPlanName
        ffnProgramNumberSubject.onNext(frequentFlyerProgramNumber)
        frequentFlyerProgramObservable.onNext(frequentFlyerProgramName)
    }
}