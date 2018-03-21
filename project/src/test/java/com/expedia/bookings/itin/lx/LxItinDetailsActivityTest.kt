package com.expedia.bookings.itin.lx

import android.content.Intent
import com.expedia.bookings.itin.lx.details.LxItinDetailsActivity
import com.expedia.bookings.test.robolectric.RobolectricRunner
import junit.framework.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.Shadows

@RunWith(RobolectricRunner::class)
class LxItinDetailsActivityTest {
    lateinit var sut: LxItinDetailsActivity

    @Before
    fun setup() {
        val intent = Intent()
        intent.putExtra("LX_ITIN_ID", "LX_ITIN_ID1")
        sut = Robolectric.buildActivity(LxItinDetailsActivity::class.java, intent).create().get()
    }

    @Test
    fun testFinishActivity() {
        val shadow = Shadows.shadowOf(sut)
        Assert.assertFalse(shadow.isFinishing)
        sut.finishActivity()
        Assert.assertTrue(shadow.isFinishing)
    }
}
