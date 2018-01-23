package com.expedia.bookings.utils

import com.expedia.bookings.test.MultiBrand
import com.expedia.bookings.test.RunForBrands
import com.expedia.bookings.test.robolectric.RobolectricRunner
import junit.framework.Assert.assertEquals
import org.joda.time.DateTime
import org.joda.time.LocalDate
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(RobolectricRunner::class)
@RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
class LocaleBasedDateFormatUtilsTests {

    val localDateExpected = LocalDate()
            .withMonthOfYear(6)
            .withYear(2020)
            .withDayOfMonth(25)

    val dateTimeExpected = DateTime()
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
}
