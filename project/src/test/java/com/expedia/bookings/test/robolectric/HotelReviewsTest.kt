package com.expedia.bookings.test

import com.expedia.bookings.data.ReviewSort
import com.expedia.bookings.data.hotels.HotelReviewsResponse.ReviewSummary
import com.expedia.bookings.data.hotels.HotelReviewsResponse.Review
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.utils.Ui
import com.expedia.vm.HotelReviewsAdapterViewModel
import org.joda.time.LocalDate
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RuntimeEnvironment
import rx.observers.TestSubscriber
import java.util.concurrent.TimeUnit
import kotlin.properties.Delegates
import kotlin.test.assertEquals

RunWith(RobolectricRunner::class)
public class HotelReviewsTest {
    public var service: ReviewsServicesRule = ReviewsServicesRule()
        @Rule get

    private val REVIEWS_PER_PAGE: Int = 25

    public var vm: HotelReviewsAdapterViewModel by Delegates.notNull()

    private val LOTS_MORE: Long = 100

    @Before
    fun before() {
        val hotelId = "26650"
        vm = HotelReviewsAdapterViewModel(hotelId, service.reviewsServices())
    }

    @Test
    fun reviewsBySortParam() {
        val criticalReviewsSubscriber = TestSubscriber<List<Review>>()
        val newestReviewsSubscriber = TestSubscriber<List<Review>>()
        val favorableReviewsSubscriber = TestSubscriber<List<Review>>()

        vm.criticalReviewsObservable.subscribe(criticalReviewsSubscriber)
        vm.newestReviewsObservable.subscribe(newestReviewsSubscriber)
        vm.favorableReviewsObservable.subscribe(favorableReviewsSubscriber)

        // Fetch reviews by lowest rating first.
        vm.reviewsObserver.onNext(ReviewSort.LOWEST_RATING_FIRST)
        // Fetch reviews by highest rating twice.
        vm.reviewsObserver.onNext(ReviewSort.HIGHEST_RATING_FIRST)
        vm.reviewsObserver.onNext(ReviewSort.HIGHEST_RATING_FIRST)

        criticalReviewsSubscriber.awaitTerminalEvent(10, TimeUnit.SECONDS)
        criticalReviewsSubscriber.requestMore(LOTS_MORE)
        criticalReviewsSubscriber.assertValueCount(1)
        assertEquals(REVIEWS_PER_PAGE, criticalReviewsSubscriber.getOnNextEvents().get(0).count())

        favorableReviewsSubscriber.requestMore(LOTS_MORE)
        favorableReviewsSubscriber.assertValueCount(2)
        assertEquals(REVIEWS_PER_PAGE * 2, favorableReviewsSubscriber.getOnNextEvents().get(0).count()
                + favorableReviewsSubscriber.getOnNextEvents().get(1).count())

        newestReviewsSubscriber.requestMore(LOTS_MORE)
        newestReviewsSubscriber.assertValueCount(0)

    }

    @Test
    fun reviewsSummary() {
        val testSubscriber = TestSubscriber<ReviewSummary>()

        vm.reviewsSummaryObservable.subscribe(testSubscriber)
        vm.reviewsObserver.onNext(ReviewSort.NEWEST_REVIEW_FIRST)

        testSubscriber.awaitTerminalEvent(10, TimeUnit.SECONDS)
        testSubscriber.requestMore(LOTS_MORE)
        testSubscriber.assertValueCount(1)
    }


}
