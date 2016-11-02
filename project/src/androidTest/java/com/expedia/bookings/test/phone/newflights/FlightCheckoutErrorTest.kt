package com.expedia.bookings.test.phone.newflights

import android.support.test.espresso.action.ViewActions
import com.expedia.bookings.test.espresso.Common
import com.expedia.bookings.test.phone.packages.PackageScreen
import com.expedia.bookings.test.phone.pagemodels.common.CheckoutViewModel
import com.mobiata.mocke3.FlightApiMockResponseGenerator
import org.junit.Test

class FlightCheckoutErrorTest : FlightErrorTestCase() {

    fun getToCheckoutWithFilledInTravelerDetails() {
        searchFlights(FlightApiMockResponseGenerator.SuggestionResponseType.HAPPY_PATH)
        selectOutboundFlight(FlightApiMockResponseGenerator.SearchResultsResponseType.HAPPY_ONE_WAY)
        PackageScreen.checkout().perform(ViewActions.click())
        PackageScreen.enterTravelerInfo()
    }

    @Test
    fun testCheckoutUnknownError() {
        getToCheckoutWithFilledInTravelerDetails()
        PackageScreen.enterPaymentInfo("unknownerror lastname")

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
        getToCheckoutWithFilledInTravelerDetails()
        PackageScreen.enterPaymentInfo("paymentfailederror lastname")

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
        getToCheckoutWithFilledInTravelerDetails()
        PackageScreen.enterPaymentInfo("sessiontimeout lastname")

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
        getToCheckoutWithFilledInTravelerDetails()
        PackageScreen.enterPaymentInfo("tripalreadybooked lastname")

        CheckoutViewModel.performSlideToPurchase()

        assertConfirmationViewIsDisplayed()
    }


}