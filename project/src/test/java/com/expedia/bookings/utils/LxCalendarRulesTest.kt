package com.expedia.bookings.utils

import com.expedia.bookings.features.Features
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
    fun testInitialStartDate() {
        assertEquals(LocalDate.now(), testRules.getFirstAvailableDate())
    }

    @Test
    fun testMaxSearchDuration() {
        FeatureTestUtils.disableFeature(context, Features.all.lxMultipleDatesSearch)
        assertEquals(0, testRules.getMaxSearchDurationDays(), "FAILURE: end date selection should be disabled for LX")

        FeatureTestUtils.enableFeature(context, Features.all.lxMultipleDatesSearch)
        assertEquals(14, testRules.getMaxSearchDurationDays(), "FAILURE: maximum end date should be start plus 14 days")
    }

    @Test
    fun testMaxDateRange() {
        assertEquals(314, testRules.getMaxDateRange(), "FAILURE: Max search range for lx should be 314")
    }

    @Test
    fun testSameStartAndEndDateAllowed() {
        FeatureTestUtils.disableFeature(context, Features.all.lxMultipleDatesSearch)
        assertFalse(testRules.sameStartAndEndDateAllowed())

        FeatureTestUtils.enableFeature(context, Features.all.lxMultipleDatesSearch)
        assertTrue(testRules.sameStartAndEndDateAllowed())
    }

    @Test
    fun testIsStartDateOnlyAllowed() {
        FeatureTestUtils.disableFeature(context, Features.all.lxMultipleDatesSearch)
        assertTrue(testRules.isStartDateOnlyAllowed())

        FeatureTestUtils.enableFeature(context, Features.all.lxMultipleDatesSearch)
        assertFalse(testRules.isStartDateOnlyAllowed())
    }
}
