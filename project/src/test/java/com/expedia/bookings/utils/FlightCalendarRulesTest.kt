package com.expedia.bookings.utils

import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.util.FlightCalendarRules
import org.joda.time.LocalDate
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RuntimeEnvironment
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@RunWith(RobolectricRunner::class)
class FlightCalendarRulesTest {
    private val context = RuntimeEnvironment.application

    private val testOneWay = FlightCalendarRules(context, roundTrip = false)
    private val testRoundTrip = FlightCalendarRules(context, roundTrip = true)

    @Test
    fun testStartDate() {
        assertEquals(LocalDate.now(), testOneWay.getFirstAvailableDate())
        assertEquals(LocalDate.now(), testRoundTrip.getFirstAvailableDate())
    }

    @Test
    fun testMaxSearchDuration() {
        assertEquals(0, testOneWay.getMaxSearchDurationDays(), "FAILURE: end date selection should be disabled for one way")
        assertEquals(330, testRoundTrip.getMaxSearchDurationDays())
    }

    @Test
    fun testMaxDateRange() {
        assertEquals(330, testOneWay.getMaxDateRange(), "FAILURE: Max search range for flights should be 330")
        assertEquals(330, testRoundTrip.getMaxDateRange(), "FAILURE: Max search range for flights should be 330")
    }

    @Test
    fun testSameStartAndEndDateAllowed() {
        assertTrue(testOneWay.sameStartAndEndDateAllowed())
        assertTrue(testRoundTrip.sameStartAndEndDateAllowed())
    }

    @Test
    fun testIsStartDateOnlyAllowed() {
        assertTrue(testOneWay.isStartDateOnlyAllowed())
        assertTrue(testRoundTrip.isStartDateOnlyAllowed())
    }
}