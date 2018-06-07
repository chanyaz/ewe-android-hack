package com.expedia.bookings.notification

import android.content.ComponentName
import android.content.Intent
import com.activeandroid.ActiveAndroid
import com.expedia.bookings.data.TNSRegisterDeviceResponse
import com.expedia.bookings.data.trips.ItinCardData
import com.expedia.bookings.data.trips.ItineraryManager
import com.expedia.bookings.data.trips.ItineraryManagerInterface
import com.expedia.bookings.data.trips.TripFlight
import com.expedia.bookings.interceptors.MockInterceptor
import com.expedia.bookings.services.TNSServices
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.mobiata.mocke3.ExpediaDispatcher
import com.mobiata.mocke3.FileSystemOpener
import io.reactivex.observers.TestObserver
import io.reactivex.schedulers.Schedulers
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import okhttp3.mockwebserver.MockWebServer
import org.json.JSONArray
import org.json.JSONObject
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RuntimeEnvironment
import java.io.File
import java.util.concurrent.TimeUnit
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@RunWith(RobolectricRunner::class)
class GCMIntentServiceV2Test {
    var server: MockWebServer = MockWebServer()
        @Rule get
    private var service: TNSServices? = null
    private lateinit var testServiceObserver: TestObserver<TNSRegisterDeviceResponse>
    private lateinit var intent: Intent
    private lateinit var intentService: GCMIntentServiceV2
    private lateinit var mockItinManager: MockItineraryManager
    var syncListenerAdded: Boolean = false

    @Before
    fun setup() {
        intentService = Robolectric.buildService(GCMIntentServiceV2::class.java).get()
        testServiceObserver = TestObserver()
        val logger = HttpLoggingInterceptor()
        val file = File("../lib/mocked/templates")
        val root = file.canonicalPath
        val opener = FileSystemOpener(root)
        ActiveAndroid.initialize(RuntimeEnvironment.application)
        syncListenerAdded = false
        val interceptor = MockInterceptor()
        logger.level = HttpLoggingInterceptor.Level.BODY
        server.setDispatcher(ExpediaDispatcher(opener))
        service = TNSServices("http://localhost:" + server.port,
                OkHttpClient.Builder().addInterceptor(logger).build(),
                listOf(interceptor), Schedulers.trampoline(), Schedulers.trampoline(), testServiceObserver)
        intentService.tnsService = service as TNSServices
        mockItinManager = MockItineraryManager()

        intentService.intenaryManager = mockItinManager
    }

    @Test
    fun testNotificationReceivedEventIsNotSyncing() {
        intent = Intent()
        val mPackage = "com.your.package"
        val mClass = ".actYouAreLaunching"
        intent.component = ComponentName(mPackage, mPackage + mClass)
        assertFalse(mockItinManager.startSyncCalled)
        createExtrasForIntent(intent)
        intentService.onHandleIntent(intent)
        testServiceObserver.awaitTerminalEvent(10, TimeUnit.SECONDS)
        val response = testServiceObserver.values()[0]

        testServiceObserver.assertComplete()
        testServiceObserver.assertValueCount(1)
        assertFalse(mockItinManager.startSyncCalled)
        assertTrue(mockItinManager.syncListenerAdded)

        assertEquals("success", response.status)
    }

    @Test
    fun testNotificationReceivedEventIsSyncing() {
        mockItinManager = MockItineraryManager(false)

        intentService.intenaryManager = mockItinManager
        intent = Intent()
        val mPackage = "com.your.package"
        val mClass = ".actYouAreLaunching"
        intent.component = ComponentName(mPackage, mPackage + mClass)
        createExtrasForIntent(intent)
        assertFalse(mockItinManager.startSyncCalled)
        intentService.onHandleIntent(intent)
        testServiceObserver.awaitTerminalEvent(10, TimeUnit.SECONDS)
        val response = testServiceObserver.values()[0]

        testServiceObserver.assertComplete()
        testServiceObserver.assertValueCount(1)
        assertTrue(mockItinManager.startSyncCalled)
        assertTrue(mockItinManager.syncListenerAdded)

        assertEquals("success", response.status)
    }

    @Test
    fun onSyncFinishedTest() {
        intent = Intent()
        val mPackage = "com.your.package"
        val mClass = ".actYouAreLaunching"
        intent.component = ComponentName(mPackage, mPackage + mClass)
        createExtrasForIntent(intent)
        intentService.onHandleIntent(intent)
        assertFalse(mockItinManager.syncListenerRemoved)
        mockItinManager.callOnSyncFinished()
        assertTrue(mockItinManager.syncListenerRemoved)
    }

