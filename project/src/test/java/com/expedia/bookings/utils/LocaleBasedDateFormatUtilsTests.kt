package com.expedia.bookings.utils

import com.expedia.bookings.test.MultiBrand
import com.expedia.bookings.test.RunForBrands
import com.expedia.bookings.test.robolectric.RobolectricRunner
import org.joda.time.DateTime
import org.joda.time.LocalDate
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.test.assertEquals

@RunWith(RobolectricRunner::class)
@RunForBrands(brands = [(MultiBrand.EXPEDIA)])
class LocaleBasedDateFormatUtilsTests {

    private val localDateExpected = LocalDate()
            .withMonthOfYear(6)
            .withYear(2020)
            .withDayOfMonth(25)

    private val dateTimeExpected = DateTime()
            .withDate(localDateExpected)
            .withHourOfDay(10)
            .withMinuteOfHour(40)

    @Test
    fun testLocalDateToEEEMMMd() {
        val actual = LocaleBasedDateFormatUtils.localDateToEEEMMMd(localDateExpected)
        assertEquals("Thu, Jun 25", actual)
    }

    @Test
    fun testLocalDateToMMyyyy() {
        val actual = LocaleBasedDateFormatUtils.localDateToMMyyyy(localDateExpected)
        assertEquals("06 / 2020", actual)
    }

    @Test
    fun testLocalDateToMMMd() {
        val actual = LocaleBasedDateFormatUtils.localDateToMMMd(localDateExpected)
        assertEquals("Jun 25", actual)
    }

    @Test
    fun testDateTimeToMMMd() {
        val actual = LocaleBasedDateFormatUtils.dateTimeToMMMd(dateTimeExpected)
        assertEquals("Jun 25", actual)
    }

    @Test
    fun testLocalDateToMMMMd() {
        val actual = LocaleBasedDateFormatUtils.localDateToMMMMd(localDateExpected)
        assertEquals("June 25", actual)
    }

    @Test
    fun testDateTimeToMMMdhmma() {
        val actual = LocaleBasedDateFormatUtils.dateTimeToMMMdhmma(dateTimeExpected)
        assertEquals("Jun 25, 10:40 AM", actual)
    }

    @Test
    fun testDateTimeToEEEMMMdhmma() {
        val actual = LocaleBasedDateFormatUtils.dateTimeToEEEMMMdhmma(dateTimeExpected)
        assertEquals("Thu, Jun 25 - 10:40 AM", actual)
    }

    @Test
    fun testDateTimeToEEEMMMd() {
        val actual = LocaleBasedDateFormatUtils.dateTimeToEEEMMMd(dateTimeExpected)
        assertEquals("Thu, Jun 25", actual)
    }

    @Test
    fun testDateTimeToEEEEMMMd() {
        val actual = LocaleBasedDateFormatUtils.dateTimeToEEEEMMMd(dateTimeExpected)
        assertEquals("Thursday, Jun 25", actual)
    }

    @Test
    fun testDateTimeToEEEMMMddyyyy() {
        val actual = LocaleBasedDateFormatUtils.dateTimeToEEEMMMddyyyy(dateTimeExpected)
        assertEquals("Thu Jun 25, 2020", actual)
    }

    @Test
    fun testLocalDateToEEEMMMddyyyy() {
        val actual = LocaleBasedDateFormatUtils.dateTimeToEEEMMMddyyyy(localDateExpected)
        assertEquals("Thu Jun 25, 2020", actual)
    }

    @Test
    fun testDateTimeToEEEMMMdd() {
        val actual = LocaleBasedDateFormatUtils.dateTimeToEEEMMMdd(dateTimeExpected)
        assertEquals(actual, "Thu Jun 25")
    }

    @Test
    fun testStringToEEEMMMddyyyy() {
        val actual = LocaleBasedDateFormatUtils.yyyyMMddStringToEEEMMMddyyyy("2020-06-25")
        assertEquals("Thu Jun 25, 2020", actual)
    }

    @Test
    fun testFormatBirthDate() {
        val actual = LocaleBasedDateFormatUtils.formatBirthDate(2020, 6, 25)
        assertEquals(actual, "Jun 25, 2020")
    }
}
