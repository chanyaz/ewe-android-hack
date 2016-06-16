package com.expedia.bookings.test.robolectric

import android.app.Activity
import com.expedia.bookings.activity.FlightSearchActivity
import com.expedia.bookings.data.abacus.AbacusUtils
import com.expedia.bookings.utils.NavUtils
import com.expedia.ui.FlightActivity
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import kotlin.properties.Delegates
import kotlin.test.assertEquals

@RunWith(RobolectricRunner::class)
class FlightABTest {

    var activity: Activity by Delegates.notNull()
    @Before
    fun setup() {
        activity = Robolectric.buildActivity(Activity::class.java).create().get()
    }

    @Test
    fun oldFlightPath() {
        RoboTestHelper.controlTests(AbacusUtils.EBAndroidAppFlightTest)
        val intent = NavUtils.getFlightIntent(activity)
        assertEquals(intent.component.className, FlightSearchActivity::class.java.name);
    }

    @Test
    fun newFlightPath() {
        RoboTestHelper.bucketTests(AbacusUtils.EBAndroidAppFlightTest)
        val intent = NavUtils.getFlightIntent(activity)
        assertEquals(intent.component.className, FlightActivity::class.java.name);
    }

}
