package com.expedia.bookings.itin.car.details

import android.content.Intent
import com.expedia.bookings.itin.cars.details.CarItinMoreHelpActivity
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
import kotlin.test.assertTrue

@RunWith(RobolectricRunner::class)
class CarItinMoreHelpActivityTest {
    lateinit var activity: CarItinMoreHelpActivity

    @Before
    fun setup() {
        val itinId = "ITIN1"
        val fileUtils = Ui.getApplication(RuntimeEnvironment.application).appComponent().tripJsonFileUtils()
        fileUtils.writeTripToFile(itinId, getJsonStringFromMock("api/trips/car_trip_details_happy.json", null))
        val intent = Intent()
        intent.putExtra("CAR_ITIN_ID", itinId)
        activity = Robolectric.buildActivity(CarItinMoreHelpActivity::class.java, intent).create().start().get()
    }

    @Test
    fun testFinishActivity() {
        val shadow = Shadows.shadowOf(activity)
        assertFalse(shadow.isFinishing)
        activity.finish()
        assertTrue(shadow.isFinishing)
    }

    @Test
    fun toolbarBackExistTest() {
        val shadow = Shadows.shadowOf(activity)
        assertFalse(shadow.isFinishing)
        activity.carRepo.dispose()

        activity.toolbarViewModel.navigationBackPressedSubject.onNext(Unit)

        assertTrue(shadow.isFinishing)
    }
}
