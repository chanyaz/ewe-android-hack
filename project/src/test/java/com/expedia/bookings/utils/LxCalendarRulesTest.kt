package com.expedia.bookings.utils

import com.expedia.bookings.test.robolectric.RobolectricRunner
import org.joda.time.LocalDate
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RuntimeEnvironment
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@RunWith(RobolectricRunner::class)
class LxCalendarRulesTest {
    private val context = RuntimeEnvironment.application

    private val testRules = LxCalendarRules(context)

    @Test
    fun testStartDate() {
        assertEquals(LocalDate.now(), testRules.getFirstAvailableDate())
    }

    @Test
    fun testMaxSearchDuration() {
        assertEquals(0, testRules.getMaxSearchDurationDays(), "FAILURE: end date selection should be disabled for LX")
    }

    @Test
    fun testMaxDateRange() {
        assertEquals(314, testRules.getMaxDateRange(), "FAILURE: Max search range for lx should be 314")
    }

    @Test
    fun testSameStartAndEndDateAllowed() {
        assertFalse(testRules.sameStartAndEndDateAllowed())
    }

    @Test
    fun testIsStartDateOnlyAllowed() {
        assertTrue(testRules.isStartDateOnlyAllowed())
    }
}