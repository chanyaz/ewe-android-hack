package com.expedia.bookings.utils

import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.util.CarCalendarRules
import org.joda.time.LocalDate
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RuntimeEnvironment
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@RunWith(RobolectricRunner::class)
class CarCalendarRulesTest {
    private val context = RuntimeEnvironment.application

    private val testRules = CarCalendarRules(context)

    @Test
    fun testStartDate() {
        assertEquals(LocalDate.now(), testRules.getFirstAvailableDate())
    }

    @Test
    fun testMaxSearchDuration() {
        assertEquals(330, testRules.getMaxSearchDurationDays())
    }

    @Test
    fun testMaxDateRange() {
        assertEquals(329, testRules.getMaxDateRange())
    }

    @Test
    fun testSameStartAndEndDateAllowed() {
        assertTrue(testRules.sameStartAndEndDateAllowed())
    }

    @Test
    fun testIsStartDateOnlyAllowed() {
        assertFalse(testRules.isStartDateOnlyAllowed())
    }
}