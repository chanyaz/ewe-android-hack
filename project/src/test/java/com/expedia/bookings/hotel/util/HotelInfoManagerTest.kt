package com.expedia.bookings.hotel.util

import com.expedia.bookings.data.SuggestionV4
import com.expedia.bookings.data.hotels.HotelOffersResponse
import com.expedia.bookings.data.hotels.HotelSearchParams
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
class HotelInfoManagerTest {
    val mockHotelServiceTestRule: MockHotelServiceTestRule = MockHotelServiceTestRule()
        @Rule get

    private lateinit var testManager: HotelInfoManager

    private val checkInDate = LocalDate.now()
    private val checkOutDate = checkInDate.plusDays(3)

    private val params = makeParams()

    private val testSuccessSub = TestSubscriber<HotelOffersResponse>()

    @Before
    fun setUp() {
        testManager = HotelInfoManager(mockHotelServiceTestRule.services!!)
    }

    @Test
    fun testHappyFetchOffers() {
        testManager.offerSuccessSubject.subscribe(testSuccessSub)

        testManager.fetchOffers(params, "happypath")

        testSuccessSub.awaitValueCount(1, 1, TimeUnit.SECONDS)
        testSuccessSub.assertNoTerminalEvent()
        testSuccessSub.assertNoErrors()
        testSuccessSub.assertValueCount(1)
    }

    @Test
    fun testSoldOutFetchOffers() {
        val testSoldOutSub = TestSubscriber<Unit>()

        testManager.offerSuccessSubject.subscribe(testSuccessSub)
        testManager.soldOutSubject.subscribe(testSoldOutSub)

        testManager.fetchOffers(params, "sold_out")

        testSoldOutSub.awaitValueCount(1, 1, TimeUnit.SECONDS)
        testSoldOutSub.assertValueCount(1)
        testSuccessSub.assertValueCount(0)
    }

    @Test
    fun testFetchInfo() {
        testManager.infoSuccessSubject.subscribe(testSuccessSub)

        testManager.fetchInfo(params, "happy")

        testSuccessSub.awaitValueCount(1, 1, TimeUnit.SECONDS)
        testSuccessSub.assertNoTerminalEvent()
        testSuccessSub.assertNoErrors()
        testSuccessSub.assertValueCount(1)
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