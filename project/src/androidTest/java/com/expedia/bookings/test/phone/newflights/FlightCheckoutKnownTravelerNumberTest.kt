package com.expedia.bookings.test.phone.newflights

import android.support.test.espresso.Espresso
import android.support.test.espresso.action.ViewActions
import android.support.test.espresso.assertion.ViewAssertions
import android.support.test.espresso.matcher.ViewMatchers
import com.expedia.bookings.R
import com.expedia.bookings.data.pos.PointOfSaleId
import com.expedia.bookings.test.espresso.Common
import com.expedia.bookings.test.espresso.EspressoUtils
import com.expedia.bookings.test.espresso.NewFlightTestCase
import com.expedia.bookings.test.pagemodels.packages.PackageScreen
import com.expedia.bookings.test.pagemodels.common.SearchScreen
import com.expedia.bookings.test.pagemodels.common.TravelerModel.TravelerDetails
import com.expedia.bookings.test.pagemodels.flights.FlightsScreen
import org.hamcrest.CoreMatchers
import org.joda.time.LocalDate
import org.junit.Test
import java.util.concurrent.TimeUnit

class FlightCheckoutKnownTravelerNumberTest : NewFlightTestCase() {

    @Test
    fun testKnownTravelerNumberHiddenOutsideUsPos() {
        setPos(PointOfSaleId.MEXICO)

        searchFlightsAndProceedToCheckout()
        goToTravelerAndClickAdvanced()

        Espresso.onView(CoreMatchers.allOf(ViewMatchers.withId(R.id.traveler_number))).check(
                ViewAssertions.matches(CoreMatchers.not(ViewMatchers.isDisplayed())))
    }

    @Test
    fun testKnownTravelerNumberShownForUsPOS() {
        setPos(PointOfSaleId.UNITED_STATES)

        searchFlightsAndProceedToCheckout()
        goToTravelerAndClickAdvanced()

        Espresso.onView(CoreMatchers.allOf(ViewMatchers.withId(R.id.traveler_number))).check(
                ViewAssertions.matches(ViewMatchers.isCompletelyDisplayed()))
    }

    private fun searchFlightsAndProceedToCheckout() {
        SearchScreen.origin().perform(ViewActions.click())
        SearchScreen.selectFlightOriginAndDestination()
        val startDate = LocalDate.now().plusDays(3)
        val endDate = LocalDate.now().plusDays(8)
        SearchScreen.selectDates(startDate, endDate)
        SearchScreen.searchButton().perform(ViewActions.click())
        FlightsScreen.selectFlight(FlightsScreen.outboundFlightList(), 0)
        FlightsScreen.selectOutboundFlight().perform(ViewActions.click())
        FlightsScreen.selectFlight(FlightsScreen.inboundFlightList(), 0)
        FlightsScreen.selectInboundFlight().perform(ViewActions.click())
        PackageScreen.checkout().perform(ViewActions.click())
    }

    private fun setPos(pointOfSaleId: PointOfSaleId) {
        Common.setPOS(pointOfSaleId)
    }

    private fun goToTravelerAndClickAdvanced() {
        EspressoUtils.waitForViewNotYetInLayoutToDisplay((ViewMatchers.withId(R.id.traveler_default_state)), 10, TimeUnit.SECONDS)
        PackageScreen.travelerInfo().perform(ViewActions.click())
        Espresso.closeSoftKeyboard()
        TravelerDetails.clickAdvanced()
    }
}
