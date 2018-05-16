package com.expedia.bookings.unit.hotels.shortlist

import com.expedia.bookings.data.hotels.shortlist.LastModifiedDate
import org.junit.Test
import kotlin.test.assertNull

class LastModifiedDateTest {
    @Test
    fun testLastModifiedDateNull() {
        val lastModifiedDate = LastModifiedDate()
        assertNull(lastModifiedDate.nano)
        assertNull(lastModifiedDate.epochSecond)
    }
}
