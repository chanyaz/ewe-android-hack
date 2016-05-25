package com.expedia.bookings.test.phone.hotels

import android.support.test.espresso.ViewAssertion
import com.expedia.bookings.data.abacus.AbacusUtils
import com.expedia.bookings.test.espresso.AbacusTestUtils
import com.expedia.bookings.test.espresso.Common
import com.expedia.bookings.test.espresso.PhoneTestCase
import com.expedia.bookings.test.phone.pagemodels.common.LaunchScreen
import com.expedia.bookings.test.phone.pagemodels.common.SearchScreen
import android.support.test.espresso.action.ViewActions.click
import android.support.test.espresso.assertion.ViewAssertions
import com.expedia.bookings.test.espresso.CustomMatchers
import org.joda.time.LocalDate

class HotelRecentSearchABTest : PhoneTestCase() {

    @Throws(Throwable::class)
    override fun runTest() {
        AbacusTestUtils.bucketTests(AbacusUtils.EBAndroidAppHotelRecentSearchTest)
        super.runTest()
    }

    @Throws(Throwable::class)
    fun testHotelRecentSearch() {
        // Verify that in case of no recent searches, recent searches widget is not displayed.
        LaunchScreen.launchHotels()
        SearchScreen.suggestionList()
                        .perform(com.expedia.bookings.test.espresso.ViewActions.waitForViewToDisplay())
                        .check(ViewAssertions.matches(CustomMatchers.withChildCount(0)))
        Common.pressBack()
        Common.pressBack()

        //Make a search
        SearchScreen.destination().perform(click())
        SearchScreen.selectDestination()
        val startDate = LocalDate.now().plusDays(3)
        val endDate = LocalDate.now().plusDays(8)
        SearchScreen.selectDates(startDate, endDate)
        Common.pressBack()

        // Verify that in on search, the new item is displayed.
        LaunchScreen.launchHotels()
        SearchScreen.suggestionList()
                .perform(com.expedia.bookings.test.espresso.ViewActions.waitForViewToDisplay())
                .check(ViewAssertions.matches(CustomMatchers.withChildCount(1)))
        SearchScreen.selectRecentSearch("San Francisco, CA (SFO-San Francisco Intl.)")
    }
}
