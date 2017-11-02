package com.expedia.bookings.hotel.util

import com.expedia.bookings.data.ApiError
import com.expedia.bookings.data.SuggestionV4
import com.expedia.bookings.data.hotels.Hotel
import com.expedia.bookings.data.hotels.HotelSearchParams
import com.expedia.bookings.data.hotels.HotelSearchResponse
import com.expedia.bookings.services.TestObserver
import com.expedia.bookings.test.MockHotelServiceTestRule
import com.expedia.bookings.test.robolectric.RobolectricRunner
import org.joda.time.LocalDate
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.TimeUnit
import kotlin.test.assertEquals

@RunWith(RobolectricRunner::class)
class HotelSearchManagerTest {
    val mockHotelServiceTestRule: MockHotelServiceTestRule = MockHotelServiceTestRule()
        @Rule get
    lateinit var testManager: HotelSearchManager

    val checkInDate = LocalDate.now()
    val checkOutDate = checkInDate.plusDays(3)

    @Before
    fun setUp() {
        testManager = HotelSearchManager(mockHotelServiceTestRule.services!!)
    }

    @Test
    fun testHappy() {
        val testSuccessSub = TestObserver<HotelSearchResponse>()
        testManager.successSubject.subscribe(testSuccessSub)

        testManager.doSearch(makeParams())

        testSuccessSub.awaitValueCount(1, 1, TimeUnit.SECONDS)
        testSuccessSub.assertNotTerminated()
        testSuccessSub.assertNoErrors()
        testSuccessSub.assertValueCount(1)
    }

    @Test
    fun testError() {
        val testSuccessSub = TestObserver<HotelSearchResponse>()
        testManager.successSubject.subscribe(testSuccessSub)

        val testErrorSub = TestObserver<ApiError>()
        testManager.errorSubject.subscribe(testErrorSub)

        testManager.doSearch(makeParams("mock_error"))

        testErrorSub.awaitValueCount(1, 1, TimeUnit.SECONDS)
        testErrorSub.assertNotTerminated()
        testErrorSub.assertNoErrors()
        testErrorSub.assertValueCount(1)

        testSuccessSub.assertNoValues()
    }

    @Test
    fun testNoResults() {
        val testSuccessSub = TestObserver<HotelSearchResponse>()
        testManager.successSubject.subscribe(testSuccessSub)

        val testErrorSub = TestObserver<Unit>()
        testManager.noResultsSubject.subscribe(testErrorSub)

        testManager.doSearch(makeParams("noresults"))

        testErrorSub.awaitValueCount(1, 1, TimeUnit.SECONDS)
        testErrorSub.assertNotTerminated()
        testErrorSub.assertNoErrors()
        testErrorSub.assertValueCount(1)

        testSuccessSub.assertNoValues()
    }

    @Test
    fun testNeighborhoodName() {
        val testSuccessSub = TestObserver<HotelSearchResponse>()
        val hotelSearchResponse = HotelSearchResponse()
        addNeighborhoodAndHotelsToResponse(hotelSearchResponse)
        testManager.isCurrentLocationSearch = false
        testManager.successSubject.subscribe(testSuccessSub)
        testManager.searchResponseObserver.onNext(hotelSearchResponse)

        assertEquals("West Loop", testSuccessSub.values().get(0).hotelList.get(0).neighborhoodName)
        assertEquals("Lincoln Park", testSuccessSub.values().get(0).hotelList.get(1).neighborhoodName)
    }

    @Test
    fun testIsCurrentLocation() {
        val testSuccessSub = TestObserver<HotelSearchResponse>()
        val hotelSearchResponse = HotelSearchResponse()
        addNeighborhoodAndHotelsToResponse(hotelSearchResponse)
        testManager.isCurrentLocationSearch = true
        testManager.successSubject.subscribe(testSuccessSub)
        testManager.searchResponseObserver.onNext(hotelSearchResponse)

        assertEquals(true, testSuccessSub.values().get(0).hotelList.get(0).isCurrentLocationSearch)
    }

    @Test
    fun testIsNotCurrentLocation() {
        val testSuccessSub = TestObserver<HotelSearchResponse>()
        val hotelSearchResponse = HotelSearchResponse()
        addNeighborhoodAndHotelsToResponse(hotelSearchResponse)
        testManager.isCurrentLocationSearch = false
        testManager.successSubject.subscribe(testSuccessSub)
        testManager.searchResponseObserver.onNext(hotelSearchResponse)

        assertEquals(false, testSuccessSub.values().get(0).hotelList.get(0).isCurrentLocationSearch)
    }

    private fun addNeighborhoodAndHotelsToResponse(hotelSearchResponse: HotelSearchResponse) {
        val neighborhood1 = HotelSearchResponse.Neighborhood()
        neighborhood1.id = "1"
        neighborhood1.name = "West Loop"
        val neighborhood2 = HotelSearchResponse.Neighborhood()
        neighborhood2.id = "2"
        neighborhood2.name = "Lincoln Park"
        hotelSearchResponse.neighborhoodsMap.put("123", neighborhood1)
        hotelSearchResponse.neighborhoodsMap.put("456", neighborhood2)
        val hotel1 = Hotel()
        hotel1.locationId = "123"
        val hotel2 = Hotel()
        hotel2.locationId = "456"
        hotelSearchResponse.hotelList.add(hotel1)
        hotelSearchResponse.hotelList.add(hotel2)
    }

    @Test
    fun testPrefetch_error() {
        val testSuccessSub = TestObserver<HotelSearchResponse>()
        testManager.successSubject.subscribe(testSuccessSub)

        val testErrorSub = TestObserver<ApiError>()
        testManager.errorSubject.subscribe(testErrorSub)

        testManager.doSearch(makeParams("mock_error"), prefetchSearch = true)

        testErrorSub.awaitValueCount(1, 1, TimeUnit.SECONDS)
        testErrorSub.assertNotTerminated()
        assertEquals(0, testErrorSub.valueCount(), "Swallow errors for prefetch searches")

        testSuccessSub.assertNoValues()
    }

    @Test
    fun testPrefetch_noResults() {
        val testSuccessSub = TestObserver<HotelSearchResponse>()
        testManager.successSubject.subscribe(testSuccessSub)

        val testErrorSub = TestObserver<Unit>()
        testManager.noResultsSubject.subscribe(testErrorSub)

        testManager.doSearch(makeParams("noresults"), prefetchSearch = true)

        testErrorSub.awaitValueCount(1, 1, TimeUnit.SECONDS)
        testErrorSub.assertNotTerminated()
        assertEquals(0, testErrorSub.valueCount(), "Swallow errors for prefetch searches")

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