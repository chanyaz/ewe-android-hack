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
    fun testSortDefault() {
        val searchParams = testParamBuilder.build()
        assertEquals(HotelSearchParams.SortType.EXPERT_PICKS, searchParams.getSortOrder())
    }

    @Test
    fun testSortWithUserPreference() {
        testParamBuilder.userSort(HotelSearchParams.SortType.REVIEWS)
        val searchParams = testParamBuilder.build()

        assertEquals(HotelSearchParams.SortType.REVIEWS, searchParams.getSortOrder())

        searchParams.sortType = "Price"
        assertEquals(HotelSearchParams.SortType.REVIEWS, searchParams.getSortOrder())
    }

    @Test
    fun testSortTypesMatchAPIExpectations() {
        assertEquals("ExpertPicks", HotelSearchParams.SortType.EXPERT_PICKS.sortName)
        assertEquals("StarRatingDesc", HotelSearchParams.SortType.STARS.sortName)
        assertEquals("PriceAsc", HotelSearchParams.SortType.PRICE.sortName)
        assertEquals("Reviews", HotelSearchParams.SortType.REVIEWS.sortName)
        assertEquals("Distance", HotelSearchParams.SortType.DISTANCE.sortName)
        assertEquals("Deals", HotelSearchParams.SortType.MOBILE_DEALS.sortName)
    }

    @Test
    fun testSortWithSortType() {
        val searchParams = testParamBuilder.build()
        searchParams.sortType = "Price"
        assertEquals(HotelSearchParams.SortType.PRICE, searchParams.getSortOrder())
    }

    @Test
    fun testCurrentLocationSortType() {
        val paramBuilder = HotelSearchParams.Builder(maxStay, maxRange)
        val currentLocation = getDummySuggestion("ChiTown", "CHI")
        currentLocation.gaiaId = ""
        paramBuilder
                .destination(currentLocation)
                .startDate(tomorrow)
                .endDate(checkoutDate)
        val searchParams = paramBuilder.build()
        assertEquals(HotelSearchParams.SortType.DISTANCE, searchParams.getSortOrder())
    }

    @Test
    fun testDestinationDeepCopy() {
        val builder = HotelSearchParams.Builder(maxStay, maxRange)
        val originalSuggestion = getDummySuggestion("chicago", "CHI")
        originalSuggestion.hotelId = "12345"
        val params = builder.destination(originalSuggestion).startDate(tomorrow).endDate(checkoutDate).build() as HotelSearchParams
        params?.clearPinnedHotelId()
        val newBuilder = HotelSearchParams.Builder(maxStay, maxRange)
                .destination(originalSuggestion)
                .startDate(params?.checkIn)
                .endDate(params?.checkOut) as HotelSearchParams.Builder

        assertTrue(newBuilder.build().isPinnedSearch())
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