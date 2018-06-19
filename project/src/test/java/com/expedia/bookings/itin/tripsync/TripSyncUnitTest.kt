package com.expedia.bookings.itin.tripsync

import com.expedia.bookings.itin.helpers.MockTripFolderFileJsonUtil
import com.expedia.bookings.itin.helpers.MockTripFolderService
import com.expedia.bookings.services.TestObserver
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TripSyncUnitTest {
    private lateinit var mockService: MockTripFolderService
    private lateinit var mockFileUtil: MockTripFolderFileJsonUtil
    private lateinit var tripSync: TripSync

    @Before
    fun setup() {
        mockService = MockTripFolderService()
        mockFileUtil = MockTripFolderFileJsonUtil()
        tripSync = TripSync(mockService, mockFileUtil)
    }

    @Test
    fun testFetchCompletion() {
        val testObserver = TestObserver<Unit>()
        tripSync.tripFoldersFetchedSubject.subscribe(testObserver)

        testObserver.assertNoValues()
        tripSync.fetchTripFoldersFromApi()
        testObserver.assertValueCount(1)
        testObserver.assertValue(Unit)
    }

    @Test
    fun testWriteToFile() {
        tripSync.fetchTripFoldersFromApi()
        assertTrue(mockFileUtil.writeToFileCalled)
        assertEquals("2515dc80-7aa5-4ddb-ad66-1c381d36989d", mockFileUtil.lastFileName)
    }
}
