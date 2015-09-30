package com.expedia.bookings.test

import com.expedia.bookings.data.ReviewSort
import com.expedia.bookings.data.hotels.HotelReviewsResponse.ReviewSummary
import com.expedia.vm.HotelReviewsAdapterViewModel
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import rx.Observable
import rx.observers.TestSubscriber
import java.util.concurrent.TimeUnit
import kotlin.properties.Delegates

public class HotelReviewsTest {

    public val service: ReviewsServicesRule = ReviewsServicesRule()
        @Rule get

    private val NUMBER_FAVOURABLE_REVIEWS: Int = 21
    private val NUMBER_CRITICAL_REVIEWS: Int = 4

    public var vm: HotelReviewsAdapterViewModel by Delegates.notNull()

    @Before
    fun before() {
        vm = HotelReviewsAdapterViewModel("26650", service.reviewsServices(), "en_US")
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
}
