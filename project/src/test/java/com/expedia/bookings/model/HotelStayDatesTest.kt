package com.expedia.bookings.model

import com.expedia.bookings.test.robolectric.RobolectricRunner
import org.joda.time.LocalDate
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

@RunWith(RobolectricRunner::class)
class HotelStayDatesTest {

    @Test
    fun testGetEndDateNullStart() {
        val hotelStayDates = createHotelStayDates(null, null)

        assertNull(hotelStayDates.getStartDate())
        assertNull(hotelStayDates.getEndDate())
    }

    @Test
    fun testGetEndDateNullEnd() {
        val hotelStayDates = createHotelStayDates(0, null)

        assertEquals(LocalDate.now(), hotelStayDates.getStartDate())
        assertEquals(LocalDate.now().plusDays(1), hotelStayDates.getEndDate())
    }

    @Test
    fun testSameHotelStayDatesNull() {
        val hotelStayDates = createHotelStayDates(null, null)

        assertFalse(hotelStayDates.sameHotelStayDates(null))
    }

    @Test
    fun testSameHotelStayDatesBothStartEndNull() {
        val hotelStayDates1 = createHotelStayDates(null, null)
        val hotelStayDates2 = createHotelStayDates(null, null)

        assertTrue(hotelStayDates1.sameHotelStayDates(hotelStayDates2))
    }

    @Test
    fun testSameHotelStayDatesBothStartNull() {
        val hotelStayDates1 = createHotelStayDates(null, 5)
        val hotelStayDates2 = createHotelStayDates(null, 7)

        assertTrue(hotelStayDates1.sameHotelStayDates(hotelStayDates2))
    }

    @Test
    fun testSameHotelStayDatesStartNull() {
        val hotelStayDates1 = createHotelStayDates(null, 0)
        val hotelStayDates2 = createHotelStayDates(1, 3)

        assertFalse(hotelStayDates1.sameHotelStayDates(hotelStayDates2))
    }

    @Test
    fun testSameHotelStayDatesOtherStartNull() {
        val hotelStayDates1 = createHotelStayDates(0, 2)
        val hotelStayDates2 = createHotelStayDates(null, 0)

        assertFalse(hotelStayDates1.sameHotelStayDates(hotelStayDates2))
    }

    @Test
    fun testSameHotelStayDatesStartEarlier() {
        val hotelStayDates1 = createHotelStayDates(0, 3)
        val hotelStayDates2 = createHotelStayDates(1, 2)

        assertFalse(hotelStayDates1.sameHotelStayDates(hotelStayDates2))
    }

    @Test
    fun testSameHotelStayDatesStartLater() {
        val hotelStayDates1 = createHotelStayDates(1, 2)
        val hotelStayDates2 = createHotelStayDates(0, 3)

        assertFalse(hotelStayDates1.sameHotelStayDates(hotelStayDates2))
    }

    @Test
    fun testSameHotelStayDatesBothEndNull() {
        val hotelStayDates1 = createHotelStayDates(0, null)
        val hotelStayDates2 = createHotelStayDates(0, null)

        assertTrue(hotelStayDates1.sameHotelStayDates(hotelStayDates2))
    }

    @Test
    fun testSameHotelStayDatesEndNull() {
        val hotelStayDates1 = createHotelStayDates(1, null)
        val hotelStayDates2 = createHotelStayDates(1, 3)

        assertFalse(hotelStayDates1.sameHotelStayDates(hotelStayDates2))
    }

    @Test
    fun testSameHotelStayDatesOtherEndNull() {
        val hotelStayDates1 = createHotelStayDates(2, 2)
        val hotelStayDates2 = createHotelStayDates(2, null)

        assertFalse(hotelStayDates1.sameHotelStayDates(hotelStayDates2))
    }

    @Test
    fun testSameHotelStayDatesExactSame() {
        val hotelStayDates1 = createHotelStayDates(7, 7)
        val hotelStayDates2 = createHotelStayDates(7, 7)

        assertTrue(hotelStayDates1.sameHotelStayDates(hotelStayDates2))
    }

    @Test
    fun testSameHotelStayDatesSameEndNull() {
        val hotelStayDates1 = createHotelStayDates(3, null)
        val hotelStayDates2 = createHotelStayDates(3, 4)

        assertTrue(hotelStayDates1.sameHotelStayDates(hotelStayDates2))
    }

    @Test
    fun testSameHotelStayDatesOtherSameEndNull() {
        val hotelStayDates1 = createHotelStayDates(0, 1)
        val hotelStayDates2 = createHotelStayDates(0, null)

        assertTrue(hotelStayDates1.sameHotelStayDates(hotelStayDates2))
    }

    @Test
    fun testSameHotelStayDatesEndEarlier() {
        val hotelStayDates1 = createHotelStayDates(4, 5)
        val hotelStayDates2 = createHotelStayDates(4, 7)

        assertFalse(hotelStayDates1.sameHotelStayDates(hotelStayDates2))
    }

    @Test
    fun testSameHotelStayDatesEndLater() {
        val hotelStayDates1 = createHotelStayDates(6, 9)
        val hotelStayDates2 = createHotelStayDates(6, 8)

        assertFalse(hotelStayDates1.sameHotelStayDates(hotelStayDates2))
    }

    private fun createHotelStayDates(startPlusDay: Int?, endPlusDay: Int?) : HotelStayDates {
        var startDate: LocalDate? = null
        var endDate: LocalDate? = null

        if (startPlusDay != null) {
            startDate = LocalDate.now().plusDays(startPlusDay)
        }
        if (endPlusDay != null) {
            endDate = LocalDate.now().plusDays(endPlusDay)
        }

        return HotelStayDates(startDate, endDate)
    }
}
