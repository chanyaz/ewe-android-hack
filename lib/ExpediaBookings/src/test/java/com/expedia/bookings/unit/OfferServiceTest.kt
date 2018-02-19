package com.expedia.bookings.unit

import com.expedia.bookings.data.os.LastMinuteDealsResponse
import com.expedia.bookings.data.os.LastMinuteDealsRequest
import com.expedia.bookings.interceptors.MockInterceptor
import com.mobiata.mocke3.ExpediaDispatcher
import com.mobiata.mocke3.FileSystemOpener
import com.expedia.bookings.services.TestObserver
import com.expedia.bookings.services.os.OfferService
import io.reactivex.schedulers.Schedulers
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import okhttp3.mockwebserver.MockWebServer
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.io.File
import java.io.IOException
import java.util.concurrent.TimeUnit

class OfferServiceTest {

    var server: MockWebServer = MockWebServer()
        @Rule get

    var service: OfferService? = null

    @Before
    fun before() {
        val logger = HttpLoggingInterceptor()
        logger.level = HttpLoggingInterceptor.Level.BODY
        val interceptor = MockInterceptor()
        service = OfferService("http://localhost:" + server.port,
                OkHttpClient.Builder().addInterceptor(logger).build(),
                interceptor, Schedulers.trampoline(), Schedulers.trampoline())

        givenServerUsingMockResponses()
    }

    @Test
    @Throws(Throwable::class)
    fun testMockSearchWorks() {

        val observer = TestLastMinuteDealsObserver()
        val tuid = "12345"
        val params = LastMinuteDealsRequest(tuid)

        service!!.fetchDeals(params, observer)
        observer.awaitTerminalEvent(3, TimeUnit.SECONDS)

        observer.assertNoErrors()
        observer.assertComplete()
        observer.assertValueCount(1)
    }

    @Test
    fun testSoSReturnedDealsAreCorrect() {
        val observer = TestLastMinuteDealsObserver()
        val tuid = "12345"
        val params = LastMinuteDealsRequest(tuid)

        service!!.fetchDeals(params, observer)
        observer.awaitTerminalEvent(3, TimeUnit.SECONDS)

        observer.assertNoErrors()
        val response = observer.values()[0]
        Assert.assertTrue(response.offers.hotels[0].offerMarkers!!.isNotEmpty())
    }

    @Throws(IOException::class)
    private fun givenServerUsingMockResponses() {
        val root = File("../mocked/templates").canonicalPath
        val opener = FileSystemOpener(root)
        server.setDispatcher(ExpediaDispatcher(opener))
    }

    class TestLastMinuteDealsObserver : TestObserver<LastMinuteDealsResponse>()
}
