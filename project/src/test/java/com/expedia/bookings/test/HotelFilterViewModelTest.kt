package com.expedia.bookings.test

import com.expedia.bookings.analytics.OmnitureTestUtils
import com.expedia.bookings.analytics.AnalyticsProvider
import com.expedia.bookings.data.hotel.DisplaySort
import com.expedia.bookings.data.hotel.UserFilterChoices
import com.expedia.bookings.data.hotels.HotelSearchResponse
import com.expedia.bookings.data.hotels.Neighborhood
import com.expedia.bookings.hotel.data.Amenity
import com.expedia.bookings.services.TestObserver
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.utils.Strings
import com.expedia.bookings.widget.GuestRatingValue
import com.expedia.bookings.widget.StarRatingValue
import com.expedia.testutils.JSONResourceReader
import com.expedia.bookings.hotel.vm.HotelFilterViewModel
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.robolectric.RuntimeEnvironment
import java.util.ArrayList
import kotlin.properties.Delegates
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@RunWith(RobolectricRunner::class)
class HotelFilterViewModelTest {

    private val linkName = "Search Results Sort"

    private var vm: HotelFilterViewModel by Delegates.notNull()
    private lateinit var mockAnalyticsProvider: AnalyticsProvider
    private lateinit var filterCountTestSubscriber: TestObserver<Int>

    @Before
    fun before() {
        val context = RuntimeEnvironment.application
        vm = HotelFilterViewModel(context)
        filterCountTestSubscriber = TestObserver()
        vm.filterCountObservable.subscribe(filterCountTestSubscriber)
        mockAnalyticsProvider = OmnitureTestUtils.setMockAnalyticsProvider()
    }

    @Test
    fun testStarRatingFilterChanged() {
        var count = 0
        assertEquals(count, vm.userFilterChoices.hotelStarRating.getStarRatingParamsAsList().count())
        StarRatingValue.values().forEach { starRatingValue ->
            assertStarRatingFilterValue(starRatingValue, false, false)
            assertStarRatingFilterValue(starRatingValue, true, false)
            assertStarRatingFilterValue(starRatingValue, false, true)
            assertStarRatingFilterValue(starRatingValue, true, true)
            count++
            assertEquals(count, vm.userFilterChoices.hotelStarRating.getStarRatingParamsAsList().count())
        }
    }

    @Test
    fun testStarRatingFilterChangedTracking() {
        StarRatingValue.values().forEach { starRatingValue ->
            assertStarRatingTracking(starRatingValue, false, false) // no tracking
            assertStarRatingTracking(starRatingValue, false, true) // do tracking
            assertStarRatingTracking(starRatingValue, true, true) // do tracking
            assertStarRatingTracking(starRatingValue, false, true) // no tracking
            assertStarRatingTracking(starRatingValue, true, false) // no tracking
            assertStarRatingTracking(starRatingValue, false, false) // no tracking
        }
    }

    @Test
    fun testGuestRatingFilterChanged() {
        assertEquals(0, vm.userFilterChoices.hotelGuestRating.getGuestRatingParamAsList().count())
        var count = GuestRatingValue.values().size
        GuestRatingValue.values().forEach { guestRatingValue ->
            vm.userFilterChoices.hotelGuestRating = UserFilterChoices.GuestRatings()
            testGuestRatingFilterValue(guestRatingValue, false, false)
            testGuestRatingFilterValue(guestRatingValue, true, false)
            testGuestRatingFilterValue(guestRatingValue, false, true)
            testGuestRatingFilterValue(guestRatingValue, true, true)

            assertEquals(count, vm.userFilterChoices.hotelGuestRating.getGuestRatingParamAsList().count())
            count--
        }
    }

    @Test
    fun testGuestRatingFilterChangedTracking() {
        GuestRatingValue.values().forEach { guestRatingValue ->
            assertGuestRatingTracking(guestRatingValue, false, false) // no tracking
            assertGuestRatingTracking(guestRatingValue, false, true) // do tracking
            assertGuestRatingTracking(guestRatingValue, true, true) // do tracking
            assertGuestRatingTracking(guestRatingValue, false, true) // no tracking
            assertGuestRatingTracking(guestRatingValue, true, false) // no tracking
            assertGuestRatingTracking(guestRatingValue, false, false) // no tracking
        }
    }

