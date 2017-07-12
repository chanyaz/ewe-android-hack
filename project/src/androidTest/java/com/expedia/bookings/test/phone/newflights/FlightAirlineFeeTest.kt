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
import com.expedia.bookings.test.pagemodels.hotels.HotelScreen
import com.expedia.bookings.test.pagemodels.packages.PackageScreen
import com.expedia.bookings.test.pagemodels.common.CardInfoScreen
import com.expedia.bookings.test.pagemodels.common.CheckoutViewModel
import com.expedia.bookings.test.pagemodels.common.SearchScreen
import com.expedia.bookings.test.pagemodels.flights.FlightsOverviewScreen
import com.expedia.bookings.test.pagemodels.flights.FlightsResultsScreen
import com.expedia.bookings.test.pagemodels.flights.FlightsScreen
import com.mobiata.android.util.SettingUtils
import com.mobiata.mocke3.FlightApiMockResponseGenerator
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
        CheckoutViewModel.clickPaymentInfo()
        assertCheckoutOverviewCardFeeWarningShown()
        CheckoutViewModel.selectStoredCard("Saved Visa 1111")
        assertCheckoutOverviewCardFeeWarningShown()
        Common.pressBack()

        assertCheckoutOverviewCardFeeWarningShown()
        assertCostSummaryDialogShowsFees()
    }

    @Test
    fun testAirlineFeeGuestCheckout() {
        selectFlightsProceedToCheckout()
        assertCardFeeWarningShown()
        assertCheckoutOverviewMayChargeCardFeeTextShown()

        CheckoutViewModel.clickPaymentInfo()
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

        CheckoutViewModel.clickPaymentInfo()
        CardInfoScreen.assertCardInfoLabelShown()
        CardInfoScreen.typeTextCreditCardEditText("4111111111111111")
        CardInfoScreen.assertPaymentFormCardFeeWarningShown("Payment method fee: $2.50")

        PackageScreen.completePaymentForm()
        Common.pressBack()

        assertCheckoutOverviewCardFeeWarningShown()
        assertCostSummaryDialogShowsFees()

        // clear existing card number
        signIn()
        CheckoutViewModel.clickPaymentInfo()
        CheckoutViewModel.clickAddCreditCard() // resets card details
        CardInfoScreen.assertPaymentFormCardFeeWarningNotShown()
    }

    @Test
    fun testAirlineMayChargeFeesAlwaysShownAustraliaPOS() {
        SettingUtils.save(activity.applicationContext, R.string.preference_payment_legal_message, true)
        Common.setPOS(PointOfSaleId.AUSTRALIA)

        SearchScreen.selectFlightOriginAndDestination(FlightApiMockResponseGenerator.SuggestionResponseType.HAPPY_PATH, 0)

        val startDate = LocalDate.now().plusDays(3)
        val endDate = LocalDate.now().plusDays(8)
        SearchScreen.selectDates(startDate, endDate)
        SearchScreen.searchButton().perform(click())
        FlightTestHelpers.assertFlightOutbound()

        FlightsResultsScreen.assertAirlineChargesFeesHeadingShown(withId(R.id.widget_flight_outbound), R.string.airline_additional_fee_notice)
    }

    @Test
    fun testAirlineMayChargeFeesAlwaysShownFrenchPOS() {
        SettingUtils.save(activity.applicationContext, R.string.preference_payment_legal_message, false)
        Common.setPOS(PointOfSaleId.FRANCE)

        SearchScreen.selectFlightOriginAndDestination(FlightApiMockResponseGenerator.SuggestionResponseType.HAPPY_PATH, 0)

        val startDate = LocalDate.now().plusDays(3)
        val endDate = LocalDate.now().plusDays(8)
        SearchScreen.selectDates(startDate, endDate)
        SearchScreen.searchButton().perform(click())
        FlightTestHelpers.assertFlightOutbound()

        FlightsResultsScreen.assertAirlineChargesFeesHeadingShown(withId(R.id.widget_flight_outbound), R.string.airline_may_charge_notice)
    }

    private fun assertCostSummaryDialogShowsFees() {
        val cardFee = "$2.50"
        onView(withId(R.id.bundle_total_text)).perform(click())
        onView(withText("Payment Method Fee")).check(ViewAssertions.matches(isDisplayed()))
        onView(withText(cardFee)).check(ViewAssertions.matches(isDisplayed()))
        onView(withId(android.R.id.button1)).perform(click())
    }

    private fun assertCheckoutOverviewMayChargeCardFeeTextShown() {
        Common.delay(2) // We wait for a short delay (in implementation) jic customer changes their card
        onView(withId(R.id.card_fee_warning_text)).perform(ViewActions.waitForViewToDisplay())
                .check(ViewAssertions.matches(isDisplayed()))
                .check(ViewAssertions.matches(withText("An airline fee, based on card type, is added upon payment. Such fee is added to the total upon payment.")))
    }

    private fun assertCheckoutOverviewCardFeeWarningShown() {
        Common.delay(2) // We wait for a short delay (in implementation) jic customer changes their card
        onView(withId(R.id.card_fee_warning_text)).perform(ViewActions.waitForViewToDisplay())
                .check(ViewAssertions.matches(isDisplayed()))
                .check(ViewAssertions.matches(withText("A payment method fee of $2.50 is included in the trip total.")))
    }

    private fun signIn() {
        HotelScreen.doLogin()
    }

    private fun assertCardFeeWarningShown() {
        FlightsOverviewScreen.assertCardFeeWarningShown()
        FlightsOverviewScreen.cardFeeWarningTextView().perform(scrollTo(), click())
        val paymentFeeWebViewBundleOverview = onView(allOf(withId(R.id.payment_fee_info_webview), isDescendantOfA(withId(R.id.widget_bundle_overview))))
        paymentFeeWebViewBundleOverview.check(ViewAssertions.matches(isDisplayed()))

        Espresso.pressBack()
    }

    private fun selectFlightsProceedToCheckout() {
        SearchScreen.selectFlightOriginAndDestination(FlightApiMockResponseGenerator.SuggestionResponseType.MAY_CHARGE_OB_FEES, 0)

        val startDate = LocalDate.now().plusDays(3)
        val endDate = LocalDate.now().plusDays(8)
        SearchScreen.selectDates(startDate, endDate)
        SearchScreen.searchButton().perform(click())
        FlightTestHelpers.assertFlightOutbound()

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
        FlightsResultsScreen.paymentFeesLinkTextView(withId(R.id.widget_flight_inbound)).perform(scrollTo(), click())
        val paymentFeeWebViewInboundResults = onView(allOf(withId(R.id.web_view), isDescendantOfA(withId(R.id.widget_flight_inbound)), isDescendantOfA(withId(R.id.payment_fee_info))))
        paymentFeeWebViewInboundResults.check(ViewAssertions.matches(isDisplayed()))
        Espresso.pressBack()

        FlightsScreen.selectInboundFlight().perform(click())
        PackageScreen.checkout().perform(click())
    }
}
