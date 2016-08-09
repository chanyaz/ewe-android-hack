package com.expedia.bookings.unit

import com.expedia.bookings.data.SuggestionResultType
import com.expedia.bookings.data.SuggestionV4
import com.expedia.bookings.interceptors.MockInterceptor
import com.expedia.bookings.services.SuggestionV4Services
import com.mobiata.mocke3.ExpediaDispatcher
import com.mobiata.mocke3.FileSystemOpener
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import okhttp3.mockwebserver.MockWebServer
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import rx.observers.TestSubscriber
import rx.schedulers.Schedulers
import java.io.File
import kotlin.test.assertEquals

class SuggestionV4ServicesTest {

    var server: MockWebServer = MockWebServer()
        @Rule get

    private var service: SuggestionV4Services? = null

    @Before
    fun before() {
        val logger = HttpLoggingInterceptor()
        logger.level = HttpLoggingInterceptor.Level.BODY
        val interceptor = MockInterceptor()
        service = SuggestionV4Services("http://localhost:" + server.port,
                OkHttpClient.Builder().addInterceptor(logger).build(),
                interceptor, Schedulers.immediate(), Schedulers.immediate())
    }

    @Test
    fun testNearbySuggestionsWithOnlyAirport() {
        val root = File("../mocked/templates").canonicalPath
        val opener = FileSystemOpener(root)
        server.setDispatcher(ExpediaDispatcher(opener))

        val test = TestSubscriber<List<SuggestionV4>>()
        val observer = service?.suggestNearbyV4("en_US", "latLng", 1, "clientId",
                SuggestionResultType.AIRPORT or SuggestionResultType.AIRPORT_METRO_CODE, "distance", "HOTELS")
        observer?.subscribe(test)

        val suggestions = test.onNextEvents[0]
        assertEquals(2, suggestions.size)
        assertEquals("AIRPORT", suggestions[0].type)
        test.assertValueCount(1)
    }

    @Test
    fun testNearbySuggestionsWithMultiCity() {
        val root = File("../mocked/templates").canonicalPath
        val opener = FileSystemOpener(root)
        server.setDispatcher(ExpediaDispatcher(opener))

        val test = TestSubscriber<List<SuggestionV4>>()
        val observer = service?.suggestNearbyV4("en_US", "latLng", 1, "clientId",
                SuggestionResultType.MULTI_CITY, "distance", "HOTELS")
        observer?.subscribe(test)

        val suggestions = test.onNextEvents[0]
        assertEquals(2, suggestions.size)
        assertEquals("MULTICITY", suggestions[0].type)
        test.assertValueCount(1)
    }
}