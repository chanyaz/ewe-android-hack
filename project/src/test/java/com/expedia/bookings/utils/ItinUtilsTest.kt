package com.expedia.bookings.utils

import android.content.Context
import com.expedia.bookings.data.trips.TripComponent
import com.expedia.bookings.test.robolectric.RobolectricRunner
import org.joda.time.DateTime
import org.joda.time.DateTimeUtils
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RuntimeEnvironment
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@RunWith(RobolectricRunner::class)
class ItinUtilsTest {

    private val context: Context = RuntimeEnvironment.application

    @Test
    fun testShouldShowCheckin() {
        val now = DateTime.now()
        DateTimeUtils.setCurrentMillisFixed(now.millis)

        assertTrue(ItinUtils.shouldShowCheckInLink(context, TripComponent.Type.FLIGHT, now.plusHours(1),
                "checkInLink"))
        assertFalse(ItinUtils.shouldShowCheckInLink(context, TripComponent.Type.FLIGHT, now.plusMinutes(59),
                "checkInLink"))
        assertFalse(ItinUtils.shouldShowCheckInLink(context, TripComponent.Type.FLIGHT, now.minusHours(1),
                "checkInLink"))
        assertTrue(ItinUtils.shouldShowCheckInLink(context, TripComponent.Type.FLIGHT, now.plusHours(23),
                "checkInLink"))
        assertFalse(ItinUtils.shouldShowCheckInLink(context, TripComponent.Type.FLIGHT, now.plusHours(25),
                "checkInLink"))
        assertFalse(ItinUtils.shouldShowCheckInLink(context, TripComponent.Type.HOTEL, now.plusHours(23),
                "checkInLink"))
        assertFalse(ItinUtils.shouldShowCheckInLink(context, TripComponent.Type.HOTEL, now.plusHours(23),
                "checkInLink"))
        assertFalse(ItinUtils.shouldShowCheckInLink(context, TripComponent.Type.FLIGHT, now.plusHours(23),
                ""))
        assertFalse(ItinUtils.shouldShowCheckInLink(context, TripComponent.Type.FLIGHT, now.plusHours(23),
                null))

        DateTimeUtils.setCurrentMillisSystem()
    }
}
