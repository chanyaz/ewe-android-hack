package com.expedia.bookings.test.phone.newhotels

import android.support.test.espresso.Espresso
import com.expedia.bookings.data.abacus.AbacusUtils
import com.expedia.bookings.test.espresso.AbacusTestUtils
import com.expedia.bookings.test.espresso.Common
import com.expedia.bookings.test.espresso.PhoneTestCase
import com.expedia.bookings.test.phone.pagemodels.common.LaunchScreen

class HotelRecentSearchABTest : PhoneTestCase() {

    @Throws(Throwable::class)
    override fun runTest() {
        AbacusTestUtils.bucketTests(AbacusUtils.EBAndroidAppHotelsABTest, AbacusUtils.EBAndroidAppHotelRecentSearchTest)
        super.runTest()
    }

    @Throws(Throwable::class)
    fun testHotelRecentSearch() {
        LaunchScreen.launchHotels()
        Espresso.closeSoftKeyboard()
        Common.pressBack()

        // Verify that in case of no recent searches, recent searches widget is not displayed.
        LaunchScreen.launchHotels()
        HotelScreen.clickSearchButton()

        //Make a search
        val location = "San Francisco, CA (SFO-San Francisco Intl.)"
        HotelScreen.doSearch(location)
        Common.pressBack()
        Common.pressBack()

        // Verify that in on search, the new item is displayed.
        LaunchScreen.launchHotels()
        HotelScreen.selectRecentSearch(location)
        HotelScreen.waitForResultsLoaded()
    }
}
