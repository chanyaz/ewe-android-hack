package com.expedia.bookings.test

import android.content.Context
import android.content.res.Resources
import com.expedia.vm.HotelFilterViewModel
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.mockito.Matchers
import org.mockito.Mockito
import kotlin.properties.Delegates
import kotlin.test.assertTrue

public class HotelFilterTest {
    public var vm: HotelFilterViewModel by Delegates.notNull()

    @Before
    fun before() {
        val context = Mockito.mock(javaClass<Context>())
        val resources = Mockito.mock(javaClass<Resources>())
        Mockito.`when`(context.getResources()).thenReturn(resources)
        Mockito.`when`(resources.getQuantityString(Matchers.anyInt(), Matchers.anyInt(), Matchers.anyInt())).thenReturn("")

        vm = HotelFilterViewModel(context)
    }

    @Test
    fun filterVip() {
        vm.vipFilteredObserver.onNext(true)
        Assert.assertEquals(true, vm.filterToggles.isVipAccess)

        vm.vipFilteredObserver.onNext(false)
        Assert.assertEquals(false, vm.filterToggles.isVipAccess)
    }

    @Test
    fun filterStar() {
        vm.filterToggles.hotelStarRating.one = false
        vm.oneStarFilterObserver.onNext(Unit)

        Assert.assertEquals(true, vm.filterToggles.hotelStarRating.one)
    }

    @Test
    fun filterStars() {
        vm.filterToggles.hotelStarRating.one = false
        vm.oneStarFilterObserver.onNext(Unit)

        Assert.assertEquals(true, vm.filterToggles.hotelStarRating.one)

        vm.filterToggles.hotelStarRating.two = false
        vm.twoStarFilterObserver.onNext(Unit)

        Assert.assertEquals(true, vm.filterToggles.hotelStarRating.two)
    }

    @Test
    fun filterName() {
        val str = "Hilton"
        vm.filterHotelNameObserver.onNext(str)
        Assert.assertEquals(str, vm.filterToggles.name)
    }

    @Test
    fun clearStar() {
        vm.filterToggles.hotelStarRating.one = true
        vm.oneStarFilterObserver.onNext(Unit)

        vm.clearObservable.onNext(Unit)
        Assert.assertEquals(false, vm.filterToggles.hotelStarRating.one)
    }

    @Test
    fun emptyFilters() {
        vm.doneObservable.onNext(Unit)
        Assert.assertEquals(null, vm.filteredResponse.hotelList)
    }
}
