package com.expedia.bookings.test.robolectric

import com.expedia.bookings.data.hotels.HotelOffersResponse
import com.expedia.bookings.test.MockHotelServiceTestRule
import com.expedia.util.endlessObserver
import com.expedia.vm.HotelMapViewModel
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RuntimeEnvironment
import rx.observers.TestSubscriber
import rx.subjects.PublishSubject
import kotlin.test.assertEquals

@RunWith(RobolectricRunner::class)
class HotelMapViewModelTest {

    var mockHotelServiceTestRule: MockHotelServiceTestRule = MockHotelServiceTestRule()
        @Rule get

    lateinit private var hotelOffersResponse: HotelOffersResponse

    @Test fun testFromPriceStyledString() {
        givenHotelOffersResponseWhenStrikethroughPriceAndPriceAreSame()

        assertEquals("From $109", HotelMapViewModel.fromPriceStyledString(RuntimeEnvironment.application, hotelOffersResponse.hotelRoomResponse?.firstOrNull()?.rateInfo?.chargeableRateInfo).toString())
        assertEquals("", HotelMapViewModel.fromPriceStyledString(RuntimeEnvironment.application, null).toString())
    }

    @Test fun testFromPriceStyledStringWithNegativePriceToSowUsers() {
        givenHotelOffersResponseWhenStrikethroughPriceAndPriceAreSame()

        val hotelRate = hotelOffersResponse.hotelRoomResponse?.firstOrNull()?.rateInfo?.chargeableRateInfo
        hotelRate?.priceToShowUsers = -10f
        assertEquals("From $0", HotelMapViewModel.fromPriceStyledString(RuntimeEnvironment.application, hotelOffersResponse.hotelRoomResponse?.firstOrNull()?.rateInfo?.chargeableRateInfo).toString())
    }

    @Test fun testViewModelOutputsForViewWhenStrikethroughPriceAndPriceAreSame() {
        givenHotelOffersResponseWhenStrikethroughPriceAndPriceAreSame()
        val strikeThroughPriceVisibilitySubscriber = TestSubscriber<Boolean>()

        val subjectUnderTest = HotelMapViewModel(RuntimeEnvironment.application, endlessObserver {  }, PublishSubject.create<Boolean>())
        subjectUnderTest.strikethroughPriceVisibility.subscribe(strikeThroughPriceVisibilitySubscriber)
        subjectUnderTest.offersObserver.onNext(hotelOffersResponse)

        assertEquals("happypath", subjectUnderTest.hotelName.value)
        assertEquals(4f, subjectUnderTest.hotelStarRating.value)
        assertEquals(true, subjectUnderTest.hotelStarRatingVisibility.value)
        assertEquals("", subjectUnderTest.strikethroughPrice.value.toString())
        strikeThroughPriceVisibilitySubscriber.assertValues(false, false)
        assertEquals("From $109", subjectUnderTest.fromPrice.value.toString())
        assertEquals(37.78458, subjectUnderTest.hotelLatLng.value.get(0))
        assertEquals(-122.40854, subjectUnderTest.hotelLatLng.value.get(1))
        assertEquals(false, subjectUnderTest.selectARoomInvisibility.value)

        val testSubscriber = TestSubscriber.create<Boolean>()
        subjectUnderTest.strikethroughPriceVisibility.subscribe(testSubscriber)
        subjectUnderTest.fromPriceVisibility.subscribe(testSubscriber)
        testSubscriber.assertValues(false, true)
    }

    @Test fun testViewModelOutputsForViewWhenStrikethroughPriceAndPriceAreDifferent() {
        givenHotelOffersResponseWhenStrikethroughPriceAndPriceAreDifferent()

        val subjectUnderTest = HotelMapViewModel(RuntimeEnvironment.application, endlessObserver {  }, PublishSubject.create<Boolean>())
        subjectUnderTest.offersObserver.onNext(hotelOffersResponse)

        assertEquals("air_attached_hotel", subjectUnderTest.hotelName.value)
        assertEquals(4f, subjectUnderTest.hotelStarRating.value)
        assertEquals(true, subjectUnderTest.hotelStarRatingVisibility.value)
        assertEquals("$284", subjectUnderTest.strikethroughPrice.value.toString())
        assertEquals("From $241", subjectUnderTest.fromPrice.value.toString())
        assertEquals(37.78458, subjectUnderTest.hotelLatLng.value.get(0))
        assertEquals(-122.40854, subjectUnderTest.hotelLatLng.value.get(1))
        assertEquals(false, subjectUnderTest.selectARoomInvisibility.value)

        val testSubscriber = TestSubscriber.create<Boolean>()
        subjectUnderTest.strikethroughPriceVisibility.subscribe(testSubscriber)
        subjectUnderTest.fromPriceVisibility.subscribe(testSubscriber)
        testSubscriber.assertValues(true, true)
    }

