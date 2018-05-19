package com.expedia.bookings.test.phone.newflights

import android.support.test.espresso.Espresso
import android.support.test.espresso.Espresso.onData
import android.support.test.espresso.Espresso.onView
import android.support.test.espresso.action.ViewActions
import android.support.test.espresso.action.ViewActions.click
import android.support.test.espresso.assertion.ViewAssertions.doesNotExist
import android.support.test.espresso.assertion.ViewAssertions.matches
import android.support.test.espresso.matcher.ViewMatchers.isDescendantOfA
import android.support.test.espresso.matcher.ViewMatchers.isDisplayed
import android.support.test.espresso.matcher.ViewMatchers.withId
import android.support.test.espresso.matcher.ViewMatchers.withText
import com.expedia.bookings.R
import com.expedia.bookings.test.espresso.Common
import com.expedia.bookings.test.espresso.ViewActions.waitForViewToDisplay
import com.expedia.bookings.test.espresso.CustomMatchers.withImageDrawable
import com.expedia.bookings.test.espresso.EspressoUser
import com.expedia.bookings.test.espresso.EspressoUtils
import com.expedia.bookings.test.espresso.NewFlightTestCase
import com.expedia.bookings.test.pagemodels.packages.PackageScreen
import com.expedia.bookings.test.pagemodels.common.CheckoutScreen
import com.expedia.bookings.test.pagemodels.common.CheckoutScreen.clickLogin
import com.expedia.bookings.test.pagemodels.common.CheckoutScreen.enterUsername
import com.expedia.bookings.test.pagemodels.common.CheckoutScreen.enterPassword
import com.expedia.bookings.test.pagemodels.common.SearchScreen
import com.expedia.bookings.test.pagemodels.common.SearchScreenActions
import com.expedia.bookings.test.pagemodels.common.TravelerModel.TravelerDetails
import com.expedia.bookings.test.pagemodels.flights.FlightsScreen
import org.hamcrest.CoreMatchers.allOf
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.instanceOf
import org.joda.time.LocalDate
import org.junit.Test
import java.util.concurrent.TimeUnit

class FlightCheckoutTravelersTest : NewFlightTestCase() {
    @Test
    fun testMultiTravelerGuestUserCheckout() {
        flightSearchAndGoToCheckout(2)
        EspressoUtils.waitForViewNotYetInLayoutToDisplay((withId(R.id.traveler_default_state)), 10, TimeUnit.SECONDS)
        onView(allOf(withId(R.id.primary_details_text), isDescendantOfA(withId(R.id.traveler_default_state)))).check(
                matches(withText("Enter traveler details")))
        onView(allOf(withId(R.id.secondary_details_text), isDescendantOfA(withId(R.id.traveler_default_state)))).check(
                matches(withText("+ 1 additional traveler")))
        onView(allOf(withImageDrawable(R.drawable.invalid),
                isDescendantOfA(withId(R.id.traveler_default_state)))).check(
                doesNotExist())

        PackageScreen.travelerInfo().perform(click())
        onView(allOf(withImageDrawable(R.drawable.invalid),
                isDescendantOfA(withId(R.id.additional_traveler_container)))).check(
                doesNotExist())
        EspressoUser.clickOnText("Traveler 1 (Adult 18+)")
        Espresso.closeSoftKeyboard()
        TravelerDetails.clickDone()
        Common.delay(1)

        onView(withText(R.string.first_name_validation_error_message)).check(matches(isDisplayed()))
        onView(withText(R.string.last_name_validation_error_message)).check(matches(isDisplayed()))

        Common.pressBack()
        onView(withId(R.id.additional_traveler_container)).perform(waitForViewToDisplay())
        Common.pressBack()

        CheckoutScreen.signInOnCheckout()
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

        onView(withText("Traveler details")).perform(waitForViewToDisplay())
        onView(allOf(isDescendantOfA(withId(R.id.main_traveler_container)), withText("11/01/1985"))).perform(click())
        onView(withText("Traveler 1 (Adult 18+)")).perform(waitForViewToDisplay())
        Espresso.closeSoftKeyboard()
        onView(withText(R.string.first_name_validation_error_message)).check(doesNotExist())
        onView(withText(R.string.last_name_validation_error_message)).check(doesNotExist())
        Common.pressBack()

        onView(withText("Traveler details")).perform(waitForViewToDisplay())
        onView(withId(R.id.additional_traveler_container)).perform(click())
        Espresso.closeSoftKeyboard()
        TravelerDetails.clickDone()
        Common.delay(1)
        onView(withText(R.string.first_name_validation_error_message)).check(matches(isDisplayed()))
        onView(withText(R.string.last_name_validation_error_message)).check(matches(isDisplayed()))

        Common.pressBack()
        EspressoUser.clickOnText("Traveler 2 (Adult 18+)")
        Espresso.closeSoftKeyboard()
        onView(withText(R.string.first_name_validation_error_message)).check(doesNotExist())
        onView(withText(R.string.last_name_validation_error_message)).check(doesNotExist())
        Common.pressBack()

        EspressoUser.clickOnText("Traveler 2 (Adult 18+)")
        Espresso.closeSoftKeyboard()

        TravelerDetails.enterFirstName("FiveStar")
        TravelerDetails.enterLastName("Bear")
        Espresso.closeSoftKeyboard()
        Common.pressBack()

        EspressoUtils.waitForViewNotYetInLayoutToDisplay((withId(R.id.additional_traveler_container)), 10, TimeUnit.SECONDS)
        onView(allOf(withImageDrawable(R.drawable.invalid),
                isDescendantOfA(withId(R.id.additional_traveler_container)))).check(
                matches(isDisplayed()))

        Common.pressBack()

        PackageScreen.travelerInfo().perform(click())
        onView(allOf(withImageDrawable(R.drawable.invalid),
                isDescendantOfA(withId(R.id.additional_traveler_container)))).check(
                matches(isDisplayed()))
    }