    @Test
    fun testVipFilterChanged() {
        assertVipFilterValue(false, false)
        assertVipFilterValue(true, false)
        assertVipFilterValue(false, true)
        assertVipFilterValue(true, true)
    }

    @Test
    fun testVipFilterChangedTracking() {
        assertVipFilterTracking(false, false)
        assertVipFilterTracking(true, false)
        assertVipFilterTracking(false, true)
        assertVipFilterTracking(true, true)
    }

    @Test
    fun testVipFilterChangedClearHotelNameFocus() {
        assertVipFilterClearHotelNameFocus(false, false)
        assertVipFilterClearHotelNameFocus(true, false)
        assertVipFilterClearHotelNameFocus(false, true)
        assertVipFilterClearHotelNameFocus(true, true)
    }

    @Test
    fun testPriceFilterChanged() {
        vm.onHotelPriceFilterChangedListener.onHotelPriceFilterChanged(0, 0, false)
        assertEquals(0, vm.userFilterChoices.minPrice)
        assertEquals(0, vm.userFilterChoices.maxPrice)

        vm.onHotelPriceFilterChangedListener.onHotelPriceFilterChanged(100, 10, true)
        assertEquals(100, vm.userFilterChoices.minPrice)
        assertEquals(10, vm.userFilterChoices.maxPrice)
    }

    @Test
    fun testPriceFilterChangedTracking() {
        assertPriceFilterTracking(10, 100, false, true) // no tracking
        assertPriceFilterTracking(10, 100, true, true) // do tracking
        assertPriceFilterTracking(0, 0, true, false) // no tracking
        assertPriceFilterTracking(0, 0, false, false) // no tracking
        vm.resetPriceSliderFilterTracking()
        assertPriceFilterTracking(0, 0, true, true) // do tracking
    }

    @Test
    fun testNameFilterChanged() {
        var hotelName = "Hilton"
        vm.onHotelNameFilterChangedListener.onHotelNameFilterChanged(hotelName, false)
        assertEquals(hotelName, vm.userFilterChoices.name)
        hotelName = "Paris"
        vm.onHotelNameFilterChangedListener.onHotelNameFilterChanged(hotelName, true)
        assertEquals(hotelName, vm.userFilterChoices.name)
    }

    @Test
    fun testNameFilterChangedTracking() {
        assertNameFilterTracking("", false, false) // no tracking
        assertNameFilterTracking("", true, false) // no tracking
        assertNameFilterTracking("Ho", false, false) // no tracking
        assertNameFilterTracking("Te", true, false) // no tracking
        assertNameFilterTracking("L", false, false) // no tracking
        assertNameFilterTracking(" ", true, false) // do tracking
        assertNameFilterTracking("~", true, true) // no tracking
        assertNameFilterTracking("", true, true) // no tracking
        assertNameFilterTracking("~", true, false) // do tracking
    }

    @Test
    fun testAmenityFilterChanged() {
        val amenities = Amenity.getFilterAmenities().toMutableList()
        amenities.add(Amenity.KITCHEN)
        var count = 0
        assertEquals(count, vm.userFilterChoices.amenities.count())
        amenities.forEach { amenity ->
            assertAmenityFilterValue(amenity, false, false)
            assertAmenityFilterValue(amenity, true, false)
            assertAmenityFilterValue(amenity, false, true)
            assertAmenityFilterValue(amenity, true, true)
            count++
            assertEquals(count, vm.userFilterChoices.amenities.count())
        }
    }

    @Test
    fun testAmenityFilterChangedTracking() {
        val amenities = Amenity.getFilterAmenities().toMutableList()
        amenities.add(Amenity.KITCHEN)
        amenities.forEach { amenity ->
            assertAmenityFilterTracking(amenity, false, false)
            assertAmenityFilterTracking(amenity, true, false)
            assertAmenityFilterTracking(amenity, false, true)
            assertAmenityFilterTracking(amenity, true, true)
        }
    }

