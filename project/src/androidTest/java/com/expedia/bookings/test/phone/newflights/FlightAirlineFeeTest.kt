package com.expedia.bookings.test.phone.newflights

import android.support.test.espresso.Espresso
import android.support.test.espresso.Espresso.onView
import android.support.test.espresso.action.ViewActions.click
import android.support.test.espresso.action.ViewActions.scrollTo
import android.support.test.espresso.assertion.ViewAssertions
import android.support.test.espresso.matcher.ViewMatchers.isDescendantOfA
import android.support.test.espresso.matcher.ViewMatchers.isDisplayed
import android.support.test.espresso.matcher.ViewMatchers.withId
import android.support.test.espresso.matcher.ViewMatchers.withText
import com.expedia.bookings.R
import com.expedia.bookings.data.pos.PointOfSaleId
import com.expedia.bookings.test.espresso.Common
import com.expedia.bookings.test.espresso.NewFlightTestCase
import com.expedia.bookings.test.espresso.ViewActions
import com.expedia.bookings.test.pagemodels.packages.PackageScreen
import com.expedia.bookings.test.pagemodels.common.CardInfoScreen
import com.expedia.bookings.test.pagemodels.common.CheckoutScreen
import com.expedia.bookings.test.pagemodels.common.SearchScreen
import com.expedia.bookings.test.pagemodels.common.SearchScreenActions
import com.expedia.bookings.test.pagemodels.flights.FlightsOverviewScreen
import com.expedia.bookings.test.pagemodels.flights.FlightsResultsScreen
import com.expedia.bookings.test.pagemodels.flights.FlightsScreen
import com.mobiata.mocke3.FlightDispatcherUtils
import org.hamcrest.Matchers.allOf
import org.hamcrest.Matchers.not
import org.joda.time.LocalDate
import org.junit.Test

class FlightAirlineFeeTest : NewFlightTestCase() {

    @Test
    fun testAirlineFeeStoredCard() {
        selectFlightsProceedToCheckout()
        assertCardFeeWarningShown()
        assertCheckoutOverviewMayChargeCardFeeTextShown()

        signIn()
        assertCheckoutOverviewCardFeeWarningShown()
        CheckoutScreen.clickPaymentInfo()
        assertCheckoutOverviewCardFeeWarningShown()
        CheckoutScreen.selectStoredCard("Saved Visa 1111")
        assertCheckoutOverviewCardFeeWarningShown()
        Common.pressBack()

        assertCheckoutOverviewCardFeeWarningShown()
        assertCostSummaryDialogShowsFees()

        Common.pressBack()
        PackageScreen.checkout().perform(click())
        assertCostSummaryDialogShowsFees()
    }

    @Test
    fun testAirlineFeeGuestCheckout() {
        selectFlightsProceedToCheckout()
        assertCardFeeWarningShown()
        assertCheckoutOverviewMayChargeCardFeeTextShown()

        CheckoutScreen.clickPaymentInfo()
        onView(withId(R.id.card_fee_warning_text)).check(ViewAssertions.matches(not(isDisplayed())))
        CardInfoScreen.assertCardInfoLabelShown()
        CardInfoScreen.typeTextCreditCardEditText("4111111111111111")
        CardInfoScreen.assertPaymentFormCardFeeWarningShown("Payment method fee: $2.50")

        PackageScreen.completePaymentForm()
        Common.pressBack()

        assertCheckoutOverviewCardFeeWarningShown()
        assertCostSummaryDialogShowsFees()
    }

    @Test
    fun testAirlineFeeReset() {
        selectFlightsProceedToCheckout()

        CheckoutScreen.clickPaymentInfo()
        CardInfoScreen.assertCardInfoLabelShown()
        CardInfoScreen.typeTextCreditCardEditText("4111111111111111")
        CardInfoScreen.assertPaymentFormCardFeeWarningShown("Payment method fee: $2.50")

        PackageScreen.completePaymentForm()
        Common.pressBack()

        assertCheckoutOverviewCardFeeWarningShown()
        assertCostSummaryDialogShowsFees()

        // clear existing card number
        signIn()
        CheckoutScreen.clickPaymentInfo()
        CheckoutScreen.clickAddCreditCard() // resets card details
        CardInfoScreen.assertPaymentFormCardFeeWarningNotShown()
    }

