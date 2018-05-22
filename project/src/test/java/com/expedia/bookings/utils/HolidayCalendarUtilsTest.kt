package com.expedia.bookings.utils

import com.expedia.bookings.data.HolidayCalendarResponse
import com.expedia.bookings.data.HolidayEntity
import org.joda.time.LocalDate
import org.junit.Test
import kotlin.test.assertEquals

class HolidayCalendarUtilsTest {

    @Test
    fun testToListOfDates() {
        val holidayCalendarResponse = makeHolidayCalendarResponse()
        val receivedHolidayDateList = holidayCalendarResponse.toListOfDates()
        val expectedHolidayDateList = listOf(LocalDate("2018-01-01"), LocalDate("2018-12-25"))
        assertEquals(expectedHolidayDateList, receivedHolidayDateList)
    }

    @Test
    fun testToLinkedHashMapOfHolidays() {
        val holidayCalendarResponse = makeHolidayCalendarResponse()
        val receivedHolidayHashMap = holidayCalendarResponse.toMapOfDatesToNames()
        val expectedHolidayHashMap = makeHolidayHashMap()
        assertEquals(expectedHolidayHashMap, receivedHolidayHashMap)
    }

    private fun makeHolidayCalendarResponse(): HolidayCalendarResponse {
        val holidayList = listOf(HolidayEntity("2018-01-01", "New Year"), HolidayEntity("2018-12-25", "Christmas"))
        val holidayCalendarResponse = HolidayCalendarResponse()
        holidayCalendarResponse.holidays = holidayList
        return holidayCalendarResponse
    }

    private fun makeHolidayHashMap(): HashMap<LocalDate, String> {
        return hashMapOf(
                LocalDate("2018-01-01") to "New Year",
                LocalDate("2018-12-25") to "Christmas"
        )
    }
}
