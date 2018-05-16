package com.expedia.bookings.unit.hotels.shortlist

import com.expedia.bookings.data.hotels.shortlist.HotelShortlistResult
import org.junit.Test
import kotlin.test.assertNull
import kotlin.test.assertTrue

class HotelShortlistResultTest {
    @Test
    fun testHotelShortlistResultNull() {
        val result = HotelShortlistResult<Int>()
        assertNull(result.product)
        assertNull(result.type)
        assertTrue(result.items.isEmpty())
    }
}