    @Test
    fun testNeighborhoodFilterChanged() {
        val neighborhoods = arrayOf(createNeighborhood("name1", "id1"),
                createNeighborhood(" ", " "),
                createNeighborhood("", ""))

        assertEquals(0, vm.userFilterChoices.neighborhoods.count())
        neighborhoods.forEach { neighborhood ->
            assertNeighborhoodFilterValue(neighborhood, false, false)
            assertNeighborhoodFilterValue(neighborhood, true, false)
            assertEquals(1, vm.userFilterChoices.neighborhoods.count())
            assertNeighborhoodFilterValue(neighborhood, false, true)
            assertEquals(0, vm.userFilterChoices.neighborhoods.count())
            assertNeighborhoodFilterValue(neighborhood, true, true)
            assertEquals(1, vm.userFilterChoices.neighborhoods.count())
        }
    }

    @Test
    fun testNeighborhoodFilterChangedTracking() {
        val neighborhoods = arrayOf(createNeighborhood("name1", "id1"),
                createNeighborhood(" ", " "),
                createNeighborhood("", ""))

        neighborhoods.forEach { neighborhood ->
            assertNeighborhoodFilterTracking(neighborhood, false, false)
            assertNeighborhoodFilterTracking(neighborhood, true, false)
            assertNeighborhoodFilterTracking(neighborhood, false, true)
            assertNeighborhoodFilterTracking(neighborhood, true, true)
        }
    }

    @Test
    fun testSortChanged() {
        assertSortValue(DisplaySort.getDefaultSort(), false)
        assertSortValue(DisplaySort.getDefaultSort(), true)

        DisplaySort.values().forEach { displaySort ->
            assertSortValue(displaySort, false)
            assertSortValue(displaySort, true)
        }
    }

    @Test
    fun testSortChangedTracking() {
        DisplaySort.values().forEach { displaySort ->
            assertSortTracking(displaySort, false)
            assertSortTracking(displaySort, true)
        }
    }

    @Test
    fun setSortAndAllFilters() {
        val hotelName = "Hilton"
        vm.onHotelNameFilterChangedListener.onHotelNameFilterChanged(hotelName, false)
        vm.onHotelVipFilterChangedListener.onHotelVipFilterChanged(true, false)
        vm.onHotelStarRatingFilterChangedListener.onHotelStarRatingFilterChanged(StarRatingValue.One, true, false)
        vm.onHotelGuestRatingFilterChangedListener.onHotelGuestRatingFilterChanged(GuestRatingValue.Three, true, false)
        vm.onHotelPriceFilterChangedListener.onHotelPriceFilterChanged(20, 50, false)
        val neighborhood = createNeighborhood("Civic", "Civic Id")
        vm.onHotelNeighborhoodFilterChangedListener.onHotelNeighborhoodFilterChanged(neighborhood, true, false)
        vm.onHotelAmenityFilterChangedListener.onHotelAmenityFilterChanged(Amenity.PETS, true, false)
        vm.onHotelSortChangedListener.onHotelSortChanged(DisplaySort.RATING, false)

        assertEquals(hotelName, vm.userFilterChoices.name)
        assertEquals(true, vm.userFilterChoices.hotelStarRating.one)
        assertEquals(true, vm.userFilterChoices.hotelGuestRating.three)
        assertEquals(true, vm.userFilterChoices.isVipOnlyAccess)
        assertTrue(vm.userFilterChoices.neighborhoods.contains(neighborhood))
        assertEquals(20, vm.userFilterChoices.minPrice)
        assertEquals(50, vm.userFilterChoices.maxPrice)
        assertEquals(1, vm.userFilterChoices.amenities.size)
        assertTrue(vm.userFilterChoices.amenities.contains(17))
        assertEquals(DisplaySort.RATING, vm.userFilterChoices.userSort)
    }

