package com.expedia.bookings.unit

import com.expedia.bookings.data.hotels.HotelReviewsParams
import com.expedia.bookings.data.hotels.HotelReviewsResponse
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
import kotlin.test.assertNotEquals
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

    @Test
    fun testReviewsSearchHappy() {
        val testObserver = TestObserver<HotelReviewsResponse>()
        var reviewsParams = HotelReviewsParams("11544584", "RATINGDESC", 20, 100, "", "PrivateBank")
        service.reviewsSearch(reviewsParams).subscribe(testObserver)

        testObserver.awaitTerminalEvent(10, TimeUnit.SECONDS)
        testObserver.assertNoErrors()
        testObserver.assertComplete()
        testObserver.assertValueCount(1)

        //Expecting happy-search.json
        val response: HotelReviewsResponse = testObserver.values()[0]
        assertNotNull(response)
        assertEquals(2, response.reviewDetails.reviewCollection.review.size)

        val reviewSearchResult = response.reviewDetails.reviewCollection.review[0]
        assertEquals("11544584", reviewSearchResult.hotelId)
    }

    @Test
    fun testReviewsSearchUnHappyEmptyResult() {
        val testObserver = TestObserver<HotelReviewsResponse>()
        var reviewsParams = HotelReviewsParams("11544584", "RATINGDESC", 20, 100, "", "xyz")
        service.reviewsSearch(reviewsParams).subscribe(testObserver)

        testObserver.awaitTerminalEvent(10, TimeUnit.SECONDS)
        testObserver.assertNoErrors()
        testObserver.assertComplete()
        testObserver.assertValueCount(1)

        //Expecting unhappy-search.json
        val response: HotelReviewsResponse = testObserver.values()[0]
        assertNotNull(response)
        assertEquals(0, response.reviewDetails.reviewCollection.review.size)
    }

    @Test
    fun testReviewsSearchUnHappyEmptyKeywordAndEmptyResult() {
        val testObserver = TestObserver<HotelReviewsResponse>()
        var reviewsParams = HotelReviewsParams("11544584", "RATINGDESC", 20, 100, "", "")
        service.reviewsSearch(reviewsParams).subscribe(testObserver)

        testObserver.awaitTerminalEvent(10, TimeUnit.SECONDS)
        testObserver.assertNoErrors()
        testObserver.assertComplete()
        testObserver.assertValueCount(1)

        //Expecting unhappy-search.json
        val response: HotelReviewsResponse = testObserver.values()[0]
        assertNotNull(response)
        assertEquals(0, response.reviewDetails.reviewCollection.review.size)
    }

    @Test
    fun testReviewsSearchUnHappySearchTermIsNull() {
        val testObserver = TestObserver<HotelReviewsResponse>()
        var reviewsParams = HotelReviewsParams("11544584", "RATINGDESC", 20, 100, "", null)
        service.reviewsSearch(reviewsParams).subscribe(testObserver)

        testObserver.awaitTerminalEvent(10, TimeUnit.SECONDS)
        testObserver.assertNoErrors()
        testObserver.assertComplete()
        testObserver.assertValueCount(1)

        //Expecting happy.json from non keyword search review service
        val response: HotelReviewsResponse = testObserver.values()[0]
        assertNotNull(response)
        assertEquals(20, response.reviewDetails.reviewCollection.review.size)
        val review = response.reviewDetails.reviewCollection.review[0]
        assertNotEquals("11544584", review.hotelId)
        assertEquals("26500", review.hotelId)
    }
}
