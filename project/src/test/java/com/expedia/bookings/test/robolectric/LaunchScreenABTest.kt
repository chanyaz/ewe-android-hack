package com.expedia.bookings.test.robolectric

import android.app.Activity
import com.expedia.bookings.activity.PhoneLaunchActivity
import com.expedia.bookings.data.abacus.AbacusUtils
import com.expedia.bookings.utils.NavUtils
import com.expedia.ui.NewPhoneLaunchActivity
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import kotlin.properties.Delegates
import kotlin.test.assertEquals

@RunWith(RobolectricRunner::class)
class LaunchScreenABTest {

    var activity: Activity by Delegates.notNull()
    @Before
    fun setup() {
        activity = Robolectric.buildActivity(Activity::class.java).create().get()
    }

    @Test
    fun oldLaunchScreen() {
        RoboTestHelper.controlTests(AbacusUtils.EBAndroidAppLaunchScreenTest)
        val intent = NavUtils.getLaunchIntent(activity)
        assertEquals(intent.component.className, PhoneLaunchActivity::class.java.name);
    }

    @Test
    fun newLaunchScreen() {
        RoboTestHelper.bucketTests(AbacusUtils.EBAndroidAppLaunchScreenTest)
        val intent = NavUtils.getLaunchIntent(activity)
        assertEquals(intent.component.className, NewPhoneLaunchActivity::class.java.name);
    }

}
