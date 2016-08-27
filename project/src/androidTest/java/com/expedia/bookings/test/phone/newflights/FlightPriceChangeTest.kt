package com.expedia.bookings.test.phone.newflights

import android.support.test.espresso.Espresso
import android.support.test.espresso.Espresso.onView
import android.support.test.espresso.action.ViewActions
import android.support.test.espresso.assertion.ViewAssertions.matches
import android.support.test.espresso.contrib.RecyclerViewActions
import android.support.test.espresso.matcher.ViewMatchers.isDisplayed
import android.support.test.espresso.matcher.ViewMatchers.withId
import android.support.test.espresso.matcher.ViewMatchers.withText
import android.support.v7.widget.RecyclerView
import com.expedia.bookings.R
import com.expedia.bookings.data.abacus.AbacusUtils
import com.expedia.bookings.test.espresso.AbacusTestUtils
import com.expedia.bookings.test.espresso.Common
import com.expedia.bookings.test.espresso.NewFlightTestCase
import com.expedia.bookings.test.espresso.ViewActions.waitForViewToDisplay
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

    override fun runTest() {
        bucketInsuranceTest(false)
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
        PackageScreen.enterPaymentInfo()
        CheckoutViewModel.performSlideToPurchase()

        FlightsOverviewScreen.assertPriceChangeShown("Price changed from $696")
    }

    @Test
    fun testCheckoutPriceChangeWithInsurance() {
        bucketInsuranceTest(true)

        getToCheckoutOverview(PriceChangeType.CHECKOUT)

        assertInsuranceIsVisible()
        PackageScreen.toggleInsurance()
        assertInsuranceBeforePriceChange()

        PackageScreen.enterTravelerInfo()
        PackageScreen.enterPaymentInfo()
        CheckoutViewModel.performSlideToPurchase()

        FlightsOverviewScreen.assertPriceChangeShown("Price changed from $715")
        assertInsuranceAfterPriceChange()
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

    private fun assertInsuranceAfterPriceChange() {
        PackageScreen.showPriceBreakdown()
        onView(withText(R.string.cost_summary_breakdown_flight_insurance)).check(matches(isDisplayed()))
        Espresso.pressBack()
        onView(withId(R.id.insurance_title)).check(matches(withText("Your trip is protected for $21/person")))
    }

    private fun assertInsuranceBeforePriceChange() {
        PackageScreen.showPriceBreakdown()
        onView(withText(R.string.cost_summary_breakdown_flight_insurance)).check(matches(isDisplayed()))
        Espresso.pressBack()
        onView(withId(R.id.insurance_title)).check(matches(withText("Your trip is protected for $19/person")))
    }

    private fun assertInsuranceIsVisible() {
        onView(withId(R.id.insurance_widget)).check(matches(isDisplayed()))
    }

    private fun bucketInsuranceTest(bucket: Boolean) {
        AbacusTestUtils.updateABTest(AbacusUtils.EBAndroidAppFlightInsurance,
                if (bucket) AbacusUtils.DefaultVariate.BUCKETED.ordinal else AbacusUtils.DefaultVariate.CONTROL.ordinal)
    }
}
