package com.expedia.bookings.test.phone.newflights

import android.support.test.espresso.Espresso.onView
import android.support.test.espresso.ViewInteraction
import android.support.test.espresso.matcher.ViewMatchers.withId
import com.expedia.bookings.R

object FlightsResultsScreen {

    @JvmStatic fun headerView(): ViewInteraction {
        return onView(withId(R.id.flight_results_price_header))
    }
}
