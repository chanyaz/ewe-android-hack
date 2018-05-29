package com.expedia.bookings.itin.lx.moreHelp

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
class LxItinMoreHelpActivityTest {
    private lateinit var activity: LxItinMoreHelpActivity

    @Before
    fun setup() {
        val intent = Intent()
        intent.putExtra("LX_ITIN_ID", "LX_ITIN_ID")
        activity = Robolectric.buildActivity(LxItinMoreHelpActivity::class.java, intent).create().start().get()
    }

    @Test
    fun testFinishActivity() {
        val shadow = Shadows.shadowOf(activity)
        assertFalse(shadow.isFinishing)
        activity.finish()
        assertTrue(shadow.isFinishing)
    }
}
