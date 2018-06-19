package com.expedia.bookings.itin.tripsync

import com.expedia.bookings.data.trips.TripFolder
import com.expedia.bookings.interceptors.MockInterceptor
import com.expedia.bookings.itin.tripstore.utils.ITripsJsonFileUtils
import com.expedia.bookings.services.TestObserver
import com.expedia.bookings.services.TripFolderService
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.utils.Ui
import com.google.gson.Gson
import com.mobiata.mocke3.DispatcherSettingsKeys
import com.mobiata.mocke3.ExpediaDispatcher
import com.mobiata.mocke3.FileSystemOpener
import io.reactivex.schedulers.Schedulers
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockWebServer
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RuntimeEnvironment
import java.io.File
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@RunWith(RobolectricRunner::class)
class TripSyncIntegrationTest {

    private val root = File("../lib/mocked/templates").canonicalPath
    private val server: MockWebServer = MockWebServer()
    private val context = RuntimeEnvironment.application
    private lateinit var fileOpener: FileSystemOpener
    private lateinit var fileUtils: ITripsJsonFileUtils
    private lateinit var service: TripFolderService
    private lateinit var tripSync: TripSync

    @Before
    fun setup() {
        fileOpener = FileSystemOpener(root)
        fileUtils = Ui.getApplication(context).appComponent().tripFolderJsonFileUtils()
        service = TripFolderService(
                "http://localhost:" + server.port,
                OkHttpClient.Builder().build(),
                MockInterceptor(),
                MockInterceptor(),
                Schedulers.trampoline(),
                Schedulers.trampoline()
        )
        tripSync = TripSync(service, fileUtils)
    }

    @Test
    fun fetchTripFoldersFromApiSingleFolder() {
        val testObserver = TestObserver<Unit>()
        val testFilename = "tripfolders_happy_path_m1_hotel"
        tripSync.tripFoldersFetchedSubject.subscribe(testObserver)
        server.setDispatcher(ExpediaDispatcher(fileOpener, mapOf(DispatcherSettingsKeys.TRIPS_DISPATCHER to testFilename)))

        testObserver.assertNoValues()
        tripSync.fetchTripFoldersFromApi()
        testObserver.assertValueCount(1)

        val filesWritten = fileUtils.readFromFileDirectory()
        assertEquals(1, filesWritten.size)
        val gson = Gson()
        val tripFolder = gson.fromJson(filesWritten.first(), TripFolder::class.java)
        assertEquals("2515dc80-7aa5-4ddb-ad66-1c381d36989d", tripFolder.tripFolderId)
    }

    @Test
    fun fetchTripFoldersFromApiMultipleFolders() {
        val testObserver = TestObserver<Unit>()
        val testFilename = "tripfolders_three_hotels_one_cruise"
        tripSync.tripFoldersFetchedSubject.subscribe(testObserver)
        server.setDispatcher(ExpediaDispatcher(fileOpener, mapOf(DispatcherSettingsKeys.TRIPS_DISPATCHER to testFilename)))

        testObserver.assertNoValues()
        tripSync.fetchTripFoldersFromApi()
        testObserver.assertValueCount(1)

        val filesWritten = fileUtils.readFromFileDirectory()
        assertEquals(4, filesWritten.size)
        val gson = Gson()
        val expectedListOfTripFolderIds = listOf(
                "8a246ebb-ef3d-43cc-aa9e-0bede99e38bd",
                "c99f4136-f468-42e8-b387-4a88f9909cec",
                "2515dc80-7aa5-4ddb-ad66-1c381d36989d",
                "70cd722e-20c5-4d81-8bb1-cbdee92b2ea5"
        )
        val tripFolder1 = gson.fromJson(filesWritten[0], TripFolder::class.java)
        val tripFolder2 = gson.fromJson(filesWritten[1], TripFolder::class.java)
        val tripFolder3 = gson.fromJson(filesWritten[2], TripFolder::class.java)
        val tripFolder4 = gson.fromJson(filesWritten[3], TripFolder::class.java)
        val actualListOfTripFolderIds = listOf(
                tripFolder1.tripFolderId,
                tripFolder2.tripFolderId,
                tripFolder3.tripFolderId,
                tripFolder4.tripFolderId
        )
        assertTrue(actualListOfTripFolderIds.containsAll(expectedListOfTripFolderIds))
    }
}
