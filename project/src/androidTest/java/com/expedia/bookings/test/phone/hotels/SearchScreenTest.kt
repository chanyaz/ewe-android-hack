package com.expedia.bookings.test.phone.hotels

import android.support.test.espresso.Espresso.onView
import android.support.test.espresso.action.ViewActions.click
import android.support.test.espresso.action.ViewActions.typeText
import android.support.test.espresso.assertion.ViewAssertions.matches
import android.support.test.espresso.matcher.ViewMatchers.isDisplayed
import android.support.test.espresso.matcher.ViewMatchers.withId
import android.support.test.espresso.matcher.ViewMatchers.withText
import android.text.style.RelativeSizeSpan
import com.expedia.bookings.R
import com.expedia.bookings.test.espresso.Common
import com.expedia.bookings.test.espresso.EspressoUtils
import com.expedia.bookings.test.espresso.HotelTestCase
import com.expedia.bookings.test.espresso.ViewActions
import com.expedia.bookings.test.pagemodels.common.SearchScreen
import com.expedia.bookings.test.pagemodels.hotels.HotelScreen
import com.expedia.bookings.utils.DateUtils
import com.expedia.bookings.utils.LocaleBasedDateFormatUtils
import com.expedia.bookings.utils.SpannableBuilder
import com.expedia.bookings.utils.StrUtils
import com.mobiata.android.time.util.JodaUtils
import com.squareup.phrase.Phrase
import org.joda.time.LocalDate
import org.junit.Test

class SearchScreenTest : HotelTestCase() {

//    @Throws(Throwable::class)
//    fun testNewSearchScreenCalendarDismiss() {
//        EspressoUtils.assertViewIsDisplayed(R.id.widget_hotel_search)
//
//        SearchScreen.searchEditText().perform(ViewActions.waitForViewToDisplay(), typeText("SFO"))
//        Common.closeSoftKeyboard(SearchScreen.searchEditText())
//        SearchScreen.selectLocation("Hyatt Regency San Francisco");
//        onView(withText(R.string.DONE)).perform(ViewActions.waitForViewToDisplay())
//
//        val firstStartDate = LocalDate.now().plusDays(10)
//        val firstEndDate = LocalDate.now().plusDays(12)
//
//        // opening calendar dialog click on widget
//        onView(withText(R.string.DONE)).check(matches(isDisplayed()))
//        SearchScreen.selectDates(firstStartDate, firstEndDate)
//        SearchScreen.selectDestinationTextView().check(matches(withText("Hyatt Regency San Francisco")))
//
//
//        SearchScreen.calendarCard().perform(ViewActions.waitForViewToDisplay())
//        SearchScreen.selectDateButton().check(matches(withText(computeDateText(firstStartDate, firstEndDate).toString())))
//
//        val secondStartDate = LocalDate.now().plusDays(20)
//        val secondEndDate = LocalDate.now().plusDays(22)
//
//        SearchScreen.calendarCard().perform(click())
//        onView(withText(R.string.DONE)).check(matches(isDisplayed()))
//        SearchScreen.selectDates(secondStartDate, secondEndDate)
//
//        // verify updates when second set of dates is selected
//        SearchScreen.calendarCard().perform(ViewActions.waitForViewToDisplay())
//        SearchScreen.selectDateButton().check(matches(withText(computeDateText(secondStartDate, secondEndDate).toString())))
//
//        val thirdStartDate = LocalDate.now().plusDays(15)
//        val thirdEndDate = LocalDate.now().plusDays(17)
//
//        SearchScreen.calendarCard().perform(click())
//        onView(withText(R.string.DONE)).check(matches(isDisplayed()))
//        SearchScreen.selectDates(thirdStartDate, thirdEndDate)
//        Common.pressBack() //NOTE - pressing back instead of clicking "DONE"
//
//        // make sure second date is still displayed on back
//        SearchScreen.calendarCard().perform(ViewActions.waitForViewToDisplay())
//        SearchScreen.selectDateButton().check(matches(withText(computeDateText(secondStartDate, secondEndDate).toString())))
//    }

