package com.expedia.bookings.test.robolectric

import android.content.Context
import com.expedia.bookings.data.trips.TripComponent
import com.expedia.bookings.utils.ItinUtils
import org.joda.time.DateTime
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RuntimeEnvironment
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@RunWith(RobolectricRunner::class)
class FlightItinCheckinTest {

    private val context: Context
        get() = RuntimeEnvironment.application

    @Test
    fun testShouldShowCheckin() {
        val dateTime = DateTime.now();
        assertTrue(ItinUtils.shouldShowCheckInLink(context, TripComponent.Type.FLIGHT, dateTime.plusHours(1), "checkInLink", dateTime))
        assertFalse(ItinUtils.shouldShowCheckInLink(context, TripComponent.Type.FLIGHT, dateTime.plusMinutes(59), "checkInLink", dateTime))
        assertFalse(ItinUtils.shouldShowCheckInLink(context, TripComponent.Type.FLIGHT, dateTime.minusHours(1), "checkInLink", dateTime))
        assertTrue(ItinUtils.shouldShowCheckInLink(context, TripComponent.Type.FLIGHT, dateTime.plusHours(23), "checkInLink", dateTime))
        assertFalse(ItinUtils.shouldShowCheckInLink(context, TripComponent.Type.FLIGHT, dateTime.plusHours(25), "checkInLink", dateTime))
        assertFalse(ItinUtils.shouldShowCheckInLink(context, TripComponent.Type.HOTEL, dateTime.plusHours(23), "checkInLink", dateTime))
        assertFalse(ItinUtils.shouldShowCheckInLink(context, TripComponent.Type.HOTEL, dateTime.plusHours(23), "checkInLink", dateTime))
        assertFalse(ItinUtils.shouldShowCheckInLink(context, TripComponent.Type.FLIGHT, dateTime.plusHours(23), "", dateTime))
        assertFalse(ItinUtils.shouldShowCheckInLink(context, TripComponent.Type.FLIGHT, dateTime.plusHours(23), null, dateTime))
    }
}
