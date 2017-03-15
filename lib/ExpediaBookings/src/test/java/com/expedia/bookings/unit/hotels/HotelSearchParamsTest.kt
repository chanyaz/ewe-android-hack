package com.expedia.bookings.unit.hotels

import com.expedia.bookings.data.SuggestionV4
import com.expedia.bookings.data.hotels.HotelSearchParams
import org.joda.time.LocalDate
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class HotelSearchParamsTest {
    val maxStay = 26
    val maxRange = 329
    val tomorrow = LocalDate.now().plusDays(1)
    val checkoutDate = tomorrow.plusDays(2)
    val testParamBuilder = HotelSearchParams.Builder(maxStay, maxRange)

    @Test
    fun testEmptyFilterOptions() {
        testParamBuilder
                .destination(getDummySuggestion("Seattle", "SEA"))
                .startDate(tomorrow)
                .endDate(checkoutDate)
        val searchParams = testParamBuilder.build()

        assertTrue(searchParams.filterOptions!!.getFiltersQueryMap().isEmpty())
    }

    @Test
    fun testFilterOptions() {
        val name = "Hyatt"
        testParamBuilder
                .destination(getDummySuggestion("Seattle", "SEA"))
                .startDate(tomorrow)
                .endDate(checkoutDate)
        testParamBuilder
                .hotelName(name)
                .starRatings(listOf(10, 20))
                .priceRange(HotelSearchParams.PriceRange(10, 30))

        val searchParams = testParamBuilder.build()
        val map = searchParams.filterOptions!!.getFiltersQueryMap()
        assertEquals(3, map.size)
        assertEquals(name, map["filterHotelName"])
        assertEquals("10,20", map["filterStarRatings"])
        assertEquals("10,30", map["filterPrice"])
    }

    private fun getDummySuggestion(city: String, airport: String): SuggestionV4 {
        val suggestion = SuggestionV4()
        suggestion.gaiaId = ""
        suggestion.regionNames = SuggestionV4.RegionNames()
        suggestion.regionNames.displayName = city
        suggestion.regionNames.fullName = city
        suggestion.regionNames.shortName = city
        suggestion.hierarchyInfo = SuggestionV4.HierarchyInfo()
        suggestion.hierarchyInfo!!.airport = SuggestionV4.Airport()
        suggestion.hierarchyInfo!!.airport!!.airportCode = airport
        return suggestion
    }


}