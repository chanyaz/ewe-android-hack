package com.expedia.bookings.test.pagemodels.common

import android.support.test.espresso.Espresso.onView
import android.support.test.espresso.ViewInteraction
import android.support.test.espresso.matcher.ViewMatchers.isCompletelyDisplayed
import android.support.test.espresso.matcher.ViewMatchers.isDescendantOfA
import android.support.test.espresso.matcher.ViewMatchers.withId
import android.support.test.espresso.matcher.ViewMatchers.withParent
import android.support.test.espresso.matcher.ViewMatchers.withText
import android.view.View
import com.expedia.bookings.BuildConfig
import com.expedia.bookings.R
import com.expedia.bookings.test.espresso.Common
import com.expedia.bookings.test.espresso.EspressoUtils
import org.hamcrest.Matchers.allOf
import java.util.concurrent.TimeUnit

object LaunchScreen {
    @JvmStatic val tripsButton = allOf(withText(R.string.trips), isDescendantOfA(withId(R.id.tab_layout)))
    @JvmStatic val shopButton = allOf(withText(R.string.shop), isDescendantOfA(withId(R.id.tab_layout)))
    @JvmStatic val accountButton = allOf(withText(R.string.account_settings_menu_label), isDescendantOfA(withId(R.id.tab_layout)))

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

    @JvmStatic fun packagesLaunchButtonForPOS(pos: String): ViewInteraction {
        waitForLOBHeaderToBeDisplayed()
        return onView(allOf<View>(withText(getPackagesLaunchButtonTextForPOS(pos)), isCompletelyDisplayed()))
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
        return onView(tripsButton)
    }

    @JvmStatic fun shopButton(): ViewInteraction {
        return onView(shopButton)
    }

    @JvmStatic fun accountButton(): ViewInteraction {
        return onView(accountButton)
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

    @JvmStatic fun getPackagesLaunchButtonTextForPOS(pos: String): String {
        if (pos == "Australia" || pos == "New Zealand") {
            return "Hotel + Flight Deals"
        } else if (pos == "Canada") {
            return "Flight + Hotel"
        } else if (pos == "Japan" || pos == "Singapore" || pos == "Malaysia" || pos == "Thailand" || pos == "Germany" || pos == "France" || pos == "Italy" || pos == "South Korea" || pos == "Mexico") {
            return "Hotel + Flight"
        } else {
            return "Bundle Deals"
        }
    }
}
