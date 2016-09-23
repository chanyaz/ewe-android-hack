package com.expedia.bookings.test.phone.newflights

import android.support.test.espresso.action.ViewActions
import com.expedia.bookings.data.ApiError
import com.expedia.bookings.test.espresso.Common
import com.expedia.bookings.test.phone.packages.PackageScreen
import com.expedia.bookings.test.phone.pagemodels.common.CheckoutViewModel
import org.junit.Test

class FlightCheckoutErrorTest: FlightErrorTestCase() {

    @Test
    fun testCheckoutUnknownError() {
        searchForFlights(ApiError.Code.UNKNOWN_ERROR, FlightErrorTestCase.TestType.CHECKOUT)
        selectFirstOutboundFlight()
        selectFirstInboundFlight()
        PackageScreen.checkout().perform(ViewActions.click())

        PackageScreen.enterTravelerInfo()
        PackageScreen.enterPaymentInfo()

        CheckoutViewModel.performSlideToPurchase()

        assertFlightErrorPresenterDisplayed()
        assertButtonDisplayedWithText("Retry")
        assertErrorTextDisplayed("Whoops. Let\'s try that again.")
        assertToolbarTitle("Error")

        Common.pressBack()
        assertSearchFormDisplayed()
    }

    @Test
    fun testCheckoutPaymentFailedError() {
        searchForFlights(ApiError.Code.PAYMENT_FAILED, FlightErrorTestCase.TestType.CHECKOUT)
        selectFirstOutboundFlight()
        selectFirstInboundFlight()
        PackageScreen.checkout().perform(ViewActions.click())

        PackageScreen.enterTravelerInfo()
        PackageScreen.enterPaymentInfo()

        CheckoutViewModel.performSlideToPurchase()

        assertFlightErrorPresenterDisplayed()
        assertButtonDisplayedWithText("Edit Payment")
        assertErrorTextDisplayed("We\'re sorry, but we were unable to process your payment. Please verify that you entered your information correctly.")
        assertToolbarTitle("Payment Failed")

        clickActionButton()
        assertPaymentFormIsDisplayed()
    }

    @Test
    fun testCheckoutSessionTimeout() {
        searchForFlights(ApiError.Code.SESSION_TIMEOUT, FlightErrorTestCase.TestType.CHECKOUT)
        selectFirstOutboundFlight()
        selectFirstInboundFlight()
        PackageScreen.checkout().perform(ViewActions.click())

        PackageScreen.enterTravelerInfo()
        PackageScreen.enterPaymentInfo()

        CheckoutViewModel.performSlideToPurchase()

        assertFlightErrorPresenterDisplayed()
        assertButtonDisplayedWithText("New Search")
        assertErrorTextDisplayed("Still there? Your session has expired. Please try your search again.")
        assertToolbarTitle("Session Expired")

        clickActionButton()
        assertSearchFormDisplayed()
    }

    @Test
    fun testCheckoutTripAlreadyBooked() {
        searchForFlights(ApiError.Code.TRIP_ALREADY_BOOKED, FlightErrorTestCase.TestType.CHECKOUT)
        selectFirstOutboundFlight()
        selectFirstInboundFlight()
        PackageScreen.checkout().perform(ViewActions.click())

        PackageScreen.enterTravelerInfo()
        PackageScreen.enterPaymentInfo()

        CheckoutViewModel.performSlideToPurchase()

        assertConfirmationViewIsDisplayed()
    }


}