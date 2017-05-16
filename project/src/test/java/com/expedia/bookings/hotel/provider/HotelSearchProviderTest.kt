package com.expedia.bookings.hotel.provider

import com.expedia.bookings.data.ApiError
import com.expedia.bookings.data.SuggestionV4
import com.expedia.bookings.data.hotels.HotelSearchParams
import com.expedia.bookings.data.hotels.HotelSearchResponse
import com.expedia.bookings.test.MockHotelServiceTestRule
import com.expedia.bookings.test.robolectric.RobolectricRunner
import org.joda.time.LocalDate
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import rx.observers.TestSubscriber
import java.util.concurrent.TimeUnit

@RunWith(RobolectricRunner::class)
class HotelSearchProviderTest {
    val mockHotelServiceTestRule: MockHotelServiceTestRule = MockHotelServiceTestRule()
        @Rule get
    lateinit var testProvider : HotelSearchProvider

    val checkInDate = LocalDate.now()
    val checkOutDate = checkInDate.plusDays(3)

    @Before
    fun setUp() {
        testProvider = HotelSearchProvider(mockHotelServiceTestRule.services!!)
    }

    @Test
    fun testHappy() {
        val testSuccessSub = TestSubscriber<HotelSearchResponse>()
        testProvider.successSubject.subscribe(testSuccessSub)

        testProvider.doSearch(makeParams())

        testSuccessSub.awaitValueCount(1, 1, TimeUnit.SECONDS)
        testSuccessSub.assertNoTerminalEvent()
        testSuccessSub.assertNoErrors()
        testSuccessSub.assertValueCount(1)
    }

    @Test
    fun testError() {
        val testSuccessSub = TestSubscriber<HotelSearchResponse>()
        testProvider.successSubject.subscribe(testSuccessSub)

        val testErrorSub = TestSubscriber<ApiError>()
        testProvider.errorSubject.subscribe(testErrorSub)

        testProvider.doSearch(makeParams("mock_error"))

        testErrorSub.awaitValueCount(1, 1, TimeUnit.SECONDS)
        testErrorSub.assertNoTerminalEvent()
        testErrorSub.assertNoErrors()
        testErrorSub.assertValueCount(1)

        testSuccessSub.assertNoValues()
    }

    @Test
    fun testNoResults() {
        val testSuccessSub = TestSubscriber<HotelSearchResponse>()
        testProvider.successSubject.subscribe(testSuccessSub)

        val testErrorSub = TestSubscriber<Unit>()
        testProvider.noResultsSubject.subscribe(testErrorSub)

        testProvider.doSearch(makeParams("noresults"))

        testErrorSub.awaitValueCount(1, 1, TimeUnit.SECONDS)
        testErrorSub.assertNoTerminalEvent()
        testErrorSub.assertNoErrors()
        testErrorSub.assertValueCount(1)

        testSuccessSub.assertNoValues()
    }

    private fun makeParams(gaiaId: String = "happy"): HotelSearchParams {
        val suggestion = makeSuggestion(gaiaId)
        val hotelSearchParams = HotelSearchParams.Builder(3, 500)
                .destination(suggestion)
                .startDate(checkInDate).endDate(checkOutDate)
                .build() as HotelSearchParams

        return hotelSearchParams
    }

    private fun makeSuggestion(gaiaId : String): SuggestionV4 {
        val suggestion = SuggestionV4()
        suggestion.gaiaId = gaiaId
        suggestion.regionNames = SuggestionV4.RegionNames()
        suggestion.regionNames.displayName = ""
        suggestion.regionNames.fullName = ""
        suggestion.regionNames.shortName = ""
        suggestion.coordinates = SuggestionV4.LatLng()

        return suggestion
    }
}