package com.expedia.bookings.itin.lx.moreHelp

import android.content.Intent
import com.expedia.bookings.data.trips.ItinCardData
import com.expedia.bookings.itin.helpers.ItinMocker
import com.expedia.bookings.itin.helpers.MockTripsTracking
import com.expedia.bookings.itin.lx.ItinLxRepo
import com.expedia.bookings.itin.tripstore.data.Itin
import com.expedia.bookings.itin.tripstore.data.ItinDetailsResponse
import com.expedia.bookings.itin.tripstore.utils.IJsonToItinUtil
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.mobiata.mocke3.mockObject
import io.reactivex.subjects.PublishSubject
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.Shadows
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@RunWith(RobolectricRunner::class)
class LxItinMoreHelpActivityTest {

    lateinit var activity: LxItinMoreHelpActivity

    @Before
    fun setup() {
        val intent = Intent()
        intent.putExtra("LX_ITIN_ID", "LX_ITIN_ID")
        activity = Robolectric.buildActivity(LxItinMoreHelpActivity::class.java, intent).create().start().get()
        activity.tripsTracking = MockTripsTracking()
    }

    @Test
    fun testInvalidDataFinishesActivity() {
        val shadow = Shadows.shadowOf(activity)
        assertFalse(shadow.isFinishing)
        activity.lxRepo.invalidDataSubject.onNext(Unit)
        assertTrue(shadow.isFinishing)
    }

    @Test
    fun testMoreHelpPageLoadTracked() {
        activity.tripsTracking = MockTripsTracking()
        activity.lxRepo = ItinLxRepo("LX_ITIN_ID", MockJsonUtil(), PublishSubject.create<MutableList<ItinCardData>>())
        activity.setRepoObservers()

        val mockTripsTracking = activity.tripsTracking as MockTripsTracking
        assertTrue(mockTripsTracking.trackItinlxMoreHelpPageLoaded)

        // asserting that page load tracking will only be called once when itin live data gets updated again
        mockTripsTracking.trackItinlxMoreHelpPageLoaded = false
        activity.lxRepo.liveDataItin.postValue(ItinMocker.lxDetailsHappy)
        assertFalse(mockTripsTracking.trackItinlxMoreHelpPageLoaded)
    }

    class MockJsonUtil(val isValid: Boolean = true) : IJsonToItinUtil {
        override fun getItin(itinId: String?): Itin? {
            return if (isValid) ItinMocker.lxDetailsHappy
            else mockObject(ItinDetailsResponse::class.java, "api/trips/error_trip_details_response.json")?.itin
        }
    }
}
