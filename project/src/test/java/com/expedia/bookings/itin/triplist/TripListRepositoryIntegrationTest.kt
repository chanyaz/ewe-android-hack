package com.expedia.bookings.itin.triplist

import com.expedia.bookings.data.trips.TripFolder
import com.expedia.bookings.interceptors.MockInterceptor
import com.expedia.bookings.itin.tripstore.utils.IJsonToFoldersUtil
import com.expedia.bookings.itin.tripstore.utils.ITripsJsonFileUtils
import com.expedia.bookings.services.TestObserver
import com.expedia.bookings.services.TripFolderService
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.utils.Ui
import com.google.gson.Gson
import com.mobiata.mocke3.DispatcherSettingsKeys
import com.mobiata.mocke3.ExpediaDispatcher
import com.mobiata.mocke3.FileSystemOpener
import com.mobiata.mocke3.getJsonStringFromMock
import io.reactivex.schedulers.Schedulers
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockWebServer
import org.json.JSONArray
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RuntimeEnvironment
import java.io.File
import kotlin.test.assertTrue

@RunWith(RobolectricRunner::class)
class TripListRepositoryIntegrationTest {
    private val root = File("../lib/mocked/templates").canonicalPath
    private val server: MockWebServer = MockWebServer()
    private val context = RuntimeEnvironment.application
    private lateinit var fileOpener: FileSystemOpener
    private lateinit var fileUtils: ITripsJsonFileUtils
    private lateinit var jsonToFolderUtil: IJsonToFoldersUtil
    private lateinit var service: TripFolderService
    private lateinit var repo: TripListRepository

    @Before
    fun setup() {
        fileOpener = FileSystemOpener(root)
        fileUtils = Ui.getApplication(context).tripComponent().tripFolderJsonFileUtils()
        jsonToFolderUtil = Ui.getApplication(context).tripComponent().jsonToFolderUtil()
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
    fun testTripFoldersExistOnDisk() {
        val testObserver = TestObserver<List<TripFolder>>()

        writeOneTripFolderToDisk()
        repo = TripListRepository(jsonToFolderUtil, service, fileUtils)
        repo.foldersSubject.subscribe(testObserver)

        val tripFoldersFromDisk = getTripFoldersFromDisk()
        assertTrue(tripFoldersFromDisk.isNotEmpty())
        assertTrue(tripFoldersFromDisk.size == 1)
        testObserver.assertValueCount(1)
        testObserver.assertValue(tripFoldersFromDisk)
    }

    @Test
    fun testNoFolderExistsOnDiskNoFoldersFromApi() {
        val testObserver = TestObserver<List<TripFolder>>()

        repo = TripListRepository(jsonToFolderUtil, service, fileUtils)
        repo.foldersSubject.subscribe(testObserver)

        testObserver.assertEmpty()
        testObserver.assertNoValues()
    }

    @Test
    fun testNoFolderExistsOnDiskFoldersExistOnApi() {
        val testObserver = TestObserver<List<TripFolder>>()

        server.setDispatcher(ExpediaDispatcher(fileOpener, mapOf(DispatcherSettingsKeys.TRIPS_DISPATCHER to "tripfolders_happy_path_m1_hotel")))
        repo = TripListRepository(jsonToFolderUtil, service, fileUtils)
        repo.foldersSubject.subscribe(testObserver)

        testObserver.assertValueCount(1)
        testObserver.assertValue(getTripFoldersFromDisk())
    }

    @Test
    fun testRefreshTripFolders() {
        val testObserver = TestObserver<List<TripFolder>>()

        server.setDispatcher(ExpediaDispatcher(fileOpener, mapOf(DispatcherSettingsKeys.TRIPS_DISPATCHER to "tripfolders_happy_path_m1_hotel")))
        repo = TripListRepository(jsonToFolderUtil, service, fileUtils)
        repo.foldersSubject.subscribe(testObserver)

        testObserver.assertValueCount(1)
        testObserver.assertValuesAndClear(getTripFoldersFromDisk())

        fileUtils.deleteAllFiles()
        assertTrue(getTripFoldersFromDisk().isEmpty())

        repo.refreshTripFolders()

        val tripFoldersFromDisk = getTripFoldersFromDisk()
        assertTrue(tripFoldersFromDisk.isNotEmpty())
        assertTrue(tripFoldersFromDisk.size == 1)
        testObserver.assertValueCount(1)
        testObserver.assertValue(tripFoldersFromDisk)
    }

    @Test
    fun testFolderFromDiskThenFolderFromApi() {
        val testObserver = TestObserver<List<TripFolder>>()

        server.setDispatcher(ExpediaDispatcher(fileOpener, mapOf(DispatcherSettingsKeys.TRIPS_DISPATCHER to "tripfolders_happy_path_m1_hotel")))
        repo = TripListRepository(jsonToFolderUtil, service, fileUtils)
        repo.foldersSubject.subscribe(testObserver)

        val tripFoldersFromDisk1 = getTripFoldersFromDisk()
        assertTrue(tripFoldersFromDisk1.isNotEmpty())
        assertTrue(tripFoldersFromDisk1.size == 1)
        testObserver.assertValueCount(1)
        testObserver.assertValue(tripFoldersFromDisk1)

        repo.refreshTripFolders()

        val tripFoldersFromDisk2 = getTripFoldersFromDisk()
        assertTrue(tripFoldersFromDisk2.isNotEmpty())
        assertTrue(tripFoldersFromDisk2.size == 1)
        testObserver.assertValueCount(2)
        testObserver.assertValues(tripFoldersFromDisk1, tripFoldersFromDisk2)
    }

    private fun writeOneTripFolderToDisk() {
        val arrayOfFolders = JSONArray(getJsonStringFromMock("api/trips/tripfolders/tripfolders_happy_path_m1_hotel.json", null))
        fileUtils.writeToFile("2515dc80-7aa5-4ddb-ad66-1c381d36989d", arrayOfFolders[0].toString())
    }

    private fun getTripFoldersFromDisk(): List<TripFolder> {
        val listOfFolders = mutableListOf<TripFolder>()
        val filesWritten = fileUtils.readFromFileDirectory()
        filesWritten.forEach {
            listOfFolders.add(Gson().fromJson(it, TripFolder::class.java))
        }
        return listOfFolders
    }
}
