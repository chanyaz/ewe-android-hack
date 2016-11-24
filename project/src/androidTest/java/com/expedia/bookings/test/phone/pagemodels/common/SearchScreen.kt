package com.expedia.bookings.test.phone.pagemodels.common

import android.support.test.espresso.Espresso
import android.support.test.espresso.Espresso.onView
import android.support.test.espresso.ViewInteraction
import android.support.test.espresso.action.ViewActions.click
import android.support.test.espresso.action.ViewActions.typeText
import android.support.test.espresso.contrib.RecyclerViewActions
import android.support.test.espresso.matcher.RootMatchers.withDecorView
import android.support.test.espresso.matcher.ViewMatchers
import android.support.test.espresso.matcher.ViewMatchers.*
import android.support.v7.widget.RecyclerView
import android.view.View
import com.expedia.bookings.R
import com.expedia.bookings.test.espresso.Common
import com.expedia.bookings.test.espresso.SpoonScreenshotUtils
import com.expedia.bookings.test.espresso.TabletViewActions
import com.expedia.bookings.test.espresso.TestValues
import com.expedia.bookings.test.espresso.ViewActions
import com.expedia.bookings.test.phone.hotels.HotelScreen
import com.mobiata.mocke3.FlightApiMockResponseGenerator
import org.hamcrest.Matchers.allOf
import org.hamcrest.Matchers.not
import org.hamcrest.core.Is.`is`
import org.joda.time.LocalDate
import java.util.concurrent.TimeUnit
import org.hamcrest.Matchers.containsString

object SearchScreen {

    @JvmStatic fun origin(): ViewInteraction {
        return onView(withId(R.id.origin_card))
    }

    @JvmStatic fun destination(): ViewInteraction {
        return onView(withId(R.id.destination_card))
    }

    @JvmStatic fun calendar(): ViewInteraction {
        return onView(withId(R.id.calendar)).inRoot(withDecorView(not<View>(`is`<View>(SpoonScreenshotUtils.getCurrentActivity().window.decorView))))
    }

    @JvmStatic fun selectDates(start: LocalDate, end: LocalDate?) {
        calendar().perform(TabletViewActions.clickDates(start, end))
        searchAlertDialogDone().perform(click())
    }

    @JvmStatic fun selectDatesOnly(start: LocalDate, end: LocalDate?) {
        calendar().perform(TabletViewActions.clickDates(start, end))
    }

    @JvmStatic fun searchAlertDialogDone(): ViewInteraction {
        return onView(withId(android.R.id.button1))
    }

