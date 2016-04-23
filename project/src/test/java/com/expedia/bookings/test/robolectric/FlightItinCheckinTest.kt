package com.expedia.bookings.test.robolectric

import android.content.Context
import com.expedia.bookings.data.trips.TripComponent
import com.expedia.bookings.utils.ItinUtils
import junit.framework.Assert.assertFalse
import junit.framework.Assert.assertTrue
import org.joda.time.DateTime
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RuntimeEnvironment

@RunWith(RobolectricRunner::class)
class FlightItinCheckinTest {

    private val context: Context
        get() = RuntimeEnvironment.application

    @Test
    fun testShouldShowCheckin() {
        assertTrue(ItinUtils.shouldShowCheckInLink(context, TripComponent.Type.FLIGHT, DateTime.now().plusHours(1),
                "checkInLink"))
        assertFalse(ItinUtils.shouldShowCheckInLink(context, TripComponent.Type.FLIGHT, DateTime.now().plusMinutes(59),
                "checkInLink"))
        assertFalse(ItinUtils.shouldShowCheckInLink(context, TripComponent.Type.FLIGHT, DateTime.now().minusHours(1),
                "checkInLink"))
        assertTrue(ItinUtils.shouldShowCheckInLink(context, TripComponent.Type.FLIGHT, DateTime.now().plusHours(23),
                "checkInLink"))
        assertFalse(ItinUtils.shouldShowCheckInLink(context, TripComponent.Type.FLIGHT, DateTime.now().plusHours(25),
                "checkInLink"))
        assertFalse(ItinUtils.shouldShowCheckInLink(context, TripComponent.Type.HOTEL, DateTime.now().plusHours(23),
                "checkInLink"))
        assertFalse(ItinUtils.shouldShowCheckInLink(context, TripComponent.Type.HOTEL, DateTime.now().plusHours(23),
                "checkInLink"))
        assertFalse(ItinUtils.shouldShowCheckInLink(context, TripComponent.Type.FLIGHT, DateTime.now().plusHours(23),
                ""))
        assertFalse(ItinUtils.shouldShowCheckInLink(context, TripComponent.Type.FLIGHT, DateTime.now().plusHours(23),
                null))
    }
}
