package com.expedia.vm.hotel

import com.expedia.bookings.R
import com.expedia.bookings.interceptors.MockInterceptor
import com.expedia.bookings.services.urgency.UrgencyServices
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.testutils.SimpleTestDispatcher
import com.squareup.phrase.Phrase
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import okhttp3.mockwebserver.MockWebServer
import org.joda.time.LocalDate
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RuntimeEnvironment
import rx.observers.TestSubscriber
import rx.schedulers.Schedulers
import kotlin.test.assertEquals

@RunWith(RobolectricRunner::class)
class UrgencyViewModelTest {
    var server = MockWebServer()
        @Rule get

    lateinit var urgencyService : UrgencyServices
    lateinit var testViewModel: UrgencyViewModel
    lateinit var today: LocalDate

    @Before
    fun setUp() {
        val logger = HttpLoggingInterceptor()
        logger.level = HttpLoggingInterceptor.Level.BODY
        val interceptor = MockInterceptor()
        urgencyService = UrgencyServices("http://localhost:" + server.getPort(), OkHttpClient.Builder().addInterceptor(logger).build(),
                interceptor, Schedulers.immediate(), Schedulers.immediate())
        testViewModel = UrgencyViewModel(RuntimeEnvironment.application, urgencyService)
        today = LocalDate.now()
    }

    @Test
    fun testHappyUrgency() {
        val expectedScore = 80
        val expectedScoreText = getExpectedScoreText(expectedScore)
        val expectedDescription = getExpectedDescription("Vail - Beaver Creek, CO")

        server.setDispatcher(SimpleTestDispatcher("src/test/resources/raw/hotel/urgency_happy.json"))

        val testSoldOutTextSub = TestSubscriber<String>()
        val testDescriptionTextSub = TestSubscriber<String>()
        testViewModel.percentSoldOutTextSubject.subscribe(testSoldOutTextSub)
        testViewModel.urgencyDescriptionSubject.subscribe(testDescriptionTextSub)

        testViewModel.fetchCompressionScore("12342", today.plusYears(1), today.plusYears(1).plusDays(1))

        assertEquals(expectedScoreText, testSoldOutTextSub.onNextEvents[0], "Error Both score and description musht")
        assertEquals(expectedDescription, testDescriptionTextSub.onNextEvents[0])
    }

    @Test
    fun testUrgencyBelowThreshold() {
        server.setDispatcher(SimpleTestDispatcher("src/test/resources/raw/hotel/urgency_below_threshold.json"))

        val testSoldOutTextSub = TestSubscriber<String>()
        val testDescriptionTextSub = TestSubscriber<String>()
        testViewModel.percentSoldOutTextSubject.subscribe(testSoldOutTextSub)
        testViewModel.urgencyDescriptionSubject.subscribe(testDescriptionTextSub)

        testViewModel.fetchCompressionScore("12342", today.plusYears(1), today.plusYears(1).plusDays(1))

        testSoldOutTextSub.assertNoValues()
        testDescriptionTextSub.assertNoValues()
    }

    @Test
    fun testUrgencyAboveMaximum() {
        val expectedRoundedScore = 95
        val expectedScoreText = getExpectedScoreText(expectedRoundedScore)

        server.setDispatcher(SimpleTestDispatcher("src/test/resources/raw/hotel/urgency_max_threshold.json"))

        val testSoldOutTextSub = TestSubscriber<String>()
        testViewModel.percentSoldOutTextSubject.subscribe(testSoldOutTextSub)

        testViewModel.fetchCompressionScore("12342", today.plusYears(1), today.plusYears(1).plusDays(1))

        assertEquals(expectedScoreText, testSoldOutTextSub.onNextEvents[0], "Error: Expected Score to be capped at 95,"
                + "the api sends 100 for some regions even though we have results")
    }

    @Test
    fun testUrgencyRounding() {
        val expectedRoundedScore = 75
        val expectedScoreText = getExpectedScoreText(expectedRoundedScore)

        server.setDispatcher(SimpleTestDispatcher("src/test/resources/raw/hotel/urgency_requires_rounding.json"))

        val testSoldOutTextSub = TestSubscriber<String>()
        testViewModel.percentSoldOutTextSubject.subscribe(testSoldOutTextSub)

        testViewModel.fetchCompressionScore("12342", today.plusYears(1), today.plusYears(1).plusDays(1))

        assertEquals(expectedScoreText, testSoldOutTextSub.onNextEvents[0], "Error: Rounding might be wrong.")
    }

    @Test
    fun testUrgencyDateFormat() {
        val testYear = LocalDate.now().plusYears(1).year
        val testMonth = 11
        val testDay = 20
        val testDate = LocalDate(testYear, testMonth, testDay)

        assertEquals("$testMonth/$testDay/$testYear", testViewModel.getUrgencyDateFormat(testDate))
    }

    @Test
    fun testInvalidRegionId() {
        server.setDispatcher(SimpleTestDispatcher("src/test/resources/raw/hotel/urgency_happy.json"))

        val testSoldOutTextSub = TestSubscriber<String>()
        val testDescriptionTextSub = TestSubscriber<String>()
        testViewModel.percentSoldOutTextSubject.subscribe(testSoldOutTextSub)
        testViewModel.urgencyDescriptionSubject.subscribe(testDescriptionTextSub)


        testViewModel.fetchCompressionScore("0", today.plusYears(1), today.plusYears(1).plusDays(1))

        testSoldOutTextSub.assertNoValues()
        testDescriptionTextSub.assertNoValues()
    }

    private fun getExpectedScoreText(score: Int) : String {
        return Phrase.from(RuntimeEnvironment.application, R.string.urgency_percent_booked_TEMPLATE)
                .put("percentage", score)
                .format().toString()
    }

    private fun getExpectedDescription(displayName: String) : String {
        return Phrase.from(RuntimeEnvironment.application, R.string.urgency_destination_description_TEMPLATE)
                .put("destination", displayName)
                .format().toString()
    }
}