    @Test
    fun clearFilters() {
        val hotelName = "Marriott"
        vm.onHotelNameFilterChangedListener.onHotelNameFilterChanged(hotelName, false)
        vm.onHotelVipFilterChangedListener.onHotelVipFilterChanged(true, false)
        vm.onHotelStarRatingFilterChangedListener.onHotelStarRatingFilterChanged(StarRatingValue.Two, true, false)
        vm.onHotelGuestRatingFilterChangedListener.onHotelGuestRatingFilterChanged(GuestRatingValue.Four, true, false)
        vm.onHotelPriceFilterChangedListener.onHotelPriceFilterChanged(10, 60, false)
        val neighborhood = createNeighborhood("Accord", "Accord Id")
        vm.onHotelNeighborhoodFilterChangedListener.onHotelNeighborhoodFilterChanged(neighborhood, true, false)
        vm.onHotelAmenityFilterChangedListener.onHotelAmenityFilterChanged(Amenity.POOL, true, false)
        vm.onHotelSortChangedListener.onHotelSortChanged(DisplaySort.PRICE, false)

        filterCountTestSubscriber.values().clear()

        vm.clearObservable.onNext(Unit)

        assertFilterCount(0)
        assertTrue(vm.userFilterChoices.name.isBlank())
        assertEquals(false, vm.userFilterChoices.isVipOnlyAccess)
        assertTrue(vm.userFilterChoices.neighborhoods.isEmpty())
        assertEquals(false, vm.userFilterChoices.hotelStarRating.two)
        assertEquals(false, vm.userFilterChoices.hotelGuestRating.four)
        assertEquals(0, vm.userFilterChoices.minPrice)
        assertEquals(0, vm.userFilterChoices.maxPrice)
        assertTrue(vm.userFilterChoices.amenities.isEmpty())
        assertEquals(DisplaySort.getDefaultSort(), vm.userFilterChoices.userSort)
    }

    @Test
    fun emptyFilters() {
        vm.filterCountObservable.onNext(vm.userFilterChoices.filterCount())
        assertFilterCount(0)
    }

    @Test
    fun filterCount() {
        vm.onHotelNameFilterChangedListener.onHotelNameFilterChanged("Hyatt", false)
        assertFilterCount(1)

        vm.onHotelVipFilterChangedListener.onHotelVipFilterChanged(true, false)
        assertFilterCount(2)

        vm.onHotelStarRatingFilterChangedListener.onHotelStarRatingFilterChanged(StarRatingValue.Three, true, false)
        assertFilterCount(3)

        vm.onHotelStarRatingFilterChangedListener.onHotelStarRatingFilterChanged(StarRatingValue.Five, true, false)
        assertFilterCount(4)

        vm.onHotelGuestRatingFilterChangedListener.onHotelGuestRatingFilterChanged(GuestRatingValue.Five, true, false)
        assertFilterCount(5)

        vm.onHotelPriceFilterChangedListener.onHotelPriceFilterChanged(1, 0, false)
        assertFilterCount(6)

        vm.onHotelPriceFilterChangedListener.onHotelPriceFilterChanged(1, 2, false)
        assertFilterCount(6)

        val neighborhood1 = createNeighborhood("Fit", "Fit Id")
        vm.onHotelNeighborhoodFilterChangedListener.onHotelNeighborhoodFilterChanged(neighborhood1,
                true, false)
        assertFilterCount(7)

        val neighborhood2 = createNeighborhood("CR-V", "CR-V Id")
        vm.onHotelNeighborhoodFilterChangedListener.onHotelNeighborhoodFilterChanged(neighborhood2,
                true, false)
        assertFilterCount(7)

        vm.onHotelAmenityFilterChangedListener.onHotelAmenityFilterChanged(Amenity.BREAKFAST, true, false)
        assertFilterCount(8)

        vm.onHotelAmenityFilterChangedListener.onHotelAmenityFilterChanged(Amenity.INTERNET, true, false)
        assertFilterCount(9)

        vm.onHotelSortChangedListener.onHotelSortChanged(DisplaySort.DISTANCE, false)
        assertEquals(9, vm.userFilterChoices.filterCount())
        filterCountTestSubscriber.assertEmpty()

        vm.onHotelNameFilterChangedListener.onHotelNameFilterChanged("", false)
        assertFilterCount(8)

        vm.onHotelVipFilterChangedListener.onHotelVipFilterChanged(false, false)
        assertFilterCount(7)

        vm.onHotelStarRatingFilterChangedListener.onHotelStarRatingFilterChanged(StarRatingValue.Three, false, false)
        assertFilterCount(6)

        vm.onHotelStarRatingFilterChangedListener.onHotelStarRatingFilterChanged(StarRatingValue.Five, false, false)
        assertFilterCount(5)

        vm.onHotelGuestRatingFilterChangedListener.onHotelGuestRatingFilterChanged(GuestRatingValue.Five, false, false)
        assertFilterCount(4)

        vm.onHotelPriceFilterChangedListener.onHotelPriceFilterChanged(0, 0, false)
        assertFilterCount(3)

        vm.onHotelNeighborhoodFilterChangedListener.onHotelNeighborhoodFilterChanged(neighborhood1,
                false, false)
        assertFilterCount(3)

        vm.onHotelNeighborhoodFilterChangedListener.onHotelNeighborhoodFilterChanged(neighborhood2,
                false, false)
        assertFilterCount(2)

        vm.onHotelAmenityFilterChangedListener.onHotelAmenityFilterChanged(Amenity.BREAKFAST, false, false)
        assertFilterCount(1)

        vm.onHotelAmenityFilterChangedListener.onHotelAmenityFilterChanged(Amenity.INTERNET, false, false)
        assertFilterCount(0)

        vm.onHotelSortChangedListener.onHotelSortChanged(DisplaySort.getDefaultSort(), false)
        assertEquals(0, vm.userFilterChoices.filterCount())
        filterCountTestSubscriber.assertEmpty()
    }

