package com.expedia.bookings.unit.hotels.shortlist

import com.expedia.bookings.data.hotels.shortlist.HotelShortlistMetadata
import org.junit.Test
import kotlin.test.assertNull

class HotelShortlistMetadataTest {
    @Test
    fun testHotelShortlistMetadataNull() {
        val metadata = HotelShortlistMetadata()
        assertNull(metadata.userContext)
    }

    @Test
    fun testHotelShortlistUserContextNull() {
        val userContext = HotelShortlistMetadata.HotelShortlistUserContext()
        assertNull(userContext.siteId)
        assertNull(userContext.expUserId)
        assertNull(userContext.guid)
    }
}
