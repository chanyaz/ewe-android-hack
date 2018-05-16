package com.expedia.bookings.unit.hotels.shortlist

import com.expedia.bookings.data.hotels.shortlist.HotelShortlistResponse
import org.junit.Test
import kotlin.test.assertNull
import kotlin.test.assertTrue

class HotelShortlistResponseTest {
    @Test
    fun testHotelShortlistResponseNull() {
        val response = HotelShortlistResponse<Int>()
        assertNull(response.metadata)
        assertTrue(response.results.isEmpty())
    }
}
