package com.expedia.bookings.test.phone.newflights

import android.support.test.espresso.Espresso
import android.support.test.espresso.ViewInteraction
import android.support.test.espresso.action.ViewActions.click
import android.support.test.espresso.contrib.RecyclerViewActions
import android.support.test.espresso.matcher.ViewMatchers
import android.view.View
import com.expedia.bookings.R
import com.expedia.bookings.test.espresso.TabletViewActions
import com.expedia.bookings.test.phone.pagemodels.common.SearchScreen
import org.joda.time.LocalDate
import android.support.test.espresso.Espresso.onView
import org.hamcrest.Matchers.allOf
import android.support.test.espresso.matcher.ViewMatchers.withId
import android.support.test.espresso.matcher.ViewMatchers.isDescendantOfA
import android.support.v7.widget.RecyclerView
import com.expedia.bookings.test.espresso.ViewActions.waitForViewToDisplay

object FlightsScreen {

    @JvmStatic fun selectOneWay(): ViewInteraction {
        return Espresso.onView(ViewMatchers.withText("ONE WAY")).perform(click())
    }

    fun selectDate(start: LocalDate) {
        SearchScreen.calendar().perform(TabletViewActions.clickDates(start, null))
        SearchScreen.searchAlertDialogDone().perform(click())
    }

    @JvmStatic fun outboundFlightList(): ViewInteraction {
        return onView(allOf<View>(withId(R.id.list_view), isDescendantOfA(withId(R.id.widget_flight_outbound))))
    }

    @JvmStatic fun inboundFlightList(): ViewInteraction {
        return onView(allOf<View>(withId(R.id.list_view), isDescendantOfA(withId(R.id.widget_flight_inbound))))
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
}
