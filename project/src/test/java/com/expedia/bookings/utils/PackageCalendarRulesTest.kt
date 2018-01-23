package com.expedia.bookings.utils

import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.util.PackageCalendarRules
import org.joda.time.LocalDate
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RuntimeEnvironment
import kotlin.test.assertEquals
import kotlin.test.assertFalse

@RunWith(RobolectricRunner::class)
class PackageCalendarRulesTest {
    private val context = RuntimeEnvironment.application

    private val testRules = PackageCalendarRules(context)

    @Test
    fun testStartDate() {
        assertEquals(LocalDate.now(), testRules.getFirstAvailableDate())
    }

    @Test
    fun testMaxSearchDuration() {
        assertEquals(26, testRules.getMaxSearchDurationDays(),
                "FAILURE: max date range should be 26, matches hotel/web")
    }

    @Test
    fun testMaxDateRange() {
        assertEquals(329, testRules.getMaxDateRange())
    }

    @Test
    fun testSameStartAndEndDateAllowed() {
        assertFalse(testRules.sameStartAndEndDateAllowed())
    }

    @Test
    fun testIsStartDateOnlyAllowed() {
        assertFalse(testRules.isStartDateOnlyAllowed())
    }
}
