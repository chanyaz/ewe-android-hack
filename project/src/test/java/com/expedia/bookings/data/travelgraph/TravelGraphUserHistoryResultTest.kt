package com.expedia.bookings.data.travelgraph

import com.expedia.bookings.test.robolectric.RobolectricRunner
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.test.assertEquals

@RunWith(RobolectricRunner::class)
class TravelGraphUserHistoryResultTest {

    @Test
    fun testConvertToSuggestionV4ListMaxThreeResults() {
        val result = createTravelGraphUserHistoryResult(10)
        val suggestionList = result.convertToSuggestionV4List()

        assertEquals(3, suggestionList.count())
    }

    @Test
    fun testConvertToSuggestionV4ListLessThanThreeResults() {
        val result = createTravelGraphUserHistoryResult(1)
        val suggestionList = result.convertToSuggestionV4List()

        assertEquals(1, suggestionList.count())
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
