package com.expedia.bookings.test.pagemodels.common

import android.support.test.espresso.Espresso
import android.support.test.espresso.action.ViewActions
import android.support.test.espresso.contrib.RecyclerViewActions
import android.support.test.espresso.matcher.ViewMatchers
import android.support.v7.widget.RecyclerView
import android.view.View
import com.expedia.bookings.R
import com.expedia.bookings.test.espresso.CalendarPickerActions
import com.expedia.bookings.test.espresso.Common
import com.expedia.bookings.test.espresso.EspressoUtils
import com.expedia.bookings.test.espresso.TestValues
import com.expedia.bookings.test.pagemodels.hotels.HotelInfoSiteScreen
import com.mobiata.mocke3.FlightApiMockResponseGenerator
import org.hamcrest.Matcher
import org.hamcrest.Matchers
import org.joda.time.LocalDate
import java.util.concurrent.TimeUnit

object SearchScreenActions {

    @Throws(Throwable::class)
    @JvmStatic fun selectPackageOriginAndDestination() {
        SearchScreen.origin().perform(ViewActions.click())
        Common.delay(1)
        typeAndSelectLocation(TestValues.TYPE_TEXT_SFO, TestValues.PACKAGE_ORIGIN_LOCATION_SFO)
        typeAndSelectLocation(TestValues.TYPE_TEXT_DTW, TestValues.DESTINATION_LOCATION_DTW)
    }

    @Throws(Throwable::class)
    @JvmStatic fun selectRailOriginAndDestination() {
        SearchScreen.origin().perform(ViewActions.click())
        SearchScreen.waitForSearchEditText().perform(ViewActions.typeText(TestValues.TYPE_TEXT_LONDON))
        Espresso.closeSoftKeyboard()
        val originPosition = 17 // origin suggestion position in suggestion list
        SearchScreen.suggestionList().perform(RecyclerViewActions.scrollToPosition<RecyclerView.ViewHolder>(originPosition))
        selectLocation(TestValues.RAIL_ORIGIN_STATION)
        //Delay from the auto advance anim

        SearchScreen.destination()
                .perform(com.expedia.bookings.test.espresso.ViewActions.waitForViewToDisplay(), ViewActions.click())
        SearchScreen.waitForSearchEditText().perform(ViewActions.typeText(TestValues.TYPE_TEXT_GLASGOW))
        Espresso.closeSoftKeyboard()
        val destinationPosition = 18 // destination suggestion position in suggestion list
        SearchScreen.suggestionList().perform(RecyclerViewActions
                .scrollToPosition<RecyclerView.ViewHolder>(destinationPosition))
        selectLocation(TestValues.RAIL_DESTINATION_STATION)
    }

    @Throws(Throwable::class)
    @JvmStatic fun selectDestination() {
        typeAndSelectLocation(TestValues.TYPE_TEXT_SFO, TestValues.DESTINATION_LOCATION_SFO)
    }

