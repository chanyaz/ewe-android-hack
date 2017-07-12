package com.expedia.bookings.test.pagemodels.common

import android.support.test.espresso.Espresso.onView
import android.support.test.espresso.ViewInteraction
import android.support.test.espresso.matcher.ViewMatchers.isCompletelyDisplayed
import android.support.test.espresso.matcher.ViewMatchers.withId
import android.support.test.espresso.matcher.ViewMatchers.withParent
import android.support.test.espresso.matcher.ViewMatchers.withText
import android.view.View
import com.expedia.bookings.R
import com.expedia.bookings.test.espresso.Common
import com.expedia.bookings.test.espresso.EspressoUtils
import org.hamcrest.Matchers.allOf
import java.util.concurrent.TimeUnit

object NewLaunchScreen {
    @JvmStatic  fun waitForLOBHeaderToBeDisplayed() {
        EspressoUtils.waitForViewNotYetInLayoutToDisplay(allOf(withId(R.id.launch_lob_widget), withParent(withId(R.id.launch_list_widget))), 10, TimeUnit.SECONDS)
    }

    @JvmStatic fun hotelsLaunchButton(): ViewInteraction {
        waitForLOBHeaderToBeDisplayed()
        return onView(allOf<View>(withText("Hotels"), isCompletelyDisplayed()))
    }

    @JvmStatic fun flightLaunchButton(): ViewInteraction {
        waitForLOBHeaderToBeDisplayed()
        return onView(allOf<View>(withText("Flights"), isCompletelyDisplayed()))
    }

    @JvmStatic fun packagesLaunchButton(): ViewInteraction {
        waitForLOBHeaderToBeDisplayed()
        return onView(allOf<View>(withText("Bundle Deals"), isCompletelyDisplayed()))
    }

    @JvmStatic fun activitiesLaunchButton(): ViewInteraction {
        Common.delay(1)
        waitForLOBHeaderToBeDisplayed()
        return onView(allOf<View>(withText("Things to Do"), isCompletelyDisplayed()))
    }

    @JvmStatic fun carsLaunchButton(): ViewInteraction {
        Common.delay(1)
        waitForLOBHeaderToBeDisplayed()
        return onView(allOf<View>(withText("Car Rentals"), isCompletelyDisplayed()))
    }

    @JvmStatic fun tripsButton(): ViewInteraction {
        waitForLOBHeaderToBeDisplayed()
        return onView(withText(R.string.trips))
    }

    @JvmStatic fun shopButton(): ViewInteraction {
        return onView(withText(R.string.shop))
    }

    @JvmStatic fun accountButton(): ViewInteraction {
        return onView(withText(R.string.account_settings_menu_label))
    }

}