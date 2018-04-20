package com.expedia.bookings.itin.hotel.pricingRewards

import android.content.Intent
import com.expedia.bookings.test.robolectric.RobolectricRunner
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.Shadows
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@RunWith(RobolectricRunner::class)
class HotelItinPricingRewardsActivityTest {
    lateinit var sut: HotelItinPricingRewardsActivity

    @Before
    fun setup() {
        val intent = Intent()
        intent.putExtra("ITINID", "ITIN1")
        sut = Robolectric.buildActivity(HotelItinPricingRewardsActivity::class.java, intent).create().get()
    }

    @Test
    fun testFinishActivity() {
        val shadow = Shadows.shadowOf(sut)
        assertFalse(shadow.isFinishing)
        sut.finishActivity()
        assertTrue(shadow.isFinishing)
    }
}
