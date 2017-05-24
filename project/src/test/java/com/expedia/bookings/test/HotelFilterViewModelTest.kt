package com.expedia.bookings.test

import com.expedia.bookings.data.hotels.HotelSearchResponse
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.testutils.JSONResourceReader
import com.expedia.vm.hotel.HotelFilterViewModel
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RuntimeEnvironment
import java.util.ArrayList
import kotlin.properties.Delegates
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@RunWith(RobolectricRunner::class)
class HotelFilterViewModelTest {
    var vm: HotelFilterViewModel by Delegates.notNull()

    @Before
    fun before() {
        val context = RuntimeEnvironment.application
        vm = HotelFilterViewModel(context)
    }

    @Test
    fun filterStars() {
        vm.userFilterChoices.hotelStarRating.one = false
        vm.oneStarFilterObserver.onNext(Unit)

        assertEquals(true, vm.userFilterChoices.hotelStarRating.one)

        vm.userFilterChoices.hotelStarRating.two = false
        vm.twoStarFilterObserver.onNext(Unit)

        assertEquals(true, vm.userFilterChoices.hotelStarRating.two)
        assertEquals(2, vm.userFilterChoices.hotelStarRating.getStarRatingParamsAsList().size)

        vm.oneStarFilterObserver.onNext(Unit)
        assertEquals(false, vm.userFilterChoices.hotelStarRating.one)
        assertEquals(1, vm.userFilterChoices.hotelStarRating.getStarRatingParamsAsList().size)
    }

    @Test
    fun filterVip() {
        vm.vipFilteredObserver.onNext(true)
        assertEquals(true, vm.userFilterChoices.isVipOnlyAccess)

        vm.vipFilteredObserver.onNext(false)
        assertEquals(false, vm.userFilterChoices.isVipOnlyAccess)
    }

    @Test
    fun filterName() {
        val str = "Hilton"
        vm.filterHotelNameObserver.onNext(str)
        assertEquals(str, vm.userFilterChoices.name)
    }

    @Test
    fun setAllFilters() {
        val str = "Hilton"
        vm.filterHotelNameObserver.onNext(str)
        vm.vipFilteredObserver.onNext(true)
        vm.userFilterChoices.hotelStarRating.one = false
        vm.oneStarFilterObserver.onNext(Unit)

        vm.userFilterChoices.minPrice = 20
        vm.userFilterChoices.maxPrice = 50

        var neighborhood = HotelSearchResponse.Neighborhood()
        neighborhood.name = "Civic Center"
        vm.selectNeighborhood.onNext(neighborhood)

        assertEquals(5, vm.userFilterChoices.filterCount())
        assertEquals(str, vm.userFilterChoices.name)
        assertEquals(true, vm.userFilterChoices.hotelStarRating.one)
        assertEquals(true, vm.userFilterChoices.isVipOnlyAccess)
        assertTrue(vm.userFilterChoices.neighborhoods.size == 1)
        assertEquals(20, vm.userFilterChoices.minPrice)
        assertEquals(50, vm.userFilterChoices.maxPrice)
    }

    @Test
    fun clearFilters() {
        val str = "Hilton"
        vm.filterHotelNameObserver.onNext(str)

        vm.userFilterChoices.hotelStarRating.one = false
        vm.oneStarFilterObserver.onNext(Unit)

        vm.vipFilteredObserver.onNext(true)

        vm.userFilterChoices.minPrice = 20
        vm.userFilterChoices.maxPrice = 50

        var neighborhood = HotelSearchResponse.Neighborhood()
        neighborhood.name = "Civic Center"
        vm.selectNeighborhood.onNext(neighborhood)

        vm.clearObservable.onNext(Unit)

        assertEquals(0, vm.userFilterChoices.filterCount())
        assertTrue(vm.userFilterChoices.name.isBlank())
        assertEquals(false, vm.userFilterChoices.isVipOnlyAccess)
        assertTrue(vm.userFilterChoices.neighborhoods.isEmpty())
        assertEquals(false, vm.userFilterChoices.hotelStarRating.one)
        assertEquals(0, vm.userFilterChoices.minPrice)
        assertEquals(0, vm.userFilterChoices.maxPrice)
    }

    @Test
    fun emptyFilters() {
        vm.doneObservable.onNext(Unit)
        assertEquals(0, vm.userFilterChoices.filterCount())
    }

    @Test
    fun filterCount() {
        val str = "Hilton"
        vm.filterHotelNameObserver.onNext(str)

        vm.userFilterChoices.hotelStarRating.one = false
        vm.oneStarFilterObserver.onNext(Unit)

        vm.doneObservable.onNext(Unit)
        assertEquals(2, vm.userFilterChoices.filterCount())

        vm.oneStarFilterObserver.onNext(Unit)
        vm.doneObservable.onNext(Unit)
        assertEquals(1, vm.userFilterChoices.filterCount())

        val neighborhood1 = HotelSearchResponse.Neighborhood()
        neighborhood1.name = "Civic Center"

        vm.selectNeighborhood.onNext(neighborhood1)
        vm.doneObservable.onNext(Unit)
        assertEquals(2, vm.userFilterChoices.filterCount())

        vm.deselectNeighborhood.onNext(neighborhood1)
        vm.doneObservable.onNext(Unit)
        assertEquals(1, vm.userFilterChoices.filterCount())
    }


    @Test
    fun testNoNeighborhoods() {
        assertFalse(vm.neighborhoodsExist)
        vm.neighborhoodsExist = true

        assertTrue(vm.neighborhoodsExist)
        vm.setHotelList(generateNoNeighborhoodSearch())
        assertFalse(vm.neighborhoodsExist)
    }

    @Test
    fun testNotShowingSearchedNeighborhood() {
        var neighborhoodList: List<HotelSearchResponse.Neighborhood> = ArrayList()
        vm.neighborhoodListObservable.subscribe { neighborhood ->
            neighborhoodList = neighborhood
        }

        vm.setHotelList(generateHotelSearchResponse())
        assertEquals(8, neighborhoodList.size)

        vm.setSearchLocationId("6139039")
        vm.setHotelList(generateHotelSearchResponse())
        assertEquals(7, neighborhoodList.size)
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