    @Test
    fun testAirlineMayChargeFeesAlwaysShownAustraliaPOS() {
        Common.setPOS(PointOfSaleId.AUSTRALIA)

        SearchScreenActions.selectFlightOriginAndDestination(FlightDispatcherUtils.SuggestionResponseType.HAPPY_PATH, 0)

        val startDate = LocalDate.now().plusDays(3)
        val endDate = LocalDate.now().plusDays(8)
        SearchScreenActions.chooseDatesWithDialog(startDate, endDate)
        SearchScreen.searchButton().perform(click())
        FlightTestHelpers.assertFlightOutbound()

        FlightsResultsScreen.assertAirlineChargesFeesHeadingShown(withId(R.id.widget_flight_outbound), R.string.airline_additional_fee_notice)
    }

    private fun assertCostSummaryDialogShowsFees() {
        val cardFee = "$2.50"
        onView(allOf(isDescendantOfA(withId(R.id.total_price_widget)), withId(R.id.bundle_total_text))).perform(click())
        onView(withText("Payment Method Fee")).check(ViewAssertions.matches(isDisplayed()))
        onView(withText(cardFee)).check(ViewAssertions.matches(isDisplayed()))
        onView(withId(android.R.id.button1)).perform(click())
    }

    private fun assertCheckoutOverviewMayChargeCardFeeTextShown() {
        Common.delay(2) // We wait for a short delay (in implementation) jic customer changes their card
        onView(withId(R.id.card_fee_warning_text)).perform(ViewActions.waitForViewToDisplay())
                .check(ViewAssertions.matches(isDisplayed()))
                .check(ViewAssertions.matches(withText("There may be an additional fee based on your payment method.")))
    }

    private fun assertCheckoutOverviewCardFeeWarningShown() {
        Common.delay(2) // We wait for a short delay (in implementation) jic customer changes their card
        onView(withId(R.id.card_fee_warning_text)).perform(ViewActions.waitForViewToDisplay())
                .check(ViewAssertions.matches(isDisplayed()))
                .check(ViewAssertions.matches(withText("A payment method fee of $2.50 is included in the trip total.")))
    }

    private fun signIn() {
        CheckoutScreen.loginAsQAUser()
    }

    private fun assertCardFeeWarningShown() {
        FlightsOverviewScreen.assertCardFeeWarningShown()
        FlightsOverviewScreen.cardFeeWarningTextView().perform(scrollTo(), click())
        val paymentFeeWebViewBundleOverview = onView(allOf(withId(R.id.payment_fee_info_webview), isDescendantOfA(withId(R.id.widget_bundle_overview))))
        paymentFeeWebViewBundleOverview.perform(ViewActions.waitForViewToDisplay()).check(ViewAssertions.matches(isDisplayed()))

        Espresso.pressBack()
    }

    private fun selectFlightsProceedToCheckout() {
        SearchScreenActions.selectFlightOriginAndDestination(FlightDispatcherUtils.SuggestionResponseType.MAY_CHARGE_OB_FEES, 0)
        val startDate = LocalDate.now().plusDays(3)
        val endDate = LocalDate.now().plusDays(8)
        SearchScreenActions.chooseDatesWithDialog(startDate, endDate)
        SearchScreen.searchButton().perform(click())
        FlightTestHelpers.assertFlightOutbound()
        FlightsScreen.selectFlight(FlightsScreen.outboundFlightList(), 0)
        FlightsResultsScreen.assertPaymentFeesMessageShowing(withId(R.id.widget_flight_outbound))
        FlightsScreen.selectOutboundFlight().perform(click())
        FlightTestHelpers.assertFlightInbound()
        FlightsScreen.selectFlight(FlightsScreen.inboundFlightList(), 0)
        FlightsResultsScreen.assertPaymentFeesMessageShowing(withId(R.id.widget_flight_inbound))
        FlightsScreen.selectInboundFlight().perform(click())
        PackageScreen.checkout().perform(click())
    }
}
