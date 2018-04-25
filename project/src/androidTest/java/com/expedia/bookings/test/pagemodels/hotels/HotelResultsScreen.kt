package com.expedia.bookings.test.pagemodels.hotels

import android.support.test.espresso.Espresso.onView
import android.support.test.espresso.ViewInteraction
import android.support.test.espresso.action.ViewActions.click
import android.support.test.espresso.contrib.RecyclerViewActions
import android.support.test.espresso.matcher.ViewMatchers.hasDescendant
import android.support.test.espresso.matcher.ViewMatchers.isDisplayed
import android.support.test.espresso.matcher.ViewMatchers.withId
import android.support.test.espresso.matcher.ViewMatchers.withText
import android.support.v7.widget.RecyclerView
import android.view.View
import com.expedia.bookings.R
import com.expedia.bookings.test.espresso.ViewActions
import com.expedia.bookings.test.espresso.ViewActions.waitForViewToDisplay
import org.hamcrest.Matchers.allOf
import org.hamcrest.Matchers.anyOf
import org.hamcrest.Matchers.not
import java.util.concurrent.TimeUnit

class HotelResultsScreen {

    companion object {

        @JvmStatic
        fun hotelResultsList(): ViewInteraction {
            return onView(withId(R.id.list_view))
        }

        @JvmStatic
        fun mapFab(): ViewInteraction {
            return onView(withId(R.id.fab))
        }

        @JvmStatic
        fun hotelResultsToolbar(): ViewInteraction {
            return onView(withId(R.id.hotel_results_toolbar))
        }

        @JvmStatic
        fun hotelCarousel(): ViewInteraction {
            return onView(withId(R.id.hotel_carousel))
        }

        @JvmStatic
        fun waitForResultsLoaded(seconds: Int = 10) {
            val resultListMatcher = hasDescendant(withId(R.id.list_view))
            onView(anyOf<View>(withId(R.id.hotel_presenter), withId(R.id.package_hotel_presenter)))
                    .perform(ViewActions.waitFor(resultListMatcher, 10, TimeUnit.SECONDS))

            hotelResultsList().perform(waitForViewToDisplay())

            val pshMatcher = hasDescendant(
                    allOf<View>(withId(R.id.results_description_header), not<View>(withText(R.string.progress_searching_hotels_hundreds)),
                            isDisplayed()))
            hotelResultsList().perform(ViewActions.waitFor(pshMatcher, seconds.toLong(), TimeUnit.SECONDS))
        }

        @JvmStatic
        fun selectHotel(name: String) {
            waitForResultsLoaded()
            hotelResultsList().perform(RecyclerViewActions.actionOnItem<RecyclerView.ViewHolder>(hasDescendant(withText(name)), click()))
            HotelInfoSiteScreen.waitForDetailsLoaded()
        }

        @JvmStatic
        fun hotelResultsDescHeader(): ViewInteraction {
            return onView(withId(R.id.results_description_header))
        }
    }
}
