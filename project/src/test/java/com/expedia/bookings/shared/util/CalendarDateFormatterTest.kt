package com.expedia.bookings.shared.util

import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.utils.LocaleBasedDateFormatUtils
import org.joda.time.LocalDate
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RuntimeEnvironment
import kotlin.test.assertEquals

@RunWith(RobolectricRunner::class)
class CalendarDateFormatterTest {
    private val context = RuntimeEnvironment.application

    private val startDate = LocalDate.now()
    private val endDate = LocalDate.now().plusDays(3)

    val expectedStart = LocaleBasedDateFormatUtils.localDateToMMMd(startDate)
    val expectedEnd = LocaleBasedDateFormatUtils.localDateToMMMd(endDate)

    @Test
    fun testStartToEnd() {
        assertEquals("$expectedStart to $expectedEnd", CalendarDateFormatter.formatStartToEnd(context, startDate, endDate))
    }

    @Test
    fun testStartDashEnd() {
        assertEquals("$expectedStart - $expectedEnd", CalendarDateFormatter.formatStartDashEnd(context, startDate, endDate))
    }

    @Test
    fun testDateAlly() {
        val expectedLabel = "label"
        val expectedDescription = "description"
        assertEquals("$expectedLabel Button. Opens dialog. $expectedDescription",
                CalendarDateFormatter.getDateAccessibilityText(context, expectedLabel, expectedDescription))
    }
}
