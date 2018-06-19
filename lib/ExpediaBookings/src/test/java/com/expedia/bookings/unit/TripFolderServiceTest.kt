package com.expedia.bookings.unit

import com.expedia.bookings.data.trips.TripFolder
import com.expedia.bookings.data.trips.TripFolderState
import com.expedia.bookings.data.trips.TripFolderTiming
import com.expedia.bookings.interceptors.MockInterceptor
import com.expedia.bookings.services.TestObserver
import com.expedia.bookings.services.TripFolderService
import com.mobiata.mocke3.DispatcherSettingsKeys
import com.mobiata.mocke3.ExpediaDispatcher
import com.mobiata.mocke3.FileSystemOpener
import io.reactivex.schedulers.Schedulers
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockWebServer
import org.junit.Before
import org.junit.Test
import java.io.File
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class TripFolderServiceTest {

    private val root = File("../mocked/templates").canonicalPath
    private val server = MockWebServer()
    private lateinit var service: TripFolderService
    private lateinit var fileOpener: FileSystemOpener

    @Before
    fun setup() {
        fileOpener = FileSystemOpener(root)
        service = TripFolderService(
                "http://localhost:" + server.port,
                OkHttpClient.Builder().build(),
                MockInterceptor(),
                MockInterceptor(),
                Schedulers.trampoline(),
                Schedulers.trampoline()
        )
    }

    @Test
    fun testGetTripFolders() {
        val testObserver = TestObserver<List<TripFolder>>()
        val testFilename = "tripfolders_happy_path_m1_hotel"
        server.setDispatcher(ExpediaDispatcher(fileOpener, mapOf(DispatcherSettingsKeys.TRIPS_DISPATCHER to testFilename)))

        testObserver.assertNoValues()
        service.getTripFoldersObservable(testObserver)
        testObserver.assertValueCount(1)

        val actualValue = testObserver.values()[0]
        assertNotNull(actualValue)
        assertTrue(actualValue.size == 1)

        val actualFolder = actualValue.first()
        assertEquals("2515dc80-7aa5-4ddb-ad66-1c381d36989d", actualFolder.tripFolderId)
        assertEquals("Portland Suites Airport East, Portland", actualFolder.title)
        assertEquals("2018-06-09T15:00:00-07:00", actualFolder.startTime.raw)
        assertEquals(1528581600, actualFolder.startTime.epochSeconds)
        assertEquals(-25200, actualFolder.startTime.timeZoneOffsetSeconds)
        assertEquals("2018-06-10T11:00:00-07:00", actualFolder.endTime.raw)
        assertEquals(1528653600, actualFolder.endTime.epochSeconds)
        assertEquals(-25200, actualFolder.endTime.timeZoneOffsetSeconds)
        assertEquals(TripFolderState.BOOKED, actualFolder.state)
        assertEquals(TripFolderTiming.UPCOMING, actualFolder.timing)
        assertEquals(listOf("Hotel"), actualFolder.lobs)
    }
}