    @JvmStatic fun searchButton(): ViewInteraction {
        val searchButton = onView(allOf(withId(R.id.search_btn), withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)))
        searchButton.perform(ViewActions.waitForViewToDisplay())
        return searchButton
    }

    @JvmStatic fun calendarCard(): ViewInteraction {
        return onView(withId(R.id.calendar_card))
    }

    @JvmStatic fun selectDateButton(): ViewInteraction {
        return onView(withId(R.id.calendar_card))
    }

    @JvmStatic fun selectTravelerText(): ViewInteraction {
        return onView(withId(R.id.traveler_card))
    }

    @JvmStatic fun selectDestinationTextView(): ViewInteraction {
        return onView(withId(R.id.destination_card))
    }

    @JvmStatic fun selectGuestsButton(): ViewInteraction {
        return onView(withId(R.id.traveler_card))
    }

    @JvmStatic fun addAdultsButton(): ViewInteraction {
        return onView(withId(R.id.adults_plus))
    }

    @JvmStatic fun removeAdultsButton(): ViewInteraction {
        return onView(withId(R.id.adults_minus))
    }

    @JvmStatic fun addChildButton(): ViewInteraction {
        return onView(withId(R.id.children_plus))
    }

    @JvmStatic fun removeChildButton(): ViewInteraction {
        return onView(withId(R.id.children_minus))
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

    @JvmStatic fun childAgeDropDown(childNumber: Int): ViewInteraction? {
        if (childNumber == 1) {
            return onView(withId(R.id.child_spinner_1))
        } else if (childNumber == 2) {
            return onView(withId(R.id.child_spinner_2))
        } else if (childNumber == 3) {
            return onView(withId(R.id.child_spinner_3))
        } else if (childNumber == 4) {
            return onView(withId(R.id.child_spinner_4))
        } else {
            return null
        }
    }

    @Throws(Throwable::class)
    @JvmStatic fun search(adults: Int, children: Int, clickSwP: Boolean = false, hotelSearch: Boolean = false) {
        if (hotelSearch) {
            selectDestination()
        } else {
            selectPackageOriginAndDestination()
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

    @JvmStatic fun doGenericLXSearch() {
        selectSearchLocation()
        val startDate = LocalDate.now()
        selectDates(startDate, null)

        searchButton().perform(click())
    }

    @JvmStatic fun doGenericCarSearch() {
        selectSearchLocation()
        val startDate = LocalDate.now()
        val endDate = startDate.plusDays(3)
        selectDates(startDate, endDate)

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

    @JvmStatic fun selectFlightOriginAndDestination(suggestionResponseType: FlightApiMockResponseGenerator.SuggestionResponseType, destinationPosition: Int) {
        origin().perform(click())
        searchEditText().perform(ViewActions.waitForViewToDisplay())
        searchEditText().perform(android.support.test.espresso.action.ViewActions.typeText("origin"))
        Espresso.closeSoftKeyboard()
        Common.delay(1)
        suggestionList().perform(ViewActions.waitForViewToDisplay())

        suggestionList().perform(RecyclerViewActions.actionOnItem<RecyclerView.ViewHolder>(hasDescendant(withText(suggestionResponseType.suggestionString))
                , click()))

        //Delay for the auto advance to destination picker
        Common.delay(1)
        searchEditText().perform(ViewActions.waitForViewToDisplay())
        searchEditText().perform(android.support.test.espresso.action.ViewActions.typeText("destination"))
        Espresso.closeSoftKeyboard()
        Common.delay(1)
        suggestionList().perform(ViewActions.waitForViewToDisplay())
        suggestionList().perform(RecyclerViewActions.scrollToPosition<RecyclerView.ViewHolder>(destinationPosition))
        suggestionList().perform(RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(destinationPosition, click()))
    }

    @Throws(Throwable::class)
    @JvmStatic fun selectFlightOrigin() {
        searchEditText().perform(typeText("happy"))
        selectLocation("happy")
    }

    @Throws(Throwable::class)
    @JvmStatic fun selectFlightOriginAndDestination() {
        searchEditText().perform(typeText("happy"))
        selectLocation("happy")
        //Delay from the auto advance anim
        Common.delay(1)
        searchEditText().perform(ViewActions.waitForViewToDisplay())
        searchEditText().perform(typeText(TestValues.TYPE_TEXT_SFO))
        selectLocation(TestValues.FLIGHT_ORIGIN_LOCATION_SFO)
    }

    @Throws(Throwable::class)
    @JvmStatic fun selectSameFlightOriginAndDestination() {
        searchEditText().perform(typeText(TestValues.TYPE_TEXT_SFO))
        selectLocation(TestValues.FLIGHT_ORIGIN_LOCATION_SFO)
        //Delay from the auto advance anim
        Common.delay(1)
        searchEditText().perform(ViewActions.waitForViewToDisplay())
        searchEditText().perform(typeText(TestValues.TYPE_TEXT_SFO))
        selectLocation(TestValues.DESTINATION_LOCATION_SFO)
    }

    @Throws(Throwable::class)
    @JvmStatic fun errorDialog(text: String): ViewInteraction {
        return onView(withText(text)).inRoot(withDecorView(not(`is`(SpoonScreenshotUtils.getCurrentActivity().window.decorView))));
    }

    @Throws(Throwable::class)
    @JvmStatic fun selectPackageOriginAndDestination() {
        searchEditText().perform(typeText(TestValues.TYPE_TEXT_SFO))
        selectLocation(TestValues.PACKAGE_ORIGIN_LOCATION_SFO)
        //Delay from the auto advance anim
        Common.delay(1)
        searchEditText().perform(ViewActions.waitForViewToDisplay())
        searchEditText().perform(typeText(TestValues.TYPE_TEXT_DTW))
        selectLocation(TestValues.DESTINATION_LOCATION_DTW)
    }

    @Throws(Throwable::class)
    @JvmStatic fun selectRailOriginAndDestination() {
        origin().perform(click())
        searchEditText().perform(typeText(TestValues.TYPE_TEXT_LONDON))
        Espresso.closeSoftKeyboard()
        val originPosition = 17 // origin suggestion position in suggestion list
        suggestionList().perform(RecyclerViewActions.scrollToPosition<RecyclerView.ViewHolder>(originPosition))
        selectLocation(TestValues.RAIL_ORIGIN_STATION)
        //Delay from the auto advance anim

        destination().perform(ViewActions.waitForViewToDisplay())
        destination().perform(click())
        searchEditText().perform(ViewActions.waitForViewToDisplay())
        searchEditText().perform(typeText(TestValues.TYPE_TEXT_GLASGOW))
        Espresso.closeSoftKeyboard()
        val destinationPosition = 18 // destination suggestion position in suggestion list
        suggestionList().perform(RecyclerViewActions.scrollToPosition<RecyclerView.ViewHolder>(destinationPosition))
        selectLocation(TestValues.RAIL_DESTINATION_STATION)
    }

    @Throws(Throwable::class)
    @JvmStatic fun selectDestination() {
        searchEditText().perform(typeText(TestValues.TYPE_TEXT_SFO))
        selectLocation(TestValues.DESTINATION_LOCATION_SFO)
    }

    @Throws(Throwable::class)
    @JvmStatic fun selectSearchLocation() {
        searchEditText().perform(typeText(TestValues.TYPE_TEXT_SFO))
        selectLocation(TestValues.ACTIVITY_DESTINATION_LOCATION_SFO)
    }

    @Throws(Throwable::class)
    @JvmStatic fun selectLocation(hotel: String) {
        suggestionList().perform(ViewActions.waitForViewToDisplay())
        val viewMatcher = hasDescendant(withText(containsString(hotel)))

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
        return onView(withId(android.support.v7.appcompat.R.id.search_src_text)).perform(ViewActions.waitForViewToDisplay())
    }

    fun selectDate(startDate: LocalDate?) {
        calendar().perform(TabletViewActions.clickDates(startDate, null))
        searchAlertDialogDone().perform(click())
    }
}
