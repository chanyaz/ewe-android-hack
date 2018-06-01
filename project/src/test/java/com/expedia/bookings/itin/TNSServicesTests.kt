package com.expedia.bookings.itin

import com.expedia.bookings.data.Courier
import com.expedia.bookings.data.TNSRegisterDeviceResponse
import com.expedia.bookings.data.TNSUser
import com.expedia.bookings.interceptors.MockInterceptor
import com.expedia.bookings.services.TNSServices
import com.expedia.bookings.services.TestObserver
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.mobiata.mocke3.ExpediaDispatcher
import com.mobiata.mocke3.FileSystemOpener
import io.reactivex.schedulers.Schedulers
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import okhttp3.mockwebserver.MockWebServer
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.util.concurrent.TimeUnit
import kotlin.test.assertEquals

@RunWith(RobolectricRunner::class)
class TNSServicesTests {

    var server: MockWebServer = MockWebServer()
        @Rule get

    private lateinit var testServiceObserver: TestObserver<TNSRegisterDeviceResponse>
    private var service: TNSServices? = null

    @Before
    fun before() {
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
    }

    @Test
    fun testTnsUserResponse() {
        service!!.registerForUserDevice(TNSUser("1", "1", "1", "guid"), Courier("gcm", "1033", "ExpediaBookings", "abc", "abc"))
        testServiceObserver.awaitTerminalEvent(10, TimeUnit.SECONDS)
        val response = testServiceObserver.values()[0]
        testServiceObserver.assertNoErrors()
        testServiceObserver.assertComplete()
        testServiceObserver.assertValueCount(1)
        assertEquals("SUCCESS", response.status)
    }

    @Test
    fun testTNSUserFlightResponse() {
        service!!.registerForFlights(TNSUser("1", "1", "1", "guid"), Courier("gcm", "1033", "ExpediaBookings", "abc", "abc"), emptyList())
        testServiceObserver.awaitTerminalEvent(10, TimeUnit.SECONDS)
        val response = testServiceObserver.values()[0]
        testServiceObserver.assertNoErrors()
        testServiceObserver.assertComplete()
        testServiceObserver.assertValueCount(1)
        assertEquals("SUCCESS", response.status)
    }

    @Test
    fun testDeregisterDevice() {
        service!!.deregisterDevice(Courier("gcm", "1033", "ExpediaBookings", "abc", "abc"))
        testServiceObserver.awaitTerminalEvent(10, TimeUnit.SECONDS)
        val response = testServiceObserver.values()[0]
        testServiceObserver.assertNoErrors()
        testServiceObserver.assertComplete()
        testServiceObserver.assertValueCount(1)
        assertEquals("SUCCESS", response.status)
    }

    @Test
    fun testNotificationReceived() {
        service!!.notificationReceivedConfirmation("1234")
        testServiceObserver.awaitTerminalEvent(10, TimeUnit.SECONDS)
        val response = testServiceObserver.values()[0]
        testServiceObserver.assertNoErrors()
        testServiceObserver.assertComplete()
        testServiceObserver.assertValueCount(1)
        assertEquals("success", response.status)
    }
}