    @Throws(Throwable::class)
    @JvmStatic fun selectLocation(location: String) {
        val viewMatcher = Matchers.allOf(
                ViewMatchers.hasDescendant(ViewMatchers.withId(R.id.suggestion_text_container)),
                ViewMatchers.hasDescendant(ViewMatchers.withText(Matchers.containsString(location))),
                ViewMatchers.withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE))
        selectSuggestion(viewMatcher)
    }

    @Throws(Throwable::class)
    @JvmStatic fun selectItemWithMagnifyingGlass() {
        val viewMatcher = Matchers.allOf(
                ViewMatchers.hasDescendant(ViewMatchers.withContentDescription("MAGNIFYING_GLASS_ICON")),
                ViewMatchers.hasDescendant(Matchers.allOf(ViewMatchers.withId(R.id.suggestion_text_container))))
        selectSuggestion(viewMatcher)
    }

    @Throws(Throwable::class)
    @JvmStatic fun selectHotelWithText(text: String) {
        val viewMatcher = Matchers.allOf(
                ViewMatchers.hasDescendant(ViewMatchers.withContentDescription("HOTEL_ICON")),
                ViewMatchers.hasDescendant(Matchers.allOf(ViewMatchers.withId(R.id.suggestion_text_container),
                        ViewMatchers.hasDescendant(ViewMatchers.withText(Matchers.containsString(text))))))
        selectSuggestion(viewMatcher)
    }

    @Throws(Throwable::class)
    @JvmStatic fun selectSpecificLocationWithText(text: String) {
        val viewMatcher = Matchers.allOf(
                ViewMatchers.hasDescendant(ViewMatchers.withContentDescription("SEARCH_TYPE_ICON")),
                ViewMatchers.hasDescendant(Matchers.allOf(ViewMatchers.withId(R.id.suggestion_text_container),
                        ViewMatchers.hasDescendant(ViewMatchers.withText(text)))))
        selectSuggestion(viewMatcher)
    }

    @JvmStatic private fun selectSuggestion(viewMatcher: Matcher<View>) {
        EspressoUtils.waitForViewNotYetInLayoutToDisplay(ViewMatchers.withId(R.id.suggestion_list), 10, TimeUnit.SECONDS)
        waitForSuggestions(viewMatcher)
        SearchScreen.suggestionList()
                .perform(RecyclerViewActions.actionOnItem<RecyclerView.ViewHolder>(viewMatcher, ViewActions.click()))
    }

    @JvmStatic fun waitForSuggestions(viewMatcher: Matcher<View>) {
        SearchScreen.suggestionList().perform(
                com.expedia.bookings.test.espresso.ViewActions.waitFor(viewMatcher, 10, TimeUnit.SECONDS))
    }

    @JvmStatic fun doGenericLXSearch() {
        typeAndSelectLocation(TestValues.TYPE_TEXT_SFO, TestValues.ACTIVITY_DESTINATION_LOCATION_SFO)
        val startDate = LocalDate.now()
        chooseDatesWithDialog(startDate, null)

        SearchScreen.searchButton().perform(ViewActions.click())
    }

    @JvmStatic fun doGenericHotelSearchWithSwp() {
        search(1, 0, true, true)
    }

    @JvmStatic fun doGenericHotelSearch() {
        search(1, 0, false, true)
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
        chooseDatesWithDialog(startDate, endDate)
        SearchScreen.selectGuestsButton().perform(ViewActions.click())
        setGuests(adults, children)
        if (clickSwP) {
            HotelInfoSiteScreen.clickSwPToggle()
        }

        SearchScreen.searchButton().perform(ViewActions.click())
    }

    @JvmStatic fun selectFlightOriginAndDestination(suggestionResponseType: FlightApiMockResponseGenerator.SuggestionResponseType,
                                                    destinationPosition: Int) {
        SearchScreen.origin().perform(ViewActions.click())
        SearchScreen.waitForSearchEditText().perform(ViewActions.typeText("origin"))
        Espresso.closeSoftKeyboard()
        Common.delay(1)
        SearchScreen.suggestionList().perform(com.expedia.bookings.test.espresso.ViewActions.waitForViewToDisplay(),
                RecyclerViewActions.actionOnItem<RecyclerView.ViewHolder>(ViewMatchers.hasDescendant(
                        ViewMatchers.withText(suggestionResponseType.suggestionString)), ViewActions.click()))

        //Delay for the auto advance to destination picker
        Common.delay(1)
        SearchScreen.waitForSearchEditText().perform(ViewActions.typeText("destination"))
        Espresso.closeSoftKeyboard()
        Common.delay(1)
        SearchScreen.suggestionList().perform(com.expedia.bookings.test.espresso.ViewActions.waitForViewToDisplay(),
                RecyclerViewActions.scrollToPosition<RecyclerView.ViewHolder>(destinationPosition),
                RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(destinationPosition, ViewActions.click()))
    }

    @Throws(Throwable::class)
    @JvmStatic fun typeAndSelectLocation(text: String , location: String) {
        SearchScreen.waitForSearchEditText().perform(ViewActions.typeText(text))
        selectLocation(location)
    }

    @Throws(Throwable::class)
    @JvmStatic fun selectFlightOriginAndDestination() {
        typeAndSelectLocation("happy", "happy")
        //Delay from the auto advance anim
        Common.delay(1)
        typeAndSelectLocation(TestValues.TYPE_TEXT_SFO, TestValues.FLIGHT_ORIGIN_LOCATION_SFO)
    }

    @Throws(Throwable::class)
    @JvmStatic fun selectSameFlightOriginAndDestination() {
        typeAndSelectLocation(TestValues.TYPE_TEXT_SFO, TestValues.FLIGHT_ORIGIN_LOCATION_SFO)
        //Delay from the auto advance anim
        Common.delay(1)
        typeAndSelectLocation(TestValues.TYPE_TEXT_SFO, TestValues.DESTINATION_LOCATION_SFO)
    }

    @JvmStatic fun chooseDatesWithDialog(start: LocalDate, end: LocalDate?) {
        chooseDates(start, end)
        SearchScreen.searchAlertDialogDone().perform(ViewActions.click())
    }

    @JvmStatic fun chooseDates(start: LocalDate, end: LocalDate?) {
        SearchScreen.calendar().perform(CalendarPickerActions.clickDates(start, end))
    }

    @JvmStatic fun validateDatesToolTip(firstLine: String, secondLine: String) {
        SearchScreen.calendar().perform(CalendarPickerActions.validateDatesTooltip(firstLine, secondLine))
    }

    @JvmStatic fun setGuests(adults: Int, children: Int) {
        //Minimum 1 ADT selected
        for (i in 1..adults - 1) {
            clickIncrementAdultsButton()
        }

        for (i in 0..children - 1) {
            clickIncrementChildButton()
        }
        SearchScreen.searchAlertDialogDone().perform(ViewActions.click())
    }

    @JvmStatic fun clickIncrementChildButton() {
        SearchScreen.incrementChildButton().perform(ViewActions.click())
    }

    @JvmStatic fun clickIncrementAdultsButton() {
        SearchScreen.incrementAdultsButton().perform(ViewActions.click())
    }
}
