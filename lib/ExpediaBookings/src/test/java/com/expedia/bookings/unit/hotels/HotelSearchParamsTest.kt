package com.expedia.bookings.unit.hotels

import com.expedia.bookings.data.SuggestionV4
import com.expedia.bookings.data.hotels.HotelSearchParams
import org.joda.time.LocalDate
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class HotelSearchParamsTest {
    val maxStay = 26
    val maxRange = 329
    val tomorrow = LocalDate.now().plusDays(1)
    val checkoutDate = tomorrow.plusDays(2)
    val testParamBuilder = HotelSearchParams.Builder(maxStay, maxRange)

    @Before
    fun setup() {
        testParamBuilder
                .destination(getDummySuggestion("Seattle", "SEA"))
                .startDate(tomorrow)
                .endDate(checkoutDate)
    }

    @Test
    fun testEmptyFilterOptions() {
        val searchParams = testParamBuilder.build()
        assertTrue(searchParams.filterOptions!!.getFiltersQueryMap().isEmpty())
    }

    @Test
    fun testFilterOptions() {
        val name = "Hyatt"
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

    @Test
    fun testClientSideSortTypeIsAlwaysDefault() {
        testParamBuilder.userSort(HotelSearchParams.SortType.REVIEWS)
        val searchParams = testParamBuilder.build()
        searchParams.serverSort = false

        assertEquals(HotelSearchParams.SortType.EXPERT_PICKS, searchParams.getSortOrder())

        searchParams.sortType = "SortMeSortMe"
        assertEquals(HotelSearchParams.SortType.EXPERT_PICKS, searchParams.getSortOrder())
    }

    @Test
    fun testServerSideSortDefault() {
        val searchParams = testParamBuilder.build()
        searchParams.serverSort = true
        assertEquals(HotelSearchParams.SortType.EXPERT_PICKS, searchParams.getSortOrder())
    }

    @Test
    fun testServerSideSortWithUserPreference() {
        testParamBuilder.userSort(HotelSearchParams.SortType.REVIEWS)
        val searchParams = testParamBuilder.build()
        searchParams.serverSort = true

        assertEquals(HotelSearchParams.SortType.REVIEWS, searchParams.getSortOrder())

        searchParams.sortType = "Price"
        assertEquals(HotelSearchParams.SortType.REVIEWS, searchParams.getSortOrder())
    }

    @Test
    fun testServerSideSortWithSortType() {
        val searchParams = testParamBuilder.build()
        searchParams.serverSort = true
        searchParams.sortType = "Price"
        assertEquals(HotelSearchParams.SortType.PRICE, searchParams.getSortOrder())
    }

    @Test
    fun testCurrentLocationServerSideSort() {
        val paramBuilder = HotelSearchParams.Builder(maxStay, maxRange)
        val currentLocation = getDummySuggestion("ChiTown", "CHI")
        currentLocation.gaiaId = ""
        paramBuilder
                .destination(currentLocation)
                .startDate(tomorrow)
                .endDate(checkoutDate)
        val searchParams = paramBuilder.build()
        searchParams.serverSort = true
        assertEquals(HotelSearchParams.SortType.DISTANCE, searchParams.getSortOrder())
    }


    private fun getDummySuggestion(city: String, airport: String): SuggestionV4 {
        val suggestion = SuggestionV4()
        suggestion.gaiaId = "123"
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