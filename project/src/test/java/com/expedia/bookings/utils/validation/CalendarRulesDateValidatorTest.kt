package com.expedia.bookings.utils.validation

import com.expedia.bookings.shared.CalendarRules
import org.joda.time.LocalDate
import org.junit.Test
import kotlin.test.assertEquals

class CalendarRulesDateValidatorTest {

    private val todayMinusOne = LocalDate.now().plusDays(-1)
    private val today = LocalDate.now()
    private val todayPlusOne = LocalDate.now().plusDays(1)
    private val todayPlusTwo = LocalDate.now().plusDays(2)
    private val todayPlusThree = LocalDate.now().plusDays(3)
    private val todayPlusFour = LocalDate.now().plusDays(4)
    private val todayPlusFive = LocalDate.now().plusDays(5)
    private val todayPlusSix = LocalDate.now().plusDays(6)

    private val testDates = listOf(Pair(null, null), //0 both null
            Pair(null, todayPlusThree), //1 start null
            Pair(todayPlusThree, null), //2 end null
            Pair(todayMinusOne, today), //3 before first available date
            Pair(today, todayPlusOne), //4 end at first available date
            Pair(todayPlusOne, todayPlusTwo), //5 start at first available date
            Pair(todayPlusThree, todayPlusFour), //6 end at last available date
            Pair(todayPlusFour, todayPlusFive), //7 start at last available date
            Pair(todayPlusFive, todayPlusSix), //8 after last available date
            Pair(todayPlusOne, todayPlusThree), //9 at max duration
            Pair(todayPlusOne, todayPlusFour), //10 beyond max duration
            Pair(todayPlusThree, todayPlusTwo), //11 start after end
            Pair(today, today), //12 same date before first available date
            Pair(todayPlusOne, todayPlusOne), //13 same date at first available date
            Pair(todayPlusFour, todayPlusFour), //14 same date at last available date
            Pair(todayPlusFive, todayPlusFive), //15 same date after first available date
            Pair(today, null), //16 before first available date end null
            Pair(todayPlusOne, null), //17 at first available date end null
            Pair(todayPlusFour, null), //18 at last available date end null
            Pair(todayPlusFive, null) //19 after last available date end null
    )

    @Test
    fun testValidateStartEndDate() {
        val rules = TestCalendarRules()
        val validator = CalendarRulesDateValidator(rules, false)
        val expectedResults = listOf(
                false, false, false, false, false,
                true, true, false, false, true,
                false, false, false, false, false,
                false, false, false, false, false)

        assertValidateStartEndDate(validator, expectedResults)
    }

    @Test
    fun testValidateStartEndDateZeroMaxDateRange() {
        val rules = TestCalendarRules(maxDateRange = 0)
        val validator = CalendarRulesDateValidator(rules, false)
        val expectedResults = listOf(
                false, false, false, false, false,
                false, false, false, false, false,
                false, false, false, false, false,
                false, false, false, false, false)

        assertValidateStartEndDate(validator, expectedResults)
    }

    @Test
    fun testValidateStartEndDateZeroMaxDateRangeSameDateAllow() {
        val rules = TestCalendarRules(maxDateRange = 0, sameStartAndEndDateAllowed = true)
        val validator = CalendarRulesDateValidator(rules, false)
        val expectedResults = listOf(
                false, false, false, false, false,
                false, false, false, false, false,
                false, false, false, true, false,
                false, false, false, false, false)

        assertValidateStartEndDate(validator, expectedResults)
    }

    @Test
    fun testValidateStartEndDateZeroMaxDateRangeStartDateOnlyAllowed() {

        val rules = TestCalendarRules(maxDateRange = 0, isStartDateOnlyAllowed = true)
        val validator = CalendarRulesDateValidator(rules, false)
        val expectedResults = listOf(
                false, false, false, false, false,
                false, false, false, false, false,
                false, false, false, false, false,
                false, false, true, false, false)

        assertValidateStartEndDate(validator, expectedResults)
    }

    @Test
    fun testValidateStartEndDateOneMaxDateRange() {
        val rules = TestCalendarRules(maxDateRange = 1)
        val validator = CalendarRulesDateValidator(rules, false)
        val expectedResults = listOf(
                false, false, false, false, false,
                true, false, false, false, false,
                false, false, false, false, false,
                false, false, false, false, false)

        assertValidateStartEndDate(validator, expectedResults)
    }

    @Test
    fun testValidateStartEndDateZeroMaxDuration() {
        val rules = TestCalendarRules(maxSearchDurationDays = 0)
        val validator = CalendarRulesDateValidator(rules, false)
        val expectedResults = listOf(
                false, false, false, false, false,
                false, false, false, false, false,
                false, false, false, false, false,
                false, false, false, false, false)

        assertValidateStartEndDate(validator, expectedResults)
    }

    @Test
    fun testValidateStartEndDateZeroMaxDurationSameDateAllow() {
        val rules = TestCalendarRules(maxSearchDurationDays = 0, sameStartAndEndDateAllowed = true)
        val validator = CalendarRulesDateValidator(rules, false)
        val expectedResults = listOf(
                false, false, false, false, false,
                false, false, false, false, false,
                false, false, false, true, true,
                false, false, false, false, false)

        assertValidateStartEndDate(validator, expectedResults)
    }

