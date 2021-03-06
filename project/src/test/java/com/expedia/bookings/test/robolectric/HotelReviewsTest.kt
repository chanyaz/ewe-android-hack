package com.expedia.bookings.test

import com.expedia.bookings.data.hotels.HotelReviewsResponse.ReviewSummary
import com.expedia.bookings.data.hotels.ReviewSort
import com.expedia.bookings.services.ReviewsServices
import com.expedia.vm.HotelReviewsAdapterViewModel
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import rx.Observable
import rx.observers.TestSubscriber
import java.util.concurrent.TimeUnit
import kotlin.properties.Delegates
import kotlin.test.assertEquals

public class HotelReviewsTest {

    public var reviewServicesRule = ServicesRule(ReviewsServices::class.java)
        @Rule get

    private val HOTEL_ID = "26650"
    private val NUMBER_FAVOURABLE_REVIEWS: Int = 9
    private val NUMBER_CRITICAL_REVIEWS: Int = 11

    public var vm: HotelReviewsAdapterViewModel by Delegates.notNull()

    @Before
    fun before() {
        vm = HotelReviewsAdapterViewModel(HOTEL_ID, reviewServicesRule.services!!, "en_US")
    }

    @Test
    fun reviewsBySortParam() {
        val numberOfCriticalReviewsSubscriber = TestSubscriber<Int>()
        val numberOfNewestReviewsSubscriber = TestSubscriber<Int>()
        val numberOfFavorableReviewsSubscriber = TestSubscriber<Int>()

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
        numberOfCriticalReviewsSubscriber.assertReceivedOnNext(listOf(NUMBER_CRITICAL_REVIEWS))

        numberOfFavorableReviewsSubscriber.awaitTerminalEvent(10, TimeUnit.SECONDS)
        numberOfFavorableReviewsSubscriber.assertCompleted()
        numberOfFavorableReviewsSubscriber.assertReceivedOnNext(listOf(NUMBER_FAVOURABLE_REVIEWS, NUMBER_FAVOURABLE_REVIEWS))

        numberOfNewestReviewsSubscriber.awaitTerminalEvent(5, TimeUnit.SECONDS)
        numberOfNewestReviewsSubscriber.assertNoTerminalEvent()
    }

    @Test
    fun moreReviewsFetchedOnSubsequentCalls() {
        Observable.concat(
                Observable.just(ReviewSort.LOWEST_RATING_FIRST, ReviewSort.LOWEST_RATING_FIRST, ReviewSort.LOWEST_RATING_FIRST),
                Observable.never())
                .subscribe(vm.reviewsObserver)

        val recordedRequest1 = reviewServicesRule.server.takeRequest()
        assertEquals(getExpectedReviewRequestStr(0), recordedRequest1.path);
        val recordedRequest2 = reviewServicesRule.server.takeRequest()
        assertEquals(getExpectedReviewRequestStr(25), recordedRequest2.path);
        val recordedRequest3 = reviewServicesRule.server.takeRequest()
        assertEquals(getExpectedReviewRequestStr(50), recordedRequest3.path);
    }

    @Test
    fun reviewsSummary() {
        val testSubscriber = TestSubscriber<ReviewSummary>()
        vm.reviewsSummaryObservable.take(1).subscribe(testSubscriber)

        Observable.concat(
                Observable.just(ReviewSort.NEWEST_REVIEW_FIRST),
                Observable.never())
                .subscribe(vm.reviewsObserver)

        testSubscriber.awaitTerminalEvent(10, TimeUnit.SECONDS)
        testSubscriber.assertNoErrors()
        testSubscriber.assertCompleted()
    }

    private fun getExpectedReviewRequestStr(startParam: Int): String {
        return "/api/hotelreviews/hotel/%s?sortBy=RATINGASC&start=%d&items=25&locale=en_US".format(HOTEL_ID, startParam)
    }
}
