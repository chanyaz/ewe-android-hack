package com.expedia.bookings.test.pagemodels.common

import android.support.test.espresso.Espresso
import android.support.test.espresso.action.ViewActions.click
import android.support.test.espresso.action.ViewActions.typeText
import android.support.test.espresso.contrib.RecyclerViewActions
import android.support.test.espresso.matcher.ViewMatchers.hasDescendant
import android.support.test.espresso.matcher.ViewMatchers.Visibility.VISIBLE
import android.support.test.espresso.matcher.ViewMatchers.withContentDescription
import android.support.test.espresso.matcher.ViewMatchers.withEffectiveVisibility
import android.support.test.espresso.matcher.ViewMatchers.withId
import android.support.test.espresso.matcher.ViewMatchers.withText
import android.support.v7.widget.RecyclerView
import android.view.View
import com.expedia.bookings.R
import com.expedia.bookings.test.espresso.CalendarPickerActions
import com.expedia.bookings.test.espresso.Common
import com.expedia.bookings.test.espresso.EspressoUtils
import com.expedia.bookings.test.espresso.TestValues
import com.expedia.bookings.test.espresso.ViewActions.waitForViewToDisplay
import com.expedia.bookings.test.pagemodels.hotels.HotelInfoSiteScreen
import com.mobiata.mocke3.FlightDispatcherUtils
import org.hamcrest.Matcher
import org.hamcrest.Matchers
import org.hamcrest.Matchers.allOf
import org.hamcrest.Matchers.containsString
import org.joda.time.LocalDate
import java.util.concurrent.TimeUnit

object SearchScreenActions {
    @JvmField val MAGNIFYING_GLASS_ICON = "MAGNIFYING_GLASS_ICON"
    @JvmField val LOCATION_ICON = "SEARCH_TYPE_ICON"
    @JvmField val HOTEL_ICON = "HOTEL_ICON"

    @Throws(Throwable::class)
    @JvmStatic fun selectPackageOriginAndDestination() {
        SearchScreen.origin().perform(click())
        Common.delay(1)
        typeAndSelectLocation(TestValues.TYPE_TEXT_SFO, TestValues.PACKAGE_ORIGIN_LOCATION_SFO)
        typeAndSelectLocation(TestValues.TYPE_TEXT_DTW, TestValues.DESTINATION_LOCATION_DTW)
    }

