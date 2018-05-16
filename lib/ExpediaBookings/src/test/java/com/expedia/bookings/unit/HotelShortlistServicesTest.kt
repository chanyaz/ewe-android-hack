package com.expedia.bookings.unit

import com.expedia.bookings.data.hotels.shortlist.HotelShortlistItem
import com.expedia.bookings.data.hotels.shortlist.HotelShortlistResponse
import com.expedia.bookings.data.hotels.shortlist.ShortlistItem
import com.expedia.bookings.interceptors.MockInterceptor
import com.expedia.bookings.services.TestObserver
import com.expedia.bookings.services.HotelShortlistServices
import com.mobiata.mocke3.ExpediaDispatcher
import com.mobiata.mocke3.FileSystemOpener
import io.reactivex.schedulers.Schedulers
import org.junit.Assert.assertTrue
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import okhttp3.mockwebserver.MockWebServer
import org.joda.time.LocalDate
import org.junit.Before
import org.junit.Test
import java.io.File
import java.util.concurrent.TimeUnit
import kotlin.properties.Delegates
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class HotelShortlistServicesTest {
    var server = MockWebServer()
    private var service: HotelShortlistServices by Delegates.notNull()
    private lateinit var interceptor: MockInterceptor
    private lateinit var hotelShortlistInterceptor: MockInterceptor

    @Before
    fun before() {
        val logger = HttpLoggingInterceptor()
        logger.level = HttpLoggingInterceptor.Level.BODY
        interceptor = MockInterceptor()
        hotelShortlistInterceptor = MockInterceptor()

        service = HotelShortlistServices("http://localhost:" + server.port,
                OkHttpClient.Builder().addInterceptor(logger).build(),
                interceptor, hotelShortlistInterceptor,
                Schedulers.trampoline(), Schedulers.trampoline())

        val root = File("../mocked/templates").canonicalPath
        val opener = FileSystemOpener(root)
        server.setDispatcher(ExpediaDispatcher(opener))
    }

    @Test
    fun testFetchShortlistHappyResponse() {
        val testObserver = TestObserver<HotelShortlistResponse<HotelShortlistItem>>()
        service.fetchFavoriteHotels(testObserver)

        testObserver.awaitValueCount(1, 10, TimeUnit.SECONDS)
        testObserver.assertValueCount(1)

        val response = testObserver.values()[0]
        assertNotNull(response)

        assertEquals(1, response.results.size)
        assertTrue(response.results[0].items.isNotEmpty())
    }

    @Test
    fun hotelFetchShortlistServicesHitAllInterceptors() {
        val testObserver = TestObserver<HotelShortlistResponse<HotelShortlistItem>>()

        service.fetchFavoriteHotels(testObserver)

        testObserver.awaitTerminalEvent()
        testObserver.assertComplete()

        assertTrue(interceptor.wasCalled())
        assertTrue(hotelShortlistInterceptor.wasCalled())
    }

    @Test
    fun testSaveShortlistHappyResponse() {
        val testObserver = TestObserver<HotelShortlistResponse<ShortlistItem>>()
        service.saveFavoriteHotel("", LocalDate.now(), LocalDate.now(), "1", testObserver)

        testObserver.awaitValueCount(1, 10, TimeUnit.SECONDS)
        testObserver.assertValueCount(1)

        val response = testObserver.values()[0]
        assertNotNull(response)

        assertEquals(1, response.results.size)
        assertTrue(response.results[0].items.isNotEmpty())
    }

    @Test
    fun hotelSaveShortlistServicesHitAllInterceptors() {
        val testObserver = TestObserver<HotelShortlistResponse<ShortlistItem>>()

        service.saveFavoriteHotel("", LocalDate.now(), LocalDate.now(), "1", testObserver)

        testObserver.awaitTerminalEvent()
        testObserver.assertComplete()

        assertTrue(interceptor.wasCalled())
        assertTrue(hotelShortlistInterceptor.wasCalled())
    }
}
