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
import kotlin.test.assertEquals

@RunWith(RobolectricRunner::class)
public class HotelMapViewModelTest {

    public var mockHotelServiceTestRule: MockHotelServiceTestRule = MockHotelServiceTestRule()
        @Rule get

    lateinit private var hotelOffersResponse: HotelOffersResponse

    @Test fun testFromPriceStyledString() {
        givenHotelOffersResponseWhenStrikethroughPriceAndPriceAreSame()

        assertEquals("From $109", HotelMapViewModel.fromPriceStyledString(RuntimeEnvironment.application, hotelOffersResponse.hotelRoomResponse.first().rateInfo.chargeableRateInfo).toString())
    }

    @Test fun testViewModelOutputsForViewWhenStrikethroughPriceAndPriceAreSame() {
        givenHotelOffersResponseWhenStrikethroughPriceAndPriceAreSame()

        var subjectUnderTest = HotelMapViewModel(RuntimeEnvironment.application, endlessObserver {  })
        subjectUnderTest.offersObserver.onNext(hotelOffersResponse)

        assertEquals("happypath", subjectUnderTest.hotelName.value)
        assertEquals(4f, subjectUnderTest.hotelStarRating.value)
        assertEquals(true, subjectUnderTest.hotelStarRatingVisibility.value)
        assertEquals("$109", subjectUnderTest.strikethroughPrice.value.toString())
        assertEquals("From $109", subjectUnderTest.fromPrice.value.toString())
        assertEquals(37.78458, subjectUnderTest.hotelLatLng.value.get(0))
        assertEquals(-122.40854, subjectUnderTest.hotelLatLng.value.get(1))

        val testSubscriber = TestSubscriber.create<Boolean>()
        subjectUnderTest.strikethroughPriceVisibility.subscribe(testSubscriber)
        testSubscriber.assertValue(false)
    }

    @Test fun testViewModelOutputsForViewWhenStrikethroughPriceAndPriceAreDifferent() {
        givenHotelOffersResponseWhenStrikethroughPriceAndPriceAreDifferent()

        var subjectUnderTest = HotelMapViewModel(RuntimeEnvironment.application, endlessObserver {  })
        subjectUnderTest.offersObserver.onNext(hotelOffersResponse)

        assertEquals("air_attached_hotel", subjectUnderTest.hotelName.value)
        assertEquals(4f, subjectUnderTest.hotelStarRating.value)
        assertEquals(true, subjectUnderTest.hotelStarRatingVisibility.value)
        assertEquals("$284", subjectUnderTest.strikethroughPrice.value.toString())
        assertEquals("From $241", subjectUnderTest.fromPrice.value.toString())
        assertEquals(37.78458, subjectUnderTest.hotelLatLng.value.get(0))
        assertEquals(-122.40854, subjectUnderTest.hotelLatLng.value.get(1))

        val testSubscriber = TestSubscriber.create<Boolean>()
        subjectUnderTest.strikethroughPriceVisibility.subscribe(testSubscriber)
        testSubscriber.assertValue(true)
    }

    @Test fun testViewModelOutputsForViewWhenHotelStarRatingIsZero() {
        givenHotelOffersResponseWhenHotelStarRatingIsZero()

        var subjectUnderTest = HotelMapViewModel(RuntimeEnvironment.application, endlessObserver {  })
        subjectUnderTest.offersObserver.onNext(hotelOffersResponse)

        assertEquals("zero_star_rating", subjectUnderTest.hotelName.value)
        assertEquals(0f, subjectUnderTest.hotelStarRating.value)
        assertEquals(false, subjectUnderTest.hotelStarRatingVisibility.value)
        assertEquals("$284", subjectUnderTest.strikethroughPrice.value.toString())
        assertEquals("From $241", subjectUnderTest.fromPrice.value.toString())
        assertEquals(37.78458, subjectUnderTest.hotelLatLng.value.get(0))
        assertEquals(-122.40854, subjectUnderTest.hotelLatLng.value.get(1))

        val testSubscriber = TestSubscriber.create<Boolean>()
        subjectUnderTest.strikethroughPriceVisibility.subscribe(testSubscriber)
        testSubscriber.assertValue(true)
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
}