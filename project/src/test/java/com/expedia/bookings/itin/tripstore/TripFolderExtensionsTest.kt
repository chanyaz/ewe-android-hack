package com.expedia.bookings.itin.tripstore

import com.expedia.bookings.R
import com.expedia.bookings.data.trips.TripFolder
import com.expedia.bookings.itin.helpers.MockStringProvider
import com.expedia.bookings.itin.tripstore.extensions.abbreviatedDateRange
import com.expedia.bookings.itin.tripstore.extensions.endDate
import com.expedia.bookings.itin.tripstore.extensions.startDate
import com.expedia.bookings.itin.utils.StringSource
import com.google.gson.Gson
import com.mobiata.mocke3.getJsonStringFromMock
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals

class TripFolderExtensionsTest {
    private lateinit var foldersJson: String
    private lateinit var folders: List<TripFolder>
    private lateinit var folder: TripFolder
    private lateinit var mockStringProvider: StringSource

    @Before
    fun setup() {
        foldersJson = getJsonStringFromMock("api/trips/tripfolders/tripfolders_three_hotels_one_cruise.json", null)
        folders = Gson().fromJson(foldersJson, Array<TripFolder>::class.java).toList()
        folder = folders[0]
        mockStringProvider = MockStringProvider()
    }

    @Test
    fun testStartDate() {
        val expected = DateTime(1537920000000, DateTimeZone.forOffsetMillis(-25200000))
        val actual = folder.startDate()
        assertEquals(expected, actual)
    }

    @Test
    fun testEndDate() {
        val expected = DateTime(1538524800000, DateTimeZone.forOffsetMillis(-25200000))
        val actual = folder.endDate()
        assertEquals(expected, actual)
    }

    @Test
    fun testAbbreviatedRange_StartAndEndDatesCrossMonthBoundryIncludeMonthInEndDate() {
        val startDate = DateTime().withYear(2030).withMonthOfYear(3).withDayOfMonth(25)
        val endDate = DateTime().withYear(2030).withMonthOfYear(4).withDayOfMonth(1)
        val expected = (R.string.trip_folder_abbreviated_date_range_TEMPLATE).toString().plus(
                mapOf("startdate" to "Mar 25", "enddate" to "Apr 1")
        )
        assertEquals(expected, abbreviatedDateRange(startDate, endDate, mockStringProvider))
    }

    @Test
    fun testAbbreviatedRange_SameMonthDoesNotIncludeMonthAndDoesntPadSingleDigits() {
        val startDate = DateTime().withYear(2030).withMonthOfYear(3).withDayOfMonth(1)
        val endDate = DateTime().withYear(2030).withMonthOfYear(3).withDayOfMonth(9)
        val expected = (R.string.trip_folder_abbreviated_date_range_TEMPLATE).toString().plus(
                mapOf("startdate" to "Mar 1", "enddate" to "9")
        )
        assertEquals(expected, abbreviatedDateRange(startDate, endDate, mockStringProvider))
    }

    @Test
    fun testAbbreviatedRange_SameMonth() {
        val startDate = DateTime().withYear(2030).withMonthOfYear(6).withDayOfMonth(10)
        val endDate = DateTime().withYear(2030).withMonthOfYear(6).withDayOfMonth(19)
        val expected = (R.string.trip_folder_abbreviated_date_range_TEMPLATE).toString().plus(
                mapOf("startdate" to "Jun 10", "enddate" to "19")
        )
        assertEquals(expected, abbreviatedDateRange(startDate, endDate, mockStringProvider))
    }

    @Test
    fun testAbbreviatedRange_DatesCrossYear() {
        val startDate = DateTime().withYear(2030).withMonthOfYear(6).withDayOfMonth(10)
        val endDate = DateTime().withYear(2031).withMonthOfYear(6).withDayOfMonth(19)
        val expected = (R.string.trip_folder_abbreviated_date_range_TEMPLATE).toString().plus(
                mapOf("startdate" to "Jun 10 2030", "enddate" to "Jun 19 2031")
        )
        assertEquals(expected, abbreviatedDateRange(startDate, endDate, mockStringProvider))
    }

    @Test
    fun testAbbreviatedRange_IncludeYearWhenInThePastEvenWhenDatesAreInTheSameMonth() {
        val startDate = DateTime().withYear(2010).withMonthOfYear(3).withDayOfMonth(1)
        val endDate = DateTime().withYear(2010).withMonthOfYear(3).withDayOfMonth(9)
        val expected = (R.string.trip_folder_abbreviated_date_range_TEMPLATE).toString().plus(
                mapOf("startdate" to "Mar 1 2010", "enddate" to "Mar 9 2010")
        )
        assertEquals(expected, abbreviatedDateRange(startDate, endDate, mockStringProvider))
    }
}
