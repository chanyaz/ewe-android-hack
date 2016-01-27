package com.expedia.bookings.test.phone.newhotels

import android.support.test.espresso.Espresso.onView
import android.support.test.espresso.action.ViewActions.click
import android.support.test.espresso.assertion.ViewAssertions.matches
import android.support.test.espresso.matcher.ViewMatchers.isDisplayed
import android.support.test.espresso.matcher.ViewMatchers.withText
import com.expedia.bookings.R
import com.expedia.bookings.data.abacus.AbacusUtils
import com.expedia.bookings.test.espresso.AbacusTestUtils
import com.expedia.bookings.test.espresso.EspressoUtils
import com.expedia.bookings.test.espresso.HotelTestCase

public class NewSearchScreenTest: HotelTestCase() {

    @Throws(Throwable::class)
    override fun runTest() {
        AbacusTestUtils.updateABTest(AbacusUtils.EBAndroidAppHotelsABTest,
                AbacusUtils.DefaultVariate.BUCKETED.ordinal)
        AbacusTestUtils.updateABTest(AbacusUtils.EBAndroidAppHotelsSearchScreenTest,
                AbacusUtils.DefaultVariate.BUCKETED.ordinal)
        super.runTest()
    }

    @Throws(Throwable::class)
    fun testNewSearchScreenTravelerInteraction() {
        EspressoUtils.assertViewIsDisplayed(R.id.widget_hotel_params_v2)
        HotelScreen.selectTraveler().perform(click())
        onView(withText(R.string.select_traveler_title)).check(matches(isDisplayed()));
        //To Do add more test scenario just added basic scenario
    }

}
