package com.expedia.bookings.test.phone.newflights

import android.support.test.espresso.Espresso
import android.support.test.espresso.Espresso.onView
import android.support.test.espresso.action.ViewActions.click
import android.support.test.espresso.assertion.ViewAssertions.doesNotExist
import android.support.test.espresso.assertion.ViewAssertions.matches
import android.support.test.espresso.matcher.ViewMatchers.isDescendantOfA
import android.support.test.espresso.matcher.ViewMatchers.isDisplayed
import android.support.test.espresso.matcher.ViewMatchers.withId
import android.support.test.espresso.matcher.ViewMatchers.withText
import com.expedia.bookings.R
import com.expedia.bookings.test.espresso.Common
import com.expedia.bookings.test.espresso.CustomMatchers.withCompoundDrawable
import com.expedia.bookings.test.espresso.CustomMatchers.withImageDrawable
import com.expedia.bookings.test.espresso.EspressoUser
import com.expedia.bookings.test.espresso.EspressoUtils
import com.expedia.bookings.test.espresso.NewFlightTestCase
import com.expedia.bookings.test.phone.packages.PackageScreen
import com.expedia.bookings.test.phone.pagemodels.common.CheckoutViewModel
import com.expedia.bookings.test.phone.pagemodels.common.SearchScreen
import org.hamcrest.CoreMatchers.allOf
import org.joda.time.LocalDate
import org.junit.Test
import java.util.concurrent.TimeUnit

class FlightCheckoutTravelersTest : NewFlightTestCase() {
    @Test
    fun testMultiTravelerGuestUserCheckout() {
        flightSearchAndGoToCheckout(2)
        EspressoUtils.waitForViewNotYetInLayoutToDisplay((withId(R.id.traveler_default_state)), 10, TimeUnit.SECONDS)
        onView(allOf(withId(R.id.primary_details_text), isDescendantOfA(withId(R.id.traveler_default_state)))).check(
                matches(withText("Traveler Details")))
        onView(allOf(withId(R.id.secondary_details_text), isDescendantOfA(withId(R.id.traveler_default_state)))).check(
                matches(withText("+ 1 additional traveler")))
        onView(allOf(withImageDrawable(R.drawable.invalid),
                isDescendantOfA(withId(R.id.traveler_default_state)))).check(
                doesNotExist())

        PackageScreen.travelerInfo().perform(click())
        onView(allOf(withImageDrawable(R.drawable.invalid),
                isDescendantOfA(withId(R.id.additional_traveler_container)))).check(
                doesNotExist())
        EspressoUser.clickOnText("Edit Traveler 1 (Adult)")
        Espresso.closeSoftKeyboard()
        PackageScreen.clickTravelerDone()
        Common.delay(1)
        onView(withId(R.id.first_name_input)).check(matches(withCompoundDrawable(R.drawable.invalid)))
        onView(withId(R.id.last_name_input)).check(matches(withCompoundDrawable(R.drawable.invalid)))

        Common.pressBack()
        Common.pressBack()

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

        Common.delay(1)
        EspressoUser.clickOnText("11/01/1985")
        Espresso.closeSoftKeyboard()
        EspressoUtils.assertViewDoesNotHaveCompoundDrawable(R.id.first_name_input, R.drawable.invalid)
        EspressoUtils.assertViewDoesNotHaveCompoundDrawable(R.id.last_name_input, R.drawable.invalid)
        Common.pressBack()

        onView(withText("Save")).perform(click())
        EspressoUser.clickOnText("Edit Traveler 2 (Adult)")
        Espresso.closeSoftKeyboard()
        PackageScreen.clickTravelerDone()
        Common.delay(1)
        onView(withId(R.id.first_name_input)).check(matches(withCompoundDrawable(R.drawable.invalid)))
        onView(withId(R.id.last_name_input)).check(matches(withCompoundDrawable(R.drawable.invalid)))


        Common.pressBack()
        EspressoUser.clickOnText("Edit Traveler 2 (Adult)")
        Espresso.closeSoftKeyboard()
        EspressoUtils.assertViewDoesNotHaveCompoundDrawable(R.id.first_name_input, R.drawable.invalid)
        EspressoUtils.assertViewDoesNotHaveCompoundDrawable(R.id.last_name_input, R.drawable.invalid)
        Common.pressBack()

        EspressoUser.clickOnText("Edit Traveler 2 (Adult)")
        Espresso.closeSoftKeyboard()

        PackageScreen.enterFirstName("FiveStar")
        PackageScreen.enterLastName("Bear")
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
        CheckoutViewModel.signInOnCheckout()
        EspressoUtils.waitForViewNotYetInLayoutToDisplay(withId(R.id.login_widget), 10, TimeUnit.SECONDS)

        onView(allOf(withId(R.id.primary_details_text), isDescendantOfA(withId(R.id.traveler_default_state)))).check(
                matches(withText("Mock Web Server")))

        PackageScreen.travelerInfo().perform(click())

        onView(withId(R.id.select_traveler_button)).perform(click())
        onView(withText("Add New Traveler")).perform(click())

        Common.pressBack();
        onView(allOf(withId(R.id.primary_details_text), isDescendantOfA(withId(R.id.traveler_default_state)))).check(
                matches(withText("Traveler Details")))
        onView(allOf(withId(R.id.secondary_details_text), isDescendantOfA(withId(R.id.traveler_default_state)))).check(
                matches(withText("Enter traveler details")))
    }

    private fun flightSearchAndGoToCheckout(numberOfTravelers: Int) {
        SearchScreen.origin().perform(click())
        SearchScreen.selectFlightOriginAndDestination()
        val startDate = LocalDate.now().plusDays(3)
        val endDate = LocalDate.now().plusDays(8)
        SearchScreen.selectDates(startDate, endDate)
        SearchScreen.selectGuestsButton().perform(click())
        SearchScreen.setGuests(numberOfTravelers, 0)
        SearchScreen.searchButton().perform(click())
        FlightsScreen.selectFlight(FlightsScreen.outboundFlightList(), 0)
        FlightsScreen.selectOutboundFlight().perform(click())
        FlightsScreen.selectFlight(FlightsScreen.inboundFlightList(), 0)
        FlightsScreen.selectInboundFlight().perform(click())
        PackageScreen.checkout().perform(click())
    }
}