    @Throws(Throwable::class)
    @JvmStatic fun selectRailOriginAndDestination() {
        SearchScreen.origin().perform(click())
        SearchScreen.waitForSearchEditText().perform(typeText(TestValues.TYPE_TEXT_LONDON))
        Espresso.closeSoftKeyboard()
        val originPosition = 17 // origin suggestion position in suggestion list
        SearchScreen.suggestionList().perform(RecyclerViewActions.scrollToPosition<RecyclerView.ViewHolder>(originPosition))
        selectLocation(TestValues.RAIL_ORIGIN_STATION)
        //Delay from the auto advance anim

        SearchScreen.destination()
                .perform(com.expedia.bookings.test.espresso.ViewActions.waitForViewToDisplay(), click())
        SearchScreen.waitForSearchEditText().perform(typeText(TestValues.TYPE_TEXT_GLASGOW))
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
                hasDescendant(withId(R.id.suggestion_text_container)),
                hasDescendant(withText(Matchers.containsString(location))),
                withEffectiveVisibility(VISIBLE))
        selectSuggestion(viewMatcher)
    }

    @Throws(Throwable::class)
    @JvmStatic fun selectLocationForLxMip(location: String) {
        val originPosition = 19 // origin suggestion position in suggestion list
        SearchScreen.suggestionList().perform(RecyclerViewActions.scrollToPosition<RecyclerView.ViewHolder>(originPosition))
        selectLocation(location)
    }

    @Throws(Throwable::class)
    @JvmStatic fun selectItemWithMagnifyingGlass() {
        val viewMatcher = allOf(
                hasDescendant(withContentDescription(MAGNIFYING_GLASS_ICON)),
                hasDescendant(withId(R.id.suggestion_text_container))
        )
        selectSuggestion(viewMatcher)
    }

    @Throws(Throwable::class)
    @JvmStatic fun selectHotelWithText(text: String) {
        val viewMatcher = allOf(
                hasDescendant(withContentDescription(HOTEL_ICON)),
                hasDescendant(allOf(
                        withId(R.id.suggestion_text_container),
                        hasDescendant(withText(containsString(text)))
                )))
        selectSuggestion(viewMatcher)
    }

    @Throws(Throwable::class)
    @JvmStatic fun selectSpecificLocationWithText(text: String) {
        val viewMatcher = allOf(
                hasDescendant(withContentDescription(LOCATION_ICON)),
                hasDescendant(allOf(
                        withId(R.id.suggestion_text_container),
                        hasDescendant(withText(text))
                )))
        selectSuggestion(viewMatcher)
    }

    @JvmStatic private fun selectSuggestion(viewMatcher: Matcher<View>) {
        EspressoUtils.waitForViewNotYetInLayoutToDisplay(withId(R.id.suggestion_list), 10, TimeUnit.SECONDS)
        waitForSuggestions(viewMatcher)
        SearchScreen.suggestionList()
                .perform(RecyclerViewActions.actionOnItem<RecyclerView.ViewHolder>(viewMatcher, click()))
    }

    @JvmStatic fun waitForSuggestions(viewMatcher: Matcher<View>) {
        SearchScreen.suggestionList().perform(
                com.expedia.bookings.test.espresso.ViewActions.waitFor(viewMatcher, 10, TimeUnit.SECONDS))
    }

    @JvmStatic fun doGenericLXSearch() {
        typeAndSelectLocation(TestValues.TYPE_TEXT_SFO, TestValues.ACTIVITY_DESTINATION_LOCATION_SFO)
        val startDate = LocalDate.now()
        chooseDatesWithDialog(startDate, null)

        SearchScreen.searchButton().perform(click())
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
        SearchScreen.selectGuestsButton().perform(click())
        setGuests(adults, children)
        if (clickSwP) {
            HotelInfoSiteScreen.clickSwPToggle()
        }

        SearchScreen.searchButton().perform(click())
    }

    @JvmStatic fun selectFlightOriginAndDestination(suggestionResponseType: FlightDispatcherUtils.SuggestionResponseType,
                                                    destinationPosition: Int) {
        SearchScreen.origin().perform(click())
        SearchScreen.waitForSearchEditText().perform(typeText("origin"))
        Espresso.closeSoftKeyboard()
        Common.delay(1)
        SearchScreen.suggestionList().perform(com.expedia.bookings.test.espresso.ViewActions.waitForViewToDisplay(),
                RecyclerViewActions.actionOnItem<RecyclerView.ViewHolder>(hasDescendant(
                        withText(suggestionResponseType.suggestionString)), click()))

        //Delay for the auto advance to destination picker
        Common.delay(1)
        SearchScreen.waitForSearchEditText().perform(typeText("destination"))
        Espresso.closeSoftKeyboard()
        Common.delay(1)
        SearchScreen.suggestionList().perform(com.expedia.bookings.test.espresso.ViewActions.waitForViewToDisplay(),
                RecyclerViewActions.scrollToPosition<RecyclerView.ViewHolder>(destinationPosition),
                RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(destinationPosition, click()))
    }

    @Throws(Throwable::class)
    @JvmStatic fun typeAndSelectLocation(text: String , location: String) {
        SearchScreen.waitForSearchEditText().perform(typeText(text))
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
        SearchScreen.searchAlertDialogDone().perform(click())
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
            clickIncrementAdultTravelerButton()
        }

        for (i in 0..children - 1) {
            clickIncrementChildTravelerButton()
        }
        SearchScreen.searchAlertDialogDone().perform(click())
    }

    @JvmStatic fun clickIncrementAdultTravelerButton() {
        SearchScreen.incrementAdultTravelerButton().perform(waitForViewToDisplay(), click())
    }

    @JvmStatic fun clickIncrementYouthTravelerButton() {
        SearchScreen.incrementYouthTravelerButton().perform(waitForViewToDisplay(), click())
    }

    @JvmStatic fun clickIncrementChildTravelerButton() {
        SearchScreen.incrementChildTravelerButton().perform(waitForViewToDisplay(), click())
    }

    @JvmStatic fun clickIncrementInfantTravelerButton() {
        SearchScreen.incrementInfantTravelerButton().perform(waitForViewToDisplay(), click())
    }

    @JvmStatic fun clickDecrementAdultTravelerButton() {
        SearchScreen.decrementAdultTravelerButton().perform(waitForViewToDisplay(), click())
    }

    @JvmStatic fun clickDecrementYouthTravelerButton() {
        SearchScreen.decrementYouthTravelerButton().perform(waitForViewToDisplay(), click())
    }

    @JvmStatic fun clickDecrementChildTravelerButton() {
        SearchScreen.decrementChildTravelerButton().perform(waitForViewToDisplay(), click())
    }

    @JvmStatic fun clickDecrementInfantTravelerButton() {
        SearchScreen.decrementInfantTravelerButton().perform(waitForViewToDisplay(), click())
    }
}
