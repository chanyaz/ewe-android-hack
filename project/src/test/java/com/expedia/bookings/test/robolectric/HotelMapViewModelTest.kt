package com.expedia.bookings.test.robolectric

import com.expedia.bookings.data.LineOfBusiness
import com.expedia.bookings.data.abacus.AbacusUtils
import com.expedia.bookings.data.hotels.HotelOffersResponse
import com.expedia.bookings.test.MockHotelServiceTestRule
import com.expedia.bookings.test.MultiBrand
import com.expedia.bookings.test.RunForBrands
import com.expedia.bookings.utils.AbacusTestUtils
import com.expedia.util.endlessObserver
import com.expedia.vm.HotelMapViewModel
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RuntimeEnvironment
import com.expedia.bookings.services.TestObserver
import io.reactivex.subjects.PublishSubject
import kotlin.test.assertEquals

@RunWith(RobolectricRunner::class)
class HotelMapViewModelTest {

    var mockHotelServiceTestRule: MockHotelServiceTestRule = MockHotelServiceTestRule()
        @Rule get

    lateinit private var hotelOffersResponse: HotelOffersResponse

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA, MultiBrand.ORBITZ, MultiBrand.CHEAPTICKETS, MultiBrand.TRAVELOCITY))
    fun testFromPriceStyledString() {
        givenHotelOffersResponseWhenStrikethroughPriceAndPriceAreSame()

        assertEquals("From $109", HotelMapViewModel.fromPriceStyledString(RuntimeEnvironment.application, hotelOffersResponse.hotelRoomResponse?.firstOrNull()?.rateInfo?.chargeableRateInfo).toString())
        assertEquals("", HotelMapViewModel.fromPriceStyledString(RuntimeEnvironment.application, null).toString())
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA, MultiBrand.ORBITZ, MultiBrand.CHEAPTICKETS, MultiBrand.TRAVELOCITY))
    fun testFromPriceStyledStringWithNegativePriceToSowUsers() {
        givenHotelOffersResponseWhenStrikethroughPriceAndPriceAreSame()

        val hotelRate = hotelOffersResponse.hotelRoomResponse?.firstOrNull()?.rateInfo?.chargeableRateInfo
        hotelRate?.priceToShowUsers = -10f
        assertEquals("From $0", HotelMapViewModel.fromPriceStyledString(RuntimeEnvironment.application, hotelOffersResponse.hotelRoomResponse?.firstOrNull()?.rateInfo?.chargeableRateInfo).toString())
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA, MultiBrand.ORBITZ, MultiBrand.CHEAPTICKETS, MultiBrand.TRAVELOCITY))
    fun testViewModelOutputsForViewWhenStrikethroughPriceAndPriceAreSame() {
        AbacusTestUtils.bucketTests(AbacusUtils.EBAndroidAppHotelHideStrikethroughPrice)
        givenHotelOffersResponseWhenStrikethroughPriceAndPriceAreSame()
        val strikeThroughPriceVisibilitySubscriber = TestObserver<Boolean>()
        val selectRoomContDescriptionSubscriber = TestObserver<String>()

        val subjectUnderTest = HotelMapViewModel(RuntimeEnvironment.application, endlessObserver {  }, PublishSubject.create<Boolean>(), LineOfBusiness.HOTELS)
        subjectUnderTest.strikethroughPriceVisibility.subscribe(strikeThroughPriceVisibilitySubscriber)
        subjectUnderTest.selectRoomContDescription.subscribe(selectRoomContDescriptionSubscriber)
        subjectUnderTest.offersObserver.onNext(hotelOffersResponse)

        assertEquals("happypath", subjectUnderTest.hotelName.value)
        assertEquals(4f, subjectUnderTest.hotelStarRating.value)
        assertEquals(true, subjectUnderTest.hotelStarRatingVisibility.value)
        assertEquals("", subjectUnderTest.strikethroughPrice.value.toString())
        strikeThroughPriceVisibilitySubscriber.assertValues(false, false)
        assertEquals("From $109", subjectUnderTest.fromPrice.value.toString())
        assertEquals(37.78458, subjectUnderTest.hotelLatLng.value[0])
        assertEquals(-122.40854, subjectUnderTest.hotelLatLng.value[1])
        assertEquals(false, subjectUnderTest.selectARoomInvisibility.value)
        selectRoomContDescriptionSubscriber.assertValue("Select a Room From $109 button")

        val testSubscriber = TestObserver.create<Boolean>()
        subjectUnderTest.strikethroughPriceVisibility.subscribe(testSubscriber)
        subjectUnderTest.fromPriceVisibility.subscribe(testSubscriber)
        testSubscriber.assertValues(true)
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA, MultiBrand.ORBITZ, MultiBrand.CHEAPTICKETS, MultiBrand.TRAVELOCITY))
    fun testViewModelOutputsForViewWhenStrikethroughPriceAndPriceAreDifferent() {
        givenHotelOffersResponseWhenStrikethroughPriceAndPriceAreDifferent()
        val selectRoomContDescriptionSubscriber = TestObserver<String>()

        val subjectUnderTest = HotelMapViewModel(RuntimeEnvironment.application, endlessObserver {  }, PublishSubject.create<Boolean>(), LineOfBusiness.HOTELS)
        subjectUnderTest.selectRoomContDescription.subscribe(selectRoomContDescriptionSubscriber)
        subjectUnderTest.offersObserver.onNext(hotelOffersResponse)

        assertEquals("air_attached_hotel", subjectUnderTest.hotelName.value)
        assertEquals(4f, subjectUnderTest.hotelStarRating.value)
        assertEquals(true, subjectUnderTest.hotelStarRatingVisibility.value)
        assertEquals("$284", subjectUnderTest.strikethroughPrice.value.toString())
        assertEquals("From $241", subjectUnderTest.fromPrice.value.toString())
        assertEquals(37.78458, subjectUnderTest.hotelLatLng.value[0])
        assertEquals(-122.40854, subjectUnderTest.hotelLatLng.value[1])
        assertEquals(false, subjectUnderTest.selectARoomInvisibility.value)
        selectRoomContDescriptionSubscriber.assertValue("Select a Room From $241 button")

        val testSubscriber = TestObserver.create<Boolean>()
        subjectUnderTest.strikethroughPriceVisibility.subscribe(testSubscriber)
        subjectUnderTest.fromPriceVisibility.subscribe(testSubscriber)
        testSubscriber.assertValues(true)
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA, MultiBrand.ORBITZ, MultiBrand.CHEAPTICKETS, MultiBrand.TRAVELOCITY))
    fun testViewModelOutputsForViewWhenHotelStarRatingIsZero() {
        givenHotelOffersResponseWhenHotelStarRatingIsZero()

        val subjectUnderTest = HotelMapViewModel(RuntimeEnvironment.application, endlessObserver {  }, PublishSubject.create<Boolean>(), LineOfBusiness.HOTELS)
        subjectUnderTest.offersObserver.onNext(hotelOffersResponse)

        assertEquals("zero_star_rating", subjectUnderTest.hotelName.value)
        assertEquals(0f, subjectUnderTest.hotelStarRating.value)
        assertEquals(false, subjectUnderTest.hotelStarRatingVisibility.value)
        assertEquals("$284", subjectUnderTest.strikethroughPrice.value.toString())
        assertEquals("From $241", subjectUnderTest.fromPrice.value.toString())
        assertEquals(37.78458, subjectUnderTest.hotelLatLng.value[0])
        assertEquals(-122.40854, subjectUnderTest.hotelLatLng.value[1])
        assertEquals(false, subjectUnderTest.selectARoomInvisibility.value)

        val testSubscriber = TestObserver.create<Boolean>()
        subjectUnderTest.strikethroughPriceVisibility.subscribe(testSubscriber)
        subjectUnderTest.fromPriceVisibility.subscribe(testSubscriber)
        testSubscriber.assertValues(true)
    }

    @Test fun testViewModelOutputsForViewWhenRoomOffersAreNotAvailable() {
        AbacusTestUtils.bucketTests(AbacusUtils.EBAndroidAppHotelHideStrikethroughPrice)
        givenHotelOffersResponseWhenRoomOffersAreNotAvailable()

        val hotelSoldOut = PublishSubject.create<Boolean>()
        val subjectUnderTest = HotelMapViewModel(RuntimeEnvironment.application, endlessObserver {  }, hotelSoldOut, LineOfBusiness.HOTELS)
        subjectUnderTest.offersObserver.onNext(hotelOffersResponse)
        hotelSoldOut.onNext(true)

        assertEquals("room_offers_not_available", subjectUnderTest.hotelName.value)
        assertEquals(4f, subjectUnderTest.hotelStarRating.value)
        assertEquals(true, subjectUnderTest.hotelStarRatingVisibility.value)
        assertEquals("", subjectUnderTest.strikethroughPrice.value.toString())
        assertEquals("", subjectUnderTest.fromPrice.value.toString())
        assertEquals(37.78458, subjectUnderTest.hotelLatLng.value[0])
        assertEquals(-122.40854, subjectUnderTest.hotelLatLng.value[1])
        assertEquals(true, subjectUnderTest.selectARoomInvisibility.value)

        val testSubscriber = TestObserver.create<Boolean>()
        subjectUnderTest.strikethroughPriceVisibility.subscribe(testSubscriber)
        subjectUnderTest.fromPriceVisibility.subscribe(testSubscriber)
        testSubscriber.assertValues(false)
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