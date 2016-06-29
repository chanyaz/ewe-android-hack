package com.expedia.bookings.test.phone.newflights

import com.expedia.bookings.R
import com.expedia.bookings.test.espresso.EspressoUtils
import com.expedia.bookings.test.espresso.ViewActions.waitForViewToDisplay

object FlightTestHelpers {
    @JvmStatic fun assertFlightOutbound() {
        FlightsScreen.outboundFlightList().perform(waitForViewToDisplay())
        EspressoUtils.assertViewWithTextIsDisplayedAtPosition(FlightsScreen.outboundFlightList(), 2, R.id.flight_time_detail_text_view,
                "9:00 pm - 11:00 pm")
        EspressoUtils.assertViewWithTextIsDisplayedAtPosition(FlightsScreen.outboundFlightList(), 2, R.id.flight_duration_text_view, "2h 0m (Nonstop)")
        EspressoUtils.assertViewWithTextIsDisplayedAtPosition(FlightsScreen.outboundFlightList(), 2, R.id.price_text_view, "$696.00")
        EspressoUtils.assertViewWithIdIsDisplayedAtPosition(FlightsScreen.outboundFlightList(), 2, R.id.custom_flight_layover_widget)
    }

    @JvmStatic fun assertFlightInbound() {
        FlightsScreen.inboundFlightList().perform(waitForViewToDisplay())
        EspressoUtils.assertViewWithTextIsDisplayedAtPosition(FlightsScreen.inboundFlightList(), 2, R.id.flight_time_detail_text_view, "5:40 pm - 8:15 pm")
        EspressoUtils.assertViewWithTextIsDisplayedAtPosition(FlightsScreen.inboundFlightList(), 2, R.id.flight_duration_text_view, "2h 35m (Nonstop)")
        EspressoUtils.assertViewWithTextIsDisplayedAtPosition(FlightsScreen.inboundFlightList(), 2, R.id.price_text_view, "$696.00")
        EspressoUtils.assertViewWithIdIsDisplayedAtPosition(FlightsScreen.inboundFlightList(), 2, R.id.custom_flight_layover_widget)
    }

    @JvmStatic fun assertDockedOutboundFlightSelectionWidget() {
        FlightsResultsScreen.dockedOutboundFlightSelection().perform(waitForViewToDisplay())
    }
}
