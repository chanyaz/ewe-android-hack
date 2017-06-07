package com.expedia.vm.hotel

import com.expedia.bookings.data.hotel.Sort
import com.expedia.bookings.data.hotel.UserFilterChoices
import com.expedia.bookings.test.robolectric.RobolectricRunner
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RuntimeEnvironment
import rx.observers.TestSubscriber
import kotlin.properties.Delegates
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@RunWith(RobolectricRunner::class)
class AdvancedSearchOptionsViewModelTest {
    var vm: AdvancedSearchOptionsViewModel by Delegates.notNull()

    @Before
    fun before() {
        val context = RuntimeEnvironment.application
        vm = AdvancedSearchOptionsViewModel(context)
    }

    @Test
    fun testSummaryText() {
        val name = "Hilton"
        val starRating = UserFilterChoices.StarRatings()
        starRating.one = true

        vm.selectHotelName(name)
        vm.selectSortOption(Sort.PRICE)
        vm.updateStarRating(starRating)
        vm.isVipAccess(true)

        val expected = "Hilton • 1 star • VIP only • Sort by Price"
        assertEquals(expected, vm.getSummaryString())
    }

    @Test
    fun testSummaryTextIfNothingSelected() {
        assertEquals("Advanced Options", vm.getSummaryString())
    }

    @Test
    fun testClearFilters() {
        val name = "Hilton"
        val starRating = UserFilterChoices.StarRatings()
        starRating.one = true

        vm.selectHotelName(name)
        vm.selectSortOption(Sort.PRICE)
        vm.updateStarRating(starRating)
        vm.isVipAccess(true)

        val testSubject = TestSubscriber<UserFilterChoices>()
        vm.resetViewsSubject.subscribe(testSubject)

        vm.clearObservable.onNext(Unit)

        val searchOption = testSubject.onNextEvents[0]
        assertNotNull(searchOption)
        assertTrue(searchOption.name.isBlank())
        assertEquals(Sort.RECOMMENDED, searchOption.userSort)
        assertFalse(searchOption.isVipOnlyAccess)
        assertEquals(0, searchOption.hotelStarRating.getStarRatingParamsAsList().size)
    }

    @Test
    fun testShowClearButton() {
        val testSubject = TestSubscriber<Boolean>()
        vm.showClearButtonSubject.subscribe(testSubject)

        vm.selectHotelName("Hyatt")
        assertTrue(testSubject.onNextEvents[0])

        vm.clearObservable.onNext(Unit)
        assertFalse(testSubject.onNextEvents[1])

        vm.selectSortOption(Sort.PRICE)
        assertTrue(testSubject.onNextEvents[2])

        vm.clearObservable.onNext(Unit)
        assertFalse(testSubject.onNextEvents[3])
    }
}