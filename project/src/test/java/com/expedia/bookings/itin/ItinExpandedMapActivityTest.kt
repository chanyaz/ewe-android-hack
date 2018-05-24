package com.expedia.bookings.itin

import android.content.Intent
import com.expedia.bookings.R
import com.expedia.bookings.itin.common.ItinExpandedMapActivity
import com.expedia.bookings.itin.common.TripProducts
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.utils.Ui
import com.mobiata.mocke3.getJsonStringFromMock
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RuntimeEnvironment
import org.robolectric.Shadows
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

@RunWith(RobolectricRunner::class)
class ItinExpandedMapActivityTest {
    lateinit var activity: ItinExpandedMapActivity

    @Before
    fun before() {
        val itinId = "7123456789"
        val fileUtils = Ui.getApplication(RuntimeEnvironment.application).appComponent().tripJsonFileUtils()
        fileUtils.writeTripToFile(itinId, getJsonStringFromMock("api/trips/activity_trip_details.json", null))
        val intent = Intent()
        intent.putExtra("ITINID", itinId)
        intent.putExtra("ITIN_TYPE", TripProducts.ACTIVITY.name)
        activity = Robolectric.buildActivity(ItinExpandedMapActivity::class.java, intent).create().get()
        activity.setTheme(R.style.ItinTheme)
    }

    @Test
    fun testFinishActivity() {
        val shadow = Shadows.shadowOf(activity)
        assertFalse(shadow.isFinishing)
        shadow.overridePendingTransition(-1, -1)
        activity.finish()
        assertNotEquals(-1, shadow.pendingTransitionEnterAnimationResourceId)
        assertNotEquals(-1, shadow.pendingTransitionExitAnimationResourceId)
        assertTrue(shadow.isFinishing)
    }
}
