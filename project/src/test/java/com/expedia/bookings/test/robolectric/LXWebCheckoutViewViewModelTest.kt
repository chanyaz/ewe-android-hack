package com.expedia.bookings.test.robolectric

import com.expedia.bookings.data.lx.LXCreateTripResponseV2
import com.expedia.bookings.data.pos.PointOfSale
import com.expedia.bookings.data.pos.PointOfSaleId
import com.expedia.bookings.services.TestObserver
import com.expedia.bookings.utils.Ui
import com.expedia.util.Optional
import com.expedia.vm.LXWebCheckoutViewViewModel
import com.expedia.vm.lx.LXCreateTripViewModel
import com.mobiata.android.util.SettingUtils
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RuntimeEnvironment
import kotlin.test.assertEquals

@RunWith(RobolectricRunner::class)
class LXWebCheckoutViewViewModelTest {

    private fun getContext() = RuntimeEnvironment.application

    lateinit var webCheckoutViewModel: LXWebCheckoutViewViewModel
    lateinit var createTripResponse: LXCreateTripResponseV2

    @Before
    fun setup() {
        webCheckoutViewModel = LXWebCheckoutViewViewModel(getContext(), Ui.getApplication(getContext()).appComponent().endpointProvider(), LXCreateTripViewModel(getContext()))
        createTripResponse = LXCreateTripResponseV2()
        createTripResponse.tripId = "12345"
    }

    @Test
    fun testWebViewURLObservableForUSPOS() {
        val testSubscriber = TestObserver<String>()
        webCheckoutViewModel.webViewURLObservable.subscribe(testSubscriber)
        webCheckoutViewModel.lxCreateTripViewModel.createTripResponseObservable.onNext(Optional(createTripResponse))

        assertEquals("https://www.expedia.com/MultiItemCheckout?tripid=12345", testSubscriber.values()[0])
    }

    @Test
    fun testWebViewURLObservableForUKPOS() {
        SettingUtils.save(getContext(), "point_of_sale_key", PointOfSaleId.UNITED_KINGDOM.id.toString())
        PointOfSale.onPointOfSaleChanged(getContext())
        webCheckoutViewModel = LXWebCheckoutViewViewModel(getContext(), Ui.getApplication(getContext()).appComponent().endpointProvider(), LXCreateTripViewModel(getContext()))
        val testSubscriber = TestObserver<String>()
        webCheckoutViewModel.webViewURLObservable.subscribe(testSubscriber)
        webCheckoutViewModel.lxCreateTripViewModel.createTripResponseObservable.onNext(Optional(createTripResponse))

        assertEquals("https://www.expedia.co.uk/MultiItemCheckout?tripid=12345", testSubscriber.values()[0])
    }
}
