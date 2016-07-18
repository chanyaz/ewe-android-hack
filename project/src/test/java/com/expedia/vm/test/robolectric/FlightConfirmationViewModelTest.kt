package com.expedia.vm.test.robolectric

import android.content.Context
import com.expedia.bookings.data.TripDetails
import com.expedia.bookings.data.flights.FlightCheckoutResponse
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.vm.flights.FlightConfirmationViewModel
import org.joda.time.DateTime
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RuntimeEnvironment
import rx.observers.TestSubscriber

@RunWith(RobolectricRunner::class)

class FlightConfirmationViewModelTest {
    val viewModel = TestFlightConfirmationViewModelImpl(getContext())
    val customerEmail = "fakeEmail@mobiata.com"


    @Test
    fun flightConfirmationViewModelTest() {
        val expiresInFuture = DateTime.now().plusDays(50).toString()
        val response = getCheckoutResponse(expiresInFuture)
        val destination = "Detroit"
        val userPoints = "100"

        val destinationTestSubscriber= TestSubscriber<String>()
        val itinNumberTestSubscriber = TestSubscriber<String>()
        val expediaPointsSubscriber = TestSubscriber<String>()
        val crossSellWidgetView = TestSubscriber<Boolean>()


        viewModel.destinationObservable.subscribe(destinationTestSubscriber)
        viewModel.itinNumberMessageObservable.subscribe(itinNumberTestSubscriber)
        viewModel.rewardsPointsObservable.subscribe(expediaPointsSubscriber)
        viewModel.crossSellWidgetVisibility.subscribe(crossSellWidgetView)


        viewModel.destinationObservable.onNext(destination)
        viewModel.rewardPointsObservable.onNext(userPoints)
        viewModel.confirmationObservable.onNext(Pair(response, customerEmail))

        destinationTestSubscriber.assertValue(destination)
        itinNumberTestSubscriber.assertValue("#${response.newTrip.itineraryNumber} sent to $customerEmail")
        expediaPointsSubscriber.assertValue("$userPoints Expedia+ Rewards Points")
        crossSellWidgetView.assertValue(true)
    }

    @Test
    fun crossSellNotOfferedTest() {
        val pastExpiration = DateTime.now().minusDays(50).toString()
        val response = getCheckoutResponseWithoutAirAttachOffer(pastExpiration)

        val crossSellExpiresTodayView = TestSubscriber<Boolean>()
        val crossSellExpiresFutureView = TestSubscriber<Boolean>()
        val crossSellWidgetView = TestSubscriber<Boolean>()

        viewModel.crossSellCountDownVisibility.subscribe(crossSellExpiresFutureView)
        viewModel.crossSellTodayVisibility.subscribe(crossSellExpiresTodayView)
        viewModel.crossSellWidgetVisibility.subscribe(crossSellWidgetView)
        viewModel.confirmationObservable.onNext(Pair(response, customerEmail))

        crossSellExpiresFutureView.assertNoValues()
        crossSellExpiresTodayView.assertNoValues()
        crossSellWidgetView.assertValue(false)
    }

    @Test
    fun crossSellFutureExpirationTest(){
        val expiresInFuture = DateTime.now().plusDays(50).toString()
        val response = getCheckoutResponse(expiresInFuture)
        val crossSellDaysRemaining = TestSubscriber<String>()
        val crossSellExpiresTodayView = TestSubscriber<Boolean>()


        viewModel.crossSellText.subscribe(crossSellDaysRemaining)
        viewModel.crossSellTodayVisibility.subscribe(crossSellExpiresTodayView)

        viewModel.confirmationObservable.onNext(Pair(response, customerEmail))

        crossSellDaysRemaining.assertValue("50 days")
        crossSellExpiresTodayView.assertNoValues()
    }

    @Test
    fun crossSellExpirationTodayTest(){
        val expiresToday = DateTime.now().toString()
        val response = getCheckoutResponse(expiresToday)
        val crossSellExpiresTodayView = TestSubscriber<Boolean>()
        val crossSellExpiresFutureView = TestSubscriber<Boolean>()

        viewModel.crossSellCountDownVisibility.subscribe(crossSellExpiresFutureView)
        viewModel.crossSellTodayVisibility.subscribe(crossSellExpiresTodayView)
        viewModel.confirmationObservable.onNext(Pair(response, customerEmail))

        crossSellExpiresFutureView.assertNoValues()
        crossSellExpiresTodayView.assertValue(true)
    }

    @Test
    fun crossSellExpirationPastTest(){
        val pastExpiration = DateTime.now().minusDays(50).toString()
        val response = getCheckoutResponse(pastExpiration)

        val crossSellExpiresTodayView = TestSubscriber<Boolean>()
        val crossSellExpiresFutureView = TestSubscriber<Boolean>()

        viewModel.crossSellCountDownVisibility.subscribe(crossSellExpiresFutureView)
        viewModel.crossSellTodayVisibility.subscribe(crossSellExpiresTodayView)
        viewModel.confirmationObservable.onNext(Pair(response, customerEmail))

        crossSellExpiresFutureView.assertNoValues()
        crossSellExpiresTodayView.assertValue(true)
    }

    fun getCheckoutResponse(dateOfExpiration: String) : FlightCheckoutResponse {
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

    fun getCheckoutResponseWithoutAirAttachOffer(dateOfExpiration: String) : FlightCheckoutResponse {
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
        boolField.set(qualifierObject, false)
        timeRemainingField.set(qualifierObject, offerTimeField )
        field.set(response, qualifierObject)

        return response
    }

    private fun getContext(): Context {
        return RuntimeEnvironment.application
    }

    class TestFlightConfirmationViewModelImpl(context: Context): FlightConfirmationViewModel(context) {

        override fun isUserLoggedIn(): Boolean {
            return true
        }
    }
}