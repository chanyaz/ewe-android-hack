package com.expedia.bookings.itin.car.details

import android.content.Intent
import com.expedia.bookings.itin.cars.details.CarsItinDetailsActivity
import com.expedia.bookings.itin.helpers.MockCarRepo
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
class CarsItinDetailsActivityTest {
    private lateinit var activity: CarsItinDetailsActivity

    @Before
    fun setup() {
        val itinId = "ITIN1"
        val fileUtils = Ui.getApplication(RuntimeEnvironment.application).appComponent().tripJsonFileUtils()
        fileUtils.writeTripToFile(itinId, getJsonStringFromMock("api/trips/car_trip_details_happy.json", null))
        val intent = Intent()
        intent.putExtra("CAR_ITIN_ID", itinId)
        activity = Robolectric.buildActivity(CarsItinDetailsActivity::class.java, intent).create().start().get()
    }

    @Test
    fun toolbarBackExistTest() {
        val mockRepo = MockCarRepo()
        activity.repo = mockRepo
        val shadow = Shadows.shadowOf(activity)
        assertFalse(shadow.isFinishing)
        assertFalse(mockRepo.disposed)

        activity.toolbarViewModel.navigationBackPressedSubject.onNext(Unit)

        assertTrue(shadow.isFinishing)
        assertTrue(mockRepo.disposed)
    }

    @Test
    fun testFinishActivity() {
        val mockRepo = MockCarRepo()
        activity.repo = mockRepo
        val shadow = Shadows.shadowOf(activity)
        assertFalse(shadow.isFinishing)
        assertFalse(mockRepo.disposed)
        shadow.overridePendingTransition(-1, -1)
        activity.finish()
        assertNotEquals(-1, shadow.pendingTransitionEnterAnimationResourceId)
        assertNotEquals(-1, shadow.pendingTransitionExitAnimationResourceId)
        assertTrue(shadow.isFinishing)
        assertTrue(mockRepo.disposed)
    }
}
