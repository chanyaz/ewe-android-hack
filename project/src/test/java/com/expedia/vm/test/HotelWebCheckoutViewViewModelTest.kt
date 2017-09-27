package com.expedia.vm.test

import com.expedia.bookings.data.hotels.HotelCreateTripParams
import com.expedia.bookings.data.pos.PointOfSale
import com.expedia.bookings.data.pos.PointOfSaleId
import com.expedia.bookings.services.HotelServices
import com.expedia.bookings.test.MultiBrand
import com.expedia.bookings.test.RunForBrands
import com.expedia.bookings.test.robolectric.HotelPresenterTestUtil
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.testrule.ServicesRule
import com.expedia.vm.HotelCreateTripViewModel
import com.expedia.vm.HotelWebCheckoutViewViewModel
import com.mobiata.android.util.SettingUtils
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RuntimeEnvironment
import rx.observers.TestSubscriber
import com.expedia.bookings.data.hotels.RoomInfoFields
import com.expedia.bookings.test.MockHotelServiceTestRule
import kotlin.test.assertFalse
import kotlin.test.assertEquals

@RunWith(RobolectricRunner::class)
class HotelWebCheckoutViewViewModelTest {

    lateinit var hotelWebCheckoutViewViewModel: HotelWebCheckoutViewViewModel

    var servicesRule = ServicesRule(HotelServices::class.java)
        @Rule get

    var mockHotelServiceTestRule: MockHotelServiceTestRule = MockHotelServiceTestRule()
        @Rule get

    private fun getContext() = RuntimeEnvironment.application

    @Before
    fun setup() {
        hotelWebCheckoutViewViewModel = HotelWebCheckoutViewViewModel(getContext())
        hotelWebCheckoutViewViewModel.createTripViewModel = HotelCreateTripViewModel(servicesRule.services!!, null)
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun testWebViewURLObservable() {
        SettingUtils.save(getContext(), "point_of_sale_key", PointOfSaleId.INDIA.id.toString())
        PointOfSale.onPointOfSaleChanged(getContext())
        val webViewURLSubscriber = TestSubscriber<String>()
        hotelWebCheckoutViewViewModel.webViewURLObservable.subscribe(webViewURLSubscriber)
        val hotelCreateTripParams = HotelCreateTripParams("happypath_0", false, 1, arrayListOf())
        hotelWebCheckoutViewViewModel.createTripViewModel.tripParams.onNext(hotelCreateTripParams)
        webViewURLSubscriber.assertValueCount(1)
        webViewURLSubscriber.assertValue("${PointOfSale.getPointOfSale().hotelsWebCheckoutURL}?tripid=happypath_0")
    }

    @Test
    fun testFunctionalityOfDoCreateTrip() {
        var hotelSearchParams = HotelPresenterTestUtil.getDummyHotelSearchParams(getContext())
        var testSubscriber = TestSubscriber<HotelCreateTripParams>()
        var roomInfoFields = RoomInfoFields(2, listOf(10, 10, 10))
        var hotelOfferResponse = mockHotelServiceTestRule.getHappyOfferResponse()
        var offerResponse = hotelOfferResponse.hotelRoomResponse.first()

        hotelWebCheckoutViewViewModel.hotelSearchParamsObservable.onNext(hotelSearchParams)
        hotelWebCheckoutViewViewModel.offerObservable.onNext(offerResponse)
        hotelWebCheckoutViewViewModel.createTripViewModel.tripParams.subscribe(testSubscriber)
        hotelWebCheckoutViewViewModel.doCreateTrip()

        assertEquals(offerResponse.productKey, testSubscriber.onNextEvents[0].productKey)
        assertFalse(testSubscriber.onNextEvents[0].qualityAirAttach)
        assertEquals(roomInfoFields.room, testSubscriber.onNextEvents[0].roomInfoFields.room)
    }

}