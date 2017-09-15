package com.expedia.vm.traveler

import com.expedia.bookings.data.Traveler
import com.expedia.bookings.data.flights.FrequentFlyerCard
import com.expedia.bookings.data.flights.FrequentFlyerPlansTripResponse
import rx.subjects.PublishSubject
import java.util.ArrayList
import java.util.LinkedHashMap

class FlightTravelerFrequentFlyerItemViewModel(var traveler: Traveler) {
    private lateinit var frequentFlyerCard: FrequentFlyerCard
    var allFrequentFlyerPlans: LinkedHashMap<String, FrequentFlyerPlansTripResponse> = LinkedHashMap()
    var enrolledPlans: LinkedHashMap<String, FrequentFlyerPlansTripResponse> = LinkedHashMap()
    var allAirlineCodes: ArrayList<String> = ArrayList()

    val frequentFlyerProgramObservable = PublishSubject.create<String>()
    val frequentFlyerNumberObservable = PublishSubject.create<String>()
    val frequentFlyerProgramNumberViewModel = FrequentFlyerProgramNumberViewModel(traveler, "")
    val enrolledFrequentFlyerPlansObservable = PublishSubject.create<LinkedHashMap<String, FrequentFlyerPlansTripResponse>>()

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
        val frequentFlyerProgramName = allFrequentFlyerPlans[frequentFlyerCard.airlineCode]?.frequentFlyerPlanName ?: ""
        frequentFlyerNumberObservable.onNext(frequentFlyerProgramNumber)
        frequentFlyerProgramObservable.onNext(frequentFlyerProgramName)
        enrolledPlans = createEnrolledPlansFromTraveler()
        enrolledFrequentFlyerPlansObservable.onNext(enrolledPlans)
    }

    private fun createEnrolledPlansFromTraveler() : LinkedHashMap<String, FrequentFlyerPlansTripResponse> {
        val enrolledPlans = LinkedHashMap<String, FrequentFlyerPlansTripResponse>()
        traveler.frequentFlyerMemberships.asIterable().forEach { plan ->
            val tripResponse = FrequentFlyerPlansTripResponse()
            val airlineCode = plan.key
            tripResponse.airlineCode = airlineCode
            tripResponse.frequentFlyerPlanName = allFrequentFlyerPlans[airlineCode]?.frequentFlyerPlanName ?: ""
            tripResponse.membershipNumber = plan.value.membershipNumber
            enrolledPlans.put(airlineCode, tripResponse)
        }
        return enrolledPlans
    }
}