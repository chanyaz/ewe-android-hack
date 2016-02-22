package com.expedia.bookings.test.phone.newhotels

import com.expedia.bookings.data.abacus.AbacusUtils
import com.expedia.bookings.test.espresso.AbacusTestUtils
import com.expedia.bookings.test.espresso.HotelTestCase

public class NewSearchScreenTest: HotelTestCase() {

    @Throws(Throwable::class)
    override fun runTest() {
        AbacusTestUtils.updateABTest(AbacusUtils.EBAndroidAppHotelsSearchScreenTest,
                AbacusUtils.DefaultVariate.BUCKETED.ordinal)
        super.runTest()
    }

    @Throws(Throwable::class)
    fun testNewSearchScreenTravelerInteraction() {
//        EspressoUtils.assertViewIsDisplayed(R.id.widget_hotel_params_v2)
//        HotelScreen.selectTraveler().perform(click())
//        onView(withText(R.string.select_traveler_title)).check(matches(isDisplayed()));
        //To Do add more test scenario just added basic scenario
    }

}
