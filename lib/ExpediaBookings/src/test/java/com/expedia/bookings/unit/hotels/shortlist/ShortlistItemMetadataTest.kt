package com.expedia.bookings.unit.hotels.shortlist

import com.expedia.bookings.data.hotels.shortlist.ShortlistItemMetadata
import org.junit.Test
import kotlin.test.assertNull

class ShortlistItemMetadataTest {
    @Test
    fun testShortlistItemMetaDataNull() {
        val metadata = ShortlistItemMetadata()
        assertNull(metadata.hotelId)
        assertNull(metadata.chkIn)
        assertNull(metadata.chkOut)
        assertNull(metadata.roomConfiguration)
    }
}
