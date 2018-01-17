package com.expedia.bookings.unit.hotels

import com.expedia.bookings.data.SuggestionV4
import com.expedia.bookings.data.hotels.HotelSearchParams
import org.joda.time.LocalDate
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class HotelSearchParamsTest {
    val maxStay = 26
    val maxRange = 329
    val testParamBuilder = HotelSearchParams.Builder(maxStay, maxRange)

    private val firstParamBuilder = HotelSearchParams.Builder(maxStay, maxRange)
    private val secondParamBuilder = HotelSearchParams.Builder(maxStay, maxRange)
    private val dummySuggestion = getDummySuggestion("chicago", "Chi")

    private val today = LocalDate.now()
    private val tomorrow = LocalDate.now().plusDays(1)
    private val checkoutDate = tomorrow.plusDays(2)

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
                .amenities(hashSetOf(4,16))

        val searchParams = testParamBuilder.build()
        val map = searchParams.filterOptions!!.getFiltersQueryMap()
        assertEquals(4, map.size)
        assertEquals(name, map["filterHotelName"])
        assertEquals("10,20", map["filterStarRatings"])
        assertEquals("10,30", map["filterPrice"])
        assertEquals("4,16", map["filterAmenities"])
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
        searchParams.sortType = "Discounts"
        assertEquals(HotelSearchParams.SortType.MOBILE_DEALS, searchParams.getSortOrder())
        searchParams.sortType = "Deals"
        assertEquals(HotelSearchParams.SortType.MOBILE_DEALS, searchParams.getSortOrder())
        searchParams.sortType = "Rating"
        assertEquals(HotelSearchParams.SortType.REVIEWS, searchParams.getSortOrder())
        searchParams.sortType = "guestRating"
        assertEquals(HotelSearchParams.SortType.REVIEWS, searchParams.getSortOrder())
        searchParams.sortType = ""
        assertEquals(HotelSearchParams.SortType.EXPERT_PICKS, searchParams.getSortOrder())
        searchParams.sortType = null
        assertEquals(HotelSearchParams.SortType.EXPERT_PICKS, searchParams.getSortOrder())
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

    @Test
    fun testPrefetchNotEqual_Suggestion() {
        val builder = HotelSearchParams.Builder(maxStay, maxRange)
        val firstParams = builder.destination(getDummySuggestion("chicago", "Chi", "gaia1"))
                .startDate(today).endDate(today.plusDays(1))
                .build() as HotelSearchParams
        val params2 = builder.destination(getDummySuggestion("chicago", "Chi", "gaia2"))
                .startDate(today).endDate(today.plusDays(1))
                .build() as HotelSearchParams

        assertTrue(firstParams.equalForPrefetch(firstParams)) //sanity check
        assertFalse(firstParams.equalForPrefetch(params2), "Error: Different suggestions expected not equal for greedy searches")
    }

    @Test
    fun testPrefetchNotEqual_StartDates() {
        firstParamBuilder.destination(dummySuggestion).startDate(today).endDate(today)
        secondParamBuilder.destination(dummySuggestion).startDate(today.plusDays(1)).endDate(today)

        val firstParams = firstParamBuilder.build()
        assertTrue(firstParams.equalForPrefetch(firstParams)) //sanity check
        assertFalse(firstParams.equalForPrefetch(secondParamBuilder.build()), "Error: Different startDate expected not equal for greedy searches")
    }

    @Test
    fun testPrefetchNotEqual_EndDates() {
        firstParamBuilder.destination(dummySuggestion).startDate(today).endDate(today.plusDays(1))
        secondParamBuilder.destination(dummySuggestion).startDate(today).endDate(today.plusDays(2))

        val firstParams = firstParamBuilder.build()
        assertTrue(firstParams.equalForPrefetch(firstParams)) //sanity check
        assertFalse(firstParams.equalForPrefetch(secondParamBuilder.build()), "Error: Different endDate expected not equal for greedy searches")
    }

    @Test
    fun testPrefetchNotEqual_Adults() {
        firstParamBuilder.destination(dummySuggestion).startDate(today).endDate(today)
                .adults(1)
        secondParamBuilder.destination(dummySuggestion).startDate(today).endDate(today)
                .adults(2)

        val firstParams = firstParamBuilder.build()
        assertTrue(firstParams.equalForPrefetch(firstParams)) //sanity check
        assertFalse(firstParams.equalForPrefetch(secondParamBuilder.build()), "Error: Different adult counts expected not equal for greedy searches")
    }

    @Test
    fun testPrefetchNotEqual_Children() {
        firstParamBuilder.destination(dummySuggestion).startDate(today).endDate(today)
                .children(listOf(1))
        secondParamBuilder.destination(dummySuggestion).startDate(today).endDate(today)
                .children(listOf(1, 1))

        val firstParams = firstParamBuilder.build()
        assertTrue(firstParams.equalForPrefetch(firstParams)) //sanity check
        assertFalse(firstParams.equalForPrefetch(secondParamBuilder.build()), "Error: Different child counts expected not equal for greedy searches")
    }

    @Test
    fun testPrefetchNotEqual_swp() {
        firstParamBuilder.shopWithPoints(false).destination(dummySuggestion).startDate(today).endDate(today)
        secondParamBuilder.shopWithPoints(true).destination(dummySuggestion).startDate(today).endDate(today)

        val firstParams = firstParamBuilder.build()
        assertTrue(firstParams.equalForPrefetch(firstParams)) //sanity check
        assertFalse(firstParams.equalForPrefetch(secondParamBuilder.build()), "Error: SWP difference expected not equal for greedy searches")
    }

    @Test
    fun testPrefetchNotEqual_filterOptions() {
        firstParamBuilder.destination(dummySuggestion).startDate(today).endDate(today)
        secondParamBuilder.hotelName("ADVANCED_OPTION").destination(dummySuggestion).startDate(today).endDate(today)

        val firstParams = firstParamBuilder.build()
        assertTrue(firstParams.equalForPrefetch(firstParams)) //sanity check
        assertFalse(firstParams.equalForPrefetch(secondParamBuilder.build()), "Error: If either param has any filter value not equal for greedy")

        val newfirstParams = firstParamBuilder.hotelName("ADVANCED_OPTION").build()
        val newSecondParams = HotelSearchParams.Builder(maxStay, maxRange)
                .destination(dummySuggestion).startDate(today).endDate(today)
                .build() as HotelSearchParams
        assertTrue(newSecondParams.equalForPrefetch(newSecondParams)) //sanity check
        assertFalse(newfirstParams.equalForPrefetch(newSecondParams), "Error: If either param has any filter value not equal for greedy")
    }

    @Test
    fun testPrefetchEqual() {
        val firstParams = firstParamBuilder.destination(dummySuggestion).startDate(today).endDate(today)
                .adults(1).children(listOf(1)).build() as HotelSearchParams
        val secondParams = secondParamBuilder.destination(dummySuggestion).startDate(today).endDate(today)
                .adults(1).children(listOf(1)).build() as HotelSearchParams

        assertTrue(firstParams.equalForPrefetch(firstParams)) //sanity check
        assertTrue(firstParams.equalForPrefetch(secondParams))
    }

    @Test
    fun testFrom() {
        val firstParams = firstParamBuilder
                .shopWithPoints(true)
                .destination(dummySuggestion)
                .startDate(today).endDate(today)
                .adults(1).children(listOf(1)).build() as HotelSearchParams
        val secondParams = secondParamBuilder.from(firstParams).build()

        assertEquals(firstParams.suggestion, secondParams.suggestion)
        assertEquals(firstParams.checkIn, secondParams.checkIn)
        assertEquals(firstParams.checkOut, secondParams.checkOut)
        assertEquals(firstParams.adults, secondParams.adults)
        assertEquals(firstParams.children.size, secondParams.children.size)
        assertTrue(secondParams.shopWithPoints)
    }

    @Test
    fun testFromFilterOptions_name() {
        val expectedName = "Oscar Inn"
        val testParams = firstParamBuilder.destination(dummySuggestion).startDate(today).endDate(today)
                .adults(1).children(listOf(1)).build() as HotelSearchParams
        val testFilterOptions = HotelSearchParams.HotelFilterOptions()
        testFilterOptions.filterHotelName = expectedName

        testParams.filterOptions = testFilterOptions

        val fromParams = secondParamBuilder.from(testParams).build()
        assertEquals(expectedName, fromParams.filterOptions?.filterHotelName)
    }

    @Test
    fun testFromFilterOptions_star() {
        val expectedStar = listOf(1)
        val testParams = firstParamBuilder.destination(dummySuggestion).startDate(today).endDate(today)
                .adults(1).children(listOf(1)).build() as HotelSearchParams

        val testFilterOptions = HotelSearchParams.HotelFilterOptions()
        testFilterOptions.filterStarRatings = expectedStar
        testParams.filterOptions = testFilterOptions

        val fromParams = secondParamBuilder.from(testParams).build()
        assertEquals(expectedStar, fromParams.filterOptions?.filterStarRatings)
    }

    @Test
    fun testFromFilterOptions_priceRange() {
        val expectedPriceRange = HotelSearchParams.PriceRange(0, 100)
        val testParams = firstParamBuilder.destination(dummySuggestion).startDate(today).endDate(today)
                .adults(1).children(listOf(1)).build() as HotelSearchParams
        val testFilterOptions = HotelSearchParams.HotelFilterOptions()
        testFilterOptions.filterPrice = expectedPriceRange


        testParams.filterOptions = testFilterOptions

        val fromParams = secondParamBuilder.from(testParams).build()
        assertEquals(expectedPriceRange.minPrice, fromParams.filterOptions?.filterPrice?.minPrice)
        assertEquals(expectedPriceRange.maxPrice, fromParams.filterOptions?.filterPrice?.maxPrice)
    }

    @Test
    fun testFromFilterOptions_neighborhood() {
        val expectedNeighborId = "12345"
        val testParams = firstParamBuilder.destination(dummySuggestion).startDate(today).endDate(today)
                .adults(1).children(listOf(1)).build() as HotelSearchParams

        val testFilterOptions = HotelSearchParams.HotelFilterOptions()
        testFilterOptions.filterByNeighborhoodId = expectedNeighborId
        testParams.filterOptions = testFilterOptions

        val fromParams = secondParamBuilder.from(testParams).build()
        assertEquals(expectedNeighborId, fromParams.filterOptions?.filterByNeighborhoodId)
    }

    @Test
    fun testFromFilterOptions_filterVip() {
        val testParams = firstParamBuilder.destination(dummySuggestion).startDate(today).endDate(today)
                .adults(1).children(listOf(1)).build() as HotelSearchParams

        val testFilterOptions = HotelSearchParams.HotelFilterOptions()
        testFilterOptions.filterVipOnly = true
        testParams.filterOptions = testFilterOptions

        val fromParams = secondParamBuilder.from(testParams).build()
        assertTrue(fromParams.filterOptions!!.filterVipOnly)
    }

    @Test
    fun testFromFilterOptions_sort() {
        val testParams = firstParamBuilder.destination(dummySuggestion).startDate(today).endDate(today)
                .adults(1).children(listOf(1)).build() as HotelSearchParams
        val testFilterOptions = HotelSearchParams.HotelFilterOptions()
        testFilterOptions.userSort = HotelSearchParams.SortType.PRICE

        testParams.filterOptions = testFilterOptions

        val fromParams = secondParamBuilder.from(testParams).build()
        assertEquals(HotelSearchParams.SortType.PRICE, fromParams.filterOptions?.userSort)
    }

    private fun getDummySuggestion(city: String, airport: String, gaiaId: String = "123"): SuggestionV4 {
        val suggestion = SuggestionV4()
        suggestion.gaiaId = gaiaId
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
