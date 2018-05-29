package com.expedia.bookings.test.phone.newflights

import android.support.test.espresso.Espresso.closeSoftKeyboard
import android.support.test.espresso.Espresso.onView
import android.support.test.espresso.Espresso.pressBack
import android.support.test.espresso.action.ViewActions.click
import android.support.test.espresso.action.ViewActions.replaceText
import android.support.test.espresso.assertion.ViewAssertions.doesNotExist
import android.support.test.espresso.assertion.ViewAssertions.matches
import android.support.test.espresso.matcher.ViewMatchers.isDisplayed
import android.support.test.espresso.matcher.ViewMatchers.withId
import android.support.test.espresso.matcher.ViewMatchers.withText
import com.expedia.bookings.R
import com.expedia.bookings.test.espresso.NewFlightTestCase
import com.expedia.bookings.test.pagemodels.packages.PackageScreen
import com.expedia.bookings.test.pagemodels.common.CardInfoScreen
import com.expedia.bookings.test.pagemodels.common.CheckoutScreen
import com.expedia.bookings.test.pagemodels.common.SearchScreen
import com.expedia.bookings.test.pagemodels.common.SearchScreenActions
import com.expedia.bookings.test.pagemodels.flights.FlightsScreen
import com.mobiata.mocke3.FlightDispatcherUtils
import org.joda.time.LocalDate
import org.junit.Test

class FlightCheckoutFormsTest : NewFlightTestCase() {

    @Test
    fun testNoUnicodeOnRomanPaymentFields() {
        selectFlightsProceedToCheckout()
        CheckoutScreen.clickPaymentInfo()
        CheckoutScreen.waitForPaymentInfoDisplayed()

        CardInfoScreen.nameOnCardEditText().perform(replaceText("ㅎ"))
        assertInvalidCharacter()
        onView(withId(R.id.edit_address_line_one)).perform(replaceText("ㅎ"))
        assertInvalidCharacter()
        onView(withId(R.id.edit_address_line_two)).perform(replaceText("ㅎ"))
        assertInvalidCharacter()
        CardInfoScreen.postalCodeEditText().perform(replaceText("ㅎ"))
        assertInvalidCharacter()
        onView(withId(R.id.edit_address_city)).perform(replaceText("ㅎ"))
        assertInvalidCharacter()
        onView(withId(R.id.edit_address_state)).perform(replaceText("ㅎ"))
        assertInvalidCharacter()

        closeSoftKeyboard()
        pressBack()
        onView(withId(R.id.traveler_default_state_card_view)).perform(click())

        onView(withId(R.id.first_name_input)).perform(replaceText("ㅎ"))
        assertInvalidCharacter()
        onView(withId(R.id.middle_name_input)).perform(replaceText("ㅎ"))
        assertInvalidCharacter()
        onView(withId(R.id.last_name_input)).perform(replaceText("ㅎ"))
        assertInvalidCharacter()
        onView(withId(R.id.edit_email_address)).perform(replaceText("ㅎ"))
        assertInvalidCharacter()
        onView(withId(R.id.traveler_advanced_options_button)).perform(click())
        onView(withId(R.id.traveler_number)).perform(replaceText("dasd123d"))
        onView(withText("Please use the Roman Alphabet")).check(doesNotExist())
        onView(withId(R.id.traveler_number)).perform(replaceText("ㅎ"))
        assertInvalidCharacter()
    }

    private fun selectFlightsProceedToCheckout() {
        SearchScreenActions.selectFlightOriginAndDestination(FlightDispatcherUtils.SuggestionResponseType.HAPPY_PATH, 0)

        val startDate = LocalDate.now().plusDays(3)
        val endDate = LocalDate.now().plusDays(8)
        SearchScreenActions.chooseDatesWithDialog(startDate, endDate)
        SearchScreen.searchButton().perform(click())
        FlightTestHelpers.assertFlightOutbound()

        FlightsScreen.selectFlight(FlightsScreen.outboundFlightList(), 0)
        FlightsScreen.selectOutboundFlight().perform(click())
        FlightTestHelpers.assertFlightInbound()
        FlightsScreen.selectFlight(FlightsScreen.inboundFlightList(), 0)

        FlightsScreen.selectInboundFlight().perform(click())
        PackageScreen.checkout().perform(click())
    }

    private fun assertInvalidCharacter() {
        onView(withText("Please use the Roman alphabet")).check(matches(isDisplayed()))
        onView(withText("OK")).perform(click())
    }
}
