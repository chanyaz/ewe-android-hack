package com.expedia.bookings.utils

import android.content.Context
import com.expedia.bookings.data.DeviceType
import com.expedia.bookings.server.EndPoint
import com.expedia.bookings.test.MultiBrand
import com.expedia.bookings.test.RunForBrands
import com.expedia.bookings.test.robolectric.RobolectricRunner
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import kotlin.test.assertEquals

@RunWith(RobolectricRunner::class)
class ServicesUtilTest {

    private val context: Context by lazy {
        RuntimeEnvironment.application
    }

    @Test
    fun testDistortCoordinates() {
        assertEquals(37.3, ServicesUtil.distortCoordinates(37.2994921))
        assertEquals(-122.5, ServicesUtil.distortCoordinates(-122.4995990))
        assertEquals(37.2, ServicesUtil.distortCoordinates(37.2134921))
        assertEquals(-122.4, ServicesUtil.distortCoordinates(-122.4445990))
    }

    @Test
    fun deviceTypeIsPhoneByDefault() {
        assertEquals(DeviceType.PHONE, ServicesUtil.getDeviceType(context))
    }

    @Test
    @Config(qualifiers = "sw720dp")
    fun deviceTypeIsTabletOnTablets() {
        assertEquals(DeviceType.TABLET, ServicesUtil.getDeviceType(context))
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun clientIsCorrectForExpedia() {
        assertEquals("expedia.app.android.phone", ServicesUtil.generateClient(context))
    }

    @Test
    fun testGetTravelPulseClientId() {
        assertEquals("androidHotel", ServicesUtil.getTravelPulseClientId(context))
    }

    @Test
    fun testGetTravelPulseClientTokenProduction() {
        // use MTA3ZTc3ZmYtZjZiMi00M2IxLWFjODEtYzY3ZjcwMmFlY2Iy once prod endpoint works
        assertEquals("YjNlMmM2YzYtYTJhNS00NDEwLWE1NjMtMTI4MmRmNTc2MTcy", ServicesUtil.getTravelPulseClientToken(context, EndPoint.PRODUCTION))
    }

    @Test
    fun testGetTravelPulseClientTokenNonProduction() {
        assertEquals("YjNlMmM2YzYtYTJhNS00NDEwLWE1NjMtMTI4MmRmNTc2MTcy", ServicesUtil.getTravelPulseClientToken(context, EndPoint.INTEGRATION))
    }
}
