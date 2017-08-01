package com.expedia.bookings.unit

import com.expedia.bookings.data.sos.MemberDealRequest
import com.expedia.bookings.data.sos.MemberDealResponse
import com.expedia.bookings.interceptors.MockInterceptor
import com.expedia.bookings.services.sos.SmartOfferService
import com.mobiata.mocke3.ExpediaDispatcher
import com.mobiata.mocke3.FileSystemOpener
import com.expedia.bookings.services.TestObserver
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

class SOSServiceTest {

    var server: MockWebServer = MockWebServer()
        @Rule get

    var service: SmartOfferService? = null

    @Before
    fun before() {
        val logger = HttpLoggingInterceptor()
        logger.level = HttpLoggingInterceptor.Level.BODY
        val interceptor = MockInterceptor()
        service = SmartOfferService("http://localhost:" + server.port,
                OkHttpClient.Builder().addInterceptor(logger).build(),
                interceptor, Schedulers.trampoline(), Schedulers.trampoline())

        givenServerUsingMockResponses()
    }

    @Test
    @Throws(Throwable::class)
    fun testMockSearchWorks() {

        val observer = TestMemberDealObsrver()
        val params = MemberDealRequest()

        service!!.fetchMemberDeals(params,observer)
        observer.awaitTerminalEvent(3, TimeUnit.SECONDS)

        observer.assertNoErrors()
        observer.assertComplete()
        observer.assertValueCount(1)
    }

    @Test
    fun testSoSReturnedDealsAreCorrect() {
        val observer = TestMemberDealObsrver()
        val params = MemberDealRequest()

        service!!.fetchMemberDeals(params, observer)
        observer.awaitTerminalEvent(3, TimeUnit.SECONDS)

        observer.assertNoErrors()
        val response = observer.values()[0]
        Assert.assertTrue(response!!.destinations!![0].hotels!![0].offerMarkers!!.isNotEmpty())
    }

    @Throws(IOException::class)
    private fun givenServerUsingMockResponses() {
        val root = File("../mocked/templates").canonicalPath
        val opener = FileSystemOpener(root)
        server.setDispatcher(ExpediaDispatcher(opener))
    }

    class TestMemberDealObsrver : TestObserver<MemberDealResponse>() {
    }
}
