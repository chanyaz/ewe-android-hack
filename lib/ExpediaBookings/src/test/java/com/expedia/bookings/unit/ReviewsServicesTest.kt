package com.expedia.bookings.unit

import com.expedia.bookings.data.hotels.HotelReviewsSummaryResponse
import com.expedia.bookings.interceptors.MockInterceptor
import com.expedia.bookings.services.ReviewsServices
import com.expedia.bookings.services.TestObserver
import com.mobiata.mocke3.ExpediaDispatcher
import com.mobiata.mocke3.FileSystemOpener
import io.reactivex.schedulers.Schedulers
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

class ReviewsServicesTest {
    private var server = MockWebServer()
    private var service: ReviewsServices by Delegates.notNull()

    @Before
    fun before() {
        val logger = HttpLoggingInterceptor()
        val interceptor = MockInterceptor()

        service = ReviewsServices("http://localhost:" + server.port,
                OkHttpClient.Builder().addInterceptor(logger).build(),
                interceptor, Schedulers.trampoline(), Schedulers.trampoline())

        val root = File("../mocked/templates").canonicalPath
        val opener = FileSystemOpener(root)
        server.setDispatcher(ExpediaDispatcher(opener))
    }

    @Test
    fun testReviewsSummaryHappy() {
        val testObserver = TestObserver<HotelReviewsSummaryResponse>()
        service.reviewsSummary("565746").subscribe(testObserver)

        testObserver.awaitTerminalEvent(10, TimeUnit.SECONDS)
        testObserver.assertNoErrors()
        testObserver.assertComplete()
        testObserver.assertValueCount(1)

        val response: HotelReviewsSummaryResponse = testObserver.values()[0]
        assertNotNull(response)
        assertEquals(1, response.reviewSummaryCollection.reviewSummary.size)

        val reviewSummary = response.reviewSummaryCollection.reviewSummary[0]
        assertEquals("565746", reviewSummary.hotelId)
    }
}
