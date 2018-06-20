package com.expedia.bookings.itin.car.details

import android.content.Intent
import com.expedia.bookings.data.trips.ItinCardData
import com.expedia.bookings.itin.cars.ItinCarRepo
import com.expedia.bookings.itin.cars.details.CarsItinDetailsActivity
import com.expedia.bookings.itin.helpers.ItinMocker
import com.expedia.bookings.itin.helpers.MockCarRepo
import com.expedia.bookings.itin.helpers.MockTripsTracking
import com.expedia.bookings.itin.tripstore.data.Itin
import com.expedia.bookings.itin.tripstore.data.ItinDetailsResponse
import com.expedia.bookings.itin.tripstore.utils.IJsonToItinUtil
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.utils.Ui
import com.mobiata.mocke3.getJsonStringFromMock
import com.mobiata.mocke3.mockObject
import io.reactivex.subjects.PublishSubject
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
        fileUtils.writeToFile(itinId, getJsonStringFromMock("api/trips/car_trip_details_happy.json", null))
        val intent = Intent()
        intent.putExtra("CAR_ITIN_ID", itinId)
        activity = Robolectric.buildActivity(CarsItinDetailsActivity::class.java, intent).create().start().get()
        activity.tripsTracking = MockTripsTracking()
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

    @Test
    fun testCarDetailsPageLoadTracked() {
        activity.tripsTracking = MockTripsTracking()
        activity.repo = ItinCarRepo("CAR_ITIN_ID", MockJsonUtil(), PublishSubject.create<MutableList<ItinCardData>>())
        activity.setRepoObservers()

        val mockTripsTracking = activity.tripsTracking as MockTripsTracking
        assertTrue(mockTripsTracking.trackItinCarDetailsPageLoad)

        // asserting that page load tracking will only be called once when itin live data gets updated again
        mockTripsTracking.trackItinCarDetailsPageLoad = false
        activity.repo.liveDataItin.postValue(ItinMocker.carDetailsHappy)
        assertFalse(mockTripsTracking.trackItinCarDetailsPageLoad)
    }

    class MockJsonUtil(val isValid: Boolean = true) : IJsonToItinUtil {
        override fun getItin(itinId: String?): Itin? {
            return if (isValid) ItinMocker.carDetailsHappy
            else mockObject(ItinDetailsResponse::class.java, "api/trips/error_trip_details_response.json")?.itin
        }
        override fun getItinList(): List<Itin> {
            return emptyList()
        }
    }
}
