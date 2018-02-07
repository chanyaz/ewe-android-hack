package com.expedia.bookings.unit.travelgraph

import com.expedia.bookings.data.travelgraph.TravelGraphHotelSearchInfo
import org.junit.Assert.assertNull
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class TravelGraphHotelSearchInfoTest {
    @Test
    fun testIsInvalidIfSearchRegionMissingData() {
        val searchRegion = TravelGraphHotelSearchInfo.TravelGraphSearchRegion()
        assertNull(searchRegion.toSuggestionV4())
    }

    @Test
    fun testValidSearchRegion() {
        val searchRegion = TravelGraphHotelSearchInfo.TravelGraphSearchRegion()
        searchRegion.id = "123"
        assertNotNull(searchRegion.toSuggestionV4())
    }

    @Test
    fun testInvalidSearchRegionIfMissingId() {
        val searchRegion = TravelGraphHotelSearchInfo.TravelGraphSearchRegion()
        searchRegion.name = "Chicago"
        assertNull(searchRegion.toSuggestionV4())
    }

    @Test
    fun testReturnDefaultTravelerIfTravelersMissing() {
        val searchInfo = TravelGraphHotelSearchInfo()
        assertEquals(1, searchInfo.getTravelerInfo().totalTravelers())
    }

    @Test
    fun testTravelInfo() {
        val searchInfo = TravelGraphHotelSearchInfo()
        val room = TravelGraphHotelSearchInfo.TravelGraphHotelRoom()
        val roomOccupants = TravelGraphHotelSearchInfo.TravelGraphTravelerDetails()
        roomOccupants.numberOfAdults = 1
        searchInfo.roomList = listOf(room)
        assertEquals(1, searchInfo.getTravelerInfo().totalTravelers())
    }
}
