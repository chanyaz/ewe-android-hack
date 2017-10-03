package com.expedia.bookings.test.pagemodels.common

import android.support.test.espresso.Espresso.onView
import android.support.test.espresso.ViewInteraction
import android.support.test.espresso.matcher.ViewMatchers.*
import android.view.View
import com.expedia.bookings.BuildConfig
import com.expedia.bookings.R
import com.expedia.bookings.test.espresso.Common
import com.expedia.bookings.test.espresso.EspressoUtils
import org.hamcrest.Matchers.allOf
import java.util.concurrent.TimeUnit

object LaunchScreen {
    @JvmStatic fun waitForLOBHeaderToBeDisplayed() {
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
        return onView(allOf<View>(withText(getPackagesLaunchButtonText(BuildConfig.brand)), isCompletelyDisplayed()))
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
        return onView(allOf(withText(R.string.trips), isDescendantOfA(withId(R.id.tab_layout))))
    }

    @JvmStatic fun shopButton(): ViewInteraction {
        return onView(allOf(withText(R.string.shop), isDescendantOfA(withId(R.id.tab_layout))))
    }

    @JvmStatic fun accountButton(): ViewInteraction {
        return onView(allOf(withText(R.string.account_settings_menu_label), isDescendantOfA(withId(R.id.tab_layout))))
    }

    @JvmStatic fun getPackagesLaunchButtonText(brand: String): String {

        return when (brand) {
            "Orbitz", "CheapTickets" -> "Packages"
            "ebookers" -> "Flight + Hotel"
            "Travelocity" -> "Vacation Packages"
            else -> {
                "Bundle Deals"
            }
        }
    }


}