    @Test
    fun testNoNeighborhoods() {
        assertFalse(vm.neighborhoodsExist)
        vm.neighborhoodsExist = true
        // do we need this assert true??
        assertTrue(vm.neighborhoodsExist)
        vm.setHotelList(generateNoNeighborhoodSearch())
        assertFalse(vm.neighborhoodsExist)
    }

    @Test
    fun testNotShowingSearchedNeighborhood() {
        var neighborhoodList: List<Neighborhood> = ArrayList()
        vm.neighborhoodListObservable.subscribe { neighborhood ->
            neighborhoodList = neighborhood
        }

        vm.setHotelList(generateHotelSearchResponse())
        assertEquals(8, neighborhoodList.size)

        vm.setSearchLocationId("6139039")
        vm.setHotelList(generateHotelSearchResponse())
        assertEquals(7, neighborhoodList.size)
    }

    private fun assertFilterCount(filterCount: Int) {
        assertEquals(filterCount, vm.userFilterChoices.filterCount())
        filterCountTestSubscriber.assertValuesAndClear(filterCount)
    }

    private fun assertTracking(isTracking: Boolean, rfrrId: String) {
        if (isTracking) {
            OmnitureTestUtils.assertLinkTracked(linkName,
                    rfrrId,
                    mockAnalyticsProvider)
        } else {
            OmnitureTestUtils.assertLinkNotTracked(linkName,
                    rfrrId,
                    mockAnalyticsProvider)
        }
        Mockito.reset(mockAnalyticsProvider)
    }

    private fun assertStarRatingFilterValue(starRatingValue: StarRatingValue, selected: Boolean, doTracking: Boolean) {
        vm.onHotelStarRatingFilterChangedListener.onHotelStarRatingFilterChanged(starRatingValue, selected, doTracking)
        when (starRatingValue) {
            StarRatingValue.One -> {
                assertEquals(selected, vm.userFilterChoices.hotelStarRating.one)
            }
            StarRatingValue.Two -> {
                assertEquals(selected, vm.userFilterChoices.hotelStarRating.two)
            }
            StarRatingValue.Three -> {
                assertEquals(selected, vm.userFilterChoices.hotelStarRating.three)
            }
            StarRatingValue.Four -> {
                assertEquals(selected, vm.userFilterChoices.hotelStarRating.four)
            }
            StarRatingValue.Five -> {
                assertEquals(selected, vm.userFilterChoices.hotelStarRating.five)
            }
        }
    }

