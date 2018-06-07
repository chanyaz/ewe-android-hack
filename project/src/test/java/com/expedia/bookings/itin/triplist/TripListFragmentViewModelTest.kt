package com.expedia.bookings.itin.triplist

import com.expedia.bookings.itin.helpers.MockTripsTracking
import com.expedia.bookings.services.TestObserver
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class TripListFragmentViewModelTest {
    private val mockTripsTracking = MockTripsTracking()
    private val viewModel = TripListFragmentViewModel(mockTripsTracking)

    @Test
    fun testTripListVisitTracking() {
        val testObserver = TestObserver<Int>()
        viewModel.tripListVisitTrackingSubject.subscribe(testObserver)
        testObserver.assertNoValues()
        assertNull(mockTripsTracking.trackTripListVisited)

        viewModel.tripListVisitTrackingSubject.onNext(0)
        testObserver.assertValueCount(1)
        testObserver.assertValue(0)
        assertNotNull(mockTripsTracking.trackTripListVisited)
        assertEquals(0, mockTripsTracking.trackTripListVisited)
    }

    @Test
    fun testTabSelected() {
        val testObserver = TestObserver<Int>()
        viewModel.tabSelectedSubject.subscribe(testObserver)
        testObserver.assertNoValues()

        viewModel.tabSelectedSubject.onNext(2)
        testObserver.assertValueCount(1)
        testObserver.assertValue(2)
    }
}