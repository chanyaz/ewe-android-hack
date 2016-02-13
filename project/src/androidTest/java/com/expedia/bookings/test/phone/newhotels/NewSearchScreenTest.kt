package com.expedia.bookings.test.phone.newhotels

import android.support.test.espresso.Espresso.onView
import android.support.test.espresso.action.ViewActions.click
import android.support.test.espresso.action.ViewActions.typeText
import android.support.test.espresso.assertion.ViewAssertions.matches
import android.support.test.espresso.matcher.ViewMatchers.isDisplayed
import android.support.test.espresso.matcher.ViewMatchers.withId
import android.support.test.espresso.matcher.ViewMatchers.withText
import com.expedia.bookings.R
import com.expedia.bookings.data.abacus.AbacusUtils
import com.expedia.bookings.test.espresso.AbacusTestUtils
import com.expedia.bookings.test.espresso.Common
import com.expedia.bookings.test.espresso.EspressoUtils
import com.expedia.bookings.test.espresso.HotelTestCase
import com.expedia.bookings.test.espresso.ViewActions
import com.expedia.bookings.utils.StrUtils
import com.expedia.vm.HotelSearchViewModel
import org.joda.time.LocalDate

public class NewSearchScreenTest : HotelTestCase() {

    @Throws(Throwable::class)
    override fun runTest() {
        AbacusTestUtils.updateABTest(AbacusUtils.EBAndroidAppHotelsSearchScreenTest,
                AbacusUtils.DefaultVariate.BUCKETED.ordinal)
        super.runTest()
    }

    @Throws(Throwable::class)
    fun testNewSearchScreenCalendarDismiss() {
        EspressoUtils.assertViewIsDisplayed(R.id.widget_hotel_search_v2)

        val firstStartDate = LocalDate.now().plusDays(10)
        val firstEndDate = LocalDate.now().plusDays(12)

        // opening calendar dialog click on widget
        HotelScreen.selectCalendarV2().perform(click())
        onView(withText(R.string.select_dates)).check(matches(isDisplayed()))
        HotelScreen.selectDates(firstStartDate, firstEndDate)
        HotelScreen.searchAlertDialogDoneV2().perform(click())

        HotelScreen.selectCalendarV2().perform(ViewActions.waitForViewToDisplay())
        HotelScreen.selectCalendarV2().check(matches(withText(HotelSearchViewModel.computeDateText(activity, firstStartDate, firstEndDate).toString())))

        val secondStartDate = LocalDate.now().plusDays(20)
        val secondEndDate = LocalDate.now().plusDays(22)

        HotelScreen.selectCalendarV2().perform(click())
        onView(withText(R.string.select_dates)).check(matches(isDisplayed()))
        HotelScreen.selectDates(secondStartDate, secondEndDate)
        HotelScreen.searchAlertDialogDoneV2().perform(click())

        // verify updates when second set of dates is selected
        HotelScreen.selectCalendarV2().perform(ViewActions.waitForViewToDisplay())
        HotelScreen.selectCalendarV2().check(matches(withText(HotelSearchViewModel.computeDateText(activity, secondStartDate, secondEndDate).toString())))

        val thirdStartDate = LocalDate.now().plusDays(15)
        val thirdEndDate = LocalDate.now().plusDays(17)

        HotelScreen.selectCalendarV2().perform(click())
        onView(withText(R.string.select_dates)).check(matches(isDisplayed()))
        HotelScreen.selectDates(thirdStartDate, thirdEndDate)
        Common.pressBack() //NOTE - pressing back instead of clicking "DONE"

        // make sure second date is still displayed on back
        HotelScreen.selectCalendarV2().perform(ViewActions.waitForViewToDisplay())
        HotelScreen.selectCalendarV2().check(matches(withText(HotelSearchViewModel.computeDateText(activity, secondStartDate, secondEndDate).toString())))
    }