    @Test
    fun testSingleTravelerCheckout() {
        flightSearchAndGoToCheckout(1)
        CheckoutScreen.signInOnCheckout()
        EspressoUtils.waitForViewNotYetInLayoutToDisplay(withId(R.id.login_widget), 10, TimeUnit.SECONDS)

        onView(allOf(withId(R.id.primary_details_text), isDescendantOfA(withId(R.id.traveler_default_state)))).check(
                matches(withText("Mock Web Server")))

        PackageScreen.travelerInfo().perform(click())

        onView(withId(R.id.select_traveler_button)).perform(click())
        onView(withText("Add New Traveler")).perform(click())

        Common.pressBack()
        onView(allOf(withId(R.id.primary_details_text), isDescendantOfA(withId(R.id.traveler_default_state)))).check(
                matches(withText("Enter traveler details")))
        onView(allOf(withId(R.id.secondary_details_text), isDescendantOfA(withId(R.id.traveler_default_state)))).check(
                matches(withText("")))
    }

    @Test
    fun testMultiTravelerWithPassportFlow() {
        flightSearchWithPassportAndGoToCheckout(2)

        clickLogin()
        enterUsername("qa-ehcc@mobiata.com")
        enterPassword("password")
        val signInButton = onView(withId(R.id.sign_in_button))
        signInButton.perform(waitForViewToDisplay())
        Common.closeSoftKeyboard(CheckoutScreen.password())
        signInButton.perform(click())
        EspressoUtils.waitForViewNotYetInLayoutToDisplay(withId(android.R.id.button1), 10, TimeUnit.SECONDS)
        onView(withId(android.R.id.button1)).perform(ViewActions.click())

        PackageScreen.travelerInfo().perform(click())
        onView(allOf(withImageDrawable(R.drawable.invalid),
                isDescendantOfA(withId(R.id.main_traveler_container)))).check(
                matches(isDisplayed()))
        onView(withText("Traveler details")).perform(waitForViewToDisplay())
        onView(allOf(isDescendantOfA(withId(R.id.main_traveler_container)), withText("Enter missing traveler details"))).perform(click())

        onView(withText("Traveler 1 (Adult 18+)")).perform(waitForViewToDisplay())
        EspressoUtils.assertViewIsDisplayed(R.id.passport_country_btn)
        onView(withId(R.id.passport_country_btn)).perform(click())
        onData(allOf<String>(`is`<Any>(instanceOf<Any>(String::class.java)), `is`<String>("Afghanistan"))).perform(click())
        EspressoUtils.assertViewWithTextIsDisplayed("Afghanistan")
        Common.pressBack()
        onView(withId(android.R.id.button1)).perform(ViewActions.click())

        onView(withText("Traveler details")).perform(waitForViewToDisplay())
        onView(allOf(withImageDrawable(R.drawable.validated),
                isDescendantOfA(withId(R.id.main_traveler_container)))).check(
                matches(isDisplayed()))
    }

    private fun flightSearchAndGoToCheckout(numberOfTravelers: Int) {
        SearchScreen.origin().perform(click())
        SearchScreenActions.selectFlightOriginAndDestination()
        val startDate = LocalDate.now().plusDays(3)
        val endDate = LocalDate.now().plusDays(8)
        SearchScreenActions.chooseDatesWithDialog(startDate, endDate)
        SearchScreen.selectGuestsButton().perform(click())
        SearchScreenActions.setGuests(numberOfTravelers, 0)
        SearchScreen.searchButton().perform(click())
        FlightsScreen.selectFlight(FlightsScreen.outboundFlightList(), 0)
        FlightsScreen.selectOutboundFlight().perform(click())
        FlightsScreen.selectFlight(FlightsScreen.inboundFlightList(), 0)
        FlightsScreen.selectInboundFlight().perform(click())
        PackageScreen.checkout().perform(click())
    }

    private fun flightSearchWithPassportAndGoToCheckout(numberOfTravelers: Int) {
        SearchScreen.origin().perform(click())
        SearchScreenActions.selectFlightOriginAndDestination()
        val startDate = LocalDate.now().plusDays(3)
        val endDate = LocalDate.now().plusDays(8)
        SearchScreenActions.chooseDatesWithDialog(startDate, endDate)
        SearchScreen.selectGuestsButton().perform(click())
        SearchScreenActions.setGuests(numberOfTravelers, 0)
        SearchScreen.searchButton().perform(click())
        FlightsScreen.selectFlight(FlightsScreen.outboundFlightList(), 1)
        FlightsScreen.selectOutboundFlight().perform(click())
        FlightsScreen.selectFlight(FlightsScreen.inboundFlightList(), 0)
        FlightsScreen.selectInboundFlight().perform(click())
        onView(withId(android.R.id.button1)).perform(ViewActions.click())
        PackageScreen.checkout().perform(click())
    }
}
