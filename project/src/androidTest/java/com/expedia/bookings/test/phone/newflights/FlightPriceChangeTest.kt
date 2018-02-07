package com.expedia.bookings.test.phone.newflights

import android.support.test.espresso.Espresso
import android.support.test.espresso.Espresso.onView
import android.support.test.espresso.action.ViewActions
import android.support.test.espresso.assertion.ViewAssertions.matches
import android.support.test.espresso.matcher.ViewMatchers.isDisplayed
import android.support.test.espresso.matcher.ViewMatchers.withId
import android.support.test.espresso.matcher.ViewMatchers.withText
import com.expedia.bookings.R
import com.expedia.bookings.test.pagemodels.packages.PackageScreen
import com.expedia.bookings.test.pagemodels.common.CheckoutScreen
import com.expedia.bookings.test.pagemodels.flights.FlightsOverviewScreen
import com.mobiata.mocke3.FlightApiMockResponseGenerator
import org.hamcrest.CoreMatchers.allOf
import org.junit.Test
import com.expedia.bookings.test.espresso.Common

class FlightPriceChangeTest : FlightErrorTestCase() {

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

        FlightsOverviewScreen.assertPriceChangeShown(
                "The price of your trip has changed from $763.00 to $696.00. Rates can change frequently. Book now to lock in this price.")
        onView(withId(android.R.id.button1)).perform(ViewActions.click())
        PackageScreen.checkout().perform(ViewActions.click())

        PackageScreen.enterTravelerInfo()
        PackageScreen.enterPaymentInfo()
        CheckoutScreen.performSlideToPurchase()

        FlightTestHelpers.assertConfirmationViewIsDisplayed()
    }

    @Test
    fun testCheckoutPriceChange() {
        getToCheckoutOverview(PriceChangeType.CHECKOUT)

        PackageScreen.checkout().perform(ViewActions.click())

        PackageScreen.enterTravelerInfo()
        PackageScreen.enterPaymentInfo("checkoutpricechange lastname")
        CheckoutScreen.performSlideToPurchase()
    }

    @Test
    fun testCheckoutPriceChangeWithInsurance() {
        getToCheckoutOverview(PriceChangeType.CHECKOUT, false)

        PackageScreen.checkout().perform(ViewActions.click())

        assertInsuranceIsVisible()
        PackageScreen.toggleInsuranceSwitch()
        assertInsuranceBeforePriceChange()

        PackageScreen.enterTravelerInfo()
        PackageScreen.enterPaymentInfo("checkoutpricechangewithinsurance lastname")
        CheckoutScreen.performSlideToPurchase()
        FlightsOverviewScreen.assertPriceChangeShown("The price of your trip has changed from \$715.00 to \$763.00. Rates can change frequently. Book now to lock in this price.")
        onView(withId(android.R.id.button1)).perform(ViewActions.click())
        assertInsuranceAfterPriceChange()
    }

    @Test
    fun testCheckoutSignedInPriceChange() {
        getToCheckoutOverview(PriceChangeType.CHECKOUT)
        PackageScreen.checkout().perform(ViewActions.click())
        CheckoutScreen.signInOnCheckout()

        Common.delay(2) // waitForViewToDisplay does not work as this button is not in previous view (sign in)
        CheckoutScreen.clickPaymentInfo()
        CheckoutScreen.selectStoredCard("Saved checkoutpricechange")
        Common.pressBack()
        CheckoutScreen.performSlideToPurchase(true)
        FlightsOverviewScreen.assertPriceChangeShown("The price of your trip has changed from \$696.00 to \$763.00. Rates can change frequently. Book now to lock in this price.")
    }

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
