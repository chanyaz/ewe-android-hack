package com.expedia.bookings.itin.vm

import android.app.Activity
import com.expedia.bookings.itin.common.ItinTimeDurationViewModel
import com.expedia.bookings.itin.flight.details.FlightItinLayoverViewModel
import com.expedia.bookings.test.robolectric.RobolectricRunner
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import kotlin.test.assertEquals

@RunWith(RobolectricRunner::class)
class ItinTimeDurationViewModelTest {
    private lateinit var activity: Activity
    private lateinit var sut: ItinTimeDurationViewModel

    @Before
    fun setup() {
        activity = Robolectric.buildActivity(Activity::class.java).create().get()
        sut = FlightItinLayoverViewModel(activity) //using FlightItinLayoverViewModel since ItinTimeDurationViewModel is abstract
    }

    @Test
    fun testGetLayoverDurationMinutes() {
        var durationInMins = sut.getDurationMinutesFromISO("PT7H4M")
        assertEquals(424, durationInMins)

        durationInMins = sut.getDurationMinutesFromISO("PT1H16M")
        assertEquals(76, durationInMins)
    }

    @Test
    fun testGetFormattedLayoverDuration() {
        var durationMins = sut.getFormattedDuration(424)
        assertEquals("7h 4m", durationMins)

        durationMins = sut.getFormattedDuration(76)
        assertEquals("1h 16m", durationMins)

        durationMins = sut.getFormattedDuration(10000)
        assertEquals("6d 22h 40m", durationMins)

        durationMins = sut.getFormattedDuration(36)
        assertEquals("36m", durationMins)

        durationMins = sut.getFormattedDuration(0)
        assertEquals("", durationMins)

        durationMins = sut.getFormattedDuration(-5)
        assertEquals("", durationMins)
    }
}
