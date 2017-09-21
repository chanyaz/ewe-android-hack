package com.expedia.vm.test

import com.expedia.bookings.data.hotels.HotelCreateTripParams
import com.expedia.bookings.data.pos.PointOfSale
import com.expedia.bookings.data.pos.PointOfSaleId
import com.expedia.bookings.services.HotelServices
import com.expedia.bookings.test.MultiBrand
import com.expedia.bookings.test.RunForBrands
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
import org.robolectric.RuntimeEnvironment
import rx.observers.TestSubscriber

@RunWith(RobolectricRunner::class)
class WebCheckoutViewViewModelTest {

    lateinit var webCheckoutViewViewModel: HotelWebCheckoutViewViewModel

    var servicesRule = ServicesRule(HotelServices::class.java)
        @Rule get

    private fun getContext() = RuntimeEnvironment.application

    @Before
    fun setup() {
        webCheckoutViewViewModel = HotelWebCheckoutViewViewModel(getContext())
        webCheckoutViewViewModel.createTripViewModel = HotelCreateTripViewModel(servicesRule.services!!, null)
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun testWebViewURLObservable() {
        SettingUtils.save(getContext(), "point_of_sale_key", PointOfSaleId.INDIA.id.toString())
        PointOfSale.onPointOfSaleChanged(getContext())
        val webViewURLSubscriber = TestSubscriber<String>()
        webCheckoutViewViewModel.webViewURLObservable.subscribe(webViewURLSubscriber)
        val hotelCreateTripParams = HotelCreateTripParams("happypath_0", false, 1, arrayListOf())
        webCheckoutViewViewModel.createTripViewModel.tripParams.onNext(hotelCreateTripParams)
        webViewURLSubscriber.assertValueCount(1)
        webViewURLSubscriber.assertValue("${PointOfSale.getPointOfSale().hotelsWebCheckoutURL}?tripid=happypath_0")
    }
}