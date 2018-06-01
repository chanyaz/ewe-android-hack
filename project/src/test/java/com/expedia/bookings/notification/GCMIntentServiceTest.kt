package com.expedia.bookings.notification

import android.content.Intent
import com.expedia.bookings.test.robolectric.RobolectricRunner
import org.json.JSONObject
import org.junit.Test
import org.junit.runner.RunWith
import android.content.ComponentName
import com.expedia.bookings.data.TNSRegisterDeviceResponse
import com.expedia.bookings.data.trips.ItineraryManager
import com.expedia.bookings.interceptors.MockInterceptor
import com.expedia.bookings.services.TNSServices
import com.expedia.bookings.services.TestObserver
import com.mobiata.mocke3.ExpediaDispatcher
import com.mobiata.mocke3.FileSystemOpener
import io.reactivex.schedulers.Schedulers
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import okhttp3.mockwebserver.MockWebServer
import org.json.JSONArray
import org.junit.Before
import org.junit.Rule
import org.mockito.Mockito
import org.robolectric.Robolectric
import java.io.File
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import kotlin.test.assertEquals

@RunWith(RobolectricRunner::class)
class GCMIntentServiceTest {
    var server: MockWebServer = MockWebServer()
        @Rule get
    private var service: TNSServices? = null
    private lateinit var testServiceObserver: TestObserver<TNSRegisterDeviceResponse>
    private lateinit var intent: Intent
    private lateinit var intentService: GCMIntentService
    lateinit var tnsService: TNSServices
        @Inject set

    @Before
    fun setup() {
        intentService = Robolectric.buildService(GCMIntentService::class.java).get()
        testServiceObserver = TestObserver<TNSRegisterDeviceResponse>()
        val logger = HttpLoggingInterceptor()
        val file = File("../lib/mocked/templates")
        val root = file.canonicalPath
        val opener = FileSystemOpener(root)
        val interceptor = MockInterceptor()
        logger.level = HttpLoggingInterceptor.Level.BODY
        server.setDispatcher(ExpediaDispatcher(opener))
        service = TNSServices("http://localhost:" + server.port,
                OkHttpClient.Builder().addInterceptor(logger).build(),
                listOf(interceptor), Schedulers.trampoline(), Schedulers.trampoline(), testServiceObserver)
        intentService.tnsService = service
        val mockItinManager = Mockito.mock(ItineraryManager::class.java)
        Mockito.`when`(mockItinManager.isSyncing()).thenReturn(true)
        intentService.itineraryManager = mockItinManager
    }

    @Test
    fun testNotificationReceivedEvent() {
        intent = Intent()
        val mPackage = "com.your.package"
        val mClass = ".actYouAreLaunching"
        intent.setComponent(ComponentName(mPackage, mPackage + mClass))
        createExtrasForIntent(intent)
        intentService.onHandleIntent(intent)
        testServiceObserver.awaitTerminalEvent(10, TimeUnit.SECONDS)
        val response = testServiceObserver.values()[0]

        testServiceObserver.assertNoErrors()
        testServiceObserver.assertComplete()
        testServiceObserver.assertValueCount(1)

        assertEquals("success", response.status)
    }

    @Test
    fun testNotificationReceivedNotFiredWithoutNotificationId() {
        intent = Intent()
        val mPackage = "com.your.package"
        val mClass = ".actYouAreLaunching"
        intent.setComponent(ComponentName(mPackage, mPackage + mClass))
        createExtrasForIntent(intent, false)
        intentService.onHandleIntent(intent)
        testServiceObserver.assertValueCount(0)
    }

    private fun createExtrasForIntent(intent: Intent, hasNotificationId: Boolean = true) {
        val dataObject = JSONObject()
        if (hasNotificationId) {
            dataObject.put("nid", "1234")
        }
        dataObject.put("t", "1234")
        dataObject.put("fhid", "1234")

        val messageObject = JSONObject()
        messageObject.put("loc-key", "1234")
        messageObject.put("title-loc-key", "1234")
        messageObject.put("loc-args", JSONArray())

        intent.putExtra("data", dataObject.toString())
        intent.putExtra("message", messageObject.toString())
        intent.putExtra("from", "1234")
    }
}
