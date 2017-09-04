package com.expedia.bookings.unit

import com.expedia.bookings.data.flights.FlightCreateTripParams
import com.expedia.bookings.utils.Constants
import junit.framework.Assert.assertEquals
import org.junit.Assert.fail
import org.junit.Test
import java.math.BigDecimal
import kotlin.test.assertTrue

class FlightCreateTripParamsTest {

    @Test
    fun testFlightCreateTripParamsBuilder() {
        val params = FlightCreateTripParams.Builder().productKey("happy")
                .setFlexEnabled(true).enableSubPubFeature().
                fareFamilyCode("Eco").fareFamilyTotalPrice(BigDecimal(100))
                .build()

        assertEquals("happy", params.productKey)
        assertEquals(Constants.FEATURE_SUBPUB, params.featureOverride)
        assertEquals("Eco", params.fareFamilyCode)
        assertEquals(BigDecimal(100), params.fareFamilyTotalPrice)
        assertTrue(params.flexEnabled)
    }

    @Test(expected = IllegalArgumentException::class)
    fun testIncompleteFlightCreateTripParams() {
        FlightCreateTripParams.Builder().productKey(null).build()
    }
}
