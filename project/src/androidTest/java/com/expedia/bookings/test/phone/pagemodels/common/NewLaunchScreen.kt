package com.expedia.bookings.test.phone.pagemodels.common

import android.support.test.espresso.Espresso.onView
import android.support.test.espresso.ViewInteraction
import android.support.test.espresso.matcher.ViewMatchers.isCompletelyDisplayed
import android.support.test.espresso.matcher.ViewMatchers.withText
import android.view.View
import com.expedia.bookings.R
import org.hamcrest.Matchers.allOf

object NewLaunchScreen {

    @JvmStatic fun hotelsLaunchButton(): ViewInteraction {
        return onView(allOf<View>(withText("Hotels"), isCompletelyDisplayed()))
    }

    @JvmStatic fun flightLaunchButton(): ViewInteraction {
        return onView(allOf<View>(withText("Flights"), isCompletelyDisplayed()))
    }

    @JvmStatic fun activitiesLaunchButton(): ViewInteraction {
        return onView(allOf<View>(withText("Things to Do"), isCompletelyDisplayed()))
    }

    @JvmStatic fun carsLaunchButton(): ViewInteraction {
        return onView(allOf<View>(withText("Car Rentals"), isCompletelyDisplayed()))
    }

    @JvmStatic fun packagesLaunchButton(displayName: String): ViewInteraction {
        return onView(allOf<View>(withText(displayName), isCompletelyDisplayed()))
    }

    @JvmStatic fun tripsButton(): ViewInteraction {
        return onView(withText(R.string.trips))
    }

    @JvmStatic fun shopButton(): ViewInteraction {
        return onView(withText(R.string.shop))
    }

    @JvmStatic fun accountButton(): ViewInteraction {
        return onView(withText(R.string.account_settings_menu_label))
    }

}