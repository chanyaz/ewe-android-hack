package com.expedia.bookings.unit.hotels.shortlist

import com.expedia.bookings.data.hotels.shortlist.HotelShortlistItem
import org.junit.Test
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
}
