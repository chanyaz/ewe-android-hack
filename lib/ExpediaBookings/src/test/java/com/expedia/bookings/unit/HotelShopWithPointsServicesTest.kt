package com.expedia.bookings.unit

import com.expedia.bookings.data.SuggestionV4
import com.expedia.bookings.data.hotels.HotelOffersResponse
import com.expedia.bookings.data.hotels.HotelSearchParams
import com.expedia.bookings.data.hotels.HotelSearchResponse
import com.expedia.bookings.services.HotelServices
import com.expedia.bookings.testrule.ServicesRule
import org.joda.time.LocalDate
import org.junit.Rule
import org.junit.Test
import rx.observers.TestSubscriber
import java.util.ArrayList
import java.util.concurrent.TimeUnit
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue


class HotelShopWithPointsServicesTest {
    var serviceRule = ServicesRule(HotelServices::class.java, "../mocked/templates")
            @Rule get

    @Test
    fun testSearchResponseWithoutLoyalty() {
        val testObserver: TestSubscriber<HotelSearchResponse> = TestSubscriber.create()

        val suggestion = SuggestionV4()
        suggestion.gaiaId = "happy"
        suggestion.coordinates = SuggestionV4.LatLng()

        val hotelSearchParams = HotelSearchParams.Builder(0).suggestion(suggestion).checkIn(LocalDate.now()).checkOut(LocalDate.now().plusDays(1)).build()

        serviceRule.services!!.regionSearch(hotelSearchParams, null).subscribe(testObserver)

        testObserver.awaitTerminalEvent()
        testObserver.assertCompleted()
        testObserver.assertValueCount(1)
        val searchResponse = testObserver.onNextEvents[0]
        assertFalse(searchResponse.hasLoyaltyInformation)

    }

    // Loyalty earn in ExpediaRewards is in Points
    @Test
    fun testSearchResponseWithLoyaltyPoints() {
        val testObserver: TestSubscriber<HotelSearchResponse> = TestSubscriber.create()

        val suggestion = SuggestionV4()
        suggestion.gaiaId = "happy_with_loyalty_points"
        suggestion.coordinates = SuggestionV4.LatLng()

        val hotelSearchParams = HotelSearchParams.Builder(0).suggestion(suggestion).checkIn(LocalDate.now()).checkOut(LocalDate.now().plusDays(1)).shopWithPoints(true).build()

        serviceRule.services!!.regionSearch(hotelSearchParams, null).subscribe(testObserver)

        testObserver.awaitTerminalEvent()
        testObserver.assertCompleted()
        testObserver.assertValueCount(1)

        val searchResponse = testObserver.onNextEvents[0]
        assertTrue(searchResponse.hasLoyaltyInformation)
        val loyaltyInfo = searchResponse.hotelList[1].lowRateInfo.loyaltyInfo
        assertNotNull(loyaltyInfo.burn)
        assertNotNull(loyaltyInfo.earn)
        assertNotNull(loyaltyInfo.earn.points)
        assertNull(loyaltyInfo.earn.price)
        assertTrue(loyaltyInfo.isShopWithPoints)
    }

    // Loyalty earn in Orbitz is in money (Orbucks)
    @Test
    fun testSearchResponseWithLoyaltyPrice() {
        val testObserver: TestSubscriber<HotelSearchResponse> = TestSubscriber.create()

        val suggestion = SuggestionV4()
        suggestion.gaiaId = "happy_with_loyalty_price"
        suggestion.coordinates = SuggestionV4.LatLng()

        val hotelSearchParams = HotelSearchParams.Builder(0).suggestion(suggestion).checkIn(LocalDate.now()).checkOut(LocalDate.now().plusDays(1)).build()

        serviceRule.services!!.regionSearch(hotelSearchParams, null).subscribe(testObserver)

        testObserver.awaitTerminalEvent(10, TimeUnit.SECONDS)
        testObserver.assertCompleted()
        testObserver.assertValueCount(1)

        val searchResponse = testObserver.onNextEvents[0]
        assertTrue(searchResponse.hasLoyaltyInformation)
        val loyaltyInfo = searchResponse.hotelList[1].lowRateInfo.loyaltyInfo
        assertNotNull(loyaltyInfo.burn)
        assertNotNull(loyaltyInfo.earn)
        assertNull(loyaltyInfo.earn.points)
        assertNotNull(loyaltyInfo.earn.price)
        assertTrue(loyaltyInfo.isShopWithPoints)
    }

