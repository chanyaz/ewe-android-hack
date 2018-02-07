package com.expedia.bookings.test.phone.newflights

import android.support.test.espresso.Espresso
import android.support.test.espresso.action.ViewActions
import android.support.test.espresso.assertion.ViewAssertions
import android.support.test.espresso.matcher.ViewMatchers
import com.expedia.bookings.R
import com.expedia.bookings.test.espresso.NewFlightTestCase
import com.expedia.bookings.test.pagemodels.packages.PackageScreen
import com.expedia.bookings.test.pagemodels.common.CardInfoScreen
import com.expedia.bookings.test.pagemodels.common.CheckoutScreen
import com.expedia.bookings.test.pagemodels.common.SearchScreen
import com.expedia.bookings.test.pagemodels.flights.FlightsScreen
import com.mobiata.mocke3.FlightApiMockResponseGenerator
import org.joda.time.LocalDate
import org.junit.Test

class FlightCheckoutInvalidPaymentTypeTest : NewFlightTestCase() {

    @Test
    fun testInvalidPaymentTypeWarningShown() {
        selectFlightsProceedToCheckout()
        assertWarningNotShown()

        CheckoutScreen.clickPaymentInfo()
        CardInfoScreen.assertCardInfoLabelShown()
        CardInfoScreen.typeTextCreditCardEditText("6333333333333333")
        assertWarningWarningShown()

        Espresso.pressBack()
        assertWarningNotShown()
    }

    @Test
    fun testValidPaymentTypeNoWarning() {
        selectFlightsProceedToCheckout()
        assertWarningNotShown()

        CheckoutScreen.clickPaymentInfo()
        CardInfoScreen.assertCardInfoLabelShown()
        CardInfoScreen.typeTextCreditCardEditText("4111111111111111")

        assertWarningNotShown()
        CardInfoScreen.assertCardInfoLabelShown()
        PackageScreen.completePaymentForm()
        CheckoutScreen.clickDone()

        assertWarningNotShown()
    }

    private fun assertWarningNotShown() {
        invalidPaymentTextView()
                .check(ViewAssertions.matches(ViewMatchers.withEffectiveVisibility(ViewMatchers.Visibility.GONE)))
    }

    private fun assertWarningWarningShown() {
        invalidPaymentTextView()
                .perform(com.expedia.bookings.test.espresso.ViewActions.waitForViewToDisplay())
                .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
                .check(ViewAssertions.matches(ViewMatchers.withText("Airline does not accept Maestro")))
    }

    private fun invalidPaymentTextView() = Espresso.onView(ViewMatchers.withId(R.id.invalid_payment_type_warning))

    private fun selectFlightsProceedToCheckout() {
        SearchScreen.selectFlightOriginAndDestination(FlightApiMockResponseGenerator.SuggestionResponseType.HAPPY_PATH, 0)

        val startDate = LocalDate.now().plusDays(3)
        val endDate = LocalDate.now().plusDays(8)
        SearchScreen.selectDates(startDate, endDate)
        SearchScreen.searchButton().perform(ViewActions.click())
        FlightTestHelpers.assertFlightOutbound()

        FlightsScreen.selectFlight(FlightsScreen.outboundFlightList(), 0)
        FlightsScreen.selectOutboundFlight().perform(ViewActions.click())
        FlightTestHelpers.assertFlightInbound()
        FlightsScreen.selectFlight(FlightsScreen.inboundFlightList(), 0)

        FlightsScreen.selectInboundFlight().perform(ViewActions.click())
        PackageScreen.checkout().perform(ViewActions.click())
    }
}
