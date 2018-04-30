package com.expedia.bookings.unit

import com.expedia.bookings.data.HolidayCalendarResponse
import com.expedia.bookings.interceptors.MockInterceptor
import com.expedia.bookings.services.HolidayCalendarService
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

class HolidayCalendarServiceTest {
    var server: MockWebServer = MockWebServer()
        @Rule get

    private var service: HolidayCalendarService? = null

    @Before
    fun before() {
        val logger = HttpLoggingInterceptor()
        logger.level = HttpLoggingInterceptor.Level.BODY
        val interceptor = MockInterceptor()
        service = HolidayCalendarService("http://localhost:" + server.port,
                OkHttpClient.Builder().addInterceptor(logger).build(),
                interceptor, Schedulers.trampoline(), Schedulers.trampoline())
    }

    @Test
    @Throws(Throwable::class)
    fun testHolidayInfoResponse() {
        val root = File("../mocked/templates").canonicalPath
        val opener = FileSystemOpener(root)
        server.setDispatcher(ExpediaDispatcher(opener))
        val observer = TestObserver<HolidayCalendarResponse>()
        service!!.getHoliday("US", "en_US", observer)
        observer.awaitTerminalEvent(10, TimeUnit.SECONDS)
        observer.assertNoErrors()
        observer.assertComplete()
        observer.assertValueCount(1)
    }
}