    @Throws(Throwable::class)
    @Test
    fun testNewSearchScreenTravelerDismiss() {
        EspressoUtils.assertViewIsDisplayed(R.id.widget_hotel_search)

        SearchScreen.searchEditText().perform(ViewActions.waitForViewToDisplay(), typeText("SFO"))
        Common.closeSoftKeyboard(SearchScreen.searchEditText())
        SearchScreen.selectLocation("Hyatt Regency San Francisco");
        onView(withText(R.string.DONE)).perform(ViewActions.waitForViewToDisplay())
        val firstStartDate = LocalDate.now().plusDays(10)
        SearchScreen.selectDates(firstStartDate, firstStartDate.plusDays(2))

        // opening traveler dialog click on widget
        SearchScreen.selectGuestsButton().perform(click())
        onView(withText(R.string.DONE)).check(matches(isDisplayed()))

        // closing traveler dialog click on widget
        SearchScreen.searchAlertDialogDone().perform(click())
        SearchScreen.selectGuestsButton().check(matches(isDisplayed()))
        SearchScreen.selectTravelerText().check(matches(withText(StrUtils.formatGuestString(activity, 1))))

        // opening traveler dialog click on widget
        SearchScreen.selectGuestsButton().perform(click())
        onView(withText(R.string.DONE)).check(matches(isDisplayed()))
        onView(withId(R.id.adults_plus)).perform(click())

        // closing traveler dialog click on widget
        SearchScreen.searchAlertDialogDone().perform(click())
        SearchScreen.selectGuestsButton().check(matches(isDisplayed()))
        SearchScreen.selectTravelerText().check(matches(withText(StrUtils.formatGuestString(activity, 2))))

        // opening traveler dialog click on widget
        SearchScreen.selectGuestsButton().perform(click())
        onView(withText(R.string.DONE)).check(matches(isDisplayed()))
        onView(withId(R.id.adults_plus)).perform(click())

        // closing traveler dialog click on widget
        Common.pressBack()
        SearchScreen.selectGuestsButton().check(matches(isDisplayed()))
        SearchScreen.selectTravelerText().check(matches(withText(StrUtils.formatGuestString(activity, 2))))
    }

    @Throws(Throwable::class)
    @Test
    fun testNewSearchScreenToResult() {
        EspressoUtils.assertViewIsDisplayed(R.id.widget_hotel_search)

        SearchScreen.searchEditText().perform(ViewActions.waitForViewToDisplay(), typeText("SFO"))

        SearchScreen.selectLocation("San Francisco, CA (SFO-San Francisco Intl.)")
        onView(withText(R.string.DONE)).perform(ViewActions.waitForViewToDisplay())

        // opening calendar dialog click on widget
        onView(withText(R.string.DONE)).check(matches(isDisplayed()))

        val startDate = LocalDate.now().plusDays(10)
        SearchScreen.selectDates(startDate, null)

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
        HotelScreen.waitForResultsLoaded()
    }

    @Throws(Throwable::class)
    @Test
    fun testNewSearchScreenToDetail() {
        SearchScreen.searchEditText().perform(ViewActions.waitForViewToDisplay(), typeText("SFO"))
        Common.closeSoftKeyboard(SearchScreen.searchEditText())
        SearchScreen.selectLocation("Hyatt Regency San Francisco");

        onView(withText(R.string.DONE)).perform(ViewActions.waitForViewToDisplay())
        onView(withText(R.string.DONE)).check(matches(isDisplayed()))

        val startDate = LocalDate.now().plusDays(10)
        SearchScreen.selectDates(startDate, null)

        SearchScreen.selectDestinationTextView().check(matches(withText("Hyatt Regency San Francisco")))

        SearchScreen.selectGuestsButton().check(matches(isDisplayed()))

        SearchScreen.searchButton().perform(click())
        HotelScreen.waitForDetailsLoaded()
        HotelScreen.selectRoomButton().check(matches(isDisplayed()))
    }

    private fun computeDateText(start: LocalDate?, end: LocalDate?): CharSequence {
        val dateRangeText = computeDateRangeText(start, end)
        val sb = SpannableBuilder()
        sb.append(dateRangeText)

        if (start != null && end != null) {
            val nightCount = JodaUtils.daysBetween(start, end)
            val nightsString = instrumentation.targetContext.resources.getQuantityString(R.plurals.length_of_stay, nightCount, nightCount)
            sb.append(" ");
            sb.append(instrumentation.targetContext.resources.getString(R.string.nights_count_TEMPLATE, nightsString), RelativeSizeSpan(0.8f))
        }
        return sb.build()
    }

    private fun computeDateRangeText(start: LocalDate?, end: LocalDate?): String? {
        if (start == null && end == null) {
            return instrumentation.targetContext.resources.getString(R.string.select_dates)
        } else if (end == null) {
            return instrumentation.targetContext.resources.getString(R.string.select_checkout_date_TEMPLATE, LocaleBasedDateFormatUtils.localDateToMMMd(start!!))
        } else {
            return Phrase.from(instrumentation.targetContext, R.string.calendar_instructions_date_range_TEMPLATE).put("startdate", LocaleBasedDateFormatUtils.localDateToMMMd(start!!)).put("enddate", LocaleBasedDateFormatUtils.localDateToMMMd(end)).format().toString()
        }
    }
}
