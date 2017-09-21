package com.expedia.bookings.hotel.util

import com.expedia.bookings.R
import com.expedia.bookings.shared.util.CalendarDateFormatter
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.utils.LocaleBasedDateFormatUtils
import org.joda.time.LocalDate
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RuntimeEnvironment
import kotlin.test.assertEquals

@RunWith(RobolectricRunner::class)
class HotelCalendarInstructionsTest {
    private val context = RuntimeEnvironment.application
    private val testInstructions = HotelCalendarInstructions(RuntimeEnvironment.application)

    private val testStartDate = LocalDate.now().plusDays(3)
    private val testEndDate = LocalDate.now().plusDays(4)

    @Test
    fun testDateInstructionText_noDates() {
        val expectedText = context.getString(R.string.select_checkin_date)
        assertEquals(expectedText, testInstructions.getDateInstructionText(null, null))
    }

    @Test
    fun testDateInstructionText_noEndDate() {
        val expectedText = context.getString(R.string.select_checkout_date_TEMPLATE,
                LocaleBasedDateFormatUtils.localDateToMMMd(testStartDate))

        assertEquals(expectedText, testInstructions.getDateInstructionText(testStartDate, null))
    }

    @Test
    fun testDateInstructionText_bothDates() {
        val expectedDateString = CalendarDateFormatter.formatStartDashEnd(context, testStartDate, testEndDate)
        val expectedNights = "(1 night)"

        assertEquals("$expectedDateString $expectedNights",
                testInstructions.getDateInstructionText(testStartDate, testEndDate))
    }

    @Test
    fun testToolTipTitle_noDates() {
        val expectedText = context.getString(R.string.select_dates_proper_case)
        assertEquals(expectedText, testInstructions.getToolTipTitle(null, null))
    }

    @Test
    fun testToolTipTitle_noEndDate() {
        val expectedText = LocaleBasedDateFormatUtils.localDateToMMMd(testStartDate)

        assertEquals(expectedText, testInstructions.getToolTipTitle(testStartDate, null))
    }

    @Test
    fun testToolTipTitle_bothDates() {
        val expectedDateString = CalendarDateFormatter.formatStartDashEnd(context, testStartDate, testEndDate)

        assertEquals(expectedDateString, testInstructions.getToolTipTitle(testStartDate, testEndDate))
    }

    @Test
    fun testToolTipInstruction_noEndDate() {
        val expectedText = context.getString(R.string.hotel_calendar_tooltip_bottom)

        assertEquals(expectedText, testInstructions.getToolTipInstructions(null))
    }

    @Test
    fun testToolTipInstruction_withEndDate() {
        val expectedText = context.getString(R.string.calendar_drag_to_modify)

        assertEquals(expectedText, testInstructions.getToolTipInstructions(testEndDate))
    }

    @Test
    fun testToolTipContDesc_noDates() {
        val expectedText = context.getString(R.string.select_dates_proper_case)

        assertEquals(expectedText, testInstructions.getToolTipContDesc(null, null))
    }

    @Test
    fun testToolTipContDesc_noEndDate() {
        val startDateText = LocaleBasedDateFormatUtils.localDateToMMMd(testStartDate!!)

        assertEquals("$startDateText. Next: Select check out date",
                testInstructions.getToolTipContDesc(testStartDate, null))
    }

    @Test
    fun testToolTipContDesc_bothDates() {
        val dateText = CalendarDateFormatter.formatStartToEnd(context, testStartDate, testEndDate)

        assertEquals("$dateText. Select dates again to modify",
                testInstructions.getToolTipContDesc(testStartDate, testEndDate))
    }

    @Test
    fun testNoEndDateText() {
        val expectedText = context.getString(R.string.select_checkout_date_TEMPLATE,
                LocaleBasedDateFormatUtils.localDateToMMMd(testStartDate))

        assertEquals(expectedText, testInstructions.getNoEndDateText(testStartDate, false))
    }

    @Test
    fun testNoEndDateTextContDesc() {
        val text = context.getString(R.string.select_checkout_date_TEMPLATE,
                LocaleBasedDateFormatUtils.localDateToMMMd(testStartDate))

        val expectedText = CalendarDateFormatter.getDateAccessibilityText(context, text, "")
        assertEquals(expectedText, testInstructions.getNoEndDateText(testStartDate, true))
    }
}