    private fun assertStarRatingTracking(starRatingValue: StarRatingValue, selected: Boolean, doTracking: Boolean) {
        val oldValue = when (starRatingValue) {
            StarRatingValue.One -> {
                vm.userFilterChoices.hotelStarRating.one
            }
            StarRatingValue.Two -> {
                vm.userFilterChoices.hotelStarRating.two
            }
            StarRatingValue.Three -> {
                vm.userFilterChoices.hotelStarRating.three
            }
            StarRatingValue.Four -> {
                vm.userFilterChoices.hotelStarRating.four
            }
            StarRatingValue.Five -> {
                vm.userFilterChoices.hotelStarRating.five
            }
        }
        vm.onHotelStarRatingFilterChangedListener.onHotelStarRatingFilterChanged(starRatingValue, selected, doTracking)

        val rfrrId = "App.Hotels.Search.Filter." + starRatingValue.trackingString + "Star"
        assertTracking(doTracking && !oldValue, rfrrId)
    }

    private fun testGuestRatingFilterValue(guestRatingValue: GuestRatingValue, selected: Boolean, doTracking: Boolean) {
        vm.onHotelGuestRatingFilterChangedListener.onHotelGuestRatingFilterChanged(guestRatingValue, selected, doTracking)
        when (guestRatingValue) {
            GuestRatingValue.Three -> {
                assertEquals(selected, vm.userFilterChoices.hotelGuestRating.three)
            }
            GuestRatingValue.Four -> {
                assertEquals(selected, vm.userFilterChoices.hotelGuestRating.four)
            }
            GuestRatingValue.Five -> {
                assertEquals(selected, vm.userFilterChoices.hotelGuestRating.five)
            }
        }
    }

    private fun assertGuestRatingTracking(guestRatingValue: GuestRatingValue, selected: Boolean, doTracking: Boolean) {
        val oldValue = when (guestRatingValue) {
            GuestRatingValue.Three -> {
                vm.userFilterChoices.hotelGuestRating.three
            }
            GuestRatingValue.Four -> {
                vm.userFilterChoices.hotelGuestRating.four
            }
            GuestRatingValue.Five -> {
                vm.userFilterChoices.hotelGuestRating.five
            }
        }
        vm.onHotelGuestRatingFilterChangedListener.onHotelGuestRatingFilterChanged(guestRatingValue, selected, doTracking)

        val rfrrId = "App.Hotels.Search.Filter.GuestRating" + guestRatingValue.trackingString
        assertTracking(doTracking && !oldValue, rfrrId)
    }

    private fun assertVipFilterValue(selected: Boolean, doTracking: Boolean) {
        vm.onHotelVipFilterChangedListener.onHotelVipFilterChanged(selected, doTracking)

        assertEquals(selected, vm.userFilterChoices.isVipOnlyAccess)
    }

    private fun assertVipFilterClearHotelNameFocus(selected: Boolean, doTracking: Boolean) {
        val testClearHotelNameFocusObservable = TestObserver<Unit>()
        vm.clearHotelNameFocusObservable.subscribe(testClearHotelNameFocusObservable)

        vm.onHotelVipFilterChangedListener.onHotelVipFilterChanged(selected, doTracking)

        testClearHotelNameFocusObservable.assertValuesAndClear(Unit)
    }

    private fun assertVipFilterTracking(selected: Boolean, doTracking: Boolean) {
        vm.onHotelVipFilterChangedListener.onHotelVipFilterChanged(selected, doTracking)

        val vipState = if (selected) "On" else "Off"
        val rfrrId = "App.Hotels.Search.Filter.VIP.$vipState"
        assertTracking(doTracking, rfrrId)
    }

