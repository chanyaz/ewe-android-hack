package com.expedia.bookings.unit.hotels.shortlist

import com.expedia.bookings.data.hotels.shortlist.ShortlistItem
import org.junit.Test
import kotlin.test.assertNull

class ShortlistItemTest {
    @Test
    fun testShortlistItemNull() {
        val item = ShortlistItem()
        assertNull(item.id)
        assertNull(item.itemId)
        assertNull(item.configId)
        assertNull(item.guid)
        assertNull(item.uid)
        assertNull(item.expUserId)
        assertNull(item.siteId)
        assertNull(item.metadata)
        assertNull(item.lastModifiedDate)
    }
}
