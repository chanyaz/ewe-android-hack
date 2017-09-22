package com.expedia.vm.test

import android.app.Activity
import com.expedia.bookings.R
import com.expedia.bookings.data.hotels.HotelCreateTripParams
import com.expedia.bookings.data.hotels.RoomInfoFields
import com.expedia.bookings.data.pos.PointOfSale
import com.expedia.bookings.data.pos.PointOfSaleId
import com.expedia.bookings.services.HotelServices
import com.expedia.bookings.test.MockHotelServiceTestRule
import com.expedia.bookings.test.MultiBrand
import com.expedia.bookings.test.RunForBrands
import com.expedia.bookings.test.robolectric.HotelPresenterTestUtil
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.testrule.ServicesRule
import com.expedia.vm.HotelCreateTripViewModel
import com.expedia.vm.HotelWebCheckoutViewViewModel
import com.expedia.vm.WebCheckoutViewViewModel
import com.mobiata.android.util.SettingUtils
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RuntimeEnvironment
import rx.observers.TestSubscriber
import kotlin.test.assertEquals
import kotlin.test.assertFalse

@RunWith(RobolectricRunner::class)
class hotelWebCheckoutViewViewModelTest {

    lateinit var hotelWebCheckoutViewViewModel: HotelWebCheckoutViewViewModel

    var servicesRule = ServicesRule(HotelServices::class.java)
        @Rule get

    private fun getContext() = RuntimeEnvironment.application

    var mockHotelServiceTestRule: MockHotelServiceTestRule = MockHotelServiceTestRule()
        @Rule get

    lateinit var activity: Activity

    @Before
    fun setup() {
        hotelWebCheckoutViewViewModel = HotelWebCheckoutViewViewModel(getContext())
        hotelWebCheckoutViewViewModel.createTripViewModel = HotelCreateTripViewModel(servicesRule.services!!, null)
        activity = Robolectric.buildActivity(Activity::class.java).create().get()
        activity.setTheme(R.style.Theme_Hotels_Default)
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
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun testFunctionalityOfDoCreateTrip() {
        var hotelSearchParams = HotelPresenterTestUtil.getDummyHotelSearchParams(activity)
        var roomInfoFields = RoomInfoFields(2, listOf(10, 10, 10))
        var hotelOfferResponse = mockHotelServiceTestRule.getHappyOfferResponse()
        var offerResponse = hotelOfferResponse.hotelRoomResponse.first()
        var testSubscriber = TestSubscriber<HotelCreateTripParams>()
        hotelWebCheckoutViewViewModel.createTripViewModel.tripParams.subscribe(testSubscriber)

        hotelWebCheckoutViewViewModel.hotelSearchParamsObservable.onNext(hotelSearchParams)
        hotelWebCheckoutViewViewModel.offerObservable.onNext(offerResponse)
        hotelWebCheckoutViewViewModel.doCreateTrip()

        assertEquals(offerResponse.productKey, testSubscriber.onNextEvents[0].productKey)
        assertFalse(testSubscriber.onNextEvents[0].qualityAirAttach)
        assertEquals(roomInfoFields.room, testSubscriber.onNextEvents[0].roomInfoFields.room)
    }
}