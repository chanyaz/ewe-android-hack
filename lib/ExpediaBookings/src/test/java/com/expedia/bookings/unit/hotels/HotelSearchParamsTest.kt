package com.expedia.bookings.unit.hotels

import com.expedia.bookings.data.BaseHotelFilterOptions
import com.expedia.bookings.data.SuggestionV4
import com.expedia.bookings.data.hotels.HotelFilterOptions
import com.expedia.bookings.data.hotels.HotelSearchParams
import com.expedia.bookings.data.hotels.Neighborhood
import com.expedia.bookings.data.hotels.convertPackageToSearchParams
import com.expedia.bookings.data.packages.PackageHotelFilterOptions
import com.expedia.bookings.data.packages.PackageSearchParams
import org.joda.time.LocalDate
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class HotelSearchParamsTest {
    private val maxStay = 26
    private val maxRange = 329
    private val testParamBuilder = HotelSearchParams.Builder(maxStay, maxRange)

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
                .guestRatings(listOf(3, 4, 5))
                .priceRange(HotelSearchParams.PriceRange(10, 30))
                .amenities(hashSetOf(4, 16))

        val searchParams = testParamBuilder.build()
        val map = searchParams.filterOptions!!.getFiltersQueryMap()
        assertEquals(5, map.size)
        assertEquals(name, map["filterHotelName"])
        assertEquals("10,20", map["filterStarRatings"])
        assertEquals("10,30", map["filterPrice"])
        assertEquals("4,16", map["filterAmenities"])
        assertEquals("3,4,5", map["guestRatingFilterItems"])
    }

    @Test
    fun testSortDefault() {
        val searchParams = testParamBuilder.build()
        assertEquals(BaseHotelFilterOptions.SortType.EXPERT_PICKS, searchParams.getSortOrder())
    }

    @Test
    fun testSortWithUserPreference() {
        testParamBuilder.userSort(BaseHotelFilterOptions.SortType.REVIEWS)
        val searchParams = testParamBuilder.build()

        assertEquals(BaseHotelFilterOptions.SortType.REVIEWS, searchParams.getSortOrder())

        searchParams.sortType = "Price"
        assertEquals(BaseHotelFilterOptions.SortType.REVIEWS, searchParams.getSortOrder())
    }

    @Test
    fun testSortTypesMatchAPIExpectations() {
        assertEquals("ExpertPicks", BaseHotelFilterOptions.SortType.EXPERT_PICKS.sortName)
        assertEquals("StarRatingDesc", BaseHotelFilterOptions.SortType.STARS.sortName)
        assertEquals("PriceAsc", BaseHotelFilterOptions.SortType.PRICE.sortName)
        assertEquals("Reviews", BaseHotelFilterOptions.SortType.REVIEWS.sortName)
        assertEquals("Distance", BaseHotelFilterOptions.SortType.DISTANCE.sortName)
        assertEquals("Deals", BaseHotelFilterOptions.SortType.MOBILE_DEALS.sortName)
    }

    @Test
    fun testSortWithSortType() {
        val searchParams = testParamBuilder.build()
        searchParams.sortType = "Price"
        assertEquals(BaseHotelFilterOptions.SortType.PRICE, searchParams.getSortOrder())
        searchParams.sortType = "Discounts"
        assertEquals(BaseHotelFilterOptions.SortType.MOBILE_DEALS, searchParams.getSortOrder())
        searchParams.sortType = "Deals"
        assertEquals(BaseHotelFilterOptions.SortType.MOBILE_DEALS, searchParams.getSortOrder())
        searchParams.sortType = "Rating"
        assertEquals(BaseHotelFilterOptions.SortType.REVIEWS, searchParams.getSortOrder())
        searchParams.sortType = "guestRating"
        assertEquals(BaseHotelFilterOptions.SortType.REVIEWS, searchParams.getSortOrder())
        searchParams.sortType = ""
        assertEquals(BaseHotelFilterOptions.SortType.EXPERT_PICKS, searchParams.getSortOrder())
        searchParams.sortType = null
        assertEquals(BaseHotelFilterOptions.SortType.EXPERT_PICKS, searchParams.getSortOrder())
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
        assertEquals(BaseHotelFilterOptions.SortType.DISTANCE, searchParams.getSortOrder())
    }

    @Test
    fun testDestinationDeepCopy() {
        val builder = HotelSearchParams.Builder(maxStay, maxRange)
        val originalSuggestion = getDummySuggestion("chicago", "CHI")
        originalSuggestion.hotelId = "12345"
        val params = builder.destination(originalSuggestion).startDate(tomorrow).endDate(checkoutDate).build() as HotelSearchParams
        params.clearPinnedHotelId()
        val newBuilder = HotelSearchParams.Builder(maxStay, maxRange)
                .destination(originalSuggestion)
                .startDate(params.checkIn)
                .endDate(params.checkOut) as HotelSearchParams.Builder

        assertTrue(newBuilder.build().isPinnedSearch())
    }

    @Test
    fun testWithinDateRange() {
        val builder = HotelSearchParams.Builder(maxStay, maxRange)
        val endDate = LocalDate.now().plusDays(maxRange)
        builder.endDate(endDate)
        assertTrue(builder.isWithinDateRange())
    }

    @Test
    fun testBeyondDateRange() {
        val builder = HotelSearchParams.Builder(maxStay, maxRange)
        val endDate = LocalDate.now().plusDays(maxRange + 2)
        builder.endDate(endDate)
        assertFalse(builder.isWithinDateRange())
    }

    @Test
    fun testWithinDateRangeAtMaxPlusOne() {
        val builder = HotelSearchParams.Builder(maxStay, maxRange)
        val endDate = LocalDate.now().plusDays(maxRange + 1)
        builder.endDate(endDate)
        assertTrue(builder.isWithinDateRange())
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
                .children(listOf(1, 2, 1))
        secondParamBuilder.destination(dummySuggestion).startDate(today).endDate(today)
                .children(listOf(1, 1, 1))

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

        val newFirstParams = firstParamBuilder.hotelName("ADVANCED_OPTION").build()
        val newSecondParams = HotelSearchParams.Builder(maxStay, maxRange)
                .destination(dummySuggestion).startDate(today).endDate(today)
                .build() as HotelSearchParams
        assertTrue(newSecondParams.equalForPrefetch(newSecondParams)) //sanity check
        assertFalse(newFirstParams.equalForPrefetch(newSecondParams), "Error: If either param has any filter value not equal for greedy")
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
    fun testEqualIgnoringFilterDifferentSuggestion() {
        val builder = HotelSearchParams.Builder(maxStay, maxRange)
        val params1 = builder.destination(getDummySuggestion("chicago", "Chi", "gaia1"))
                .startDate(today).endDate(today.plusDays(1))
                .build() as HotelSearchParams
        val params2 = builder.destination(getDummySuggestion("chicago", "Chi", "gaia2"))
                .startDate(today).endDate(today.plusDays(1))
                .build() as HotelSearchParams

        assertFalse(params1.equalIgnoringFilter(params2))
    }

    @Test
    fun testEqualIgnoringFilterDifferentStartDate() {
        firstParamBuilder.destination(dummySuggestion).startDate(today).endDate(today)
        secondParamBuilder.destination(dummySuggestion).startDate(today.plusDays(1)).endDate(today)

        val params1 = firstParamBuilder.build()
        val params2 = secondParamBuilder.build()
        assertFalse(params1.equalIgnoringFilter(params2))
    }

    @Test
    fun testEqualIgnoringFilterDifferentEndDate() {
        firstParamBuilder.destination(dummySuggestion).startDate(today).endDate(today.plusDays(1))
        secondParamBuilder.destination(dummySuggestion).startDate(today).endDate(today.plusDays(2))

        val params1 = firstParamBuilder.build()
        val params2 = secondParamBuilder.build()
        assertFalse(params1.equalIgnoringFilter(params2))
    }

    @Test
    fun testEqualIgnoringFilterDifferentAdults() {
        firstParamBuilder.destination(dummySuggestion).startDate(today).endDate(today)
                .adults(1)
        secondParamBuilder.destination(dummySuggestion).startDate(today).endDate(today)
                .adults(2)

        val params1 = firstParamBuilder.build()
        val params2 = secondParamBuilder.build()
        assertFalse(params1.equalIgnoringFilter(params2))
    }

    @Test
    fun testEqualIgnoringFilterDifferentChildren() {
        firstParamBuilder.destination(dummySuggestion).startDate(today).endDate(today)
                .children(listOf(1, 2, 1))
        secondParamBuilder.destination(dummySuggestion).startDate(today).endDate(today)
                .children(listOf(1, 1, 1))

        val params1 = firstParamBuilder.build()
        val params2 = secondParamBuilder.build()
        assertFalse(params1.equalIgnoringFilter(params2))
    }

    @Test
    fun testEqualIgnoringFilterDifferentSwp() {
        firstParamBuilder.shopWithPoints(false).destination(dummySuggestion).startDate(today).endDate(today)
        secondParamBuilder.shopWithPoints(true).destination(dummySuggestion).startDate(today).endDate(today)

        val params1 = firstParamBuilder.build()
        val params2 = secondParamBuilder.build()
        assertFalse(params1.equalIgnoringFilter(params2))
    }

    @Test
    fun testEqualIgnoringFilterDifferentFilterOptions() {
        firstParamBuilder.destination(dummySuggestion).startDate(today).endDate(today)
        secondParamBuilder.userSort(BaseHotelFilterOptions.SortType.STARS).destination(dummySuggestion).startDate(today).endDate(today)

        val params1 = firstParamBuilder.build()
        val params2 = secondParamBuilder.build()
        assertTrue(params1.equalIgnoringFilter(params2))
    }

    @Test
    fun testEqualIgnoringFilterSameParams() {
        val firstParams = firstParamBuilder.destination(dummySuggestion).startDate(today).endDate(today)
                .adults(1).children(listOf(1)).build() as HotelSearchParams
        val secondParams = secondParamBuilder.destination(dummySuggestion).startDate(today).endDate(today)
                .adults(1).children(listOf(1)).build() as HotelSearchParams

        assertTrue(firstParams.equalIgnoringFilter(secondParams))
    }

    @Test
    fun testEqualIgnoringFilterNullParams() {
        val firstParams = firstParamBuilder.destination(dummySuggestion).startDate(today).endDate(today)
                .adults(1).children(listOf(1)).build() as HotelSearchParams

        assertFalse(firstParams.equalIgnoringFilter(null))
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
        val testFilterOptions = HotelFilterOptions()
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

        val testFilterOptions = HotelFilterOptions()
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
        val testFilterOptions = HotelFilterOptions()
        testFilterOptions.filterPrice = expectedPriceRange

        testParams.filterOptions = testFilterOptions

        val fromParams = secondParamBuilder.from(testParams).build()
        assertEquals(expectedPriceRange.minPrice, fromParams.filterOptions?.filterPrice?.minPrice)
        assertEquals(expectedPriceRange.maxPrice, fromParams.filterOptions?.filterPrice?.maxPrice)
    }

    @Test
    fun testFromFilterOptions_neighborhood() {
        val expectedNeighborhood = Neighborhood()
        expectedNeighborhood.name = "name"
        expectedNeighborhood.id = "12345"
        val testParams = firstParamBuilder.destination(dummySuggestion).startDate(today).endDate(today)
                .adults(1).children(listOf(1)).build() as HotelSearchParams

        val testFilterOptions = HotelFilterOptions()
        testFilterOptions.filterByNeighborhood = expectedNeighborhood
        testParams.filterOptions = testFilterOptions

        val fromParams = secondParamBuilder.from(testParams).build()
        assertEquals(expectedNeighborhood, fromParams.filterOptions?.filterByNeighborhood)
    }

    @Test
    fun testFromFilterOptions_filterVip() {
        val testParams = firstParamBuilder.destination(dummySuggestion).startDate(today).endDate(today)
                .adults(1).children(listOf(1)).build() as HotelSearchParams

        val testFilterOptions = HotelFilterOptions()
        testFilterOptions.filterVipOnly = true
        testParams.filterOptions = testFilterOptions

        val fromParams = secondParamBuilder.from(testParams).build()
        assertTrue(fromParams.filterOptions!!.filterVipOnly)
    }

    @Test
    fun testFromFilterOptions_sort() {
        val testParams = firstParamBuilder.destination(dummySuggestion).startDate(today).endDate(today)
                .adults(1).children(listOf(1)).build() as HotelSearchParams
        val testFilterOptions = HotelFilterOptions()
        testFilterOptions.userSort = BaseHotelFilterOptions.SortType.PRICE

        testParams.filterOptions = testFilterOptions

        val fromParams = secondParamBuilder.from(testParams).build()
        assertEquals(BaseHotelFilterOptions.SortType.PRICE, fromParams.filterOptions?.userSort)
    }

    @Test
    fun testAddPackageFilterParamsHappy() {
        val packageParams = getPackageParamsWithFilters()
        val hotelParams = convertPackageToSearchParams(packageParams, 26, 329)
        assertPackageAndHotelFilterOptionsEqual(packageParams, hotelParams)
        assertEquals(packageParams.filterOptions!!.filterHotelName, "Test_Hotel")
        assertEquals(packageParams.filterOptions!!.filterStarRatings, listOf(2))
        assertEquals(packageParams.filterOptions!!.filterVipOnly, true)
        assertEquals(packageParams.filterOptions!!.userSort, BaseHotelFilterOptions.SortType.EXPERT_PICKS)
    }

    @Test
    fun testAddPackageFilterParamsEmptyHotelName() {
        val packageParams = getPackageParamsWithEmptyHotelName()
        val hotelParams = convertPackageToSearchParams(packageParams, 26, 329)
        assertPackageAndHotelFilterOptionsEqual(packageParams, hotelParams)
        assertEquals(packageParams.filterOptions!!.filterStarRatings, listOf(2))
        assertEquals(packageParams.filterOptions!!.filterVipOnly, true)
        assertEquals(packageParams.filterOptions!!.userSort, BaseHotelFilterOptions.SortType.EXPERT_PICKS)
    }

    @Test
    fun testAddPackageFilterParamsNullHotelName() {
        val packageParams = getPackageParamsWithHotelNameNull()
        val hotelParams = convertPackageToSearchParams(packageParams, 26, 329)
        assertPackageAndHotelFilterOptionsEqual(packageParams, hotelParams)
        assertNull(packageParams.filterOptions!!.filterHotelName)
        assertEquals(packageParams.filterOptions!!.filterStarRatings, listOf(2))
        assertEquals(packageParams.filterOptions!!.filterVipOnly, true)
        assertEquals(packageParams.filterOptions!!.userSort, BaseHotelFilterOptions.SortType.EXPERT_PICKS)
    }

    @Test
    fun testAddPackageFilterParamsEmptyStarRatings() {
        val packageParams = getPackageParamsWithEmptyStarRating()
        val hotelParams = convertPackageToSearchParams(packageParams, 26, 329)
        assertPackageAndHotelFilterOptionsEqual(packageParams, hotelParams)
        assertEquals(packageParams.filterOptions!!.filterHotelName, "Test_Hotel")
        assertEquals(packageParams.filterOptions!!.filterStarRatings, listOf())
        assertEquals(packageParams.filterOptions!!.filterVipOnly, true)
        assertEquals(packageParams.filterOptions!!.userSort, BaseHotelFilterOptions.SortType.EXPERT_PICKS)
    }

    @Test
    fun testAddPackageFilterParamsNullSort() {
        val packageParams = getPackageParamsWithSortingNull()
        val hotelParams = convertPackageToSearchParams(packageParams, 26, 329)
        assertPackageAndHotelFilterOptionsEqual(packageParams, hotelParams)
        assertEquals(packageParams.filterOptions!!.filterHotelName, "Test_Hotel")
        assertEquals(packageParams.filterOptions!!.filterStarRatings, listOf(2))
        assertEquals(packageParams.filterOptions!!.filterVipOnly, true)
        assertNull(packageParams.filterOptions!!.userSort)
    }

    private fun assertPackageAndHotelFilterOptionsEqual(packageParams: PackageSearchParams, hotelParams: HotelSearchParams) {
        assertEquals(packageParams.filterOptions!!.filterHotelName, hotelParams.filterOptions!!.filterHotelName)
        assertEquals(packageParams.filterOptions!!.filterStarRatings, hotelParams.filterOptions!!.filterStarRatings)
        assertEquals(packageParams.filterOptions!!.filterVipOnly, hotelParams.filterOptions!!.filterVipOnly)
        assertEquals(packageParams.filterOptions!!.userSort, hotelParams.filterOptions!!.userSort)
    }

    private fun getPackageParamsWithEmptyHotelName(): PackageSearchParams {
        val packageParamsWithFilters = getPackageParams()
        packageParamsWithFilters.filterOptions = buildFilterOptions("", listOf(2), true, BaseHotelFilterOptions.SortType.EXPERT_PICKS)
        return packageParamsWithFilters
    }

    private fun getPackageParamsWithHotelNameNull(): PackageSearchParams {
        val packageParamsWithFilters = getPackageParams()
        packageParamsWithFilters.filterOptions = buildFilterOptions(null, listOf(2), true, BaseHotelFilterOptions.SortType.EXPERT_PICKS)
        return packageParamsWithFilters
    }

    private fun getPackageParamsWithEmptyStarRating(): PackageSearchParams {
        val packageParamsWithFilters = getPackageParams()
        packageParamsWithFilters.filterOptions = buildFilterOptions("Test_Hotel", listOf(), true, BaseHotelFilterOptions.SortType.EXPERT_PICKS)
        return packageParamsWithFilters
    }

    private fun getPackageParamsWithSortingNull(): PackageSearchParams {
        val packageParamsWithFilters = getPackageParams()
        packageParamsWithFilters.filterOptions = buildFilterOptions("Test_Hotel", listOf(2), true, null)
        return packageParamsWithFilters
    }

    private fun getPackageParamsWithFilters(): PackageSearchParams {
        val packageParamsWithFilters = getPackageParams()
        packageParamsWithFilters.filterOptions = buildFilterOptions("Test_Hotel", listOf(2), true, BaseHotelFilterOptions.SortType.EXPERT_PICKS)
        return packageParamsWithFilters
    }

    private fun getPackageParams(): PackageSearchParams {
        val packageParams = PackageSearchParams.Builder(26, 329)
                .flightCabinClass("coach")
                .infantSeatingInLap(true)
                .children(listOf(16, 10, 1))
                .origin(getDummySuggestion("Seattle", "SEA"))
                .destination(getDummySuggestion("London", "LHR"))
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusDays(1)).build() as PackageSearchParams
        return packageParams
    }

    private fun buildFilterOptions(hotelName: String?, starRatings: List<Int>, vipOnly: Boolean, userSort: BaseHotelFilterOptions.SortType?): PackageHotelFilterOptions {
        var filterOptions = PackageHotelFilterOptions()
        filterOptions.filterHotelName = hotelName
        filterOptions.filterStarRatings = starRatings
        filterOptions.filterVipOnly = vipOnly
        filterOptions.userSort = userSort
        return filterOptions
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
