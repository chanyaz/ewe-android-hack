package com.expedia.bookings.itin

import com.expedia.bookings.data.Courier
import com.expedia.bookings.data.TNSRegisterDeviceResponse
import com.expedia.bookings.data.TNSUser
import com.expedia.bookings.interceptors.MockInterceptor
import com.expedia.bookings.services.TNSServices
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.mobiata.mocke3.ExpediaDispatcher
import com.mobiata.mocke3.FileSystemOpener
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import okhttp3.mockwebserver.MockWebServer
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import rx.observers.TestSubscriber
import rx.schedulers.Schedulers
import java.io.File
import java.util.concurrent.TimeUnit
import kotlin.test.assertEquals

@RunWith(RobolectricRunner::class)
class TNSServicesTests {

    var server: MockWebServer = MockWebServer()
        @Rule get

    private lateinit var testServiceObserver: TestSubscriber<TNSRegisterDeviceResponse>
    private var service: TNSServices? = null

    @Before
    fun before() {
        testServiceObserver = TestSubscriber<TNSRegisterDeviceResponse>()

        val logger = HttpLoggingInterceptor()
        val file = File("../lib/mocked/templates")
        val root = file.canonicalPath
        val opener = FileSystemOpener(root)
        val interceptor = MockInterceptor()
        logger.level = HttpLoggingInterceptor.Level.BODY
        server.setDispatcher(ExpediaDispatcher(opener))
        service = TNSServices("http://localhost:" + server.port,
                OkHttpClient.Builder().addInterceptor(logger).build(),
                interceptor, Schedulers.immediate(), Schedulers.immediate(), testServiceObserver)
    }

    @Test
    fun testTnsUserResponse() {
        service!!.registerForUserDevice(TNSUser("1", "1", "1", "guid"), Courier("gcm", "1033", "ExpediaBookings", "abc","abc"))
        testServiceObserver.awaitTerminalEvent(10, TimeUnit.SECONDS)
        val response = testServiceObserver.onNextEvents[0]
        testServiceObserver.assertNoErrors()
        testServiceObserver.assertCompleted()
        testServiceObserver.assertValueCount(1)
        assertEquals("SUCCESS", response.status)
    }

    @Test
    fun testTNSUserFlightResponse() {
        service!!.registerForFlights(TNSUser("1", "1", "1", "guid"), Courier("gcm", "1033", "ExpediaBookings", "abc","abc"), emptyList())
        testServiceObserver.awaitTerminalEvent(10, TimeUnit.SECONDS)
        val response = testServiceObserver.onNextEvents[0]
        testServiceObserver.assertNoErrors()
        testServiceObserver.assertCompleted()
        testServiceObserver.assertValueCount(1)
        assertEquals("SUCCESS", response.status)
    }

    @Test
    fun testDeregisterDevice() {
        service!!.deregisterDevice(Courier("gcm", "1033", "ExpediaBookings", "abc","abc"))
        testServiceObserver.awaitTerminalEvent(10, TimeUnit.SECONDS)
        val response = testServiceObserver.onNextEvents[0]
        testServiceObserver.assertNoErrors()
        testServiceObserver.assertCompleted()
        testServiceObserver.assertValueCount(1)
        assertEquals("SUCCESS", response.status)
    }
}