package com.expedia.bookings.rails.util

import com.expedia.bookings.rail.util.RailCalendarRules
import com.expedia.bookings.test.robolectric.RobolectricRunner
import org.joda.time.LocalDate
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RuntimeEnvironment
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@RunWith(RobolectricRunner::class)
class RailCalendarRulesTest {
    private val context = RuntimeEnvironment.application

    private val testOneWay = RailCalendarRules(context, roundTrip = false)
    private val testRoundTrip = RailCalendarRules(context, roundTrip = true)

    @Test
    fun testStartDate() {
        assertEquals(LocalDate.now().plusDays(1), testOneWay.getFirstAvailableDate(),
                "FAILURE: Rail First Day must be at least one day out")
        assertEquals(LocalDate.now().plusDays(1), testRoundTrip.getFirstAvailableDate(),
                "FAILURE: Rail First Day must be at least one day out")
    }

    @Test
    fun testMaxSearchDuration() {
        assertEquals(0, testOneWay.getMaxSearchDurationDays(), "FAILURE: end date selection should be disabled for one way")
        assertEquals(30, testRoundTrip.getMaxSearchDurationDays(), "FAILURE: end date selection should be maxed at 30 for roundtrip, matches web")
    }

    @Test
    fun testMaxDateRange() {
        assertEquals(84, testOneWay.getMaxDateRange(), "FAILURE: Max search range for rail should be 84")
        assertEquals(84, testRoundTrip.getMaxDateRange(), "FAILURE: Max search range for rail should be 84")
    }

    @Test
    fun testSameStartAndEndDateAllowed() {
        assertFalse(testOneWay.sameStartAndEndDateAllowed())
        assertFalse(testRoundTrip.sameStartAndEndDateAllowed())
    }

    @Test
    fun testIsStartDateOnlyAllowed() {
        assertTrue(testOneWay.isStartDateOnlyAllowed())
        assertFalse(testRoundTrip.isStartDateOnlyAllowed())
    }
}
