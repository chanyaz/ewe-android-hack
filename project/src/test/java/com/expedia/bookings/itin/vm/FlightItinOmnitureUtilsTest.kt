package com.expedia.bookings.itin.vm

import android.app.Activity
import android.content.Context
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.widget.itin.support.ItinCardDataFlightBuilder
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RuntimeEnvironment
import kotlin.test.assertEquals

@RunWith(RobolectricRunner::class)
class FlightItinOmnitureUtilsTest {
    private lateinit var activity: Activity
    private lateinit var sut: FlightItinOmnitureUtils
    private lateinit var context: Context

    @Before
    fun setup() {
        activity = Robolectric.buildActivity(Activity::class.java).create().get()
        sut = FlightItinOmnitureUtils
        context = RuntimeEnvironment.application
    }

    @Test
    fun getTripDuration() {
        var testItinCardData = ItinCardDataFlightBuilder().build()
        val duration = sut.calculateTripDuration(testItinCardData)
        assertEquals("8", duration )
    }

    @Test
    fun getDaysUntilTrip() {
        val testItinCardData = ItinCardDataFlightBuilder().build()
        val daysUntil = sut.calculateDaysUntilTripStart(testItinCardData)
        assertEquals("30", daysUntil)
    }

    @Test
    fun buildOrderNumberAndItinNumberString() {
        val testItinCardData = ItinCardDataFlightBuilder().build()
        val orderNumberAndItinNumber = sut.buildOrderNumberAndItinNumberString(testItinCardData)
        assertEquals("8063550177859|7238007847306", orderNumberAndItinNumber)
    }

    @Test
    fun buildProductString() {
        val testItinCardData = ItinCardDataFlightBuilder().build(false, true)
        val productString = sut.buildFlightProductString(testItinCardData)
        assertEquals(";Flight:UA:RT;;", productString)
    }
}
