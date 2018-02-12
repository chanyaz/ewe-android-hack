package com.expedia.bookings.test.pagemodels.common

import android.support.test.espresso.Espresso
import android.support.test.espresso.Espresso.onView
import android.support.test.espresso.ViewInteraction
import android.support.test.espresso.action.ViewActions.click
import android.support.test.espresso.action.ViewActions.typeText
import android.support.test.espresso.contrib.RecyclerViewActions
import android.support.test.espresso.matcher.RootMatchers.withDecorView
import android.support.test.espresso.matcher.ViewMatchers
import android.support.test.espresso.matcher.ViewMatchers.hasDescendant
import android.support.test.espresso.matcher.ViewMatchers.isDescendantOfA
import android.support.test.espresso.matcher.ViewMatchers.withParent
import android.support.test.espresso.matcher.ViewMatchers.withClassName
import android.support.test.espresso.matcher.ViewMatchers.withEffectiveVisibility
import android.support.test.espresso.matcher.ViewMatchers.withId
import android.support.test.espresso.matcher.ViewMatchers.withText
import android.support.v7.widget.AppCompatImageButton
import android.support.v7.widget.RecyclerView
import android.view.View
import com.expedia.bookings.R
import com.expedia.bookings.test.espresso.Common
import com.expedia.bookings.test.espresso.CustomMatchers
import com.expedia.bookings.test.espresso.SpoonScreenshotUtils
import com.expedia.bookings.test.espresso.CalendarPickerActions
import com.expedia.bookings.test.espresso.EspressoUtils
import com.expedia.bookings.test.espresso.TestValues
import com.expedia.bookings.test.espresso.ViewActions
import com.expedia.bookings.test.pagemodels.hotels.HotelInfoSiteScreen
import com.mobiata.mocke3.FlightApiMockResponseGenerator
import org.hamcrest.Matcher
import org.hamcrest.Matchers
import org.hamcrest.Matchers.allOf
import org.hamcrest.Matchers.containsString
import org.hamcrest.Matchers.not
import org.hamcrest.core.Is.`is`
import org.joda.time.LocalDate
import java.util.concurrent.TimeUnit

object SearchScreen {
    @JvmStatic val suggestionList = withId(R.id.suggestion_list)

    @JvmStatic fun origin(): ViewInteraction {
        return onView(withId(R.id.origin_card))
    }

    @JvmStatic fun destination(): ViewInteraction {
        return onView(withId(R.id.destination_card))
    }

    @JvmStatic fun calendar(): ViewInteraction {
        return onView(withId(R.id.calendar)).inRoot(withDecorView(not<View>(`is`<View>(SpoonScreenshotUtils.getCurrentActivity().window.decorView))))
    }

    @JvmStatic fun flightClass(): ViewInteraction {
        return onView(withId(R.id.flight_cabin_class_widget))
    }

    @JvmStatic fun selectDates(start: LocalDate, end: LocalDate?) {
        calendar().perform(CalendarPickerActions.clickDates(start, end))
        searchAlertDialogDone().perform(click())
    }

