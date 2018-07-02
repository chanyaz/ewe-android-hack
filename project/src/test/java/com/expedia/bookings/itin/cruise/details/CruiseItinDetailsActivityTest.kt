package com.expedia.bookings.itin.cruise.details

import android.content.Intent
import com.expedia.bookings.itin.tripstore.utils.ITripsJsonFileUtils
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.utils.Ui
import com.mobiata.mocke3.getJsonStringFromMock
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RuntimeEnvironment
import org.robolectric.Shadows
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@RunWith(RobolectricRunner::class)
class CruiseItinDetailsActivityTest {
    lateinit var activity: CruiseItinDetailsActivity
    lateinit var util: ITripsJsonFileUtils

    @Before
    fun setup() {
        val intent = Intent()
        intent.putExtra("CRUISE_ITIN_ID", "cruise")
        util = Ui.getApplication(RuntimeEnvironment.application).appComponent().tripJsonFileUtils()
        util.writeToFile("cruise", getJsonStringFromMock("api/trips/cruise_trip_details.json", null))
        activity = Robolectric.buildActivity(CruiseItinDetailsActivity::class.java, intent).create().start().get()
    }

    @After
    fun cleanup() {
        util.deleteAllFiles()
    }

    @Test
    fun testToolbarBackFinishesActivity() {
        val shadow = Shadows.shadowOf(activity)
        assertFalse(shadow.isFinishing)
        activity.toolbarViewModel.navigationBackPressedSubject.onNext(Unit)
        assertTrue(shadow.isFinishing)
    }
}
