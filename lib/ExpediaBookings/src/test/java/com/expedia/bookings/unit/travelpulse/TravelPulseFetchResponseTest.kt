package com.expedia.bookings.unit.travelpulse

import com.expedia.bookings.data.travelpulse.TravelPulseFetchResponse
import org.junit.Test
import kotlin.test.assertNull
import kotlin.test.assertTrue

class TravelPulseFetchResponseTest {

    @Test
    fun testTravelPulseFetchResponseNull() {
        val response = TravelPulseFetchResponse()
        assertNull(response.metadata)
        assertTrue(response.results.isEmpty())
    }

    @Test
    fun testTravelPulseMetadataNull() {
        val metadata = TravelPulseFetchResponse.TravelPulseMetadata()
        assertNull(metadata.userContext)
    }

    @Test
    fun testTravelPulseUserContextNull() {
        val userContext = TravelPulseFetchResponse.TravelPulseUserContext()
        assertNull(userContext.siteId)
        assertNull(userContext.expUserId)
        assertNull(userContext.guid)
    }

    @Test
    fun testTravelPulseResultNull() {
        val result = TravelPulseFetchResponse.TravelPulseResult()
        assertNull(result.product)
        assertNull(result.type)
        assertTrue(result.items.isEmpty())
    }

    @Test
    fun testTravelPulseItemNull() {
        val item = TravelPulseFetchResponse.TravelPulseItem()
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
        val item = TravelPulseFetchResponse.ShortlistItem()
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
        val metadata = TravelPulseFetchResponse.ShortlistItemMetaData()
        assertNull(metadata.hotelId)
        assertNull(metadata.chkIn)
        assertNull(metadata.chkOut)
        assertNull(metadata.roomConfiguration)
    }

    @Test
    fun testLastModifiedDateNull() {
        val lastModifiedDate = TravelPulseFetchResponse.LastModifiedDate()
        assertNull(lastModifiedDate.nano)
        assertNull(lastModifiedDate.epochSecond)
    }
}
