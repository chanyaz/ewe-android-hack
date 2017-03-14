package com.expedia.bookings.test

import com.expedia.bookings.data.hotels.HotelSearchResponse
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.vm.hotel.HotelServerFilterViewModel
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RuntimeEnvironment
import kotlin.properties.Delegates
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@RunWith(RobolectricRunner::class)
class HotelServerFilterViewModelTest {
    var vm: HotelServerFilterViewModel by Delegates.notNull()

    @Before
    fun before() {
        val context = RuntimeEnvironment.application
        vm = HotelServerFilterViewModel(context)
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
    }
}