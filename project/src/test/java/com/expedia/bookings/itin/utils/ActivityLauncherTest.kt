package com.expedia.bookings.itin.utils

import android.support.v7.app.AppCompatActivity
import com.expedia.bookings.itin.lx.details.LxItinDetailsActivity
import com.expedia.bookings.test.robolectric.RobolectricRunner
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.Shadows
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

@RunWith(RobolectricRunner::class)
class ActivityLauncherTest {
    lateinit var sut: ActivityLauncher
    lateinit var activity: AppCompatActivity

    @Before
    fun setup() {
        activity = Robolectric.buildActivity(AppCompatActivity::class.java).get()
        sut = ActivityLauncher(activity)
    }

    @Test
    fun launchActivityTest() {
        val shadow = Shadows.shadowOf(activity)
        assertNull(shadow.peekNextStartedActivity())
        sut.launchActivity(LxItinDetailsActivity, "123")
        val intent = shadow.peekNextStartedActivity()
        assertNotNull(intent)
        val className = intent.component.className
        assertTrue(className.contains("LxItinDetailsActivity"))
        assertTrue(intent.hasExtra("LX_ITIN_ID"))
    }
}
