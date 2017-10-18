package com.expedia.vm.test.robolectric

import android.app.Activity
import android.support.v7.app.AppCompatActivity
import com.expedia.bookings.R
import com.expedia.bookings.data.TripDetails
import com.expedia.bookings.data.flights.FlightCheckoutResponse
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.test.robolectric.shadows.ShadowAccountManagerEB
import com.expedia.bookings.test.robolectric.shadows.ShadowGCM
import com.expedia.bookings.test.robolectric.shadows.ShadowUserManager
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.widget.packages.HotelCrossSellViewModel
import org.joda.time.DateTime
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.Shadows
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowApplication
import rx.observers.TestSubscriber
import kotlin.properties.Delegates

@RunWith(RobolectricRunner::class)

class FlightAirAttachViewModelTest {
    private var viewModel: HotelCrossSellViewModel by Delegates.notNull()
    private var activity: Activity by Delegates.notNull()

    @Before
    fun before() {
        activity = Robolectric.buildActivity(AppCompatActivity::class.java).create().get()
        activity.setTheme(R.style.Theme_Hotels_Default)
        Ui.getApplication(activity).defaultHotelComponents()
    }

    @Test
    fun crossSellFutureExpirationTest() {
        val expiresInFuture = DateTime.now().plusDays(50).toString()
        val response = getCheckoutResponse(expiresInFuture)

        val crossSellDaysRemaining = TestSubscriber<String>()
        val crossSellExpiresTodayView = TestSubscriber<Boolean>()
        val crossSellExpiresFutureView = TestSubscriber<Boolean>()

        viewModel = HotelCrossSellViewModel(activity)
        viewModel.daysRemainingVisibility.subscribe(crossSellExpiresFutureView)
        viewModel.daysRemainingText.subscribe(crossSellDaysRemaining)
        viewModel.expiresTodayVisibility.subscribe(crossSellExpiresTodayView)

        viewModel.confirmationObservable.onNext(response)

        crossSellExpiresFutureView.assertValue(true)
        crossSellDaysRemaining.assertValue("50 days")
        crossSellExpiresTodayView.assertNoValues()
    }

    @Test
    fun crossSellExpirationTodayTest() {
        val expiresToday = DateTime.now().toString()
        val response = getCheckoutResponse(expiresToday)
        val crossSellExpiresTodayView = TestSubscriber<Boolean>()
        val crossSellExpiresFutureView = TestSubscriber<Boolean>()

        viewModel = HotelCrossSellViewModel(activity)
        viewModel.daysRemainingVisibility.subscribe(crossSellExpiresFutureView)
        viewModel.expiresTodayVisibility.subscribe(crossSellExpiresTodayView)
        viewModel.confirmationObservable.onNext(response)

        crossSellExpiresFutureView.assertNoValues()
        crossSellExpiresTodayView.assertValue(true)
    }

    @Test
    fun crossSellExpirationPastTest() {
//        note this scenario is unlikely
        val pastExpiration = DateTime.now().minusDays(50).toString()
        val response = getCheckoutResponse(pastExpiration)

        val crossSellExpiresTodayView = TestSubscriber<Boolean>()
        val crossSellExpiresFutureView = TestSubscriber<Boolean>()

        viewModel = HotelCrossSellViewModel(activity)
        viewModel.daysRemainingVisibility.subscribe(crossSellExpiresFutureView)
        viewModel.expiresTodayVisibility.subscribe(crossSellExpiresTodayView)
        viewModel.confirmationObservable.onNext(response)

        crossSellExpiresFutureView.assertNoValues()
        crossSellExpiresTodayView.assertValue(true)
    }

   private fun getCheckoutResponse(dateOfExpiration: String) : FlightCheckoutResponse {
        val response = FlightCheckoutResponse()
        response.newTrip = TripDetails("12345", "", "")
        val qualifierObject = FlightCheckoutResponse.AirAttachInfo()
        val offerTimeField = FlightCheckoutResponse.AirAttachInfo.AirAttachExpirationInfo()

        val field = response.javaClass.getDeclaredField("airAttachInfo")
        field.isAccessible = true

        val boolField = qualifierObject.javaClass.getDeclaredField("hasAirAttach")
        boolField.isAccessible = true

        val timeRemainingField = qualifierObject.javaClass.getDeclaredField("offerExpirationTimes")
        timeRemainingField.isAccessible = true

        val timeField = offerTimeField.javaClass.getDeclaredField("fullExpirationDate")
        timeField.isAccessible = true

        timeField.set(offerTimeField , dateOfExpiration)
        boolField.set(qualifierObject, true)
        timeRemainingField.set(qualifierObject, offerTimeField )
        field.set(response, qualifierObject)

        return response
    }
}