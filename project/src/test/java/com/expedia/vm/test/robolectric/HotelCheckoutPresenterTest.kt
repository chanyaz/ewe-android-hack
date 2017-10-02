package com.expedia.vm.test.robolectric

import android.support.v4.app.FragmentActivity
import android.view.LayoutInflater
import com.expedia.bookings.R
import com.expedia.bookings.data.hotels.HotelCreateTripParams
import com.expedia.bookings.data.hotels.HotelCreateTripResponse
import com.expedia.bookings.data.payment.PaymentModel
import com.expedia.bookings.data.payment.PaymentSplits
import com.expedia.bookings.presenter.hotel.HotelCheckoutPresenter
import com.expedia.bookings.services.LoyaltyServices
import com.expedia.bookings.test.MockHotelServiceTestRule
import com.expedia.bookings.test.ShampooRule
import com.expedia.bookings.test.robolectric.HotelPresenterTestUtil
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.testrule.ServicesRule
import com.expedia.bookings.utils.Ui
import com.expedia.vm.HotelCreateTripViewModel
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import rx.observers.TestSubscriber
import java.util.concurrent.TimeUnit


@RunWith(RobolectricRunner::class)
class HotelCheckoutPresenterTest {

    lateinit var activity: FragmentActivity
    lateinit var hotelCheckoutPresenter: HotelCheckoutPresenter
    val mockHotelServicesTestRule = MockHotelServiceTestRule()
        @Rule get

    var loyaltyServiceRule = ServicesRule(LoyaltyServices::class.java)
        @Rule get

//    val shampooRule = ShampooRule(100)
//        @Rule get

    lateinit var paymentModel: PaymentModel<HotelCreateTripResponse>

//    companion object {
//        var i = 0
//    }

    @Before
    fun setUp() {
        activity = Robolectric.buildActivity(FragmentActivity::class.java).create().get()
        activity.setTheme(R.style.Theme_Hotels_Default)
        Ui.getApplication(activity).defaultHotelComponents()
        hotelCheckoutPresenter = LayoutInflater.from(activity).inflate(R.layout.hotel_checkout_presenter_stub, null) as HotelCheckoutPresenter
        paymentModel = hotelCheckoutPresenter.hotelCheckoutWidget.paymentModel

        hotelCheckoutPresenter.hotelCheckoutWidget.createTripViewmodel = HotelCreateTripViewModel(mockHotelServicesTestRule.services!!, paymentModel)
        hotelCheckoutPresenter.hotelCheckoutWidget.setSearchParams(HotelPresenterTestUtil.getDummyHotelSearchParams(activity))
    }


    @Test
    fun testRandom() {
        for (i in 1..100) {
            println(i.toString())

            val paymentSplitsTestSubscriber = TestSubscriber<PaymentSplits>()
            val mainProblemSubscriber = TestSubscriber<Any>()
            val newProblemSubscriber = TestSubscriber<Any>()
            paymentModel.swpOpted.onNext(false)
            paymentModel.pwpOpted.onNext(true)

            paymentModel.paymentSplits.subscribe(paymentSplitsTestSubscriber)
            paymentModel.paymentSplitsWithLatestTripTotalPayableAndTripResponse.subscribe(mainProblemSubscriber)
            hotelCheckoutPresenter.hotelCheckoutWidget.createTripViewmodel.tripParams.onNext(givenGoodCreateTripParams())
            hotelCheckoutPresenter.hotelCheckoutWidget.hotelCheckoutSummaryWidget.viewModel.newDataObservable.subscribe(newProblemSubscriber)

            paymentSplitsTestSubscriber.awaitTerminalEvent(10, TimeUnit.SECONDS)
            paymentSplitsTestSubscriber.assertValueCount(1)
//        mainProblemSubscriber.awaitTerminalEvent(10, TimeUnit.SECONDS)
//        mainProblemSubscriber.assertValueCount(1)
//        newProblemSubscriber.awaitTerminalEvent(10, TimeUnit.SECONDS)
//        newProblemSubscriber.assertValueCount(1)
            println("Success")
        }
    }

    private fun givenGoodCreateTripParams(): HotelCreateTripParams {
        return HotelCreateTripParams("hotel_price_change_up", false, 1, listOf(1))
    }


}