    private fun assertPriceFilterTracking(minPrice: Int, maxPrice: Int, doTracking: Boolean, shouldTrackFilterPriceSlider: Boolean) {
        vm.onHotelPriceFilterChangedListener.onHotelPriceFilterChanged(minPrice, maxPrice, doTracking)

        val rfrrId = "App.Hotels.Search.Price"
        assertTracking(doTracking && shouldTrackFilterPriceSlider, rfrrId)
    }

    private fun assertNameFilterTracking(hotelName: String, doTracking: Boolean, trackingDone: Boolean) {
        vm.onHotelNameFilterChangedListener.onHotelNameFilterChanged(hotelName, doTracking)

        val rfrrId = "App.Hotels.Search.HotelName"
        assertTracking(doTracking && hotelName.length == 1 && !trackingDone, rfrrId)
    }

    private fun assertAmenityFilterValue(amenity: Amenity, selected: Boolean, doTracking: Boolean) {
        vm.onHotelAmenityFilterChangedListener.onHotelAmenityFilterChanged(amenity, selected, doTracking)
        val id = Amenity.getSearchKey(amenity)
        if (selected) {
            assertTrue(vm.userFilterChoices.amenities.contains(id))
        } else {
            assertFalse(vm.userFilterChoices.amenities.contains(id))
        }
    }

    private fun assertAmenityFilterTracking(amenity: Amenity, selected: Boolean, doTracking: Boolean) {
        vm.onHotelAmenityFilterChangedListener.onHotelAmenityFilterChanged(amenity, selected, doTracking)

        val rfrrId = "App.Hotels.Search.Filter.$amenity"
        assertTracking(doTracking, rfrrId)
    }

    private fun assertNeighborhoodFilterValue(neighborhood: Neighborhood, selected: Boolean, doTracking: Boolean) {
        vm.onHotelNeighborhoodFilterChangedListener.onHotelNeighborhoodFilterChanged(neighborhood, selected, doTracking)
        if (selected) {
            assertTrue(vm.userFilterChoices.neighborhoods.contains(neighborhood))
        } else {
            assertFalse(vm.userFilterChoices.neighborhoods.contains(neighborhood))
        }
    }

    private fun assertNeighborhoodFilterTracking(neighborhood: Neighborhood, selected: Boolean, doTracking: Boolean) {
        vm.onHotelNeighborhoodFilterChangedListener.onHotelNeighborhoodFilterChanged(neighborhood, selected, doTracking)

        val rfrrId = "App.Hotels.Search.Neighborhood"
        assertTracking(doTracking && selected, rfrrId)
    }

    private fun assertSortValue(displaySort: DisplaySort, doTracking: Boolean) {
        vm.onHotelSortChangedListener.onHotelSortChanged(displaySort, doTracking)

        assertEquals(displaySort, vm.userFilterChoices.userSort)
    }

    private fun assertSortTracking(displaySort: DisplaySort, doTracking: Boolean) {
        vm.onHotelSortChangedListener.onHotelSortChanged(displaySort, doTracking)

        val sortByString = if (displaySort == DisplaySort.PACKAGE_DISCOUNT) {
            "Discounts"
        } else {
            Strings.capitalizeFirstLetter(displaySort.toString())
        }
        val rfrrId = "App.Hotels.Search.Sort.$sortByString"
        assertTracking(doTracking, rfrrId)
    }

    private fun createNeighborhood(name: String, id: String): Neighborhood {
        return Neighborhood().apply {
            this.name = name
            this.id = id
        }
    }

    private fun generateNoNeighborhoodSearch(): HotelSearchResponse {
        val resourceReader = JSONResourceReader("src/test/resources/raw/hotel/no_neighborhood_search_response.json")
        val searchResponse = resourceReader.constructUsingGson(HotelSearchResponse::class.java)
        return searchResponse
    }

    private fun generateHotelSearchResponse(): HotelSearchResponse {
        val resourceReader = JSONResourceReader("../lib/mocked/templates/m/api/hotel/search/happy.json")
        val response = resourceReader.constructUsingGson(HotelSearchResponse::class.java)
        return response
    }
}
