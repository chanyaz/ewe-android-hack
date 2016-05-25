package com.expedia.bookings.test.phone.pagemodels.common

import android.support.test.espresso.ViewInteraction
import android.view.View
import com.expedia.bookings.R
import com.expedia.bookings.test.espresso.SpoonScreenshotUtils
import android.support.test.espresso.Espresso.onView
import android.support.test.espresso.matcher.ViewMatchers.withId
import android.support.test.espresso.matcher.RootMatchers.withDecorView
import org.hamcrest.Matchers.not
import org.hamcrest.core.Is.`is`
import org.joda.time.LocalDate
import android.support.test.espresso.action.ViewActions.click
import com.expedia.bookings.test.espresso.Common
import com.expedia.bookings.test.espresso.ViewActions
import android.support.test.espresso.action.ViewActions.typeText
import android.support.test.espresso.assertion.ViewAssertions
import android.support.test.espresso.contrib.RecyclerViewActions
import android.support.test.espresso.matcher.ViewMatchers
import android.support.v7.widget.RecyclerView
import java.util.concurrent.TimeUnit
import android.support.test.espresso.matcher.ViewMatchers.hasDescendant
import android.support.test.espresso.matcher.ViewMatchers.withEffectiveVisibility
import android.support.test.espresso.matcher.ViewMatchers.withText
import com.expedia.bookings.test.espresso.TabletViewActions
import android.support.test.espresso.matcher.ViewMatchers.withParent
import com.expedia.bookings.test.phone.hotels.HotelScreen
import org.hamcrest.Matchers.allOf

object SearchScreen {

    @JvmStatic fun origin(): ViewInteraction {
        return onView(withId(R.id.origin_card))
    }

    @JvmStatic fun calendar(): ViewInteraction {
        return onView(withId(R.id.calendar)).inRoot(withDecorView(not<View>(`is`<View>(SpoonScreenshotUtils.getCurrentActivity().window.decorView))))
    }

    @JvmStatic fun selectDates(start: LocalDate, end: LocalDate?) {
        calendar().perform(TabletViewActions.clickDates(start, end))
        searchAlertDialogDone().perform(click())
    }

    @JvmStatic fun searchAlertDialogDone(): ViewInteraction {
        return onView(withId(android.R.id.button1))
    }

    @JvmStatic fun searchButton(): ViewInteraction {
        return onView(allOf(withId(R.id.search_button), withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)))
    }

    @JvmStatic fun calendarCard(): ViewInteraction {
        return onView(withId(R.id.calendar_card))
    }

    @JvmStatic fun selectDateButton(): ViewInteraction {
        return onView(allOf<View>(withId(R.id.input_label), withParent(withId(R.id.calendar_card))))
    }

    @JvmStatic fun selectTravelerText(): ViewInteraction {
        return onView(allOf(withId(R.id.input_label), withParent(withId(R.id.traveler_card))))
    }

    @JvmStatic fun selectDestinationTextView(): ViewInteraction {
        return onView(allOf(withId(R.id.input_label), withParent(withId(R.id.destination_card))))
    }

    @JvmStatic fun selectGuestsButton(): ViewInteraction {
        return onView(withId(R.id.traveler_card))
    }

    fun setGuests(adults: Int, children: Int) {
        //Minimum 1 ADT selected
        for (i in 1..adults - 1) {
            incrementAdultsButton()
        }

        for (i in 0..children - 1) {
            incrementChildrenButton()
        }
        searchAlertDialogDone().perform(click())
    }

    @JvmStatic fun incrementChildrenButton() {
        onView(withId(R.id.children_plus)).perform(click())
    }

    @JvmStatic fun incrementAdultsButton() {
        onView(withId(R.id.adults_plus)).perform(click())
    }

    @JvmStatic fun destination(): ViewInteraction {
        return onView(withId(R.id.destination_card))
    }

    @Throws(Throwable::class)
    @JvmStatic fun search(adults: Int, children: Int, clickSwP: Boolean = false, hotelSearch: Boolean = false) {
        if (hotelSearch) {
            selectDestination()
        }
        else {
            selectOriginAndDestination()
        }
        val startDate = LocalDate.now().plusDays(3)
        val endDate = LocalDate.now().plusDays(8)
        selectDates(startDate, endDate)
        selectGuestsButton().perform(click())
        setGuests(adults, children)
        if (clickSwP) {
            HotelScreen.clickSwPToggle();
        }

        searchButton().perform(click())
    }

    @JvmStatic fun doGenericSearch() {
        search(1, 0, false)
    }

    @JvmStatic fun doGenericHotelSearchWithSwp() {
        search(1, 0, true, true)
    }

    @JvmStatic fun doGenericHotelSearch() {
        search(1, 0, false, true)
    }

    @Throws(Throwable::class)
    @JvmStatic fun selectOriginAndDestination() {
        searchEditText().perform(typeText("SFO"))
        selectLocation("San Francisco, CA (SFO-San Francisco Intl.)")
        //Delay from the auto advance anim
        Common.delay(1)
        searchEditText().perform(ViewActions.waitForViewToDisplay())
        searchEditText().perform(typeText("DTW"))
        selectLocation("Detroit, MI (DTW-Detroit Metropolitan Wayne County)")
    }

    @Throws(Throwable::class)
    @JvmStatic fun selectDestination() {
        searchEditText().perform(typeText("SFO"))
        selectLocation("San Francisco, CA (SFO-San Francisco Intl.)")
    }

    @Throws(Throwable::class)
    @JvmStatic fun selectLocation(hotel: String) {
        suggestionList().perform(ViewActions.waitForViewToDisplay())
        val viewMatcher = hasDescendant(withText(hotel))

        suggestionList().perform(ViewActions.waitFor(viewMatcher, 10, TimeUnit.SECONDS))
        suggestionList().perform(RecyclerViewActions.actionOnItem<RecyclerView.ViewHolder>(viewMatcher, click()))
    }

    @JvmStatic fun selectRecentSearch(location: String) {
        suggestionList().perform(RecyclerViewActions.actionOnItem<RecyclerView.ViewHolder>(hasDescendant(withText(location)), click()))
    }

    @JvmStatic fun suggestionList(): ViewInteraction {
        return onView(withId(R.id.suggestion_list))
    }

    @JvmStatic fun searchEditText(): ViewInteraction {
        return onView(withId(android.support.v7.appcompat.R.id.search_src_text))
    }
}