    @Test
    fun testValidateStartEndDateZeroMaxDurationStartDateOnlyAllowed() {

        val rules = TestCalendarRules(maxSearchDurationDays = 0, isStartDateOnlyAllowed = true)
        val validator = CalendarRulesDateValidator(rules, false)
        val expectedResults = listOf(
                false, false, true, false, false,
                false, false, false, false, false,
                false, false, false, false, false,
                false, false, true, true, false)

        assertValidateStartEndDate(validator, expectedResults)
    }

    @Test
    fun testValidateStartEndDateOneMaxDurationRange() {
        val rules = TestCalendarRules(maxSearchDurationDays = 1)
        val validator = CalendarRulesDateValidator(rules, false)
        val expectedResults = listOf(
                false, false, false, false, false,
                true, true, false, false, false,
                false, false, false, false, false,
                false, false, false, false, false)

        assertValidateStartEndDate(validator, expectedResults)
    }

    @Test
    fun testValidateStartEndDateOneMaxDateRangeOneMaxDurationRange() {
        val rules = TestCalendarRules(maxDateRange = 1, maxSearchDurationDays = 1)
        val validator = CalendarRulesDateValidator(rules, false)
        val expectedResults = listOf(
                false, false, false, false, false,
                true, false, false, false, false,
                false, false, false, false, false,
                false, false, false, false, false)

        assertValidateStartEndDate(validator, expectedResults)
    }

    @Test
    fun testValidateStartEndDateFirstAvailableYesterday() {
        val rules = TestCalendarRules(firstAvailableDate = todayMinusOne)
        val validator = CalendarRulesDateValidator(rules, false)
        val expectedResults = listOf(
                false, false, false, true, true,
                true, false, false, false, false,
                false, false, false, false, false,
                false, false, false, false, false)

        assertValidateStartEndDate(validator, expectedResults)
    }

    @Test
    fun testValidateStartEndDateFirstAvailableSixDaysAhead() {
        val rules = TestCalendarRules(firstAvailableDate = todayPlusSix)
        val validator = CalendarRulesDateValidator(rules, false)
        val expectedResults = listOf(
                false, false, false, false, false,
                false, false, false, false, false,
                false, false, false, false, false,
                false, false, false, false, false)

        assertValidateStartEndDate(validator, expectedResults)
    }

    @Test
    fun testValidateStartEndDateSameStartAndEndDateAllowed() {
        val rules = TestCalendarRules(sameStartAndEndDateAllowed = true)
        val validator = CalendarRulesDateValidator(rules, false)
        val expectedResults = listOf(
                false, false, false, false, false,
                true, true, false, false, true,
                false, false, false, true, true,
                false, false, false, false, false)

        assertValidateStartEndDate(validator, expectedResults)
    }

    @Test
    fun testValidateStartEndDateIsStartDateOnlyAllowed() {
        val rules = TestCalendarRules(isStartDateOnlyAllowed = true)
        val validator = CalendarRulesDateValidator(rules, false)
        val expectedResults = listOf(
                false, false, true, false, false,
                true, true, false, false, true,
                false, false, false, false, false,
                false, false, true, true, false)

        assertValidateStartEndDate(validator, expectedResults)
    }

    @Test
    fun testValidateStartEndDateAllowEndDateEqualLastDatePlusOne() {
        val rules = TestCalendarRules()
        val validator = CalendarRulesDateValidator(rules, true)
        val expectedResults = listOf(
                false, false, false, false, false,
                true, true, true, false, true,
                false, false, false, false, false,
                false, false, false, false, false)

        assertValidateStartEndDate(validator, expectedResults)
    }

    private fun assertValidateStartEndDate(validator: CalendarRulesDateValidator, expectedResults: List<Boolean>) {
        assertEquals(expectedResults.size, testDates.size, "expectedResults, testDates different size")
        for (i in 0 until expectedResults.size) {
            assertEquals(expectedResults[i], validator.validateStartEndDate(testDates[i].first, testDates[i].second), "Failed at number $i")
        }
    }

    private inner class TestCalendarRules(private val maxDateRange: Int = 3,
                                          private val firstAvailableDate: LocalDate = todayPlusOne,
                                          private val maxSearchDurationDays: Int = 2,
                                          private val sameStartAndEndDateAllowed: Boolean = false,
                                          private val isStartDateOnlyAllowed: Boolean = false) : CalendarRules {

        override fun getMaxDateRange(): Int {
            return maxDateRange
        }

        override fun getFirstAvailableDate(): LocalDate {
            return firstAvailableDate
        }

        override fun getMaxSearchDurationDays(): Int {
            return maxSearchDurationDays
        }

        override fun sameStartAndEndDateAllowed(): Boolean {
            return sameStartAndEndDateAllowed
        }

        override fun isStartDateOnlyAllowed(): Boolean {
            return isStartDateOnlyAllowed
        }
    }
}
