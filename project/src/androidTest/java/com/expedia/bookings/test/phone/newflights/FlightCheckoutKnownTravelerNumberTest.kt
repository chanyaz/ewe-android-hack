package com.expedia.bookings.test.phone.newflights

import android.support.test.InstrumentationRegistry
import android.support.test.espresso.Espresso
import android.support.test.espresso.action.ViewActions
import android.support.test.espresso.assertion.ViewAssertions
import android.support.test.espresso.matcher.ViewMatchers
import com.expedia.bookings.R
import com.expedia.bookings.data.pos.PointOfSaleId
import com.expedia.bookings.test.espresso.Common
import com.expedia.bookings.test.espresso.EspressoUtils
import com.expedia.bookings.test.espresso.NewFlightTestCase
import com.expedia.bookings.test.phone.newflights.FlightsScreen
import com.expedia.bookings.test.phone.packages.PackageScreen
import com.expedia.bookings.test.phone.pagemodels.common.SearchScreen
import com.mobiata.android.util.SettingUtils
import org.hamcrest.CoreMatchers
import org.joda.time.LocalDate
import org.junit.Test
import java.util.concurrent.TimeUnit

class FlightCheckoutKnownTravelerNumberTest : NewFlightTestCase() {

    @Test
    fun testKnownTravelerNumberHiddenOutsideUsPos() {
        setPosAndFeatureToggle(PointOfSaleId.MEXICO, true)

        searchFlightsAndProceedToCheckout()
        goToTravelerAndClickAdvanced()

        Espresso.onView(CoreMatchers.allOf(ViewMatchers.withId(R.id.traveler_number))).check(
                ViewAssertions.matches(CoreMatchers.not(ViewMatchers.isDisplayed())))
    }

    @Test
    fun testKnownTravelerNumberShownForUsPOS() {
        setPosAndFeatureToggle(PointOfSaleId.UNITED_STATES, true)

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

    private fun setPosAndFeatureToggle(pointOfSaleId: PointOfSaleId, toggleIsActive: Boolean) {
        Common.setPOS(pointOfSaleId)
        SettingUtils.save(InstrumentationRegistry.getTargetContext(), R.string.preference_enable_checkout_traveler_number, toggleIsActive)
    }

    private fun goToTravelerAndClickAdvanced() {
        EspressoUtils.waitForViewNotYetInLayoutToDisplay((ViewMatchers.withId(R.id.traveler_default_state)), 10, TimeUnit.SECONDS)
        PackageScreen.travelerInfo().perform(ViewActions.click())
        Espresso.closeSoftKeyboard()
        PackageScreen.clickTravelerAdvanced()
    }
}