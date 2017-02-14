package com.expedia.vm.hotel

import com.expedia.bookings.interceptors.MockInterceptor
import com.expedia.bookings.services.urgency.UrgencyServices
import com.expedia.testutils.SimpleTestDispatcher
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import okhttp3.mockwebserver.MockWebServer
import org.joda.time.LocalDate
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import rx.observers.TestSubscriber
import rx.schedulers.Schedulers
import kotlin.test.assertEquals

class UrgencyViewModelTest {
    var server = MockWebServer()
        @Rule get

    lateinit var urgencyService : UrgencyServices
    lateinit var testViewModel: UrgencyViewModel

    @Before
    fun setUp() {
        val logger = HttpLoggingInterceptor()
        logger.level = HttpLoggingInterceptor.Level.BODY
        val interceptor = MockInterceptor()
        urgencyService = UrgencyServices("http://localhost:" + server.getPort(), OkHttpClient.Builder().addInterceptor(logger).build(),
                interceptor, Schedulers.immediate(), Schedulers.immediate())
        testViewModel = UrgencyViewModel(urgencyService)
    }

    @Test
    fun testHappyUrgency() {
        val expectedScore = 80
        val today = LocalDate.now()

        server.setDispatcher(SimpleTestDispatcher("src/test/resources/raw/hotel/urgency_response.json"))

        val testSubscriber = TestSubscriber<Int>()
        testViewModel.percentSoldOutScoreSubject.subscribe(testSubscriber)
        testViewModel.fetchCompressionScore("12342", today.plusYears(1), today.plusYears(1).plusDays(1))

        assertEquals(expectedScore, testSubscriber.onNextEvents[0])
    }

    @Test
    fun testUrgencyDateFormat() {
        val testYear = LocalDate.now().plusYears(1).year
        val testMonth = 11
        val testDay = 20
        val testDate = LocalDate(testYear, testMonth, testDay)

        assertEquals("$testMonth/$testDay/$testYear", testViewModel.getUrgencyDateFormat(testDate))
    }
}