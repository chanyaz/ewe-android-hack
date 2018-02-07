package com.expedia.bookings.unit.travelgraph

import com.expedia.bookings.data.travelgraph.TravelGraphHotelSearchInfo
import com.expedia.bookings.data.travelgraph.TravelGraphItem
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test
import java.util.Date

class TravelGraphItemTest {

    @Test
    fun testRecentSearchIsNullIfSearchInfoMissing() {
        val tgItem = TravelGraphItem()
        assertNull(tgItem.toRecentSearchInfo())
    }

    @Test
    fun testToValidRecentSearchInfo() {
        val searchInfo = TravelGraphHotelSearchInfo()
        val searchRegion = TravelGraphHotelSearchInfo.TravelGraphSearchRegion()
        searchRegion.id = "123"
        searchInfo.searchRegion = searchRegion

        val tgItem = TravelGraphItem()
        tgItem.startDateUTCTimestamp = Date().time
        tgItem.endDateUTCTimestamp = Date().time

        tgItem.searchInfo = searchInfo
        assertNotNull(tgItem.toRecentSearchInfo())
    }
}
