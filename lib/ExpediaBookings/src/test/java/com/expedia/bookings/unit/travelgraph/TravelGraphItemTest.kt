package com.expedia.bookings.unit.travelgraph

import com.expedia.bookings.data.travelgraph.TravelGraphHotelSearchInfo
import com.expedia.bookings.data.travelgraph.TravelGraphItem
import junit.framework.Assert.assertFalse
import junit.framework.Assert.assertTrue
import org.junit.Test

class TravelGraphItemTest {

    @Test
    fun testIsInvalidIfSearchInfoMissing() {
        val tgItem = TravelGraphItem()
        assertFalse(tgItem.isValid())
    }

    @Test
    fun testIsInvalidIfSearchRegionMissing() {
        val tgItem = TravelGraphItem()
        tgItem.searchInfo = TravelGraphHotelSearchInfo()
        assertFalse(tgItem.isValid())
    }

    @Test
    fun testIsInvalidIfSearchRegionBad() {
        val searchInfo = TravelGraphHotelSearchInfo()
        searchInfo.searchRegion = TravelGraphHotelSearchInfo.TravelGraphSearchRegion()

        val tgItem = TravelGraphItem()
        tgItem.searchInfo = searchInfo
        assertFalse(tgItem.isValid())
    }

    @Test
    fun testValidSearchRegion() {
        val searchInfo = TravelGraphHotelSearchInfo()
        val searchRegion = TravelGraphHotelSearchInfo.TravelGraphSearchRegion()
        searchRegion.id = "123"
        searchInfo.searchRegion = searchRegion

        val tgItem = TravelGraphItem()
        tgItem.searchInfo = searchInfo
        assertTrue(tgItem.isValid())
    }
}
