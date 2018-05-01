package com.expedia.bookings.hotel.vm

import com.expedia.bookings.data.hotels.ReviewSummary
import com.expedia.bookings.services.ReviewsServices
import com.expedia.bookings.services.TestObserver
import com.expedia.bookings.testrule.ServicesRule
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.util.concurrent.TimeUnit

class HotelReviewsSummaryViewModelTest {
    var reviewsServicesRule = ServicesRule(ReviewsServices::class.java)
        @Rule get
    private lateinit var viewModel: HotelReviewsSummaryViewModel

    @Before
    fun setUp() {
        viewModel = HotelReviewsSummaryViewModel(reviewsServicesRule.services!!)
    }

    @Test
    fun testHappyFetchReviewSummary() {
        val testSuccessSub = TestObserver<ReviewSummary>()
        viewModel.reviewSummarySubject.subscribe(testSuccessSub)

        viewModel.fetchReviewsSummary("happy-summaries")

        testSuccessSub.awaitValueCount(1, 1, TimeUnit.SECONDS)
        testSuccessSub.assertNotTerminated()
        testSuccessSub.assertNoErrors()
        testSuccessSub.assertValueCount(1)
    }

    @Test
    fun testUnhappyFetchReviewSummary() {
        val testNoReviewSub = TestObserver<Unit>()
        viewModel.noReviewSummarySubject.subscribe(testNoReviewSub)

        viewModel.fetchReviewsSummary("unhappy-summaries")

        testNoReviewSub.awaitValueCount(1, 1, TimeUnit.SECONDS)
        testNoReviewSub.assertNotTerminated()
        testNoReviewSub.assertNoErrors()
        testNoReviewSub.assertValueCount(1)
    }
}
