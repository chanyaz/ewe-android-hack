package com.expedia.bookings.test.phone.newflights

import android.support.test.espresso.Espresso
import android.support.test.espresso.Espresso.onView
import android.support.test.espresso.action.ViewActions
import android.support.test.espresso.assertion.ViewAssertions.matches
import android.support.test.espresso.matcher.ViewMatchers.isDisplayed
import android.support.test.espresso.matcher.ViewMatchers.withId
import android.support.test.espresso.matcher.ViewMatchers.withText
import com.expedia.bookings.R
import com.expedia.bookings.data.abacus.AbacusUtils
import com.expedia.bookings.test.espresso.AbacusTestUtils
import com.expedia.bookings.test.espresso.Common
import com.expedia.bookings.test.phone.packages.PackageScreen
import com.expedia.bookings.test.phone.pagemodels.common.CheckoutViewModel
import com.mobiata.mocke3.FlightApiMockResponseGenerator
import org.hamcrest.CoreMatchers.allOf
import org.junit.Test

class FlightPriceChangeTest: FlightErrorTestCase() {

    enum class PriceChangeType {
        CHECKOUT,
        CREATE_TRIP
    }

    override fun runTest() {
        super.runTest()
    }

    @Test
    fun testCreateTripPriceChange() {
        getToCheckoutOverview(PriceChangeType.CREATE_TRIP)

        // test price change
        FlightsOverviewScreen.assertPriceChangeShown("Price dropped from $763")

        PackageScreen.enterTravelerInfo()
        PackageScreen.enterPaymentInfo()
        CheckoutViewModel.performSlideToPurchase()

        FlightTestHelpers.assertConfirmationViewIsDisplayed()
    }

    @Test
    fun testCheckoutPriceChange() {
        getToCheckoutOverview(PriceChangeType.CHECKOUT)

        PackageScreen.enterTravelerInfo()
        PackageScreen.enterPaymentInfo("checkoutpricechange lastname")
        CheckoutViewModel.performSlideToPurchase()

        FlightsOverviewScreen.assertPriceChangeShown("Price changed from $696")
    }

    @Test
    fun testCheckoutPriceChangeWithInsurance() {
        getToCheckoutOverview(PriceChangeType.CHECKOUT, false)

        assertInsuranceIsVisible()
        PackageScreen.toggleInsuranceSwitch()
        assertInsuranceBeforePriceChange()

        PackageScreen.enterTravelerInfo()
        PackageScreen.enterPaymentInfo("checkoutpricechangewithinsurance lastname")
        CheckoutViewModel.performSlideToPurchase()

        FlightsOverviewScreen.assertPriceChangeShown("Price changed from $715")
        assertInsuranceAfterPriceChange()
    }

// Disabled on April 28, 2017 for repeated flakiness - ScottW
//    @Test
//    fun testCheckoutSignedInPriceChange() {
//        getToCheckoutOverview(PriceChangeType.CHECKOUT)
//
//        CheckoutViewModel.signInOnCheckout()
//
//        Common.delay(2) // waitForViewToDisplay does not work as this button is not in previous view (sign in)
//        CheckoutViewModel.clickPaymentInfo()
//        CheckoutViewModel.selectStoredCard("Saved checkoutpricechange")
//        Common.pressBack()
//        CheckoutViewModel.performSlideToPurchase(true)
//        FlightsOverviewScreen.assertPriceChangeShown("Price changed from $696")
//    }

    private fun getToCheckoutOverview(priceChangeType: PriceChangeType, isOneWay: Boolean = true) {
        searchFlights(FlightApiMockResponseGenerator.SuggestionResponseType.HAPPY_PATH, isOneWay)
        if (priceChangeType == PriceChangeType.CREATE_TRIP) {
            selectOutboundFlight(FlightApiMockResponseGenerator.SearchResultsResponseType.CREATE_TRIP_PRICE_CHANGE)
        } else {
            if (isOneWay) {
                selectOutboundFlight(FlightApiMockResponseGenerator.SearchResultsResponseType.HAPPY_ONE_WAY)
            } else {
                selectOutboundFlight(FlightApiMockResponseGenerator.SearchResultsResponseType.HAPPY_ROUND_TRIP_WITH_INSURANCE_AVAILABLE)
                selectFirstInboundFlight()
            }
        }
        PackageScreen.checkout().perform(ViewActions.click())
    }

    private fun assertInsuranceAfterPriceChange() {
        PackageScreen.showPriceBreakdown()
        onView(withText(R.string.cost_summary_breakdown_flight_insurance)).check(matches(isDisplayed()))
        Espresso.pressBack()
        onView(allOf(withId(R.id.insurance_title), isDisplayed())).check(matches(
                withText("Your trip is protected for $21/person")))
    }

    private fun assertInsuranceBeforePriceChange() {
        PackageScreen.showPriceBreakdown()
        onView(withText(R.string.cost_summary_breakdown_flight_insurance)).check(matches(isDisplayed()))
        Espresso.pressBack()
        onView(allOf(withId(R.id.insurance_title), isDisplayed())).check(matches(
                withText("Your trip is protected for $19/person")))
    }

    private fun assertInsuranceIsVisible() {
        onView(allOf(withId(R.id.insurance_widget), isDisplayed())).check(matches(isDisplayed()))
    }
}
