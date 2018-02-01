package com.expedia.vm.hotel

import com.expedia.bookings.R
import com.expedia.bookings.data.abacus.AbacusUtils
import com.expedia.bookings.data.abacus.AbacusVariant
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
import com.expedia.bookings.services.TestObserver
import com.expedia.bookings.utils.AbacusTestUtils
import io.reactivex.schedulers.Schedulers
import kotlin.test.assertEquals

@RunWith(RobolectricRunner::class)
class UrgencyViewModelTest {
    var server = MockWebServer()
        @Rule get

    lateinit var urgencyService: UrgencyServices
    lateinit var testViewModel: UrgencyViewModel
    lateinit var today: LocalDate

    private val context = RuntimeEnvironment.application

    @Before
    fun setUp() {
        val logger = HttpLoggingInterceptor()
        logger.level = HttpLoggingInterceptor.Level.BODY
        val interceptor = MockInterceptor()
        urgencyService = UrgencyServices("http://localhost:" + server.getPort(), OkHttpClient.Builder().addInterceptor(logger).build(),
                interceptor, Schedulers.trampoline(), Schedulers.trampoline())
        testViewModel = UrgencyViewModel(RuntimeEnvironment.application, urgencyService)
        today = LocalDate.now()
    }

    @Test
    fun testHappyUrgency() {
        AbacusTestUtils.bucketTestAndEnableRemoteFeature(context, AbacusUtils.HotelUrgencyV2,
                bucketVariant = AbacusVariant.ONE.value)
        val expectedAvailability = 20
        val expectedText = getExpectedScoreText(expectedAvailability)

        server.setDispatcher(SimpleTestDispatcher("src/test/resources/raw/hotel/urgency_happy.json"))

        val testObserver = TestObserver<String>()
        testViewModel.urgencyTextSubject.subscribe(testObserver)

        testViewModel.fetchCompressionScore("12342", today.plusYears(1), today.plusYears(1).plusDays(1))
        assertEquals(expectedText, testObserver.values()[0])
    }

    @Test
    fun testUrgencyBelowThresholdVariantOne() {
        AbacusTestUtils.bucketTestAndEnableRemoteFeature(context, AbacusUtils.HotelUrgencyV2,
                bucketVariant = AbacusVariant.ONE.value)
        server.setDispatcher(SimpleTestDispatcher("src/test/resources/raw/hotel/urgency_below_threshold_variant_one.json"))

        val testObserver = TestObserver<String>()
        testViewModel.urgencyTextSubject.subscribe(testObserver)

        testViewModel.fetchCompressionScore("12342", today.plusYears(1), today.plusYears(1).plusDays(1))
        testObserver.assertNoValues()
    }

    @Test
    fun testUrgencyBelowThresholdVariantTwo() {
        AbacusTestUtils.bucketTestAndEnableRemoteFeature(context, AbacusUtils.HotelUrgencyV2,
                bucketVariant = AbacusVariant.TWO.value)
        server.setDispatcher(SimpleTestDispatcher("src/test/resources/raw/hotel/urgency_below_threshold_variant_two.json"))

        val testObserver = TestObserver<String>()
        testViewModel.urgencyTextSubject.subscribe(testObserver)

        testViewModel.fetchCompressionScore("12342", today.plusYears(1), today.plusYears(1).plusDays(1))
        testObserver.assertNoValues()
    }

    @Test
    fun testUrgencyNotBucketed() {
        AbacusTestUtils.bucketTestAndEnableRemoteFeature(context, AbacusUtils.HotelUrgencyV2,
                bucketVariant = AbacusVariant.CONTROL.value)

        server.setDispatcher(SimpleTestDispatcher("src/test/resources/raw/hotel/urgency_happy.json"))

        val testObserver = TestObserver<String>()
        testViewModel.urgencyTextSubject.subscribe(testObserver)

        testViewModel.fetchCompressionScore("12342", today.plusYears(1), today.plusYears(1).plusDays(1))
        testObserver.assertNoValues()
    }

    @Test
    fun testUrgencyAboveMaximum() {
        AbacusTestUtils.bucketTestAndEnableRemoteFeature(context, AbacusUtils.HotelUrgencyV2,
                bucketVariant = AbacusVariant.ONE.value)
        val expectedAvailability = 5
        val expectedText = getExpectedScoreText(expectedAvailability)

        server.setDispatcher(SimpleTestDispatcher("src/test/resources/raw/hotel/urgency_max_threshold.json"))

        val testObserver = TestObserver<String>()
        testViewModel.urgencyTextSubject.subscribe(testObserver)

        testViewModel.fetchCompressionScore("12342", today.plusYears(1), today.plusYears(1).plusDays(1))
        assertEquals(expectedText, testObserver.values()[0], "Error: Expected score to be capped at 95,"
                + "the api sends 100% sold out for some regions even though we have results")
    }

    @Test
    fun testUrgencyRounding() {
        AbacusTestUtils.bucketTestAndEnableRemoteFeature(context, AbacusUtils.HotelUrgencyV2,
                bucketVariant = AbacusVariant.ONE.value)
        val expectedAvailability = 25
        val expectedText = getExpectedScoreText(expectedAvailability)

        server.setDispatcher(SimpleTestDispatcher("src/test/resources/raw/hotel/urgency_requires_rounding.json"))

        val testSoldOutTextSub = TestObserver<String>()
        testViewModel.urgencyTextSubject.subscribe(testSoldOutTextSub)

        testViewModel.fetchCompressionScore("12342", today.plusYears(1), today.plusYears(1).plusDays(1))
        assertEquals(expectedText, testSoldOutTextSub.values()[0], "Error: Rounding might be wrong.")
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

        val testObserver = TestObserver<String>()
        testViewModel.urgencyTextSubject.subscribe(testObserver)

        testViewModel.fetchCompressionScore("0", today.plusYears(1), today.plusYears(1).plusDays(1))
        testObserver.assertNoValues()
    }

    private fun getExpectedScoreText(score: Int): String {
        return Phrase.from(RuntimeEnvironment.application, R.string.urgency_only_x_hotels_left_TEMPLATE)
                .put("percent", score)
                .format().toString()
    }
}