    @Test fun testViewModelOutputsForViewWhenHotelStarRatingIsZero() {
        givenHotelOffersResponseWhenHotelStarRatingIsZero()

        var subjectUnderTest = HotelMapViewModel(RuntimeEnvironment.application, endlessObserver {  }, PublishSubject.create<Boolean>())
        subjectUnderTest.offersObserver.onNext(hotelOffersResponse)

        assertEquals("zero_star_rating", subjectUnderTest.hotelName.value)
        assertEquals(0f, subjectUnderTest.hotelStarRating.value)
        assertEquals(false, subjectUnderTest.hotelStarRatingVisibility.value)
        assertEquals("$284", subjectUnderTest.strikethroughPrice.value.toString())
        assertEquals("From $241", subjectUnderTest.fromPrice.value.toString())
        assertEquals(37.78458, subjectUnderTest.hotelLatLng.value.get(0))
        assertEquals(-122.40854, subjectUnderTest.hotelLatLng.value.get(1))
        assertEquals(false, subjectUnderTest.selectARoomInvisibility.value)

        val testSubscriber = TestSubscriber.create<Boolean>()
        subjectUnderTest.strikethroughPriceVisibility.subscribe(testSubscriber)
        subjectUnderTest.fromPriceVisibility.subscribe(testSubscriber)
        testSubscriber.assertValues(true, true)
    }

    @Test fun testViewModelOutputsForViewWhenRoomOffersAreNotAvailable() {
        givenHotelOffersResponseWhenRoomOffersAreNotAvailable()

        val hotelSoldOut = PublishSubject.create<Boolean>()
        var subjectUnderTest = HotelMapViewModel(RuntimeEnvironment.application, endlessObserver {  }, hotelSoldOut)
        subjectUnderTest.offersObserver.onNext(hotelOffersResponse)
        hotelSoldOut.onNext(true)

        assertEquals("room_offers_not_available", subjectUnderTest.hotelName.value)
        assertEquals(4f, subjectUnderTest.hotelStarRating.value)
        assertEquals(true, subjectUnderTest.hotelStarRatingVisibility.value)
        assertEquals("", subjectUnderTest.strikethroughPrice.value.toString())
        assertEquals("", subjectUnderTest.fromPrice.value.toString())
        assertEquals(37.78458, subjectUnderTest.hotelLatLng.value.get(0))
        assertEquals(-122.40854, subjectUnderTest.hotelLatLng.value.get(1))
        assertEquals(true, subjectUnderTest.selectARoomInvisibility.value)

        val testSubscriber = TestSubscriber.create<Boolean>()
        subjectUnderTest.strikethroughPriceVisibility.subscribe(testSubscriber)
        subjectUnderTest.fromPriceVisibility.subscribe(testSubscriber)
        testSubscriber.assertValues(false, false)
    }

    private fun givenHotelOffersResponseWhenHotelStarRatingIsZero() {
        hotelOffersResponse = mockHotelServiceTestRule.getZeroStarRatingHotelOffersResponse()
    }

    private fun givenHotelOffersResponseWhenStrikethroughPriceAndPriceAreSame() {
        hotelOffersResponse = mockHotelServiceTestRule.getHappyHotelOffersResponse()
    }

    private fun givenHotelOffersResponseWhenStrikethroughPriceAndPriceAreDifferent() {
        hotelOffersResponse = mockHotelServiceTestRule.getAirAttachedHotelOffersResponse()
    }

    private fun givenHotelOffersResponseWhenRoomOffersAreNotAvailable() {
        hotelOffersResponse = mockHotelServiceTestRule.getRoomOffersNotAvailableHotelOffersResponse()
    }
}