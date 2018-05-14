package com.expedia.bookings.itin.utils

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.expedia.bookings.itin.lx.details.LxItinDetailsActivity
import com.expedia.bookings.test.robolectric.RobolectricRunner
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.Shadows
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

@RunWith(RobolectricRunner::class)
class ActivityLauncherTest {

    @Test
    fun launchActivityTest() {
        val activity = Robolectric.buildActivity(AppCompatActivity::class.java).get()
        val sut = ActivityLauncher(activity)
        val shadow = Shadows.shadowOf(activity)
        assertNull(shadow.peekNextStartedActivity())
        sut.launchActivity(LxItinDetailsActivity, "123")
        val intent = shadow.peekNextStartedActivity()
        assertNotNull(intent)
        val className = intent.component.className
        assertTrue(className.contains("LxItinDetailsActivity"))
        assertTrue(intent.hasExtra("LX_ITIN_ID"))
    }

    @Test
    fun launchActivityAnimation() {
        val mockActivity = Robolectric.buildActivity(MockActivity::class.java).get()
        val sut = ActivityLauncher(mockActivity)
        sut.launchActivity(LxItinDetailsActivity, "123")
        assertEquals(sut.slideRightAnimation, mockActivity.optionsSeen)

        sut.launchActivity(LxItinDetailsActivity, "123", AnimationDirection.SLIDE_RIGHT)
        assertEquals(sut.slideRightAnimation, mockActivity.optionsSeen)

        sut.launchActivity(LxItinDetailsActivity, "123", AnimationDirection.SLIDE_UP)
        assertEquals(sut.slideUpAnimation, mockActivity.optionsSeen)
    }

    class MockActivity : AppCompatActivity() {
        var optionsSeen: Bundle? = null
        override fun startActivity(intent: Intent?, options: Bundle?) {
            super.startActivity(intent, options)
            optionsSeen = options
        }
    }
}
