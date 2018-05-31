package com.expedia.bookings.unit.hotels.shortlist

import com.expedia.bookings.data.hotels.shortlist.HotelShortlistItem
import com.expedia.bookings.data.hotels.shortlist.ShortlistItem
import com.expedia.bookings.data.hotels.shortlist.ShortlistItemMetadata
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class HotelShortlistItemTest {
    @Test
    fun testHotelShortlistItemNull() {
        val item = HotelShortlistItem()
        assertNull(item.templateName)
        assertNull(item.shortlistItem)
        assertNull(item.link)
        assertNull(item.image)
        assertNull(item.title)
        assertNull(item.blurb)
        assertNull(item.id)
        assertNull(item.name)
        assertNull(item.description)
        assertNull(item.media)
        assertNull(item.rating)
        assertNull(item.guestRating)
        assertNull(item.numberOfReviews)
        assertNull(item.numberOfRooms)
        assertNull(item.price)
        assertNull(item.regionId)
        assertNull(item.currency)
        assertNull(item.tripLocations)
        assertNull(item.tripDates)
        assertNull(item.routeType)
    }

    @Test
    fun testHotelIdSetIfItemIdPresent() {
        val shortlistItem = ShortlistItem()
        shortlistItem.itemId = "hotel1"
        val item = HotelShortlistItem()
        item.shortlistItem = shortlistItem

        assertEquals("hotel1", item.getHotelId())
    }

    @Test
    fun testHotelIdSetIfMetaDataIdPresent() {
        val metadata = ShortlistItemMetadata()
        metadata.hotelId = "hotel1"
        val shortlistItem = ShortlistItem()
        shortlistItem.metaData = metadata
        val item = HotelShortlistItem()
        item.shortlistItem = shortlistItem

        assertEquals("hotel1", item.getHotelId())
    }

    @Test
    fun testHotelIdNotSetIfNotPresent() {
        val item = HotelShortlistItem()
        assertNull(item.getHotelId())
    }
}
