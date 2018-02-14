package com.expedia.bookings.test.robolectric

import android.content.Context
import com.expedia.bookings.data.hotels.HotelOffersResponse
import com.expedia.bookings.services.TestObserver
import com.expedia.bookings.utils.HotelsV2DataUtil
import com.expedia.vm.HotelInfoToolbarViewModel
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RuntimeEnvironment
import kotlin.properties.Delegates
import kotlin.test.assertTrue

@RunWith(RobolectricRunner::class)
class HotelInfoToolbarViewModelTest {

    private var vm: HotelInfoToolbarViewModel by Delegates.notNull()
    private var hotelOffer: HotelOffersResponse by Delegates.notNull()
    private var zeroRatingHotel: HotelOffersResponse by Delegates.notNull()
    private var soldOutHotelOffer: HotelOffersResponse by Delegates.notNull()

    private var context: Context by Delegates.notNull()

    @Before
    fun before() {
        context = RuntimeEnvironment.application
        vm = HotelInfoToolbarViewModel(context)

        hotelOffer = HotelOffersResponse()
        hotelOffer.hotelName = "hotel1"
        hotelOffer.hotelStarRating = 5.0
        hotelOffer.hotelRoomResponse = listOf(HotelOffersResponse.HotelRoomResponse())

        soldOutHotelOffer = HotelOffersResponse()
        soldOutHotelOffer.hotelName = "soldOutHotel"
        soldOutHotelOffer.hotelStarRating = 1.0
        soldOutHotelOffer.hotelRoomResponse = emptyList()

        zeroRatingHotel = HotelOffersResponse()
        zeroRatingHotel.hotelName = "zeroRatingHotel"
        zeroRatingHotel.hotelStarRating = 0.0
        zeroRatingHotel.hotelRoomResponse = listOf(HotelOffersResponse.HotelRoomResponse())
    }

    @Test
    fun testBindhotelOffersResponseForSoldOut() {
        val viewModelUnderTest = HotelInfoToolbarViewModel(context)
        val hotelNameSubscriber = TestObserver.create<String>()
        val hotelRatingSubscriber = TestObserver.create<Float>()
        val hotelRatingContentDescriptionSubscriber = TestObserver.create<String>()
        val toolBarRatingColorSubscriber = TestObserver.create<Int>()

        viewModelUnderTest.hotelNameObservable.subscribe(hotelNameSubscriber)
        viewModelUnderTest.hotelRatingObservable.subscribe(hotelRatingSubscriber)
        viewModelUnderTest.hotelRatingContentDescriptionObservable.subscribe(hotelRatingContentDescriptionSubscriber)

        viewModelUnderTest.bind(soldOutHotelOffer)

        assertTrue(viewModelUnderTest.hotelSoldOut.value == true)
        assertTrue(viewModelUnderTest.hotelRatingObservableVisibility.value == true)

        hotelNameSubscriber.assertValue(soldOutHotelOffer.hotelName)
        hotelRatingSubscriber.assertValue(1.0f)
        hotelRatingContentDescriptionSubscriber.assertValue(HotelsV2DataUtil.getHotelDetailRatingContentDescription(context, soldOutHotelOffer.hotelStarRating))
    }

    @Test
    fun testBindhotelOffersResponseForNotSoldOut() {
        val viewModelUnderTest = HotelInfoToolbarViewModel(context)
        val hotelNameSubscriber = TestObserver.create<String>()
        val hotelRatingSubscriber = TestObserver.create<Float>()
        val hotelRatingContentDescriptionSubscriber = TestObserver.create<String>()
        val toolBarRatingColorSubscriber = TestObserver.create<Int>()

        viewModelUnderTest.hotelNameObservable.subscribe(hotelNameSubscriber)
        viewModelUnderTest.hotelRatingObservable.subscribe(hotelRatingSubscriber)
        viewModelUnderTest.hotelRatingContentDescriptionObservable.subscribe(hotelRatingContentDescriptionSubscriber)

        viewModelUnderTest.bind(hotelOffer)

        assertTrue(viewModelUnderTest.hotelSoldOut.value == false)
        assertTrue(viewModelUnderTest.hotelRatingObservableVisibility.value == true)

        hotelNameSubscriber.assertValue(hotelOffer.hotelName)
        hotelRatingSubscriber.assertValue(5.0f)
        hotelRatingContentDescriptionSubscriber.assertValue(HotelsV2DataUtil.getHotelDetailRatingContentDescription(context, hotelOffer.hotelStarRating))
    }

    @Test
    fun testBindhotelOffersResponseForZeroRatingHotel() {
        val viewModelUnderTest = HotelInfoToolbarViewModel(context)
        viewModelUnderTest.bind(zeroRatingHotel)

        assertTrue(viewModelUnderTest.hotelRatingObservableVisibility.value == false)
    }
}
