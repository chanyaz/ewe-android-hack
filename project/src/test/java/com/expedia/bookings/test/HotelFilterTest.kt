package com.expedia.bookings.test

import android.content.Context
import android.content.res.Resources
import com.expedia.bookings.data.hotels.Hotel
import com.expedia.bookings.data.hotels.HotelRate
import com.expedia.bookings.data.hotels.HotelSearchResponse
import com.expedia.vm.HotelFilterViewModel
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.mockito.Matchers
import org.mockito.Mockito
import kotlin.properties.Delegates
import kotlin.test.assertTrue
import java.util.ArrayList

public class HotelFilterTest {
    public var vm: HotelFilterViewModel by Delegates.notNull()

    @Before
    fun before() {
        val context = Mockito.mock(Context::class.java)
        val resources = Mockito.mock(Resources::class.java)
        Mockito.`when`(context.getResources()).thenReturn(resources)
        Mockito.`when`(resources.getQuantityString(Matchers.anyInt(), Matchers.anyInt(), Matchers.anyInt())).thenReturn("")

        vm = HotelFilterViewModel(context)
    }

    @Test
    fun filterVip() {
        vm.vipFilteredObserver.onNext(true)
        Assert.assertEquals(true, vm.userFilterChoices.isVipAccess)

        vm.vipFilteredObserver.onNext(false)
        Assert.assertEquals(false, vm.userFilterChoices.isVipAccess)
    }

    @Test
    fun filterStars() {
        vm.userFilterChoices.hotelStarRating.one = false
        vm.oneStarFilterObserver.onNext(Unit)

        Assert.assertEquals(true, vm.userFilterChoices.hotelStarRating.one)

        vm.userFilterChoices.hotelStarRating.two = false
        vm.twoStarFilterObserver.onNext(Unit)

        Assert.assertEquals(true, vm.userFilterChoices.hotelStarRating.two)

        vm.oneStarFilterObserver.onNext(Unit)
        Assert.assertEquals(false, vm.userFilterChoices.hotelStarRating.one)
    }

    @Test
    fun filterName() {
        val str = "Hilton"
        vm.filterHotelNameObserver.onNext(str)
        Assert.assertEquals(str, vm.userFilterChoices.name)
    }

    @Test
    fun clearFilters() {
        vm.userFilterChoices.hotelStarRating.one = true
        vm.oneStarFilterObserver.onNext(Unit)

        vm.vipFilteredObserver.onNext(true)
        vm.clearObservable.onNext(Unit)

        var region = "Civic Center"
        vm.originalResponse = fakeFilteredResponse()
        vm.selectNeighborhood.onNext(region)

        vm.clearObservable.onNext(Unit)

        Assert.assertEquals(false, vm.userFilterChoices.hotelStarRating.one)
        Assert.assertEquals(false, vm.userFilterChoices.hotelStarRating.one)
        assertTrue(vm.filteredResponse.hotelList.size() == vm.originalResponse!!.hotelList.size())

    }

    @Test
    fun filterResultsCount(){
        vm.originalResponse = fakeFilteredResponse()
        var str = "Hil"
        vm.filterHotelNameObserver.onNext(str)
        assertTrue(vm.filteredResponse.hotelList.size() == 1)

        str = ""
        vm.filterHotelNameObserver.onNext(str)
        assertTrue(vm.filteredResponse.hotelList.size() == 2)

        vm.userFilterChoices.hotelStarRating.three = false
        vm.threeStarFilterObserver.onNext(Unit)
        assertTrue(vm.filteredResponse.hotelList.size() == 1)

        vm.clearObservable.onNext(Unit)
        vm.vipFilteredObserver.onNext(true)
        assertTrue(vm.filteredResponse.hotelList.size() == 1)

    }


    @Test
    fun filterNeighborhood() {
        var region = "Civic Center"
        vm.originalResponse = fakeFilteredResponse()
        vm.selectNeighborhood.onNext(region)
        Assert.assertEquals(region, vm.filteredResponse.hotelList.elementAt(0).locationDescription)
        assertTrue(vm.filteredResponse.hotelList.size() == 1)

        vm.selectNeighborhood.onNext(region)
        assertTrue(vm.filteredResponse.hotelList.size() == vm.originalResponse!!.hotelList.size())
    }

