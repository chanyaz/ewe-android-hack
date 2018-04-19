package com.expedia.bookings.unit

import com.expedia.bookings.data.travelpulse.TravelPulseFetchResponse
import com.expedia.bookings.interceptors.MockInterceptor
import com.expedia.bookings.services.TestObserver
import com.expedia.bookings.services.TravelPulseServices
import com.mobiata.mocke3.ExpediaDispatcher
import com.mobiata.mocke3.FileSystemOpener
import io.reactivex.schedulers.Schedulers
import org.junit.Assert.assertTrue
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import okhttp3.mockwebserver.MockWebServer
import org.junit.Before
import org.junit.Test
import java.io.File
import java.util.concurrent.TimeUnit
import kotlin.properties.Delegates
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class TravelPulseServicesTest {
    var server = MockWebServer()
    private var service: TravelPulseServices by Delegates.notNull()
    private lateinit var interceptor: MockInterceptor
    private lateinit var travelPulseInterceptor: MockInterceptor

    @Before
    fun before() {
        val logger = HttpLoggingInterceptor()
        logger.level = HttpLoggingInterceptor.Level.BODY
        interceptor = MockInterceptor()
        travelPulseInterceptor = MockInterceptor()

        service = TravelPulseServices("http://localhost:" + server.port,
                OkHttpClient.Builder().addInterceptor(logger).build(),
                interceptor, travelPulseInterceptor,
                Schedulers.trampoline(), Schedulers.trampoline())

        val root = File("../mocked/templates").canonicalPath
        val opener = FileSystemOpener(root)
        server.setDispatcher(ExpediaDispatcher(opener))
    }

    @Test
    fun testUserHistoryHappyResponse() {
        val testObserver = TestObserver<TravelPulseFetchResponse>()
        service.fetchFavoriteHotels("siteId", "clientId",
                "expUserId", "guid",
                "langId", "configId",
                testObserver)

        testObserver.awaitValueCount(1, 10, TimeUnit.SECONDS)
        testObserver.assertValueCount(1)

        val response = testObserver.values()[0]
        assertNotNull(response)

        assertEquals(1, response.results.size)
        assertTrue(response.results[0].items.isNotEmpty())
    }

    @Test
    fun travelPulseServicesHitAllInterceptors() {
        val testObserver = TestObserver<TravelPulseFetchResponse>()

        service.fetchFavoriteHotels("siteId", "clientId",
                "expUserId", "guid",
                "langId", "configId",
                testObserver)

        testObserver.awaitTerminalEvent()
        testObserver.assertComplete()

        assertTrue(interceptor.wasCalled())
        assertTrue(travelPulseInterceptor.wasCalled())
    }
}
