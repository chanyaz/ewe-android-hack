package com.expedia.bookings.itin.triplist

import android.arch.core.executor.testing.InstantTaskExecutorRule
import com.expedia.bookings.itin.helpers.MockTripListRepository
import com.expedia.bookings.itin.helpers.MockTripsTracking
import com.expedia.bookings.services.TestObserver
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class TripListFragmentViewModelTest {
    @Rule
    @JvmField
    val rule = InstantTaskExecutorRule()

    private val mockTripsTracking = MockTripsTracking()
    private val mockTripListRepo = MockTripListRepository()
    private lateinit var viewModel: ITripListFragmentViewModel

    @Before
    fun setup() {
        viewModel = TripListFragmentViewModel(mockTripsTracking, mockTripListRepo)
    }

    @Test
    fun testTripListVisitUpcomingTabTracking() {
        val testObserver = TestObserver<Int>()
        viewModel.tripListVisitTrackingSubject.subscribe(testObserver)
        testObserver.assertNoValues()

        assertFalse(mockTripsTracking.trackTripListUpcomingTabSelected)
        viewModel.tripListVisitTrackingSubject.onNext(0)
        testObserver.assertValueCount(1)
        testObserver.assertValue(0)
        assertTrue(mockTripsTracking.trackTripListUpcomingTabSelected)
    }

    @Test
    fun testTripListVisitPastTabTracking() {
        val testObserver = TestObserver<Int>()
        viewModel.tripListVisitTrackingSubject.subscribe(testObserver)
        testObserver.assertNoValues()

        assertFalse(mockTripsTracking.trackTripListPastTabSelected)
        viewModel.tripListVisitTrackingSubject.onNext(1)
        testObserver.assertValueCount(1)
        testObserver.assertValue(1)
        assertTrue(mockTripsTracking.trackTripListPastTabSelected)
    }

    @Test
    fun testTripListVisitCancelledTabTracking() {
        val testObserver = TestObserver<Int>()
        viewModel.tripListVisitTrackingSubject.subscribe(testObserver)
        testObserver.assertNoValues()

        assertFalse(mockTripsTracking.trackTripListCancelledTabSelected)
        viewModel.tripListVisitTrackingSubject.onNext(2)
        testObserver.assertValueCount(1)
        testObserver.assertValue(2)
        assertTrue(mockTripsTracking.trackTripListCancelledTabSelected)
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

    @Test
    fun testFoldersLiveDataHasFoldersOnInit() {
        assertEquals(mockTripListRepo.tripFolders, viewModel.upcomingFoldersLiveData.value)
    }

    @Test
    fun testFoldersLiveDataUpdatedOnRepoRefresh() {
        assertEquals(mockTripListRepo.tripFolders, viewModel.upcomingFoldersLiveData.value)
        viewModel.refreshTripFolders()
        assertEquals(mockTripListRepo.tripFoldersForRefresh, viewModel.upcomingFoldersLiveData.value)
    }
}
