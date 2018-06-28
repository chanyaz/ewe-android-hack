package com.expedia.bookings.itin.cruise.details

import android.content.Intent
import com.expedia.bookings.itin.helpers.MockTripsTracking
import com.expedia.bookings.test.robolectric.RobolectricRunner
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.Shadows
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@RunWith(RobolectricRunner::class)
class CruiseItinDetailsActivityTest {
    lateinit var activity: CruiseItinDetailsActivity

    @Before
    fun setup() {
        val intent = Intent()
        intent.putExtra("CRUISE_ITIN_ID", "CRUISE_ITIN_ID")
        activity = Robolectric.buildActivity(CruiseItinDetailsActivity::class.java, intent).create().start().get()
        activity.tripsTracking = MockTripsTracking()
    }

    @Test
    fun testToolbarBackFinishesActivity() {
        val shadow = Shadows.shadowOf(activity)
        assertFalse(shadow.isFinishing)
        activity.toolbarViewModel.navigationBackPressedSubject.onNext(Unit)
        assertTrue(shadow.isFinishing)
    }
}
