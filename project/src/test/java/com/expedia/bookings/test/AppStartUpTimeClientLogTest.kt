package com.expedia.bookings.test

import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.tracking.AppStartupTimeLogger
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import com.expedia.bookings.data.clientlog.EmptyResponse
import com.expedia.bookings.data.clientlog.ClientLog
import com.expedia.bookings.interceptors.MockInterceptor
import com.expedia.bookings.services.ClientLogServices
import com.expedia.bookings.tracking.AppStartupTimeClientLog
import rx.schedulers.Schedulers
import kotlin.test.assertTrue

@RunWith(RobolectricRunner::class)
class AppStartUpTimeClientLogTest {

    var server: MockWebServer = MockWebServer()
        @Rule get

    lateinit var clientLogServices: ClientLogServices
    lateinit var clientLogRequest: String

    @Before
    fun setup() {
        clientLogRequest = ""
        val logger = HttpLoggingInterceptor()
        logger.level = HttpLoggingInterceptor.Level.BODY
        val interceptor = MockInterceptor()
        clientLogServices = ClientLogServices("http://localhost:" + server.port,
                OkHttpClient.Builder().addInterceptor(logger).build(),
                interceptor, Schedulers.immediate(), Schedulers.immediate())

        val dispatcher = object : Dispatcher() {
            @Throws(InterruptedException::class)
            override fun dispatch(request: RecordedRequest): MockResponse {
                clientLogRequest = request.path
                return MockResponse()
            }
        }
        server.setDispatcher(dispatcher)
    }

    @Test
    fun testAppStartupTimeLog() {
        val logger: AppStartupTimeLogger = AppStartupTimeLogger()
        logger.setAppLaunchedTime(1349333571111)
        logger.setAppLaunchScreenDisplayed(1349333576093)

        AppStartupTimeClientLog.trackAppStartupTime(logger, clientLogServices)
        assertTrue(clientLogRequest.contains("requestToUser=4982"))
    }

}