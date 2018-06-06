package com.expedia.bookings.hotel.vm

import com.expedia.bookings.data.hotels.HotelReviewsResponse
import com.expedia.bookings.services.TestObserver
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.vm.HotelReviewsPageViewModel
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@RunWith(RobolectricRunner::class)
class HotelReviewsPageViewModelTest {

    private val viewModel = HotelReviewsPageViewModel()

    @Test
    fun testReviewsObserver() {
        val moreReviewsAvailableTest = TestObserver<Boolean>()
        val reviewsListObservable = TestObserver<Boolean>()
        val messageProgressLoadingObservable = TestObserver<Boolean>()
        val messageProgressLoadingAnimationObservable = TestObserver<Unit>()

        viewModel.moreReviewsAvailableObservable.subscribe(moreReviewsAvailableTest)
        viewModel.reviewsListObservable.subscribe(reviewsListObservable)
        viewModel.messageProgressLoadingObservable.subscribe(messageProgressLoadingObservable)
        viewModel.messageProgressLoadingAnimationObservable.subscribe(messageProgressLoadingAnimationObservable)

        assertFalse(viewModel.hasReviews)
        viewModel.reviewsObserver.onNext(createReviews(0))
        assertFalse(viewModel.hasReviews)
        viewModel.reviewsObserver.onNext(createReviews(25))
        assertTrue(viewModel.hasReviews)
        viewModel.reviewsObserver.onNext(createReviews(10))
        assertTrue(viewModel.hasReviews)

        moreReviewsAvailableTest.assertValues(true, false, true, false)
        reviewsListObservable.assertValues(false, true, true)
        messageProgressLoadingObservable.assertValues(true, false, false)
        messageProgressLoadingAnimationObservable.assertValueCount(3)
    }

    private fun createReviews(count: Int): List<HotelReviewsResponse.Review> {
        return List(count) { HotelReviewsResponse.Review() }
    }
}
