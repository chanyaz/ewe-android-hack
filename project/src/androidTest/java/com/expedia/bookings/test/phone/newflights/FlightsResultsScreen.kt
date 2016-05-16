package com.expedia.bookings.test.phone.newflights

import android.support.test.espresso.Espresso.onView
import android.support.test.espresso.ViewInteraction
import android.support.test.espresso.matcher.ViewMatchers
import android.support.test.espresso.matcher.ViewMatchers.withEffectiveVisibility
import android.support.test.espresso.matcher.ViewMatchers.withId
import com.expedia.bookings.R
import com.expedia.bookings.test.espresso.EspressoUtils
import org.hamcrest.Matchers.allOf

object FlightsResultsScreen {

    @JvmStatic fun headerView(): ViewInteraction {
        return onView(withId(R.id.flight_results_price_header))
    }

    @JvmStatic fun dockedOutboundFlightSelection(): ViewInteraction {
        return onView(allOf(withId(R.id.docked_outbound_flight_selection), withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)))
    }

    @JvmStatic fun dockedOutboundFlightSelectionWidgetContainsText(text: String) {
        EspressoUtils.viewHasDescendantsWithText(R.id.docked_outbound_flight_selection, text)
    }
}
