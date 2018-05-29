package com.expedia.bookings.test.phone.newflights

import android.support.test.espresso.Espresso.onView
import android.support.test.espresso.action.ViewActions
import android.support.test.espresso.assertion.ViewAssertions.matches
import android.support.test.espresso.matcher.ViewMatchers.isDisplayed
import android.support.test.espresso.matcher.ViewMatchers.hasSibling
import android.support.test.espresso.matcher.ViewMatchers.withId
import android.support.test.espresso.matcher.ViewMatchers.withText
import com.expedia.bookings.R
import com.expedia.bookings.test.espresso.Common
import org.hamcrest.CoreMatchers.not
import org.hamcrest.CoreMatchers.allOf
import com.expedia.bookings.test.espresso.EspressoUtils.waitForViewNotYetInLayoutToDisplay
import com.expedia.bookings.test.espresso.EspressoUtils.assertViewWithTextIsDisplayed
import com.expedia.bookings.test.espresso.CustomMatchers.withImageDrawable

import com.expedia.bookings.test.pagemodels.packages.PackageScreen
import com.expedia.bookings.test.pagemodels.common.CheckoutScreen
import com.expedia.bookings.test.pagemodels.common.PaymentOptionsScreen
import com.mobiata.mocke3.FlightDispatcherUtils
import org.junit.Test
import java.util.concurrent.TimeUnit

class FlightCheckoutErrorTest : FlightErrorTestCase() {

    fun getToCheckoutWithFilledInTravelerDetails() {
        goToCheckout()
        PackageScreen.enterTravelerInfo()
    }

    private fun goToCheckout() {
        searchFlights(FlightDispatcherUtils.SuggestionResponseType.HAPPY_PATH)
        selectOutboundFlight(FlightDispatcherUtils.SearchResultsResponseType.HAPPY_ONE_WAY)
        PackageScreen.checkout().perform(ViewActions.click())
    }

    @Test
    fun testCheckoutUnknownError() {
        getToCheckoutWithFilledInTravelerDetails()
        PackageScreen.enterPaymentInfo("unknownerror lastname")

        CheckoutScreen.performSlideToPurchase()

        assertFlightErrorPresenterDisplayed()
        assertButtonDisplayedWithText("Retry")
        assertErrorTextDisplayed("Whoops. Let\'s try that again.")
        assertToolbarTitle("Error")

        Common.pressBack()
        assertSearchFormDisplayed()
    }

    @Test
    fun testGuestCheckoutPaymentFailedError() {
        getToCheckoutWithFilledInTravelerDetails()
        PackageScreen.enterPaymentInfo("paymentfailederror lastname")

        CheckoutScreen.performSlideToPurchase()

        assertFlightErrorPresenterDisplayed()
        assertButtonDisplayedWithText("Edit Payment")
        assertErrorTextDisplayed("We\'re sorry, but we were unable to process your payment. Please verify that you entered your information correctly.")
        assertToolbarTitle("Payment Failed")

        clickActionButton()
        assertPaymentFormIsDisplayed()
        assertViewWithTextIsDisplayed(R.id.edit_creditcard_number, "")
        assertViewWithTextIsDisplayed(R.id.edit_creditcard_cvv, "")
    }

    @Test
    fun testSignedInCheckoutPaymentFailedError() {
        goToCheckout()
        CheckoutScreen.signInOnCheckout()
        waitForViewNotYetInLayoutToDisplay(withId(R.id.login_widget), 10, TimeUnit.SECONDS)

        CheckoutScreen.clickPaymentInfo()
        val cardName = "Saved Payment failed Card"
        CheckoutScreen.selectStoredCard(cardName)
        PaymentOptionsScreen.assertCardSelectionMatches(cardName, 5)
        Common.pressBack()
        CheckoutScreen.performSlideToPurchase(true)

        assertFlightErrorPresenterDisplayed()
        assertButtonDisplayedWithText("Edit Payment")
        assertErrorTextDisplayed("We\'re sorry, but we were unable to process your payment. Please verify that you entered your information correctly.")
        assertToolbarTitle("Payment Failed")

        clickActionButton()
        onView(withId(R.id.stored_card_list)).check(matches(isDisplayed()))
        onView(allOf(withId(R.id.card_info_status_icon), hasSibling(withText(cardName)))).check(matches(not(withImageDrawable(R.drawable.validated))))
    }

    @Test
    fun testCheckoutSessionTimeout() {
        getToCheckoutWithFilledInTravelerDetails()
        PackageScreen.enterPaymentInfo("sessiontimeout lastname")

        CheckoutScreen.performSlideToPurchase()

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

        CheckoutScreen.performSlideToPurchase()

        assertConfirmationViewIsDisplayed()
    }
}
