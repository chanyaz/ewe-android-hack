package com.expedia.bookings.unit

import com.expedia.bookings.interceptors.MockInterceptor
import com.expedia.bookings.services.TestObserver
import com.expedia.bookings.services.TripFolderService
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
import java.io.File
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

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
        val testObserver = TestObserver<JSONArray>()
        val testFilename = "tripfolders_happy_path_m1_hotel"
        server.setDispatcher(ExpediaDispatcher(fileOpener, mapOf(DispatcherSettingsKeys.TRIPS_DISPATCHER to testFilename)))

        testObserver.assertNoValues()
        service.getTripFoldersObservable(testObserver)
        testObserver.assertValueCount(1)
        assertNotNull(testObserver.values()[0])
        val expectedJSONArray = JSONArray(getJsonStringFromMock("api/trips/tripfolders/$testFilename.json", null))
        val actualJSONArray = testObserver.values()[0] as JSONArray
        assertEquals(expectedJSONArray.toString(), actualJSONArray.toString())
    }
}
