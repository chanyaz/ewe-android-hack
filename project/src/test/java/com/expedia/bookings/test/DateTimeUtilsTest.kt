package com.expedia.bookings.test

import android.content.Context
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.test.robolectric.shadows.ShadowDateFormat
import com.mobiata.flightlib.utils.DateTimeUtils
import org.joda.time.DateTime
import org.joda.time.LocalDateTime
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import kotlin.test.assertEquals

@RunWith(RobolectricRunner::class)
@Config(shadows = arrayOf(ShadowDateFormat::class))
class DateTimeUtilsTest {

    fun getContext(): Context = RuntimeEnvironment.application

    @Test
    fun parseFlightStatsDateTimeTest() {
        val actualLocalDateTime = DateTimeUtils.parseFlightStatsDateTime("2017-11-28-12:23")
        val expectedLocalDateTime = LocalDateTime(2017, 11, 28, 12, 23)
        assertEquals(actualLocalDateTime, expectedLocalDateTime)
    }

    @Test
    fun formatDurationTest() {
        val resource = getContext().resources

        var actualFormatDuration = DateTimeUtils.formatDuration(resource, -10)
        assertEquals("10m", actualFormatDuration)

        actualFormatDuration = DateTimeUtils.formatDuration(resource, 0)
        assertEquals("0m", actualFormatDuration)

        actualFormatDuration = DateTimeUtils.formatDuration(resource, 20)
        assertEquals("20m", actualFormatDuration)

        actualFormatDuration = DateTimeUtils.formatDuration(resource, 60)
        assertEquals("1h", actualFormatDuration)

        actualFormatDuration = DateTimeUtils.formatDuration(resource, 70)
        assertEquals("1h 10m", actualFormatDuration)
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA, MultiBrand.ORBITZ, MultiBrand.CHEAPTICKETS, MultiBrand.TRAVELOCITY, MultiBrand.AIRASIAGO,
            MultiBrand.VOYAGES, MultiBrand.WOTIF, MultiBrand.LASTMINUTE, MultiBrand.EBOOKERS))
    fun formatIntervalTest() {
        var actualFormatInterval = DateTimeUtils.formatInterval(getContext(), DateTime(2017, 11, 28, 12, 13), DateTime(2017, 11, 28, 23, 14))
        assertEquals("12:13 PM – 11:14 PM", actualFormatInterval)

        actualFormatInterval = DateTimeUtils.formatInterval(getContext(), DateTime(2017, 11, 28, 12, 13), DateTime(2018, 11, 29, 23, 14))
        assertEquals("12:13 PM – 11:14 PM", actualFormatInterval)

        actualFormatInterval = DateTimeUtils.formatInterval(getContext(), DateTime(2017, 11, 28, 11, 13), DateTime(2018, 11, 29, 23, 14))
        assertEquals("11:13 AM – 11:14 PM", actualFormatInterval)

        actualFormatInterval = DateTimeUtils.formatInterval(getContext(), DateTime(2017, 11, 28, 10, 13), DateTime(2018, 11, 29, 11, 14))
        assertEquals("10:13 AM – 11:14 AM", actualFormatInterval)
    }

    @Test
    fun getMinutesBetweenTest() {
        var actualValue = DateTimeUtils.getMinutesBetween(DateTime(2017, 11, 28, 12, 13), DateTime(2018, 11, 29, 23, 14))
        assertEquals(527701, actualValue)

        actualValue = DateTimeUtils.getMinutesBetween(DateTime(2018, 11, 29, 23, 14), DateTime(2017, 11, 28, 12, 13))
        assertEquals(-527701, actualValue)

        actualValue = DateTimeUtils.getMinutesBetween(DateTime(2018, 11, 29, 23, 14), DateTime(2018, 11, 29, 23, 14))
        assertEquals(0, actualValue)
    }

    @Test
    fun  testFormatDurationDaysHoursMinutes() {
        var actualValue = DateTimeUtils.formatDurationDaysHoursMinutes(getContext(), 424)
        assertEquals("7h 4m", actualValue)

        actualValue = DateTimeUtils.formatDurationDaysHoursMinutes(getContext(), 76)
        assertEquals("1h 16m", actualValue)

        actualValue = DateTimeUtils.formatDurationDaysHoursMinutes(getContext(), 10000)
        assertEquals("6d 22h 40m", actualValue)

        actualValue = DateTimeUtils.formatDurationDaysHoursMinutes(getContext(), 8680)
        assertEquals("6d 40m", actualValue)

        actualValue = DateTimeUtils.formatDurationDaysHoursMinutes(getContext(), 9960)
        assertEquals("6d 22h", actualValue)

        actualValue = DateTimeUtils.formatDurationDaysHoursMinutes(getContext(), 1320)
        assertEquals("22h", actualValue)

        actualValue = DateTimeUtils.formatDurationDaysHoursMinutes(getContext(), 14)
        assertEquals("14m", actualValue)

        actualValue = DateTimeUtils.formatDurationDaysHoursMinutes(getContext(), 0)
        assertEquals("", actualValue)

        actualValue = DateTimeUtils.formatDurationDaysHoursMinutes(getContext(), -1)
        assertEquals("", actualValue)
    }
}

