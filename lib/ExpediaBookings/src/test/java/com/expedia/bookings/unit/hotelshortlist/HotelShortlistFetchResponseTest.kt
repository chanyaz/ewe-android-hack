package com.expedia.bookings.unit.hotelshortlist

import com.expedia.bookings.data.hotelshortlist.HotelShortlistFetchResponse
import org.junit.Test
import kotlin.test.assertNull
import kotlin.test.assertTrue

class HotelShortlistFetchResponseTest {

    @Test
    fun testHotelShortlistFetchResponseNull() {
        val response = HotelShortlistFetchResponse()
        assertNull(response.metadata)
        assertTrue(response.results.isEmpty())
    }

    @Test
    fun testHotelShortlistMetadataNull() {
        val metadata = HotelShortlistFetchResponse.HotelShortlistMetadata()
        assertNull(metadata.userContext)
    }

    @Test
    fun testHotelShortlistUserContextNull() {
        val userContext = HotelShortlistFetchResponse.HotelShortlistUserContext()
        assertNull(userContext.siteId)
        assertNull(userContext.expUserId)
        assertNull(userContext.guid)
    }

    @Test
    fun testHotelShortlistResultNull() {
        val result = HotelShortlistFetchResponse.HotelShortlistResult()
        assertNull(result.product)
        assertNull(result.type)
        assertTrue(result.items.isEmpty())
    }

    @Test
    fun testHotelShortlistItemNull() {
        val item = HotelShortlistFetchResponse.HotelShortlistItem()
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
    fun testShortlistItemNull() {
        val item = HotelShortlistFetchResponse.ShortlistItem()
        assertNull(item.id)
        assertNull(item.itemId)
        assertNull(item.configId)
        assertNull(item.guid)
        assertNull(item.uid)
        assertNull(item.expUserId)
        assertNull(item.siteId)
        assertNull(item.metaData)
        assertNull(item.lastModifiedDate)
    }

    @Test
    fun testShortlistItemMetaDataNull() {
        val metadata = HotelShortlistFetchResponse.ShortlistItemMetaData()
        assertNull(metadata.hotelId)
        assertNull(metadata.chkIn)
        assertNull(metadata.chkOut)
        assertNull(metadata.roomConfiguration)
    }

    @Test
    fun testLastModifiedDateNull() {
        val lastModifiedDate = HotelShortlistFetchResponse.LastModifiedDate()
        assertNull(lastModifiedDate.nano)
        assertNull(lastModifiedDate.epochSecond)
    }
}
