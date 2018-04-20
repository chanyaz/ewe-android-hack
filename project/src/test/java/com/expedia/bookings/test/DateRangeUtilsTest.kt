package com.expedia.bookings.test

import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RuntimeEnvironment

import android.content.Context

import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.test.robolectric.shadows.ShadowDateFormat
import com.expedia.bookings.utils.DateRangeUtils

import org.joda.time.DateTime
import org.joda.time.LocalDate
import org.robolectric.annotation.Config
import java.util.concurrent.TimeUnit
import kotlin.test.assertEquals

@RunWith(RobolectricRunner::class)
class DateRangeUtilsTest {

    private val context: Context
        get() = RuntimeEnvironment.application

    private val firstDate = LocalDate()
            .withMonthOfYear(6)
            .withYear(2020)
            .withDayOfMonth(5)

    private val secondDate = LocalDate()
            .withMonthOfYear(6)
            .withYear(2020)
            .withDayOfMonth(26)

    private val thirdDate = LocalDate()
            .withMonthOfYear(7)
            .withYear(2020)
            .withDayOfMonth(26)

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA, MultiBrand.ORBITZ, MultiBrand.CHEAPTICKETS, MultiBrand.TRAVELOCITY, MultiBrand.AIRASIAGO, MultiBrand.VOYAGES, MultiBrand.WOTIF, MultiBrand.LASTMINUTE, MultiBrand.EBOOKERS))
    fun testyyyyMMddHHToDayDateFormat() {
        val checkInDate = "2018-10-11"
        val checkOutDate = "2018-10-15"

        val obtained = DateRangeUtils.formatPackageDateRange(context, checkInDate, checkOutDate)
        assertEquals(obtained, "Thu Oct 11, 2018 - Mon Oct 15, 2018")
    }

    @Test
    @RunForBrands(brands = [MultiBrand.EXPEDIA]) // other brands use different (non U.S.) time formats
    fun testFormatDateRangeHotelConfirmation() {
        var actual = DateRangeUtils.formatDateRangeHotelConfirmation(context, firstDate, secondDate)
        assertEquals(actual, "Jun 5 – 26, 2020")

        actual = DateRangeUtils.formatDateRangeHotelConfirmation(context, firstDate, thirdDate)
        assertEquals(actual, "Jun 5 – Jul 26, 2020")
    }

    @Test
    @RunForBrands(brands = [MultiBrand.EXPEDIA]) // other brands use different (non U.S.) time formats
    fun testFormatRailDateTimeRange() {
        val firstMillis = (TimeUnit.HOURS.toMillis(1) + TimeUnit.MINUTES.toMillis(23)).toInt()
        val secondMillis = (TimeUnit.HOURS.toMillis(2) + TimeUnit.MINUTES.toMillis(34)).toInt()
        var actual = DateRangeUtils.formatRailDateTimeRange(context, firstDate, firstMillis, secondDate, secondMillis, true)
        assertEquals(actual, "Jun 5, 1:23 AM – Jun 26, 2:34 AM")

        actual = DateRangeUtils.formatRailDateTimeRange(context, firstDate, firstMillis, null, 0, false)
        assertEquals(actual, "Jun 5, 1:23 AM")
    }

    @Test
    @RunForBrands(brands = [MultiBrand.EXPEDIA]) // other brands use different (non U.S.) time formats
    fun testFormatRailDateRange() {
        var actual = DateRangeUtils.formatRailDateRange(context, firstDate, secondDate)
        assertEquals("Jun 5 - Jun 26", actual)

        actual = DateRangeUtils.formatRailDateRange(context, firstDate, null)
        assertEquals("Jun 5", actual)

        actual = DateRangeUtils.formatRailDateRange(context, null, null)
        assertEquals("", actual)
    }

    @Test
    @RunForBrands(brands = [MultiBrand.EXPEDIA]) // other brands use different (non U.S.) time formats
    fun testFormatHotelsV2DateRange() {
        val actual = DateRangeUtils.formatHotelsV2DateRange(context, "2020-06-05", "2020-06-26")
        assertEquals("Jun 05, 2020 - Jun 26, 2020", actual)
    }

    @Test
    @RunForBrands(brands = [MultiBrand.EXPEDIA]) // other brands use different (non U.S.) time formats
    fun testFormatPackageDateRangeContDesc() {
        val actual = DateRangeUtils.formatPackageDateRangeContDesc(context, "2020-06-05", "2020-06-26")
        assertEquals("Fri Jun 05, 2020 to Fri Jun 26, 2020", actual)
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA, MultiBrand.ORBITZ, MultiBrand.CHEAPTICKETS, MultiBrand.TRAVELOCITY, MultiBrand.AIRASIAGO,
            MultiBrand.VOYAGES, MultiBrand.WOTIF, MultiBrand.LASTMINUTE, MultiBrand.EBOOKERS))
    @Config(shadows = arrayOf(ShadowDateFormat::class))
    fun formatIntervalTest() {
        var actualFormatInterval = DateRangeUtils.formatTimeInterval(context, DateTime(2017, 11, 28, 12, 13), DateTime(2017, 11, 28, 23, 14))
        assertEquals("12:13 PM – 11:14 PM", actualFormatInterval)

        actualFormatInterval = DateRangeUtils.formatTimeInterval(context, DateTime(2017, 11, 28, 12, 13), DateTime(2018, 11, 29, 23, 14))
        assertEquals("12:13 PM – 11:14 PM", actualFormatInterval)

        actualFormatInterval = DateRangeUtils.formatTimeInterval(context, DateTime(2017, 11, 28, 11, 13), DateTime(2018, 11, 29, 23, 14))
        assertEquals("11:13 AM – 11:14 PM", actualFormatInterval)

        actualFormatInterval = DateRangeUtils.formatTimeInterval(context, DateTime(2017, 11, 28, 10, 13), DateTime(2018, 11, 29, 11, 14))
        assertEquals("10:13 AM – 11:14 AM", actualFormatInterval)
    }

    @Test
    fun getMinutesBetweenTest() {
        var actualValue = DateRangeUtils.getMinutesBetween(DateTime(2017, 11, 28, 12, 13), DateTime(2018, 11, 29, 23, 14))
        assertEquals(527701, actualValue)

        actualValue = DateRangeUtils.getMinutesBetween(DateTime(2018, 11, 29, 23, 14), DateTime(2017, 11, 28, 12, 13))
        assertEquals(-527701, actualValue)

        actualValue = DateRangeUtils.getMinutesBetween(DateTime(2018, 11, 29, 23, 14), DateTime(2018, 11, 29, 23, 14))
        assertEquals(0, actualValue)
    }

    @Test
    fun formatDurationTest() {
        val resource = context.resources

        var actualFormatDuration = DateRangeUtils.formatDuration(resource, -10)
        assertEquals("10m", actualFormatDuration)

        actualFormatDuration = DateRangeUtils.formatDuration(resource, 0)
        assertEquals("0m", actualFormatDuration)

        actualFormatDuration = DateRangeUtils.formatDuration(resource, 20)
        assertEquals("20m", actualFormatDuration)

        actualFormatDuration = DateRangeUtils.formatDuration(resource, 60)
        assertEquals("1h", actualFormatDuration)

        actualFormatDuration = DateRangeUtils.formatDuration(resource, 70)
        assertEquals("1h 10m", actualFormatDuration)
    }

    @Test
    fun testFormatDurationDaysHoursMinutes() {
        var actualValue = DateRangeUtils.formatDurationDaysHoursMinutes(context, 424)
        assertEquals("7h 4m", actualValue)

        actualValue = DateRangeUtils.formatDurationDaysHoursMinutes(context, 76)
        assertEquals("1h 16m", actualValue)

        actualValue = DateRangeUtils.formatDurationDaysHoursMinutes(context, 10000)
        assertEquals("6d 22h 40m", actualValue)

        actualValue = DateRangeUtils.formatDurationDaysHoursMinutes(context, 8680)
        assertEquals("6d 40m", actualValue)

        actualValue = DateRangeUtils.formatDurationDaysHoursMinutes(context, 9960)
        assertEquals("6d 22h", actualValue)

        actualValue = DateRangeUtils.formatDurationDaysHoursMinutes(context, 1320)
        assertEquals("22h", actualValue)

        actualValue = DateRangeUtils.formatDurationDaysHoursMinutes(context, 14)
        assertEquals("14m", actualValue)

        actualValue = DateRangeUtils.formatDurationDaysHoursMinutes(context, 0)
        assertEquals("", actualValue)

        actualValue = DateRangeUtils.formatDurationDaysHoursMinutes(context, -1)
        assertEquals("", actualValue)
    }

    @Test
    fun testGetLayoverDurationContDesc() {
        var durationMins = DateRangeUtils.getDurationContDescDaysHoursMins(context, 424)
        assertEquals("7 hour 4 minutes", durationMins)

        durationMins = DateRangeUtils.getDurationContDescDaysHoursMins(context, 76)
        assertEquals("1 hour 16 minutes", durationMins)

        durationMins = DateRangeUtils.getDurationContDescDaysHoursMins(context, 10000)
        assertEquals("6 day 22 hour 40 minutes", durationMins)

        durationMins = DateRangeUtils.getDurationContDescDaysHoursMins(context, 36)
        assertEquals("36 minutes", durationMins)

        durationMins = DateRangeUtils.getDurationContDescDaysHoursMins(context, 0)
        assertEquals(null, durationMins)

        durationMins = DateRangeUtils.getDurationContDescDaysHoursMins(context, -5)
        assertEquals(null, durationMins)
    }
}
