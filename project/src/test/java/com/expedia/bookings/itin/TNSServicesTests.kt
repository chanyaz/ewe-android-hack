package com.expedia.bookings.itin

import android.content.Context
import com.expedia.bookings.interceptors.MockInterceptor
import com.expedia.bookings.itin.data.Courier
import com.expedia.bookings.itin.data.TNSRegisterDeviceResponse
import com.expedia.bookings.itin.services.TNSServices
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
import org.mockito.Mockito
import org.robolectric.RuntimeEnvironment
import rx.observers.TestSubscriber
import rx.schedulers.Schedulers
import java.io.File
import java.util.concurrent.TimeUnit
import kotlin.test.assertEquals

@RunWith(RobolectricRunner::class)
class TNSServicesTests {

    var server: MockWebServer = MockWebServer()
        @Rule get

    private var service: TNSServices? = null

    private fun getContext(): Context {
        val spyContext = Mockito.spy(RuntimeEnvironment.application)
        val spyResources = Mockito.spy(spyContext.resources)
        Mockito.`when`(spyContext.resources).thenReturn(spyResources)
        return spyContext
    }

    @Before
    fun before() {
        val logger = HttpLoggingInterceptor()
        val file = File("../lib/mocked/templates")
        val root = file.canonicalPath
        val opener = FileSystemOpener(root)
        val interceptor = MockInterceptor()
        logger.level = HttpLoggingInterceptor.Level.BODY
        server.setDispatcher(ExpediaDispatcher(opener))
        service = TNSServices("http://localhost:" + server.port,
                OkHttpClient.Builder().addInterceptor(logger).build(),
                interceptor, Schedulers.immediate(), Schedulers.immediate(), getContext())
    }

    @Test
    fun testTnsUserResponse() {
        val observer = TestSubscriber<TNSRegisterDeviceResponse>()
        service!!.registerForUserDevice(Courier("gcm", "ExpediaBookings", "abc"))!!.subscribe(observer)
        observer.awaitTerminalEvent(10, TimeUnit.SECONDS)
        val response = observer.onNextEvents[0]
        observer.assertNoErrors()
        observer.assertCompleted()
        observer.assertValueCount(1)
        assertEquals("SUCCESS", response.status)
    }
}