package com.expedia.bookings.test.phone.hotels

import android.support.test.espresso.Espresso.onView
import android.support.test.espresso.action.ViewActions.click
import android.support.test.espresso.action.ViewActions.typeText
import android.support.test.espresso.assertion.ViewAssertions.matches
import android.support.test.espresso.matcher.ViewMatchers.isDisplayed
import android.support.test.espresso.matcher.ViewMatchers.withId
import android.support.test.espresso.matcher.ViewMatchers.withText
import com.expedia.bookings.R
import com.expedia.bookings.test.espresso.Common
import com.expedia.bookings.test.espresso.EspressoUtils
import com.expedia.bookings.test.espresso.HotelTestCase
import com.expedia.bookings.test.espresso.ViewActions
import com.expedia.bookings.test.pagemodels.common.SearchScreen
import com.expedia.bookings.test.pagemodels.common.SearchScreenActions
import com.expedia.bookings.test.pagemodels.hotels.HotelResultsScreen
import com.expedia.bookings.utils.StrUtils
import org.joda.time.LocalDate
import org.junit.Test

class SearchScreenTest : HotelTestCase() {

    @Throws(Throwable::class)
    @Test
    fun testNewSearchScreenTravelerDismiss() {
        EspressoUtils.assertViewIsDisplayed(R.id.widget_hotel_search)

        SearchScreen.waitForSearchEditText().perform(typeText("SFO"))

        SearchScreenActions.selectLocation("San Francisco, CA (SFO-San Francisco Intl.)")
        onView(withText(R.string.DONE)).perform(ViewActions.waitForViewToDisplay())

        // opening calendar dialog click on widget
        onView(withText(R.string.DONE)).check(matches(isDisplayed()))

        val startDate = LocalDate.now().plusDays(10)
        SearchScreenActions.chooseDatesWithDialog(startDate, null)

        // closing calendar dialog click on widget
        SearchScreen.selectDestinationTextView().check(matches(withText("San Francisco, CA (SFO-San Francisco Intl.)")))
        SearchScreen.selectGuestsButton().check(matches(isDisplayed()))

        // opening traveler dialog click on widget
        SearchScreen.selectGuestsButton().perform(click())
        onView(withText(R.string.DONE)).check(matches(isDisplayed()))

        // closing traveler dialog click on widget
        SearchScreen.searchAlertDialogDone().perform(click())
        SearchScreen.selectGuestsButton().check(matches(isDisplayed()))
        SearchScreen.selectGuestsButton().check(matches(withText(StrUtils.formatGuestString(activity, 1))))

        // opening traveler dialog click on widget
        SearchScreen.selectGuestsButton().perform(click())
        onView(withText(R.string.DONE)).check(matches(isDisplayed()))
        onView(withId(R.id.adults_plus)).perform(click())

        // closing traveler dialog click on widget
        SearchScreen.searchAlertDialogDone().perform(click())
        SearchScreen.selectGuestsButton().check(matches(isDisplayed()))
        SearchScreen.selectGuestsButton().check(matches(withText(StrUtils.formatGuestString(activity, 2))))

        // opening traveler dialog click on widget
        SearchScreen.selectGuestsButton().perform(click())
        onView(withText(R.string.DONE)).check(matches(isDisplayed()))
        onView(withId(R.id.adults_plus)).perform(click())

        // closing traveler dialog click on widget
        Common.pressBack()
        SearchScreen.selectGuestsButton().check(matches(isDisplayed()))
        SearchScreen.selectGuestsButton().check(matches(withText(StrUtils.formatGuestString(activity, 2))))
    }

    @Throws(Throwable::class)
    @Test
    fun testNewSearchScreenToResult() {
        EspressoUtils.assertViewIsDisplayed(R.id.widget_hotel_search)

        SearchScreen.waitForSearchEditText().perform(typeText("SFO"))

        SearchScreenActions.selectLocation("San Francisco, CA (SFO-San Francisco Intl.)")
        onView(withText(R.string.DONE)).perform(ViewActions.waitForViewToDisplay())

        // opening calendar dialog click on widget
        onView(withText(R.string.DONE)).check(matches(isDisplayed()))

        val startDate = LocalDate.now().plusDays(10)
        SearchScreenActions.chooseDatesWithDialog(startDate, null)

        // closing calendar dialog click on widget
        SearchScreen.selectDestinationTextView().check(matches(withText("San Francisco, CA (SFO-San Francisco Intl.)")))
        SearchScreen.selectGuestsButton().check(matches(isDisplayed()))

        // opening traveler dialog click on widget
        SearchScreen.selectGuestsButton().perform(click())
        onView(withText(R.string.DONE)).check(matches(isDisplayed()))

        // closing traveler dialog click on widget
        SearchScreen.searchAlertDialogDone().perform(click())
        SearchScreen.selectGuestsButton().check(matches(isDisplayed()))

        //Search button will be enabled
        SearchScreen.searchButton().perform(click())
        HotelResultsScreen.waitForResultsLoaded()
    }
}
