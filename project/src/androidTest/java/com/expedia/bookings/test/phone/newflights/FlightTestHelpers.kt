package com.expedia.bookings.test.phone.newflights

import android.support.test.espresso.Espresso
import android.support.test.espresso.assertion.ViewAssertions
import android.support.test.espresso.matcher.ViewMatchers
import com.expedia.bookings.R
import com.expedia.bookings.test.espresso.EspressoUtils
import com.expedia.bookings.test.espresso.ViewActions
import com.expedia.bookings.test.espresso.ViewActions.waitForViewToDisplay
import com.expedia.bookings.test.pagemodels.flights.FlightsResultsScreen
import com.expedia.bookings.test.pagemodels.flights.FlightsScreen

object FlightTestHelpers {
    @JvmStatic fun assertFlightOutbound() {
        val resultPosition = 1
        FlightsScreen.outboundFlightList().perform(waitForViewToDisplay())
        EspressoUtils.assertViewWithTextIsDisplayedAtPosition(FlightsScreen.outboundFlightList(), resultPosition, R.id.flight_time_detail_text_view,
                "9:00 pm - 11:00 pm")
        EspressoUtils.assertViewWithTextIsDisplayedAtPosition(FlightsScreen.outboundFlightList(), resultPosition, R.id.flight_duration_text_view, "2h 0m (Nonstop)")
        EspressoUtils.assertViewWithTextIsDisplayedAtPosition(FlightsScreen.outboundFlightList(), resultPosition, R.id.price_text_view, "$696")
    }

    @JvmStatic fun assertFlightOutboundForOneWay() {
        val resultPosition = 1
        FlightsScreen.outboundFlightList().perform(waitForViewToDisplay())
        EspressoUtils.assertViewWithTextIsDisplayedAtPosition(FlightsScreen.outboundFlightList(), resultPosition, R.id.flight_time_detail_text_view,
                "6:40 am - 7:49 am")
        EspressoUtils.assertViewWithTextIsDisplayedAtPosition(FlightsScreen.outboundFlightList(), resultPosition, R.id.flight_duration_text_view, "1h 9m (Nonstop)")
        EspressoUtils.assertViewWithTextIsDisplayedAtPosition(FlightsScreen.outboundFlightList(), resultPosition, R.id.price_text_view, "$696")
    }

    @JvmStatic fun assertFlightInbound() {
        val resultPosition = 1
        FlightsScreen.inboundFlightList().perform(waitForViewToDisplay())
        EspressoUtils.assertViewWithTextIsDisplayedAtPosition(FlightsScreen.inboundFlightList(), resultPosition, R.id.flight_time_detail_text_view, "5:40 pm - 8:15 pm")
        EspressoUtils.assertViewWithTextIsDisplayedAtPosition(FlightsScreen.inboundFlightList(), resultPosition, R.id.flight_duration_text_view, "2h 35m (Nonstop)")
        EspressoUtils.assertViewWithTextIsDisplayedAtPosition(FlightsScreen.inboundFlightList(), resultPosition, R.id.price_text_view, "$696")
    }

    @JvmStatic fun assertDockedOutboundFlightSelectionWidget() {
        FlightsResultsScreen.dockedOutboundFlightSelection().perform(waitForViewToDisplay())
    }

    fun assertConfirmationViewIsDisplayed() {
        Espresso.onView(ViewMatchers.withId(R.id.confirmation_container))
                .perform(ViewActions.waitForViewToDisplay())
                .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
    }
}
