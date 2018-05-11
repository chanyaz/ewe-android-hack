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
class HotelItinPricingAdditionalInfoActivityTest {
    lateinit var sut: HotelItinPricingAdditionalInfoActivity

    @Before
    fun setup() {
        val intent = Intent()
        intent.putExtra("ITINID", "TEST_ITIN")
        sut = Robolectric.buildActivity(HotelItinPricingAdditionalInfoActivity::class.java, intent).create().get()
    }

    @Test
    fun testFinishActivity() {
        val shadow = Shadows.shadowOf(sut)
        assertFalse(shadow.isFinishing)
        sut.finish()
        assertTrue(shadow.isFinishing)
    }

    @Test
    fun testAdditionInfoViewBackPressed() {
        val shadow = Shadows.shadowOf(sut)
        assertFalse(shadow.isFinishing)
        sut.additionalInfoView.toolbarViewModel.navigationBackPressedSubject.onNext(Unit)
        assertTrue(shadow.isFinishing)
    }

    @Test
    fun testHotelRepoInvalidItin() {
        val shadow = Shadows.shadowOf(sut)
        assertFalse(shadow.isFinishing)
        sut.invalidDataObserver.onChanged(Unit)
        assertTrue(shadow.isFinishing)
    }
}
