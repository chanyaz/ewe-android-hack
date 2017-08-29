package com.expedia.bookings.test.robolectric

import android.app.Activity
import com.expedia.bookings.data.BillingInfo
import com.expedia.bookings.data.Location
import com.expedia.bookings.data.Traveler
import com.expedia.bookings.data.flights.FlightCheckoutParams
import com.expedia.bookings.data.flights.TravelerFrequentFlyerMembership
import org.joda.time.LocalDate
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import kotlin.properties.Delegates
import kotlin.test.assertEquals
import kotlin.test.assertNull
import com.expedia.bookings.data.abacus.AbacusUtils


@RunWith(RobolectricRunner::class)
class FlightCheckoutParamsTest {
    var builder: FlightCheckoutParams.Builder by Delegates.notNull()

    var activity : Activity by Delegates.notNull()
    @Before
    fun before() {
        builder = FlightCheckoutParams.Builder()
        builder.billingInfo(getBillingInfo()).cvv("").tripId("").expectedFareCurrencyCode("").expectedTotalFare("")
        builder.tealeafTransactionId("")
        activity = Robolectric.buildActivity(Activity::class.java).create().get()
        RoboTestHelper.updateABTest(AbacusUtils.EBAndroidAppFlightFrequentFlyerNumber, AbacusUtils.DefaultVariant.BUCKETED.ordinal)
    }

    @Test
    fun testFrequentFlyerMemberships() {

        // test with main and additional passenger
        val listOfTravelers = listOf(getTravelerWithFrequentFlyerMemberships(), getTravelerWithFrequentFlyerMemberships(), getTravelerWithFrequentFlyerMemberships())
        var params = builder.travelers(listOfTravelers).build()
        var mapFrequentFlyerParams = params.toQueryMap()
        var appendString = constructFrequentFlyerAppendString(0, true, 0)
        assertTravelerDetails(mapFrequentFlyerParams, appendString, "VX", "VX-0", "VX-0", "123")
        appendString = constructFrequentFlyerAppendString(0, false, 0)
        assertTravelerDetails(mapFrequentFlyerParams, appendString, "VX", "VX-0", "VX-0", "123")
        appendString = constructFrequentFlyerAppendString(0, false, 1)
        assertTravelerDetails(mapFrequentFlyerParams, appendString, "VX", "VX-0", "VX-0", "123")
    }

    @Test
    fun testCheckoutParamsWithNoFrequentFlyerPlan() {
        var traveler = getTraveler()
        var params = builder.travelers(listOf(traveler)).build()
        var mapFrequentFlyerParams = params.toQueryMap()
        var appendString = constructFrequentFlyerAppendString(0, true, 0)
        assertTravelerDetails(mapFrequentFlyerParams, appendString)
    }

    private fun assertTravelerDetails(mapFrequentFlyerParams: Map<String, Any>, appendString: String,
                                      flightAirlineCode: String? = null,
                                      frequentFlyerPlanAirlineCode: String? = null,
                                      frequentFlyerPlanCode: String? = null,
                                      membershipNumber: String? = null) {
        assertEquals(flightAirlineCode, mapFrequentFlyerParams[appendString + "flightAirlineCode"])
        assertEquals(frequentFlyerPlanAirlineCode, mapFrequentFlyerParams[appendString + "frequentFlyerPlanAirlineCode"])
        assertEquals(frequentFlyerPlanCode, mapFrequentFlyerParams[appendString + "frequentFlyerPlanCode"])
        assertEquals(membershipNumber, mapFrequentFlyerParams[appendString + "membershipNumber"])
    }

    private fun constructFrequentFlyerAppendString(frequentFlyerIndex: Int, isMainPassenger: Boolean, associatePassengerIndex: Int): String {
        val appendString: String
        if (isMainPassenger) {
            appendString = "mainFlightPassenger.frequentFlyerDetails["+ frequentFlyerIndex+"]."
        } else {
            appendString = "associatedFlightPassengers["+associatePassengerIndex+"].frequentFlyerDetails["+ frequentFlyerIndex+"]."
        }
        return appendString
    }


    private fun getBillingInfo(): BillingInfo {
        var info = BillingInfo()
        info.expirationDate = LocalDate.now()
        val location = Location()
        location.streetAddress = arrayListOf("123 street")
        info.location = location
        return info
    }

    private fun getTraveler(): Traveler {
        val traveler = Traveler()
        traveler.birthDate = LocalDate.now().minusYears(18)
        return traveler
    }

    private fun  getTravelerWithFrequentFlyerMemberships(): Traveler {
        val traveler = getTraveler()
        traveler.addFrequentFlyerMembership(getTravelerFrequentFlyerMembership())
        return traveler
    }

    private fun getTravelerFrequentFlyerMembership() : TravelerFrequentFlyerMembership {
        val membership = TravelerFrequentFlyerMembership()
        membership.airlineCode = "VX"
        membership.membershipNumber = "123"
        membership.planCode = "123"
        return membership
    }
}
