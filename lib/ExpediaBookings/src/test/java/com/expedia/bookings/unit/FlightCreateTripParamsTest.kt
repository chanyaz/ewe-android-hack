package com.expedia.bookings.unit

import com.expedia.bookings.data.flights.FlightCreateTripParams
import com.expedia.bookings.utils.Constants
import junit.framework.Assert.assertEquals
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
}
