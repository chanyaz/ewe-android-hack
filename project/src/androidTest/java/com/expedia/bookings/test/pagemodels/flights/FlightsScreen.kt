package com.expedia.bookings.test.pagemodels.flights

import android.support.test.espresso.Espresso
import android.support.test.espresso.Espresso.onView
import android.support.test.espresso.ViewInteraction
import android.support.test.espresso.action.ViewActions.click
import android.support.test.espresso.contrib.RecyclerViewActions
import android.support.test.espresso.matcher.ViewMatchers.*
import android.support.v7.widget.RecyclerView
import android.view.View
import com.expedia.bookings.R
import com.expedia.bookings.test.espresso.CalendarPickerActions
import com.expedia.bookings.test.espresso.ViewActions.waitForViewToDisplay
import com.expedia.bookings.test.pagemodels.common.SearchScreen
import org.hamcrest.Matchers.allOf
import org.joda.time.LocalDate

object FlightsScreen {

    @JvmStatic fun selectOneWay(): ViewInteraction {
        return Espresso.onView(allOf(withText("One way"), isDescendantOfA(withId(R.id.tabs)))).perform(click())
    }

    fun selectDate(start: LocalDate) {
        SearchScreen.calendar().perform(CalendarPickerActions.clickDates(start, null))
        SearchScreen.searchAlertDialogDone().perform(click())
    }

    @JvmStatic fun outboundFlightList(): ViewInteraction {
        return onView(allOf<View>(withId(R.id.list_view), isDescendantOfA(withId(R.id.widget_flight_outbound))))
    }

    @JvmStatic fun inboundFlightList(): ViewInteraction {
        return onView(allOf<View>(withId(R.id.list_view), isDescendantOfA(withId(R.id.widget_flight_inbound))))
    }

    @JvmStatic fun selectFlight(list: ViewInteraction, airline: String): ViewInteraction {
        list.perform(waitForViewToDisplay())
        val viewMatcher = hasDescendant(withText(airline))
        return list.perform(RecyclerViewActions.actionOnItem<RecyclerView.ViewHolder>(viewMatcher, click()))
    }

    @JvmStatic fun selectFlight(list: ViewInteraction, airline: String, airlineTime: String): ViewInteraction? {
        list.perform(waitForViewToDisplay())
        val viewMatcher = allOf(hasDescendant(withText(airline)), hasDescendant(withText(airlineTime)))
        return list.perform(RecyclerViewActions.actionOnItem<RecyclerView.ViewHolder>(viewMatcher, click()))
    }


    @JvmStatic fun selectFlight(list: ViewInteraction, index: Int): ViewInteraction {
        list.perform(waitForViewToDisplay())
        val adjustPosition = 1
        return list.perform(RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(index + adjustPosition, click()))
    }

    @JvmStatic fun selectOutboundFlight(): ViewInteraction {
        return onView(allOf<View>(withId(R.id.select_flight_button), isDescendantOfA(withId(R.id.widget_flight_outbound))))
    }

    @JvmStatic fun selectInboundFlight(): ViewInteraction {
        return onView(allOf<View>(withId(R.id.select_flight_button), isDescendantOfA(withId(R.id.widget_flight_inbound))))
    }

    @JvmStatic fun selectTravellerDetails(): ViewInteraction {
        return onView(withId(R.id.traveler_default_state))
    }
}
