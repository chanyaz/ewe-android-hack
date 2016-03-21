package com.expedia.bookings.test.robolectric

import org.joda.time.DateTime
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RuntimeEnvironment

import android.content.Context

import com.expedia.bookings.data.trips.TripComponent
import com.expedia.bookings.utils.ItinUtils

import junit.framework.Assert.assertFalse
import junit.framework.Assert.assertTrue

@RunWith(RobolectricRunner::class)
class FlightItinCheckinTest {

    private val context: Context
        get() = RuntimeEnvironment.application

    @Test
    fun testShouldShowCheckin() {
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
