package com.expedia.bookings.utils

import com.expedia.bookings.data.HolidayCalendarResponse
import com.expedia.bookings.data.HolidayEntity
import org.joda.time.LocalDate
import org.junit.Test
import kotlin.test.assertEquals

class HolidayCalendarUtilsTest {

    @Test
    fun testGetListOfDatesFromHolidayCalendarResponse() {
        val holidayCalendarResponse = makeHolidayCalendarResponse()
        val receivedHolidayDateList = holidayCalendarResponse.toListOfDates()
        val expectedHolidayDateList = listOf(LocalDate("2018-01-01"), LocalDate("2018-12-25"))
        assertEquals(expectedHolidayDateList, receivedHolidayDateList)
    }

    private fun makeHolidayCalendarResponse(): HolidayCalendarResponse {
        val holidayList = listOf(HolidayEntity("2018-01-01", "New Year"), HolidayEntity("2018-12-25", "Christmas"))
        val holidayCalendarResponse = HolidayCalendarResponse()
        holidayCalendarResponse.holidays = holidayList
        return holidayCalendarResponse
    }
}
