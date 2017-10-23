package com.expedia.bookings.unit

import com.expedia.bookings.data.flights.BaggageInfoResponse
import com.expedia.bookings.interceptors.MockInterceptor
import com.expedia.bookings.services.BaggageInfoService
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
import java.util.concurrent.TimeUnit
import kotlin.test.assertEquals

class BaggageInfoServiceTest {

    var server: MockWebServer = MockWebServer()
        @Rule get

    private var service: BaggageInfoService? = null

    @Before
    fun before() {
        val logger = HttpLoggingInterceptor()
        logger.level = HttpLoggingInterceptor.Level.BODY
        val interceptor = MockInterceptor()
        service = BaggageInfoService("http://localhost:" + server.port,
                OkHttpClient.Builder().addInterceptor(logger).build(),
                interceptor, Schedulers.immediate(), Schedulers.immediate())
    }

    @Test
    @Throws(Throwable::class)
    fun testbaggageInfoResponse() {
        val root = File("../mocked/templates").canonicalPath
        val opener = FileSystemOpener(root)
        server.setDispatcher(ExpediaDispatcher(opener))
        val observer = TestSubscriber<BaggageInfoResponse>()
        service!!.getBaggageInfo(getBaggageParams(), observer)
        observer.awaitTerminalEvent(10, TimeUnit.SECONDS)
        val response = observer.onNextEvents[0]
        val expectedCharges = getExpectedCharge()
        observer.assertNoErrors()
        observer.assertCompleted()
        observer.assertValueCount(1)
        assertEquals("Emirates", response.airlineName)
        assertEquals(expectedCharges, response.charges)
    }

    private fun getBaggageParams(): ArrayList<HashMap<String, String>> {
        val baggageInfoParams = ArrayList<HashMap<String, String>>()
        val map: HashMap<String, String> = (hashMapOf(
                "originapt" to "LHR",
                "destinationapt" to "DXB",
                "cabinclass" to "3",
                "mktgcarrier" to "EK",
                "opcarrier" to "",
                "bookingclass" to "U",
                "traveldate" to "10/23/2017",
                "flightnumber" to "4",
                "segmentnumber" to "1"
        ))
        baggageInfoParams.add(map)
        return baggageInfoParams
    }

    private fun getExpectedCharge(): ArrayList<HashMap<String, String>> {
        val expectedCharges = ArrayList<HashMap<String, String>>()
        expectedCharges.add(hashMapOf("Carry-on Bag" to "No fee"))
        expectedCharges.add(hashMapOf("1st Checked Bag" to "No fee up to 30 kg"))
        expectedCharges.add(hashMapOf("2nd Checked Bag" to "$45.00 per kg"))
        return expectedCharges
    }
}