    @Test
    fun testOffersResponseWithoutLoyalty() {
        val testObserver = TestSubscriber<HotelOffersResponse>()

        val suggestion = SuggestionV4()
        suggestion.gaiaId = "happypath"
        suggestion.coordinates = SuggestionV4.LatLng()

        val params = HotelSearchParams.Builder(0).suggestion(suggestion).checkIn(LocalDate.now().plusDays(5)).checkOut(LocalDate.now().plusDays(15)).adults(2).children(ArrayList<Int>()).build()
        serviceRule.services!!.offers(params, "happypath", testObserver)
        testObserver.awaitTerminalEvent(10, TimeUnit.SECONDS)
        testObserver.assertCompleted()
        testObserver.assertValueCount(1)

        val offersResponse = testObserver.onNextEvents[0]
        assertFalse(offersResponse.doesAnyHotelRateOfAnyRoomHaveLoyaltyInfo)
    }

    // Loyalty earn in ExpediaRewards is in Points
    @Test
    fun testOffersResponseWithLoyaltyPoints() {
        val testObserver = TestSubscriber<HotelOffersResponse>()

        val suggestion = SuggestionV4()
        suggestion.gaiaId = "happypath_with_loyalty_points"
        suggestion.coordinates = SuggestionV4.LatLng()

        val params = HotelSearchParams.Builder(0).suggestion(suggestion).checkIn(LocalDate.now().plusDays(5)).checkOut(LocalDate.now().plusDays(15)).adults(2).children(ArrayList<Int>()).build()
        serviceRule.services!!.offers(params, "happypath_with_loyalty_points", testObserver)
        testObserver.awaitTerminalEvent(10, TimeUnit.SECONDS)
        testObserver.assertCompleted()
        testObserver.assertValueCount(1)

        val offersResponse = testObserver.onNextEvents[0]
        val loyaltyInfo = offersResponse.hotelRoomResponse[0].rateInfo.chargeableRateInfo.loyaltyInfo
        assertTrue(offersResponse.doesAnyHotelRateOfAnyRoomHaveLoyaltyInfo)
        assertNotNull(loyaltyInfo.burn)
        assertNotNull(loyaltyInfo.earn)
        assertNotNull(loyaltyInfo.earn.points)
        assertNull(loyaltyInfo.earn.price)
        assertTrue(loyaltyInfo.isShopWithPoints)
    }

    // Loyalty earn in Orbitz is in money (Orbucks)
    @Test
    fun testOffersResponseWithLoyaltyPrice() {
        val testObserver = TestSubscriber<HotelOffersResponse>()

        val suggestion = SuggestionV4()
        suggestion.gaiaId = "happypath_with_loyalty_price"
        suggestion.coordinates = SuggestionV4.LatLng()

        val params = HotelSearchParams.Builder(0).suggestion(suggestion).checkIn(LocalDate.now().plusDays(5)).checkOut(LocalDate.now().plusDays(15)).adults(2).children(ArrayList<Int>()).build()
        serviceRule.services!!.offers(params, "happypath_with_loyalty_price", testObserver)
        testObserver.awaitTerminalEvent(10, TimeUnit.SECONDS)
        testObserver.assertCompleted()
        testObserver.assertValueCount(1)

        val offersResponse = testObserver.onNextEvents[0]
        val loyaltyInfo = offersResponse.hotelRoomResponse[0].rateInfo.chargeableRateInfo.loyaltyInfo
        assertTrue(offersResponse.doesAnyHotelRateOfAnyRoomHaveLoyaltyInfo)
        assertNotNull(loyaltyInfo.burn)
        assertNotNull(loyaltyInfo.earn)
        assertNull(loyaltyInfo.earn.points)
        assertNotNull(loyaltyInfo.earn.price)
        assertTrue(loyaltyInfo.isShopWithPoints)
    }

}