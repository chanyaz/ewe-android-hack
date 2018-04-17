package com.expedia.bookings.unit

import com.expedia.bookings.data.flights.RichContentRequestInfo
import com.expedia.bookings.data.flights.RichContentRequest
import com.expedia.bookings.data.flights.RichContentResponse
import com.expedia.bookings.interceptors.MockInterceptor
import com.expedia.bookings.services.KongFlightServices
import com.expedia.bookings.services.TestObserver
import com.mobiata.mocke3.ExpediaDispatcher
import com.mobiata.mocke3.FileSystemOpener
import io.reactivex.schedulers.Schedulers
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import okhttp3.mockwebserver.MockWebServer
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.io.File
import java.util.concurrent.TimeUnit
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class KongFlightServicesTest {
    var server: MockWebServer = MockWebServer()
        @Rule get

    private lateinit var kongService: KongFlightServices

    @Before
    fun before() {
        val logger = HttpLoggingInterceptor()
        logger.level = HttpLoggingInterceptor.Level.BODY
        kongService = KongFlightServices("http://localhost:" + server.port,
                OkHttpClient.Builder().addInterceptor(logger).build(),
                listOf(MockInterceptor()), Schedulers.trampoline(), Schedulers.trampoline())
        val root = File("../mocked/templates").canonicalPath
        val opener = FileSystemOpener(root)
        server.setDispatcher(ExpediaDispatcher(opener))
    }

    @Test
    @Throws(Throwable::class)
    fun testGetRichContent() {
        val observer = TestObserver<RichContentResponse>()
        val richContentRequest = getRichContentRequest()
        kongService.getFlightRichContent(richContentRequest, observer)
        observer.awaitTerminalEvent(10, TimeUnit.SECONDS)
        observer.assertNoErrors()
        observer.assertComplete()
        observer.assertValueCount(1)

        val response = observer.values()[0]
        assertEquals(2, response.richContentList.size)
        var richContent = response.richContentList[0]
        assertEquals(8.1f, richContent.score)
        val legAmenities = richContent.legAmenities
        assertTrue(legAmenities!!.wifi)
        assertTrue(legAmenities.entertainment)
        assertTrue(legAmenities.power)

        richContent = response.richContentList[1]
        assertEquals("VERY_GOOD", richContent.scoreExpression)
        assertEquals(1, richContent.segmentAmenitiesList.size)
        val segmentAmenities = richContent.segmentAmenitiesList[0]
        assertTrue(segmentAmenities.wifi)
        assertTrue(segmentAmenities.entertainment)
        assertTrue(segmentAmenities.power)
    }

    private fun getRichContentRequest(): RichContentRequest {
        val richContentRequest = RichContentRequest()

        val requestInfo = RichContentRequestInfo()
        requestInfo.eapid = "-1"
        requestInfo.tpid = "1"
        requestInfo.tuid = -1
        richContentRequest.requestInfo = requestInfo

        return richContentRequest
    }
}
