package com.expedia.bookings.test.robolectric

import android.app.Activity
import com.expedia.bookings.utils.NavUtils
import com.expedia.ui.FlightActivity
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.Shadows
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
    fun newFlightPath() {
        NavUtils.goToFlights(activity, true)
        val intent = Shadows.shadowOf(activity).peekNextStartedActivity()
        assertEquals(FlightActivity::class.java.name, intent.component.className)
    }

}
