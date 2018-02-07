package com.expedia.bookings.test.pagemodels.hotels

import android.support.test.espresso.Espresso.onView
import android.support.test.espresso.ViewInteraction
import android.support.test.espresso.action.ViewActions.click
import android.support.test.espresso.matcher.ViewMatchers.isDescendantOfA
import android.support.test.espresso.matcher.ViewMatchers.withId
import android.view.View
import com.expedia.bookings.R
import org.hamcrest.Matchers.allOf

class HotelSortAndFilterScreen {

    companion object {

        @JvmStatic
        fun filterHotelName(): ViewInteraction {
            return onView(allOf<View>(withId(R.id.filter_hotel_name_edit_text), isDescendantOfA(withId(R.id.hotel_filter_view))))
        }

        @JvmStatic
        fun clickSortFilter() {
            onView(withId(R.id.sort_filter_button_container)).perform(click())
        }

        @JvmStatic
        fun clickSortFilterDoneButton() {
            onView(allOf(withId(R.id.search_btn), isDescendantOfA(withId(R.id.filter_toolbar)))).perform(click())
        }
    }
}
