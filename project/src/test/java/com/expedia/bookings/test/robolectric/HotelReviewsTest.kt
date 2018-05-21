package com.expedia.bookings.test

import com.expedia.bookings.analytics.AnalyticsProvider
import com.expedia.bookings.analytics.OmnitureTestUtils
import com.expedia.bookings.data.hotels.HotelReviewsResponse
import com.expedia.bookings.data.hotels.ReviewSummary
import com.expedia.bookings.data.hotels.ReviewSort
import com.expedia.bookings.hotel.data.TranslatedReview
import com.expedia.bookings.services.ReviewsServices
import com.expedia.bookings.testrule.ServicesRule
import com.expedia.vm.HotelReviewsAdapterViewModel
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import io.reactivex.Observable
import com.expedia.bookings.services.TestObserver
import com.expedia.bookings.test.robolectric.RobolectricRunner
import org.junit.runner.RunWith
import java.util.concurrent.TimeUnit
import kotlin.properties.Delegates
import kotlin.test.assertEquals
import kotlin.test.assertFalse

@RunWith(RobolectricRunner::class)
class HotelReviewsTest {

    var reviewServicesRule = ServicesRule(ReviewsServices::class.java)
        @Rule get

    private val HOTEL_ID = "26650"
    private val NUMBER_FAVOURABLE_REVIEWS: Int = 9
    private val NUMBER_CRITICAL_REVIEWS: Int = 11
    private lateinit var mockAnalyticsProvider: AnalyticsProvider

    var vm: HotelReviewsAdapterViewModel by Delegates.notNull()

    @Before
    fun before() {
        vm = HotelReviewsAdapterViewModel(HOTEL_ID, reviewServicesRule.services!!, "en_US")
        mockAnalyticsProvider = OmnitureTestUtils.setMockAnalyticsProvider()
    }

    @Test
    fun reviewsBySortParam() {
        val numberOfCriticalReviewsSubscriber = TestObserver<Int>()
        val numberOfNewestReviewsSubscriber = TestObserver<Int>()
        val numberOfFavorableReviewsSubscriber = TestObserver<Int>()

        vm.criticalReviewsObservable.take(1).map { it.count() }.subscribe(numberOfCriticalReviewsSubscriber)
        vm.newestReviewsObservable.take(2).map { it.count() }.subscribe(numberOfNewestReviewsSubscriber)
        vm.favorableReviewsObservable.take(2).map { it.count() }.subscribe(numberOfFavorableReviewsSubscriber)

        // Fetch reviews by lowest rating first.
        // Fetch reviews by highest rating twice.
        Observable.concat(
                Observable.just(ReviewSort.LOWEST_RATING_FIRST, ReviewSort.HIGHEST_RATING_FIRST, ReviewSort.HIGHEST_RATING_FIRST),
                Observable.never())
                .subscribe(vm.reviewsObserver)

        numberOfCriticalReviewsSubscriber.awaitTerminalEvent(10, TimeUnit.SECONDS)
        numberOfCriticalReviewsSubscriber.assertValueSequence(listOf(NUMBER_CRITICAL_REVIEWS))

        numberOfFavorableReviewsSubscriber.awaitTerminalEvent(10, TimeUnit.SECONDS)
        numberOfFavorableReviewsSubscriber.assertComplete()
        numberOfFavorableReviewsSubscriber.assertValueSequence(listOf(NUMBER_FAVOURABLE_REVIEWS, NUMBER_FAVOURABLE_REVIEWS))

        numberOfNewestReviewsSubscriber.awaitTerminalEvent(5, TimeUnit.SECONDS)
        numberOfNewestReviewsSubscriber.assertNotTerminated()
    }

    @Test
    fun moreReviewsFetchedOnSubsequentCalls() {
        Observable.concat(
                Observable.just(ReviewSort.LOWEST_RATING_FIRST, ReviewSort.LOWEST_RATING_FIRST, ReviewSort.LOWEST_RATING_FIRST),
                Observable.never())
                .subscribe(vm.reviewsObserver)

        val recordedRequest1 = reviewServicesRule.server.takeRequest()
        assertEquals(getExpectedReviewRequestStr(0), recordedRequest1.path)
        val recordedRequest2 = reviewServicesRule.server.takeRequest()
        assertEquals(getExpectedReviewRequestStr(25), recordedRequest2.path)
        val recordedRequest3 = reviewServicesRule.server.takeRequest()
        assertEquals(getExpectedReviewRequestStr(50), recordedRequest3.path)
    }

    @Test
    fun reviewsSummary() {
        val testSubscriber = TestObserver<ReviewSummary>()
        vm.reviewsSummaryObservable.take(1).subscribe(testSubscriber)

        Observable.concat(
                Observable.just(ReviewSort.NEWEST_REVIEW_FIRST),
                Observable.never())
                .subscribe(vm.reviewsObserver)

        testSubscriber.awaitTerminalEvent(10, TimeUnit.SECONDS)
        testSubscriber.assertNoErrors()
        testSubscriber.assertComplete()
    }

    @Test
    fun testReviewTranslation() {
        val testSubscriber = TestObserver<String>()
        vm.translationUpdatedObservable.subscribe(testSubscriber)
        vm.toggleReviewTranslationObserver.onNext("5a2cc5ffa6ffd10dd50e1844")
        testSubscriber.assertValueCount(1)
        assertEquals(vm.translationMap.size, 1)
        OmnitureTestUtils.assertLinkTracked("Translate User Review", "App.Hotels.Reviews.SeeTranslation", OmnitureMatchers.withoutProps(36), mockAnalyticsProvider)
    }

    @Test
    fun testReviewTranslationFail() {
        val testSubscriber = TestObserver<String>()
        vm.translationUpdatedObservable.subscribe(testSubscriber)
        vm.toggleReviewTranslationObserver.onNext("")
        testSubscriber.assertValueCount(1)
        assertEquals(vm.translationMap.size, 0)
        OmnitureTestUtils.assertLinkTracked("Translate User Review", "App.Hotels.Reviews.SeeTranslation", OmnitureMatchers.withProps(mapOf(36 to "HIS:Reviews:SeeTranslationError")), mockAnalyticsProvider)
    }

    @Test
    fun testReviewAlreadyTranslated() {
        val testSubscriber = TestObserver<String>()
        vm.translationUpdatedObservable.subscribe(testSubscriber)
        vm.translationMap["a1"] = TranslatedReview(HotelReviewsResponse.Review(), true)
        vm.toggleReviewTranslationObserver.onNext("a1")
        testSubscriber.assertValue("a1")
        assertFalse(vm.translationMap["a1"]!!.showToUser)
        OmnitureTestUtils.assertLinkTracked("Translate User Review", "App.Hotels.Reviews.SeeOriginal", OmnitureMatchers.withoutProps(36), mockAnalyticsProvider)
    }

    private fun getExpectedReviewRequestStr(startParam: Int): String {
        return "/api/hotelreviews/hotel/%s?sortBy=RATINGASC&start=%d&items=25&locale=en_US".format(HOTEL_ID, startParam) + "&clientid=expedia.app.android.phone%3A6.9.0"
    }
}