    @Test
    fun emptyFilters() {
        vm.doneObservable.onNext(Unit)
        Assert.assertEquals(null, vm.filteredResponse.hotelList)
    }

    @Test
    fun sortByPrice(){
        vm.filteredResponse = fakeFilteredResponse()
        vm.sortObserver.onNext(HotelFilterViewModel.Sort.PRICE)

        for (i in 1..vm.filteredResponse.hotelList.size() - 1) {
            val current = vm.filteredResponse.hotelList.elementAt(i).lowRateInfo.getDisplayTotalPrice()
            val previous = vm.filteredResponse.hotelList.elementAt(i-1).lowRateInfo.getDisplayTotalPrice()
            assertTrue(current >= previous)
        }
    }

    @Test
    fun sortByDeals(){
        vm.filteredResponse = fakeFilteredResponse()
        vm.sortObserver.onNext(HotelFilterViewModel.Sort.DEALS)
        for (i in 1..vm.filteredResponse.hotelList.size() - 1) {
            val currentDeals = vm.filteredResponse.hotelList.elementAt(i).lowRateInfo.discountPercent
            val previousDeals = vm.filteredResponse.hotelList.elementAt(i-1).lowRateInfo.discountPercent
            assertTrue(currentDeals >= previousDeals)
        }
    }

    @Test
    fun sortByRating(){
        vm.filteredResponse = fakeFilteredResponse()
        vm.sortObserver.onNext(HotelFilterViewModel.Sort.RATING)

        for (i in 1..vm.filteredResponse.hotelList.size() - 1) {
            val current = vm.filteredResponse.hotelList.elementAt(i).hotelGuestRating
            val previous = vm.filteredResponse.hotelList.elementAt(i-1).hotelGuestRating
            assertTrue(current <= previous)
        }
    }

    @Test
    fun sortByDistance(){
        vm.filteredResponse = fakeFilteredResponse()
        vm.sortObserver.onNext(HotelFilterViewModel.Sort.DISTANCE)

        for (i in 1..vm.filteredResponse.hotelList.size() - 1) {
            val current = vm.filteredResponse.hotelList.elementAt(i).proximityDistanceInMiles
            val previous = vm.filteredResponse.hotelList.elementAt(i-1).proximityDistanceInMiles
            assertTrue(current >= previous)
        }
    }

    private fun fakeFilteredResponse() : HotelSearchResponse {
        var filteredResponse = HotelSearchResponse()
        filteredResponse.hotelList = ArrayList<Hotel>()

        val  hotel1 = Hotel()
        hotel1.localizedName = "Hilton"
        hotel1.lowRateInfo = HotelRate()
        hotel1.lowRateInfo.total = 100.0f
        hotel1.lowRateInfo.currencyCode = "USD"
        hotel1.lowRateInfo.discountPercent = -10f
        hotel1.hotelGuestRating = 4.5f
        hotel1.proximityDistanceInMiles = 1.2
        hotel1.locationDescription = "Civic Center"
        hotel1.hotelStarRating = 5f
        hotel1.isVipAccess = true


        val hotel2 = Hotel()
        hotel2.localizedName = "Double Tree"
        hotel2.lowRateInfo = HotelRate()
        hotel2.lowRateInfo.total = 200.0f
        hotel2.lowRateInfo.currencyCode = "USD"
        hotel2.lowRateInfo.discountPercent = -15f
        hotel2.hotelGuestRating = 5f
        hotel2.proximityDistanceInMiles = 2.0
        hotel2.locationDescription = "Fisherman's Wharf"
        hotel2.hotelStarRating = 3.5f
        hotel2.isVipAccess = false

        filteredResponse.hotelList.add(hotel1)
        filteredResponse.hotelList.add(hotel2)

        return filteredResponse
    }
}