    @Throws(Throwable::class)
    fun testNewSearchScreenTravelerDismiss() {
        EspressoUtils.assertViewIsDisplayed(R.id.widget_hotel_search_v2)

        // opening traveler dialog click on widget
        HotelScreen.selectTravelerV2().perform(click())
        onView(withText(R.string.select_traveler_title)).check(matches(isDisplayed()))

        // closing traveler dialog click on widget
        HotelScreen.searchAlertDialogDoneV2().perform(click())
        HotelScreen.selectTravelerV2().check(matches(isDisplayed()))
        HotelScreen.selectTravelerV2().check(matches(withText(StrUtils.formatGuestString(activity, 1))))

        // opening traveler dialog click on widget
        HotelScreen.selectTravelerV2().perform(click())
        onView(withText(R.string.select_traveler_title)).check(matches(isDisplayed()))
        onView(withId(R.id.adults_plus)).perform(click())

        // closing traveler dialog click on widget
        HotelScreen.searchAlertDialogDoneV2().perform(click())
        HotelScreen.selectTravelerV2().check(matches(isDisplayed()))
        HotelScreen.selectTravelerV2().check(matches(withText(StrUtils.formatGuestString(activity, 2))))

        // opening traveler dialog click on widget
        HotelScreen.selectTravelerV2().perform(click())
        onView(withText(R.string.select_traveler_title)).check(matches(isDisplayed()))
        onView(withId(R.id.adults_plus)).perform(click())

        // closing traveler dialog click on widget
        Common.pressBack()
        HotelScreen.selectTravelerV2().check(matches(isDisplayed()))
        HotelScreen.selectTravelerV2().check(matches(withText(StrUtils.formatGuestString(activity, 2))))
    }

    @Throws(Throwable::class)
    fun testNewSearchScreenToResult() {
        EspressoUtils.assertViewIsDisplayed(R.id.widget_hotel_search_v2)

        // opening traveler dialog click on widget
        HotelScreen.selectTravelerV2().perform(click())
        onView(withText(R.string.select_traveler_title)).check(matches(isDisplayed()))

        // closing traveler dialog click on widget
        HotelScreen.searchAlertDialogDoneV2().perform(click())
        HotelScreen.selectTravelerV2().check(matches(isDisplayed()))

        // opening calendar dialog click on widget
        HotelScreen.selectCalendarV2().perform(click())
        onView(withText(R.string.select_dates)).check(matches(isDisplayed()))

        val startDate = LocalDate.now().plusDays(10)
        HotelScreen.selectDates(startDate, null)

        // closing calendar dialog click on widget
        HotelScreen.searchAlertDialogDoneV2().perform(click())
        HotelScreen.selectTravelerV2().check(matches(isDisplayed()))

        HotelScreen.selectDestinationV2().perform(click())
        HotelScreen.locationV2().perform(ViewActions.waitForViewToDisplay(), typeText("SFO"))

        HotelScreen.selectLocationV2("San Francisco, CA (SFO-San Francisco Intl.)")
        HotelScreen.selectDestinationTextViewV2().check(matches(withText("San Francisco, CA (SFO-San Francisco Intl.)")))

        //Search button will be enabled
        HotelScreen.searchButtonV2().perform(click())
        HotelScreen.waitForResultsLoaded()
    }

    @Throws(Throwable::class)
    fun testNewSearchScreenToDetail() {
        HotelScreen.selectDestinationV2().perform(click())
        HotelScreen.locationV2().perform(ViewActions.waitForViewToDisplay(), typeText("SFO"))
        Common.closeSoftKeyboard(HotelScreen.locationV2())
        HotelScreen.selectLocationV2("Hyatt Regency San Francisco");
        HotelScreen.selectDestinationTextViewV2().check(matches(withText("Hyatt Regency San Francisco")))

        HotelScreen.selectCalendarV2().perform(click())
        onView(withText(R.string.select_dates)).perform(ViewActions.waitForViewToDisplay())
        onView(withText(R.string.select_dates)).check(matches(isDisplayed()))

        val startDate = LocalDate.now().plusDays(10)
        HotelScreen.selectDates(startDate, null)

        HotelScreen.searchAlertDialogDoneV2().perform(click())
        HotelScreen.selectTravelerV2().check(matches(isDisplayed()))

        HotelScreen.searchButtonV2().perform(click())
        HotelScreen.waitForDetailsLoaded()
        HotelScreen.selectRoomButton().check(matches(isDisplayed()))
    }
}
