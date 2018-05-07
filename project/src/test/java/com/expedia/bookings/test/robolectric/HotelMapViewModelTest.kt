package com.expedia.bookings.test.robolectric

import com.expedia.bookings.data.LineOfBusiness
import com.expedia.bookings.data.hotels.HotelOffersResponse
import com.expedia.bookings.test.MockHotelServiceTestRule
import com.expedia.bookings.test.MultiBrand
import com.expedia.bookings.test.RunForBrands
import com.expedia.util.endlessObserver
import com.expedia.vm.HotelMapViewModel
import io.reactivex.subjects.PublishSubject
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RuntimeEnvironment
import kotlin.test.assertEquals

@RunWith(RobolectricRunner::class)
class HotelMapViewModelTest {

    var mockHotelServiceTestRule: MockHotelServiceTestRule = MockHotelServiceTestRule()
        @Rule get

    private lateinit var hotelOffersResponse: HotelOffersResponse

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA, MultiBrand.ORBITZ, MultiBrand.CHEAPTICKETS, MultiBrand.TRAVELOCITY))
    fun testViewModelOutputsForViewWhenStrikethroughPriceAndPriceAreSame() {
        givenHotelOffersResponseWhenStrikethroughPriceAndPriceAreSame()

        val subjectUnderTest = HotelMapViewModel(RuntimeEnvironment.application, endlessObserver { }, PublishSubject.create<Boolean>(), LineOfBusiness.HOTELS)
        subjectUnderTest.offersObserver.onNext(hotelOffersResponse)

        assertEquals("happypath", subjectUnderTest.hotelName.value)
        assertEquals(4f, subjectUnderTest.hotelStarRating.value)
        assertEquals(true, subjectUnderTest.hotelStarRatingVisibility.value)
        assertEquals(37.78458, subjectUnderTest.hotelLatLng.value[0])
        assertEquals(-122.40854, subjectUnderTest.hotelLatLng.value[1])
        assertEquals(false, subjectUnderTest.selectARoomInvisibility.value)
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA, MultiBrand.ORBITZ, MultiBrand.CHEAPTICKETS, MultiBrand.TRAVELOCITY))
    fun testViewModelOutputsForViewWhenStrikethroughPriceAndPriceAreDifferent() {
        givenHotelOffersResponseWhenStrikethroughPriceAndPriceAreDifferent()

        val subjectUnderTest = HotelMapViewModel(RuntimeEnvironment.application, endlessObserver { }, PublishSubject.create<Boolean>(), LineOfBusiness.HOTELS)
        subjectUnderTest.offersObserver.onNext(hotelOffersResponse)

        assertEquals("air_attached_hotel", subjectUnderTest.hotelName.value)
        assertEquals(4f, subjectUnderTest.hotelStarRating.value)
        assertEquals(true, subjectUnderTest.hotelStarRatingVisibility.value)
        assertEquals(37.78458, subjectUnderTest.hotelLatLng.value[0])
        assertEquals(-122.40854, subjectUnderTest.hotelLatLng.value[1])
        assertEquals(false, subjectUnderTest.selectARoomInvisibility.value)
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA, MultiBrand.ORBITZ, MultiBrand.CHEAPTICKETS, MultiBrand.TRAVELOCITY))
    fun testViewModelOutputsForViewWhenHotelStarRatingIsZero() {
        givenHotelOffersResponseWhenHotelStarRatingIsZero()

        val subjectUnderTest = HotelMapViewModel(RuntimeEnvironment.application, endlessObserver { }, PublishSubject.create<Boolean>(), LineOfBusiness.HOTELS)
        subjectUnderTest.offersObserver.onNext(hotelOffersResponse)

        assertEquals("zero_star_rating", subjectUnderTest.hotelName.value)
        assertEquals(0f, subjectUnderTest.hotelStarRating.value)
        assertEquals(false, subjectUnderTest.hotelStarRatingVisibility.value)
        assertEquals(37.78458, subjectUnderTest.hotelLatLng.value[0])
        assertEquals(-122.40854, subjectUnderTest.hotelLatLng.value[1])
        assertEquals(false, subjectUnderTest.selectARoomInvisibility.value)
    }

    @Test fun testViewModelOutputsForViewWhenRoomOffersAreNotAvailable() {
        givenHotelOffersResponseWhenRoomOffersAreNotAvailable()

        val hotelSoldOut = PublishSubject.create<Boolean>()
        val subjectUnderTest = HotelMapViewModel(RuntimeEnvironment.application, endlessObserver { }, hotelSoldOut, LineOfBusiness.HOTELS)
        subjectUnderTest.offersObserver.onNext(hotelOffersResponse)
        hotelSoldOut.onNext(true)

        assertEquals("room_offers_not_available", subjectUnderTest.hotelName.value)
        assertEquals(4f, subjectUnderTest.hotelStarRating.value)
        assertEquals(true, subjectUnderTest.hotelStarRatingVisibility.value)
        assertEquals(37.78458, subjectUnderTest.hotelLatLng.value[0])
        assertEquals(-122.40854, subjectUnderTest.hotelLatLng.value[1])
        assertEquals(true, subjectUnderTest.selectARoomInvisibility.value)
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