    @JvmStatic fun chooseDates(start: LocalDate, end: LocalDate?) {
        calendar().perform(CalendarPickerActions.clickDates(start, end))
    }
    @JvmStatic fun validateDatesToolTip(firstLine: String, secondLine: String) {
        calendar().perform(CalendarPickerActions.validateDatesTooltip(firstLine, secondLine))
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

    @JvmStatic fun calendarSubtitle(): ViewInteraction {
        return onView(withId(R.id.instructions))
    }

    @JvmStatic fun nextMonthButton(): ViewInteraction {
        return onView(withId(R.id.next_month))
    }

    @JvmStatic fun previousMonthButton(): ViewInteraction {
        return onView(withId(R.id.previous_month))
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

    @JvmStatic fun incrementAdultTravelerButton(): ViewInteraction {
        return onView(allOf(withParent(withParent(withParent(withId(R.id.adult_count_selector)))), withClassName(`is`(AppCompatImageButton::class.java.name)), withId(R.id.traveler_plus)))
    }

    @JvmStatic fun incrementYouthTravelerButton(): ViewInteraction {
        return onView(allOf(withParent(withParent(withParent(withId(R.id.youth_count_selector)))), withClassName(`is`(AppCompatImageButton::class.java.name)), withId(R.id.traveler_plus)))
    }

    @JvmStatic fun incrementChildTravelerButton(): ViewInteraction {
        return onView(allOf(withParent(withParent(withParent(withId(R.id.child_count_selector)))), withClassName(`is`(AppCompatImageButton::class.java.name)), withId(R.id.traveler_plus)))
    }

    @JvmStatic fun incrementInfantTravelerButton(): ViewInteraction {
        return onView(allOf(withParent(withParent(withParent(withId(R.id.infant_count_selector)))), withClassName(`is`(AppCompatImageButton::class.java.name)), withId(R.id.traveler_plus)))
    }

    @JvmStatic fun decrementAdultTravelerButton(): ViewInteraction {
        return onView(allOf(withParent(withParent(withParent(withId(R.id.adult_count_selector)))), withClassName(`is`(AppCompatImageButton::class.java.name)), withId(R.id.traveler_minus)))
    }

    @JvmStatic fun decrementYouthTravelerButton(): ViewInteraction {
        return onView(allOf(withParent(withParent(withParent(withId(R.id.youth_count_selector)))), withClassName(`is`(AppCompatImageButton::class.java.name)), withId(R.id.traveler_minus)))
    }

    @JvmStatic fun decrementChildTravelerButton(): ViewInteraction {
        return onView(allOf(withParent(withParent(withParent(withId(R.id.child_count_selector)))), withClassName(`is`(AppCompatImageButton::class.java.name)), withId(R.id.traveler_minus)))
    }

    @JvmStatic fun decrementInfantTravelerButton(): ViewInteraction {
        return onView(allOf(withParent(withParent(withParent(withId(R.id.infant_count_selector)))), withClassName(`is`(AppCompatImageButton::class.java.name)), withId(R.id.traveler_minus)))
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
            HotelInfoSiteScreen.clickSwPToggle()
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
        return onView(withText(text)).inRoot(withDecorView(not(`is`(SpoonScreenshotUtils.getCurrentActivity().window.decorView))))
    }

    @Throws(Throwable::class)
    @JvmStatic fun selectPackageOriginAndDestination() {
        origin().perform(click())
        Common.delay(1)
        searchEditText().perform(typeText(TestValues.TYPE_TEXT_SFO))
        selectLocation(TestValues.PACKAGE_ORIGIN_LOCATION_SFO)
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
    @JvmStatic fun selectLocation(location: String) {
        val viewMatcher = hasDescendant(withText(containsString(location)))
        selectSuggestion(viewMatcher)
    }

    @Throws(Throwable::class)
    @JvmStatic fun selectSpecificLocation(location: String) {
        val viewMatcher = hasDescendant(allOf(
                withText(Matchers.hasToString(location)),
                withId(R.id.title_textview)
        ))

        selectSuggestion(viewMatcher)
    }

    @Throws(Throwable::class)
    @JvmStatic fun selectUnambiguousSuggestion(suggestion: String) {
        val viewMatcher = allOf(hasDescendant(withText(suggestion)), hasDescendant(CustomMatchers.withImageDrawable(R.drawable.search_type_icon)))
        selectSuggestion(viewMatcher)
    }

    @JvmStatic private fun selectSuggestion(viewMatcher: Matcher<View>) {
        EspressoUtils.waitForViewNotYetInLayoutToDisplay(suggestionList, 10, TimeUnit.SECONDS)
        waitForSuggestions(viewMatcher)
        suggestionList().perform(RecyclerViewActions.actionOnItem<RecyclerView.ViewHolder>(viewMatcher, click()))
    }

    @JvmStatic fun waitForSuggestions(viewMatcher: Matcher<View>) {
        suggestionList().perform(ViewActions.waitFor(viewMatcher, 10, TimeUnit.SECONDS))
    }

    @JvmStatic fun selectRecentSearch(location: String) {
        suggestionList().perform(RecyclerViewActions.actionOnItem<RecyclerView.ViewHolder>(hasDescendant(withText(location)), click()))
    }

    @JvmStatic fun suggestionList(): ViewInteraction {
        return onView(suggestionList)
    }

    @JvmStatic fun searchEditText(): ViewInteraction {
        return onView(withId(android.support.v7.appcompat.R.id.search_src_text)).perform(ViewActions.waitForViewToDisplay())
    }

    fun selectDate(startDate: LocalDate?) {
        calendar().perform(CalendarPickerActions.clickDates(startDate, null))
        searchAlertDialogDone().perform(click())
    }

    @JvmStatic fun didYouMeanAlertSuggestion(suggestionHasText: String): ViewInteraction {
        return onView(allOf(
                isDescendantOfA(withId(R.id.action_bar_root)),
                withId(R.id.select_dialog_listview),
                hasDescendant(withText(suggestionHasText))
        ))
    }
}