    @Test
    fun onSyncFailedTest() {
        intent = Intent()
        val mPackage = "com.your.package"
        val mClass = ".actYouAreLaunching"
        intent.component = ComponentName(mPackage, mPackage + mClass)
        createExtrasForIntent(intent)
        intentService.onHandleIntent(intent)
        assertFalse(mockItinManager.syncListenerRemoved)
        mockItinManager.callOnSyncFailed()
        assertTrue(mockItinManager.syncListenerRemoved)
    }

    @Test
    fun testOnHandleIntentNoExtras() {
        intent = Intent()
        val mPackage = "com.your.package"
        val mClass = ".actYouAreLaunching"
        intent.component = ComponentName(mPackage, mPackage + mClass)
        intentService.onHandleIntent(intent)
        testServiceObserver.assertValueCount(0)
    }

    @Test
    fun testOnHandleIntentNoMessage() {
        intent = Intent()
        val mPackage = "com.your.package"
        val mClass = ".actYouAreLaunching"
        intent.component = ComponentName(mPackage, mPackage + mClass)
        createExtrasForIntent(intent, hasMessage = false)
        intentService.onHandleIntent(intent)
        testServiceObserver.assertValueCount(0)
    }

    @Test
    fun testOnHandleIntentNoData() {
        intent = Intent()
        val mPackage = "com.your.package"
        val mClass = ".actYouAreLaunching"
        intent.component = ComponentName(mPackage, mPackage + mClass)
        createExtrasForIntent(intent, hasData = false)
        intentService.onHandleIntent(intent)
        testServiceObserver.assertValueCount(0)
    }

    @Test
    fun testNotificationReceivedNotFiredWithoutNotificationId() {
        intent = Intent()
        val mPackage = "com.your.package"
        val mClass = ".actYouAreLaunching"
        intent.component = ComponentName(mPackage, mPackage + mClass)
        createExtrasForIntent(intent, false)
        intentService.onHandleIntent(intent)
        testServiceObserver.assertValueCount(0)
    }

    @Test
    fun testNotificationReceivedNoFHid() {
        intent = Intent()
        val mPackage = "com.your.package"
        val mClass = ".actYouAreLaunching"
        intent.component = ComponentName(mPackage, mPackage + mClass)
        createExtrasForIntent(intent, noFHID = true)
        intentService.onHandleIntent(intent)
        testServiceObserver.assertValueCount(1)
    }

    private fun createExtrasForIntent(intent: Intent, hasNotificationId: Boolean = true, hasMessage: Boolean = true, hasData: Boolean = true, noFHID: Boolean = false) {

        if (hasMessage) {
            val messageObject = JSONObject()
            messageObject.put("loc-key", "1234")
            messageObject.put("title-loc-key", "1234")
            messageObject.put("loc-args", JSONArray())
            intent.putExtra("message", messageObject.toString())
        }

        if (hasData) {
            val dataObject = JSONObject()
            if (hasNotificationId) {
                dataObject.put("nid", "1234")
            }
            dataObject.put("t", "1234")
            if (!noFHID) {
                dataObject.put("fhid", "1234")
            }
            intent.putExtra("data", dataObject.toString())
        }
    }

    @After
    fun tearDown() {
        ActiveAndroid.dispose()
    }

    private class MockItineraryManager(val isAlwaysSyncing: Boolean = true) : ItineraryManagerInterface {
        var syncListenerAdded = false
        var syncListenerRemoved = false
        var syncingCalled = false
        var startSyncCalled = false
        var deepRefreshTrip = false
        lateinit var listener: ItineraryManager.ItinerarySyncListener
        override fun getItinCardDataFromItinId(id: String?): ItinCardData? {
            return ItinCardData(TripFlight())
        }

        override fun addSyncListener(listener: ItineraryManager.ItinerarySyncListener) {
            syncListenerAdded = true
            this.listener = listener
        }

        override fun removeSyncListener(listener: ItineraryManager.ItinerarySyncListener) {
            syncListenerRemoved = true
        }

        override fun getTripComponentFromFlightHistoryId(id: Int): TripFlight {
            return TripFlight()
        }

        override fun isSyncing(): Boolean {
            syncingCalled = true
            return isAlwaysSyncing
        }

        override fun startSync(boolean: Boolean): Boolean {
            startSyncCalled = true
            return true
        }

        override fun deepRefreshTrip(key: String, doSyncIfNotFound: Boolean): Boolean {
            deepRefreshTrip = true
            return true
        }

        fun callOnSyncFinished() {
            listener.onSyncFinished(listOf())
        }

        fun callOnSyncFailed() {
            listener.onSyncFailure(ItineraryManager.SyncError.CANCELLED)
        }
    }
}
