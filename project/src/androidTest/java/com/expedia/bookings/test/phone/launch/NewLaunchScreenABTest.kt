package com.expedia.bookings.test.phone.hotels

import com.expedia.bookings.R
import com.expedia.bookings.data.abacus.AbacusUtils
import com.expedia.bookings.test.espresso.AbacusTestUtils
import com.expedia.bookings.test.espresso.EspressoUtils
import com.expedia.bookings.test.espresso.PhoneTestCase

class NewLaunchScreenABTest : PhoneTestCase() {

    @Throws(Throwable::class)
    override fun runTest() {
        AbacusTestUtils.bucketTests(AbacusUtils.EBAndroidAppLaunchScreenTest)
        super.runTest()
    }

    @Throws(Throwable::class)
    fun testHotelLaunchScreen() {
        EspressoUtils.assertViewIsDisplayed(R.id.lobView)
    }
}
