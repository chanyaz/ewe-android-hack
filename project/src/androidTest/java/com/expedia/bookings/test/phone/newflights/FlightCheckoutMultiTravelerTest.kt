package com.expedia.bookings.test.phone.newflights

import android.support.test.espresso.Espresso
import com.expedia.bookings.R
import com.expedia.bookings.test.espresso.NewFlightTestCase
import com.expedia.bookings.test.phone.packages.PackageScreen
import com.expedia.bookings.test.phone.pagemodels.common.SearchScreen
import com.expedia.bookings.test.espresso.Common
import com.expedia.bookings.test.espresso.CustomMatchers.withImageDrawable
import com.expedia.bookings.test.espresso.EspressoUser
import com.expedia.bookings.test.espresso.EspressoUtils
import com.expedia.bookings.test.phone.pagemodels.common.CheckoutViewModel
import java.util.concurrent.TimeUnit
import org.joda.time.LocalDate
import org.junit.Test
import org.hamcrest.CoreMatchers.allOf

import android.support.test.espresso.Espresso.onView
import android.support.test.espresso.Espresso.pressBack
import android.support.test.espresso.action.ViewActions.click
import android.support.test.espresso.assertion.ViewAssertions.doesNotExist
import android.support.test.espresso.assertion.ViewAssertions.matches
import android.support.test.espresso.matcher.ViewMatchers.isDisplayed
import android.support.test.espresso.matcher.ViewMatchers.withId
import android.support.test.espresso.matcher.ViewMatchers.withText
import android.support.test.espresso.matcher.ViewMatchers.isDescendantOfA

class FlightCheckoutMultiTravelerTest : NewFlightTestCase() {
    @Test
    fun testMultiTravelerGuestUserCheckout() {
        flightSearchAndGoToCheckout()
        EspressoUtils.waitForViewNotYetInLayoutToDisplay((withId(R.id.traveler_default_state)), 10, TimeUnit.SECONDS)
        onView(allOf(withId(R.id.primary_details_text), isDescendantOfA(withId(R.id.traveler_default_state)))).check(
                matches(withText("Traveler Details")))
        onView(allOf(withId(R.id.secondary_details_text), isDescendantOfA(withId(R.id.traveler_default_state)))).check(
                matches(withText("+ 1 additional traveler")))
        onView(allOf(withImageDrawable(R.drawable.invalid),
                isDescendantOfA(withId(R.id.traveler_default_state)))).check(
                doesNotExist())
        CheckoutViewModel.signInOnCheckout()
        EspressoUtils.waitForViewNotYetInLayoutToDisplay(withId(R.id.login_widget), 10, TimeUnit.SECONDS)

        onView(allOf(withId(R.id.primary_details_text), isDescendantOfA(withId(R.id.traveler_default_state)))).check(
                matches(withText("Mock Web Server")))
        onView(allOf(withId(R.id.secondary_details_text), isDescendantOfA(withId(R.id.traveler_default_state)))).check(
                matches(withText("+ 1 additional traveler")))
        onView(allOf(withImageDrawable(R.drawable.invalid),
                isDescendantOfA(withId(R.id.traveler_default_state)))).check(
                doesNotExist())

        PackageScreen.travelerInfo().perform(click())
        onView(allOf(withImageDrawable(R.drawable.invalid),
                isDescendantOfA(withId(R.id.additional_traveler_container)))).check(
                doesNotExist())
        EspressoUser.clickOnText("Edit Traveler 2 (Adult)")
        Espresso.closeSoftKeyboard()
        Common.pressBack()
        EspressoUtils.waitForViewNotYetInLayoutToDisplay((withId(R.id.additional_traveler_container)), 10, TimeUnit.SECONDS)
        onView(allOf(withImageDrawable(R.drawable.invalid),
                isDescendantOfA(withId(R.id.additional_traveler_container)))).check(
                matches(isDisplayed()))
        onView(allOf(withId(R.id.primary_details_text), isDescendantOfA(withId(R.id.additional_traveler_container)))).check(
                matches(withText("Edit Traveler 2 (Adult)")))
        onView(allOf(withId(R.id.secondary_details_text), isDescendantOfA(withId(R.id.additional_traveler_container)))).check(
                matches(withText("Enter missing traveler details")))

        pressBack()

        EspressoUtils.waitForViewNotYetInLayoutToDisplay((withId(R.id.traveler_default_state)), 10, TimeUnit.SECONDS)
        onView(allOf(withImageDrawable(R.drawable.invalid),
                isDescendantOfA(withId(R.id.traveler_default_state)))).check(
                matches(isDisplayed()))

        pressBack()
        PackageScreen.checkout().perform(click())
        EspressoUtils.waitForViewNotYetInLayoutToDisplay((withId(R.id.traveler_default_state)), 10, TimeUnit.SECONDS)
        onView(allOf(withImageDrawable(R.drawable.invalid),
                isDescendantOfA(withId(R.id.traveler_default_state)))).check(
                matches(isDisplayed()))

        PackageScreen.travelerInfo().perform(click())
        onView(allOf(withImageDrawable(R.drawable.invalid),
                isDescendantOfA(withId(R.id.additional_traveler_container)))).check(
                matches(isDisplayed()))
    }

    private fun flightSearchAndGoToCheckout() {
        SearchScreen.origin().perform(click())
        SearchScreen.selectFlightOriginAndDestination()
        val startDate = LocalDate.now().plusDays(3)
        val endDate = LocalDate.now().plusDays(8)
        SearchScreen.selectDates(startDate, endDate)
        SearchScreen.selectGuestsButton().perform(click())
        SearchScreen.setGuests(2, 0)
        SearchScreen.searchButton().perform(click())
        FlightsScreen.selectFlight(FlightsScreen.outboundFlightList(), 0)
        FlightsScreen.selectOutboundFlight().perform(click())
        FlightsScreen.selectFlight(FlightsScreen.inboundFlightList(), 0)
        FlightsScreen.selectInboundFlight().perform(click())
        PackageScreen.checkout().perform(click())
    }
}


