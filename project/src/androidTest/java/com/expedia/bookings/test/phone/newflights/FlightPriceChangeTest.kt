package com.expedia.bookings.test.phone.newflights

import android.support.test.espresso.action.ViewActions
import com.expedia.bookings.test.espresso.ViewActions.waitForViewToDisplay
import android.support.test.espresso.contrib.RecyclerViewActions
import android.support.v7.widget.RecyclerView
import com.expedia.bookings.test.espresso.Common
import com.expedia.bookings.test.espresso.NewFlightTestCase
import com.expedia.bookings.test.phone.packages.PackageScreen
import com.expedia.bookings.test.phone.pagemodels.common.CheckoutViewModel
import com.expedia.bookings.test.phone.pagemodels.common.SearchScreen
import org.joda.time.LocalDate
import org.junit.Test

class FlightPriceChangeTest: NewFlightTestCase() {

    enum class PriceChangeType {
        CHECKOUT,
        CREATE_TRIP
    }

    @Test
    fun testCreateTripPriceChange() {
        getToCheckoutOverview(PriceChangeType.CREATE_TRIP)

        // test price change
        FlightsOverviewScreen.assertPriceChangeShown("Price dropped from $1,910.86")

        PackageScreen.enterTravelerInfo()
        PackageScreen.enterPaymentInfo()
        CheckoutViewModel.performSlideToPurchase()

        FlightTestHelpers.assertConfirmationViewIsDisplayed()
    }

    @Test
    fun testCheckoutPriceChange() {
        getToCheckoutOverview(PriceChangeType.CHECKOUT)

        PackageScreen.enterTravelerInfo()
        PackageScreen.enterPaymentInfo()
        CheckoutViewModel.performSlideToPurchase()

        FlightsOverviewScreen.assertPriceChangeShown("Price changed from $813.60")
    }

    private fun getToCheckoutOverview(priceChangeType: PriceChangeType) {
        val originListIndex = when (priceChangeType) {
            PriceChangeType.CHECKOUT -> 6
            PriceChangeType.CREATE_TRIP -> 7
        }

        SearchScreen.origin().perform(ViewActions.click())
        SearchScreen.searchEditText().perform(ViewActions.typeText("price change origin"))
        SearchScreen.suggestionList().perform(waitForViewToDisplay())
        SearchScreen.suggestionList().perform(RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(originListIndex, ViewActions.click()))

        //Delay for the auto advance to destination picker
        Common.delay(1)
        SearchScreen.searchEditText().perform(waitForViewToDisplay())
        SearchScreen.searchEditText().perform(ViewActions.typeText("SFO"))
        SearchScreen.suggestionList().perform(waitForViewToDisplay())
        SearchScreen.suggestionList().perform(RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(0, ViewActions.click()))

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
