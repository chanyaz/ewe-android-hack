package com.expedia.bookings.unit

import com.expedia.bookings.data.flights.FlightCreateTripParams
import com.expedia.bookings.utils.Constants
import org.junit.Assert.assertEquals
import org.junit.Test
import java.math.BigDecimal
import kotlin.test.assertTrue

class FlightCreateTripParamsTest {

    @Test
    fun testFlightCreateTripParamsBuilder() {
        val params = getCreateTripParamsBuilder(Constants.FEATURE_SUBPUB)
        assertEquals("happy", params.productKey)
        assertEquals("SubPub", params.featureOverride)
        assertEquals("Eco", params.fareFamilyCode)
        assertEquals(BigDecimal(100), params.fareFamilyTotalPrice)
        assertTrue(params.flexEnabled)
    }

    @Test
    fun testEvolableCreateTripParamsBuilder() {
        val params = getCreateTripParamsBuilder(Constants.FEATURE_EVOLABLE)
        assertEquals("GetEvolable", params.featureOverride)
    }

    @Test(expected = IllegalArgumentException::class)
    fun testIncompleteFlightCreateTripParams() {
        FlightCreateTripParams.Builder().productKey(null).build()
    }

    @Test
    fun testNewCreateTripQueryParams() {
        val params = getCreateTripParamsBuilderWithTravellerInformation(2, listOf(10, 12), false)
        val queryParams = params.queryParamsForNewCreateTrip()
        assertEquals(2, queryParams["numberOfAdultTravelers"])
        assertEquals(false, queryParams["infantSeatingInLap"])
        assertEquals(listOf(10, 12), params.childTravelerAge)
    }

    @Test
    fun testOldCreateTripQueryParams() {
        val params = getCreateTripParamsBuilder()
        val expectedParams = HashMap<String, String>()
        expectedParams["productKey"] = "happy"
        assertEquals(expectedParams, params.queryParamsForOldCreateTrip())
    }

    private fun getCreateTripParamsBuilder(vararg overrides: String): FlightCreateTripParams {
        val paramsBuilder = FlightCreateTripParams.Builder()
        if (overrides.isNotEmpty()) {
            for (flag in overrides) {
                paramsBuilder.setFeatureOverride(flag)
            }
        }

        return paramsBuilder.productKey("happy")
                .setFlexEnabled(true)
                .fareFamilyCode("Eco")
                .fareFamilyTotalPrice(BigDecimal(100))
                .build()
    }

    private fun getCreateTripParamsBuilderWithTravellerInformation(numberOfAdultTraveller: Int, childTravelerAge: List<Int>, infantSeatingInLap: Boolean): FlightCreateTripParams {
        val paramsBuilder = FlightCreateTripParams.Builder()
        return paramsBuilder.productKey("happy")
                .setFlexEnabled(true)
                .fareFamilyCode("Eco")
                .fareFamilyTotalPrice(BigDecimal(100))
                .setNumberOfAdultTravelers(numberOfAdultTraveller)
                .setChildTravelerAge(childTravelerAge)
                .setInfantSeatingInLap(infantSeatingInLap)
                .build()
    }
}
