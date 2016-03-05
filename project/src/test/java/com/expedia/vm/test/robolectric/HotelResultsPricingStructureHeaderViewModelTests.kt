package com.expedia.vm.test.robolectric

import android.content.Context
import com.expedia.bookings.data.hotels.Hotel
import com.expedia.bookings.data.hotels.HotelRate
import com.expedia.bookings.data.hotels.HotelSearchResponse
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.vm.HotelResultsPricingStructureHeaderViewModel
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RuntimeEnvironment
import java.util.ArrayList
import kotlin.properties.Delegates
import kotlin.test.assertEquals

@RunWith(RobolectricRunner::class)
class HotelResultsPricingStructureHeaderViewModelTests {

    var sut: HotelResultsPricingStructureHeaderViewModel by Delegates.notNull()

    var hotelResultsCount: Int by Delegates.notNull<Int>()
    var priceType: HotelRate.UserPriceType by Delegates.notNull<HotelRate.UserPriceType>()

    @Before
    fun before() {
        sut = HotelResultsPricingStructureHeaderViewModel(getContext().resources)
        givenUserPriceType(HotelRate.UserPriceType.UNKNOWN)
        givenHotelsResultsCount(-1)
    }

    @Test
    fun loading() {
        givenLoading()

        assertEquals("Searching hundreds of hotels for you!", sut.pricingStructureHeaderObservable.value)
    }

    @Test
    fun rateForWholeStayNoResults() {
        assertExpectedText(HotelRate.UserPriceType.RATE_FOR_WHOLE_STAY_WITH_TAXES, 0, "No Results")
    }

    @Test
    fun rateForWholeStayOneResult() {
        assertExpectedText(HotelRate.UserPriceType.RATE_FOR_WHOLE_STAY_WITH_TAXES, 1, "Total price for stay • 1 Result")
    }

    @Test
    fun rateForWholeStayMultipleResults() {
        assertExpectedText(HotelRate.UserPriceType.RATE_FOR_WHOLE_STAY_WITH_TAXES, 3, "Total price for stay • 3 Results")
    }

    @Test
    fun rateForPerNightStayNoResults() {
        assertExpectedText(HotelRate.UserPriceType.PER_NIGHT_RATE_NO_TAXES, 0, "No Results")
    }

    @Test
    fun rateForPerNightStayOneResult() {
        assertExpectedText(HotelRate.UserPriceType.PER_NIGHT_RATE_NO_TAXES, 1, "Prices average per night • 1 Result")
    }

    @Test
    fun rateForPerNightStayMultipleResults() {
        assertExpectedText(HotelRate.UserPriceType.PER_NIGHT_RATE_NO_TAXES, 3, "Prices average per night • 3 Results")
    }

    @Test
    fun loyaltyPointsAppliedHeaderVisible() {
        assertExpectedText(HotelRate.UserPriceType.PER_NIGHT_RATE_NO_TAXES, 3, "Prices average per night • 3 Results", true)
    }

    private fun assertExpectedText(userPriceType: HotelRate.UserPriceType, hotelResultCount: Int, expectedString: String, expectedLoyaltyHeaderVisibility: Boolean = false) {
        givenUserPriceType(userPriceType)
        givenHotelsResultsCount(hotelResultCount)
        setupResultsDeliveredObserver(expectedLoyaltyHeaderVisibility)

        assertEquals(expectedString, sut.pricingStructureHeaderObservable.value)
        assertEquals(expectedLoyaltyHeaderVisibility, sut.loyaltyAvailableObservable.value)
    }

    private fun givenLoading() {
        sut.loadingStartedObserver.onNext(Unit)
    }

    private fun givenUserPriceType(userPriceType: HotelRate.UserPriceType) {
        this.priceType = userPriceType
    }

    private fun givenHotelsResultsCount(hotelResultsCount: Int) {
        this.hotelResultsCount = hotelResultsCount
    }

    private fun setupResultsDeliveredObserver(loyaltyInformationAvailable: Boolean) {
        val hotelSearchResponse = HotelSearchResponse()
        hotelSearchResponse.userPriceType = this.priceType
        hotelSearchResponse.hotelList = ArrayList<Hotel>()
        if (loyaltyInformationAvailable) {
            hotelSearchResponse.hasLoyaltyInformation = true
        }
        while (this.hotelResultsCount > 0) {
            hotelSearchResponse.hotelList.add(Hotel())
            this.hotelResultsCount--
        }
        sut.resultsDeliveredObserver.onNext(hotelSearchResponse)
    }

    private fun getContext(): Context {
        return RuntimeEnvironment.application;
    }
}
