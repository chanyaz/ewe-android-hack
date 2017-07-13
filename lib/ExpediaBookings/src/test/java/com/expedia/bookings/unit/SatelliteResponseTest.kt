package com.expedia.bookings.unit

import com.expedia.bookings.interceptors.MockInterceptor
import com.expedia.bookings.services.SatelliteServices
import com.mobiata.mocke3.ExpediaDispatcher
import com.mobiata.mocke3.FileSystemOpener
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import okhttp3.mockwebserver.MockWebServer
import org.junit.Before
import org.junit.Test
import rx.observers.TestSubscriber
import rx.schedulers.Schedulers
import java.io.File
import java.util.concurrent.TimeUnit
import kotlin.properties.Delegates

class SatelliteResponseTest {
    var sat: SatelliteServices by Delegates.notNull()
    var server = MockWebServer()

    @Before
    fun before() {
        val logger = HttpLoggingInterceptor()
        logger.level = HttpLoggingInterceptor.Level.BODY
        val interceptor = MockInterceptor()
        sat = SatelliteServices("http://localhost:" + server.port, OkHttpClient.Builder().addInterceptor(logger).build(),
                interceptor, interceptor, Schedulers.immediate(), Schedulers.immediate())

        val root = File("../mocked/templates").canonicalPath
        val opener = FileSystemOpener(root)
        server.setDispatcher(ExpediaDispatcher(opener))
    }

    @Test
    fun testSatelliteResponse() {
        var searchResponseObserver: TestSubscriber<List<String>> = TestSubscriber()
        sat.subscribeSatellite(searchResponseObserver,"expedia.app.android.phone:6.9.0")
        searchResponseObserver.awaitValueCount(1,10, TimeUnit.SECONDS)
        searchResponseObserver.assertValueCount(1)
        val response  = listOf("downloadConfigsOnPOSChange","14731","14732","14484","mocked")
        searchResponseObserver.assertValues(response)
    }
}