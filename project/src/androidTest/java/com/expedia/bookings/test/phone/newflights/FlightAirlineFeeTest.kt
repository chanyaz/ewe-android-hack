package com.expedia.bookings.test.phone.newflights

import android.support.test.espresso.Espresso
import android.support.test.espresso.Espresso.onView
import android.support.test.espresso.action.ViewActions.click
import android.support.test.espresso.assertion.ViewAssertions
import android.support.test.espresso.contrib.RecyclerViewActions
import android.support.test.espresso.matcher.ViewMatchers.isDescendantOfA
import android.support.test.espresso.matcher.ViewMatchers.isDisplayed
import android.support.test.espresso.matcher.ViewMatchers.withId
import android.support.test.espresso.matcher.ViewMatchers.withText
import android.support.v7.widget.RecyclerView
import com.expedia.bookings.R
import com.expedia.bookings.test.espresso.Common
import com.expedia.bookings.test.espresso.NewFlightTestCase
import com.expedia.bookings.test.espresso.ViewActions
import com.expedia.bookings.test.phone.hotels.HotelScreen
import com.expedia.bookings.test.phone.packages.PackageScreen
import com.expedia.bookings.test.phone.pagemodels.common.CardInfoScreen
import com.expedia.bookings.test.phone.pagemodels.common.CheckoutViewModel
import com.expedia.bookings.test.phone.pagemodels.common.SearchScreen
import org.hamcrest.Matchers.allOf
import org.joda.time.LocalDate
import org.junit.Test

class FlightAirlineFeeTest: NewFlightTestCase() {

    @Test
    fun testAirlineFeeStoredCard() {
        selectFlightsProceedToCheckout()
        assertCardFeeWarningShown()

        signIn()
        CheckoutViewModel.selectStoredCardWithName("Saved Visa 1111")
        assertCheckoutOverviewCardFeeWarningShown()
    }

    @Test
    fun testAirlineFeeGuestCheckout() {
        selectFlightsProceedToCheckout()
        assertCardFeeWarningShown()

        manuallyEnterCardInfo()
        assertPaymentFormCardFeeWarningShown()

        PackageScreen.completePaymentForm()
        CheckoutViewModel.clickDone()

        assertCheckoutOverviewCardFeeWarningShown()
    }

    private fun assertCheckoutOverviewCardFeeWarningShown() {
        onView(withId(R.id.card_fee_warning_text)).perform(ViewActions.waitForViewToDisplay())
                .check(ViewAssertions.matches(isDisplayed()))
                .check(ViewAssertions.matches(withText("The airline charges a processing fee of $2.94 for using this card (cost included in the trip total).")))
    }

    private fun assertPaymentFormCardFeeWarningShown() {
        onView(withId(R.id.card_processing_fee)).perform(ViewActions.waitForViewToDisplay())
                .check(ViewAssertions.matches(isDisplayed()))
                .check(ViewAssertions.matches(withText("Airline processing fee for this card: $2.94")))
    }

    private fun signIn() {
        HotelScreen.doLogin()
    }

    private fun manuallyEnterCardInfo() {
        CheckoutViewModel.clickPaymentInfo()
        Common.delay(1)
        CardInfoScreen.typeTextCreditCardEditText("4111111111111111")
    }

    private fun assertCardFeeWarningShown() {
        FlightsOverviewScreen.assertCardFeeWarningShown()
        FlightsOverviewScreen.cardFeeWarningTextView().perform(click())
        val paymentFeeWebViewBundleOverview = onView(allOf(withId(R.id.web_view), isDescendantOfA(withId(R.id.widget_bundle_overview))))
        paymentFeeWebViewBundleOverview.check(ViewAssertions.matches(isDisplayed()))

        Espresso.pressBack()
    }

    private fun selectFlightsProceedToCheckout() {
        SearchScreen.origin().perform(click())
        SearchScreen.searchEditText().perform(android.support.test.espresso.action.ViewActions.typeText("AUK"))
        SearchScreen.suggestionList().perform(ViewActions.waitForViewToDisplay())
        SearchScreen.suggestionList().perform(RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(5, click()))

        //Delay for the auto advance to destination picker
        Common.delay(1)
        SearchScreen.searchEditText().perform(ViewActions.waitForViewToDisplay())
        SearchScreen.searchEditText().perform(android.support.test.espresso.action.ViewActions.typeText("SFO"))
        SearchScreen.suggestionList().perform(ViewActions.waitForViewToDisplay())
        SearchScreen.suggestionList().perform(RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(0, click()))

        val startDate = LocalDate.now().plusDays(3)
        val endDate = LocalDate.now().plusDays(8)
        SearchScreen.selectDates(startDate, endDate)
        SearchScreen.searchButton().perform(click())
        FlightTestHelpers.assertFlightOutbound()

        FlightsResultsScreen.assertAirlineChargesFeesHeadingShown(withId(R.id.widget_flight_outbound))

        FlightsScreen.selectFlight(FlightsScreen.outboundFlightList(), 0)
        FlightsResultsScreen.assertPaymentFeesMayApplyLinkShowing(withId(R.id.widget_flight_outbound))
        FlightsResultsScreen.paymentFeesLinkTextView(withId(R.id.widget_flight_outbound)).perform(click())
        val paymentFeeWebViewOutboundResults = onView(allOf(withId(R.id.web_view), isDescendantOfA(withId(R.id.widget_flight_outbound)), isDescendantOfA(withId(R.id.payment_fee_info))))
        paymentFeeWebViewOutboundResults.check(ViewAssertions.matches(isDisplayed()))
        Espresso.pressBack()

        FlightsScreen.selectOutboundFlight().perform(click())
        FlightTestHelpers.assertFlightInbound()
        FlightsScreen.selectFlight(FlightsScreen.inboundFlightList(), 0)

        FlightsResultsScreen.assertPaymentFeesMayApplyLinkShowing(withId(R.id.widget_flight_inbound))
        FlightsResultsScreen.paymentFeesLinkTextView(withId(R.id.widget_flight_inbound)).perform(click())
        val paymentFeeWebViewInboundResults = onView(allOf(withId(R.id.web_view), isDescendantOfA(withId(R.id.widget_flight_inbound)), isDescendantOfA(withId(R.id.payment_fee_info))))
        paymentFeeWebViewInboundResults.check(ViewAssertions.matches(isDisplayed()))
        Espresso.pressBack()

        FlightsScreen.selectInboundFlight().perform(click())
        PackageScreen.checkout().perform(click())
    }
}
