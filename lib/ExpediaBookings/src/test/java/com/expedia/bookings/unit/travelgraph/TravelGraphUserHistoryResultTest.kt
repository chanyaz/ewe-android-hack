package com.expedia.bookings.unit.travelgraph

import com.expedia.bookings.data.travelgraph.TravelGraphHotelSearchInfo
import com.expedia.bookings.data.travelgraph.TravelGraphItem
import com.expedia.bookings.data.travelgraph.TravelGraphUserHistoryResult
import org.junit.Test
import java.util.Date
import kotlin.test.assertEquals

class TravelGraphUserHistoryResultTest {
    @Test
    fun testConvertToSuggestionV4ListMaxThreeResults() {
        val result = createTravelGraphUserHistoryResult(10)
        val recentSearches = result.getRecentSearchInfos()

        assertEquals(3, recentSearches.count())
    }

    @Test
    fun testConvertToSuggestionV4ListLessThanThreeResults() {
        val result = createTravelGraphUserHistoryResult(1)
        val recentSearches = result.getRecentSearchInfos()

        assertEquals(1, recentSearches.count())
    }

    private fun createTravelGraphUserHistoryResult(numItem: Int): TravelGraphUserHistoryResult {
        val result = TravelGraphUserHistoryResult()
        result.items = List(numItem, { i ->
            createTravelGraphItem(i.toString())
        })
        return result
    }

    private fun createTravelGraphItem(searchRegionId: String?): TravelGraphItem {
        val item = TravelGraphItem()
        item.startDateUTCTimestamp = Date().time
        item.endDateUTCTimestamp = Date().time

        item.searchInfo = createTravelGraphHotelSearchInfo(searchRegionId)
        return item
    }

    private fun createTravelGraphHotelSearchInfo(searchRegionId: String?): TravelGraphHotelSearchInfo {
        val info = TravelGraphHotelSearchInfo()
        info.searchRegion = createTravelGraphSearchRegion(searchRegionId)
        return info
    }

    private fun createTravelGraphSearchRegion(id: String?, name: String = "name", shortName: String = "shortName"): TravelGraphHotelSearchInfo.TravelGraphSearchRegion {
        val region = TravelGraphHotelSearchInfo.TravelGraphSearchRegion()
        region.id = id
        region.name = name
        region.shortName = shortName
        return region
    }
}
