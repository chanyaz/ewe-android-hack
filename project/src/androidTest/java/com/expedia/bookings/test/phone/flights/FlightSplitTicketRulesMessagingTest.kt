package com.expedia.bookings.test.phone.flights

import android.support.test.espresso.Espresso.onView
import android.support.test.espresso.action.ViewActions.click
import android.support.test.espresso.assertion.ViewAssertions.matches
import android.support.test.espresso.matcher.ViewMatchers.isDisplayed
import android.support.test.espresso.matcher.ViewMatchers.withId
import android.support.test.espresso.matcher.ViewMatchers.withText
import com.expedia.bookings.R
import com.expedia.bookings.data.abacus.AbacusUtils
import com.expedia.bookings.test.espresso.AbacusTestUtils
import com.expedia.bookings.test.espresso.Common
import com.expedia.bookings.test.espresso.FlightTestCase
import com.expedia.bookings.test.phone.pagemodels.flights.FlightLegScreen
import com.expedia.bookings.test.phone.pagemodels.flights.FlightsSearchResultsScreen
import com.expedia.bookings.test.phone.pagemodels.flights.FlightsSearchScreen
import org.joda.time.LocalDate

class FlightSplitTicketRulesTest : FlightTestCase() {

    fun testRulesAndRestrictions() {
        FlightsSearchScreen.enterDepartureAirport("SFO")
        FlightsSearchScreen.enterArrivalAirport("LAS")
        FlightsSearchScreen.clickSelectDepartureButton()
        val startDate = LocalDate.now().plusDays(35)
        val endDate = startDate.plusDays(3)
        FlightsSearchScreen.clickDate(startDate, endDate)
        FlightsSearchScreen.clickSearchButton()
        FlightsSearchResultsScreen.clickListItem(3)
        Common.delay(1)
        FlightLegScreen.clickSelectFlightButton()

        Common.delay(1)
        FlightsSearchResultsScreen.clickListItem(1)
        Common.delay(1)
        FlightLegScreen.clickSelectFlightButton()

        Common.delay(1)
        assertViewHasText(R.id.split_ticket_info_link, R.string.split_ticket_important_flight_information_header)

        // verify text in alert dialog
        onView(withId(R.id.split_ticket_info_link)).perform(click())
        Common.delay(1)
        assertViewHasText(R.id.split_ticket_rules_and_restrictions, R.string.split_ticket_rules_with_link_TEMPLATE)
        assertViewHasText(R.id.split_ticket_cancellation_policy, R.string.split_ticket_rules_cancellation_policy)
        onView(withId(R.id.split_ticket_baggage_fee_links)).check(matches(isDisplayed())).check(matches(withText("Departure and Return flights have their own baggage fees")))
    }

    private fun assertViewHasText(id: Int, stringResId: Int) {
        onView(withId(id)).check(matches(isDisplayed())).check(matches(withText(stringResId)))
    }
}
