package com.expedia.bookings.test.phone.newhotels

import android.support.test.espresso.Espresso.onView
import android.support.test.espresso.action.ViewActions.click
import android.support.test.espresso.action.ViewActions.typeText
import android.support.test.espresso.assertion.ViewAssertions.matches
import android.support.test.espresso.matcher.ViewMatchers.isDisplayed
import android.support.test.espresso.matcher.ViewMatchers.withText
import com.expedia.bookings.R
import com.expedia.bookings.data.abacus.AbacusUtils
import com.expedia.bookings.test.espresso.AbacusTestUtils
import com.expedia.bookings.test.espresso.Common
import com.expedia.bookings.test.espresso.EspressoUtils
import com.expedia.bookings.test.espresso.HotelTestCase
import org.joda.time.LocalDate


public class NewSearchScreenTest: HotelTestCase() {

    @Throws(Throwable::class)
    override fun runTest() {
        AbacusTestUtils.updateABTest(AbacusUtils.EBAndroidAppHotelsSearchScreenTest,
                AbacusUtils.DefaultVariate.BUCKETED.ordinal)
        super.runTest()
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
        HotelScreen.locationV2().perform(typeText("SFO"))

        HotelScreen.selectLocationV2("San Francisco, CA (SFO-San Francisco Intl.)")
        HotelScreen.selectDestinationTextViewV2().check(matches(withText("San Francisco, CA (SFO-San Francisco Intl.)")))

        //Search button will be enabled
        HotelScreen.searchButtonV2().perform(click())
        HotelScreen.waitForResultsLoaded()

    }

    @Throws(Throwable::class)
    fun testNewSearchScreenToDetail() {
        HotelScreen.selectDestinationV2().perform(click())
        HotelScreen.locationV2().perform(typeText("SFO"))
        Common.closeSoftKeyboard(HotelScreen.locationV2())
        HotelScreen.selectLocationV2("Hyatt Regency San Francisco");
        HotelScreen.selectDestinationTextViewV2().check(matches(withText("Hyatt Regency San Francisco")))

        HotelScreen.selectCalendarV2().perform(click())
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
