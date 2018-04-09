package com.expedia.bookings.test.robolectric

import android.content.Context
import com.expedia.bookings.data.hotels.HotelReviewsResponse
import com.expedia.bookings.data.hotels.ReviewSort
import com.expedia.bookings.services.ReviewsServices
import com.expedia.bookings.services.TestObserver
import com.expedia.bookings.testrule.ServicesRule
import com.expedia.vm.HotelReviewsAdapterViewModel
import com.expedia.vm.HotelReviewsPageViewModel
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RuntimeEnvironment
import kotlin.properties.Delegates
import kotlin.test.assertEquals

@RunWith(RobolectricRunner::class)
class HotelReviewsPageViewModelTest {

    var reviewServicesRule = ServicesRule(ReviewsServices::class.java)
        @Rule get

    private val context: Context = RuntimeEnvironment.application
    private var adapterViewModel: HotelReviewsAdapterViewModel by Delegates.notNull()
    private var newPageViewModel: HotelReviewsPageViewModel by Delegates.notNull()
    private var goodPageViewModel: HotelReviewsPageViewModel by Delegates.notNull()
    private var badPageViewModel: HotelReviewsPageViewModel by Delegates.notNull()
    private val newObserver = TestObserver<HotelReviewsPageViewModel.ReviewUpdate>()
    private val goodObserver = TestObserver<HotelReviewsPageViewModel.ReviewUpdate>()
    private val badObserver = TestObserver<HotelReviewsPageViewModel.ReviewUpdate>()

    private val emptyResponse = HotelReviewsResponse().apply {
        reviewDetails = HotelReviewsResponse.ReviewDetails().apply {
            reviewSummaryCollection = HotelReviewsResponse.ReviewSummaryCollection()
            reviewSummaryCollection.reviewSummary.add(HotelReviewsResponse.ReviewSummary())
            reviewCollection = HotelReviewsResponse.ReviewCollection()
            reviewCollection.review = listOf()
        }
    }

    private val responseOneReview = HotelReviewsResponse().apply {
        reviewDetails = HotelReviewsResponse.ReviewDetails().apply {
            reviewSummaryCollection = HotelReviewsResponse.ReviewSummaryCollection()
            reviewSummaryCollection.reviewSummary.add(HotelReviewsResponse.ReviewSummary())
            reviewCollection = HotelReviewsResponse.ReviewCollection()
            reviewCollection.review = listOf(HotelReviewsResponse.Review())
        }
    }

    @Before
    fun before() {
        adapterViewModel = HotelReviewsAdapterViewModel("26650", reviewServicesRule.services!!, "en_US")
        newPageViewModel = HotelReviewsPageViewModel(context, ReviewSort.NEWEST_REVIEW_FIRST, adapterViewModel)
        badPageViewModel = HotelReviewsPageViewModel(context, ReviewSort.LOWEST_RATING_FIRST, adapterViewModel)
        goodPageViewModel = HotelReviewsPageViewModel(context, ReviewSort.HIGHEST_RATING_FIRST, adapterViewModel)

        newPageViewModel.reviewsAddedSubject.subscribe(newObserver)
        badPageViewModel.reviewsAddedSubject.subscribe(badObserver)
        goodPageViewModel.reviewsAddedSubject.subscribe(goodObserver)
    }

    @Test
    fun testNoReviewsResponse() {
        adapterViewModel.reviewsObservable.onNext(Pair(ReviewSort.NEWEST_REVIEW_FIRST, emptyResponse))
        newObserver.assertValues(HotelReviewsPageViewModel.ReviewUpdate(emptyResponse.reviewDetails.reviewCollection.review, false, false))
    }

    @Test
    fun testHasReviews() {
        adapterViewModel.reviewsObservable.onNext(Pair(ReviewSort.NEWEST_REVIEW_FIRST, responseOneReview))

        adapterViewModel.reviewsObservable.onNext(Pair(ReviewSort.NEWEST_REVIEW_FIRST, emptyResponse))

        newObserver.assertValues(HotelReviewsPageViewModel.ReviewUpdate(responseOneReview.reviewDetails.reviewCollection.review, true, false),
                HotelReviewsPageViewModel.ReviewUpdate(emptyResponse.reviewDetails.reviewCollection.review, true, false))
    }

    @Test
    fun testNewReviewsFiltering() {
        adapterViewModel.reviewsObserver.onNext(ReviewSort.NEWEST_REVIEW_FIRST)

        newObserver.assertValueCount(1)
        assertEquals(newObserver.values()[0].newReviews.size, 20)
        goodObserver.assertNoValues()
        badObserver.assertNoValues()
    }

    @Test
    fun testGoodReviewsFiltering() {
        adapterViewModel.reviewsObserver.onNext(ReviewSort.HIGHEST_RATING_FIRST)

        newObserver.assertNoValues()
        goodObserver.assertValueCount(1)
        assertEquals(goodObserver.values()[0].newReviews.size, 9)
        badObserver.assertNoValues()
    }

    @Test
    fun testBadReviewsFiltering() {
        adapterViewModel.reviewsObserver.onNext(ReviewSort.LOWEST_RATING_FIRST)

        newObserver.assertNoValues()
        goodObserver.assertNoValues()
        badObserver.assertValueCount(1)
        assertEquals(badObserver.values()[0].newReviews.size, 11)
    }

    @Test
    fun testStartDownloads() {
        adapterViewModel.startDownloads()
        newObserver.assertValueCount(1)
        goodObserver.assertValueCount(1)
        badObserver.assertValueCount(1)
    }
}
