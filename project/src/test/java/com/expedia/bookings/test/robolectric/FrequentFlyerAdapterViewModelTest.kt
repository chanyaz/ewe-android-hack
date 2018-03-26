package com.expedia.bookings.test.robolectric

import com.expedia.bookings.data.Traveler
import com.expedia.bookings.data.flights.FlightCreateTripResponse
import com.expedia.bookings.data.flights.FlightLeg
import com.expedia.bookings.data.flights.FrequentFlyerPlansTripResponse
import com.expedia.bookings.data.flights.TravelerFrequentFlyerMembership
import com.expedia.vm.traveler.FrequentFlyerAdapterViewModel
import org.junit.Test
import kotlin.test.assertEquals

class FrequentFlyerAdapterViewModelTest {

    @Test
    fun testFrequentFlyerNumberDataWithTravelerChange() {
        val frequentFlyerAdapterViewModel = FrequentFlyerAdapterViewModel(getTravelerWithFFNMembership())
        frequentFlyerAdapterViewModel.flightLegsObservable.onNext(getFlightLeg())
        frequentFlyerAdapterViewModel.frequentFlyerPlans.onNext(getFrequentFlyerPlans())

        assertEquals("98765", frequentFlyerAdapterViewModel.viewHolderViewModels[0].enrolledPlans.get("UA")!!.membershipNumber)

        frequentFlyerAdapterViewModel.updateTravelerObservable.onNext(getTravelerWithFFNMembership("DA", "123456"))

        assertEquals("123456", frequentFlyerAdapterViewModel.viewHolderViewModels[0].enrolledPlans.get("DA")!!.membershipNumber)
    }

    private fun getFlightLeg(): List<FlightLeg> {
        val inboundSegment = FlightLeg.FlightSegment()
        inboundSegment.airlineName = "United"
        inboundSegment.flightNumber = "212"
        inboundSegment.airlineCode = "UA"
        val flightLeg = FlightTestUtil.getFlightCreateTripResponse().details.legs
        flightLeg[0].segments = listOf(inboundSegment)
        return flightLeg
    }

    private fun getAllFrequentFlyerPlans(): List<FrequentFlyerPlansTripResponse> {
        return listOf(getFrequentFlyerTripResponse("AA", "Alaska Airlines", "", "AA-A1", "A1"),
                getFrequentFlyerTripResponse("DA", "Delta Airlines", "", "DA-D1", "D1"),
                getFrequentFlyerTripResponse("UA", "United Airlines", "", "UA-U1", "U1"))
    }

    private fun getFrequentFlyerTripResponse(airlineCode: String, planName: String, membershipNumber: String, planID: String, planCode: String): FrequentFlyerPlansTripResponse {
        val enrolledPlan = FrequentFlyerPlansTripResponse()
        enrolledPlan.airlineCode = airlineCode
        enrolledPlan.frequentFlyerPlanName = planName
        enrolledPlan.membershipNumber = membershipNumber
        enrolledPlan.frequentFlyerPlanID = planID
        enrolledPlan.frequentFlyerPlanCode = planCode
        return enrolledPlan
    }

    private fun getFrequentFlyerPlans(): FlightCreateTripResponse.FrequentFlyerPlans {
        val frequentFlyerPlans = FlightCreateTripResponse.FrequentFlyerPlans()
        frequentFlyerPlans.allFrequentFlyerPlans = getAllFrequentFlyerPlans()
        frequentFlyerPlans.enrolledFrequentFlyerPlans = null
        return frequentFlyerPlans
    }

    private fun getTravelerWithFFNMembership(withAirlineCode: String = "UA", membershipNumer: String = "98765"): Traveler {
        val traveler = Traveler()
        traveler.addFrequentFlyerMembership(getNewFrequentFlyerMembership(withAirlineCode, membershipNumer, withAirlineCode, "UA-U1"))
        return traveler
    }

    private fun getNewFrequentFlyerMembership(airlineCode: String, number: String, planCode: String, planID: String): TravelerFrequentFlyerMembership {
        val newMembership = TravelerFrequentFlyerMembership()
        newMembership.airlineCode = airlineCode
        newMembership.membershipNumber = number
        newMembership.planCode = planCode
        newMembership.frequentFlyerPlanID = planID
        return newMembership
    }
}
