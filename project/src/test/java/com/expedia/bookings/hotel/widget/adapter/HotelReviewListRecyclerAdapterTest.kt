package com.expedia.bookings.hotel.widget.adapter

import android.widget.LinearLayout
import com.expedia.bookings.data.hotels.HotelReviewsResponse
import com.expedia.bookings.data.hotels.ReviewSummary
import com.expedia.bookings.hotel.data.TranslatedReview
import com.expedia.bookings.hotel.widget.HotelReviewRowView
import com.expedia.bookings.hotel.widget.HotelReviewsRecyclerView
import com.expedia.bookings.hotel.widget.HotelReviewsSummaryBoxRatingWidget
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.widget.HotelReviewsSummaryWidget
import io.reactivex.observers.TestObserver
import org.joda.time.DateTime
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RuntimeEnvironment
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@RunWith(RobolectricRunner::class)
class HotelReviewListRecyclerAdapterTest {
    private val testAdapter = HotelReviewsRecyclerView.HotelReviewsRecyclerAdapter(false)
    private val testAdapterNewDesign = HotelReviewsRecyclerView.HotelReviewsRecyclerAdapter(true)
    private val testAdapterNoSummary = HotelReviewsRecyclerView.HotelReviewsRecyclerAdapter(false).apply {
        showSummary = false
    }
    private val context = RuntimeEnvironment.application

    @Test
    fun testLoadingCellLoadMore() {
        val testObserver = TestObserver<Unit>()
        testAdapter.addReviews(createReviews(1))
        val parent = LinearLayout(context)
        val viewHolder = testAdapter.onCreateViewHolder(parent, testAdapter.getItemViewType(2))
        testAdapter.bindViewHolder(viewHolder, 2)
        testAdapter.loadMoreObservable.subscribe(testObserver)

        testObserver.assertValueCount(1)
    }

    @Test
    fun testReviewCell() {
        testAdapter.addReviews(createReviews(1))
        val parent = LinearLayout(context)
        val viewHolder = testAdapter.onCreateViewHolder(parent, testAdapter.getItemViewType(1))
        testAdapter.bindViewHolder(viewHolder, 1)
        val rowView = viewHolder.itemView as HotelReviewRowView

        assertEquals(rowView.title.text, "test")
    }

    @Test
    fun testReviewWithTranslation() {
        val translatedReview = TranslatedReview(createGenericReview(), true)
        translatedReview.review.title = "test translated"

        testAdapter.translationMap = hashMapOf("123" to translatedReview)
        testAdapter.addReviews(createReviews(1))
        val parent = LinearLayout(context)
        val viewHolder = testAdapter.onCreateViewHolder(parent, testAdapter.getItemViewType(1))
        testAdapter.bindViewHolder(viewHolder, 1)
        val rowView = viewHolder.itemView as HotelReviewRowView

        assertEquals(rowView.title.text, "test translated")

        val testObserver = TestObserver<String>()
        testAdapter.toggleReviewTranslationSubject.subscribe(testObserver)
        rowView.translateButton.performClick()
        testObserver.assertValue("123")
    }

    @Test
    fun testSummary() {
        testAdapter.addReviews(createReviews(1))
        testAdapter.updateSummary(ReviewSummary().apply {
            avgOverallRating = 5.0f
        })
        val parent = LinearLayout(context)
        val viewHolder = testAdapter.onCreateViewHolder(parent, testAdapter.getItemViewType(0))
        testAdapter.bindViewHolder(viewHolder, 0)
        val summaryView = viewHolder.itemView as HotelReviewsSummaryWidget

        assertEquals(summaryView.overallRating.text, "5.0")
    }

    @Test
    fun testSummaryNewDesign() {
        testAdapterNewDesign.addReviews(createReviews(1))
        val parent = LinearLayout(context)
        val viewHolder = testAdapterNewDesign.onCreateViewHolder(parent, testAdapterNewDesign.getItemViewType(0))
        val summaryView = viewHolder.itemView as HotelReviewsSummaryBoxRatingWidget
        val testObserver = TestObserver<String>()
        summaryView.viewModel.guestRatingObservable.subscribe(testObserver)
        testAdapterNewDesign.updateSummary(ReviewSummary().apply { avgOverallRating = 5.0f })
        testAdapterNewDesign.bindViewHolder(viewHolder, 0)

        testObserver.assertValue("5.0/5")
    }

    @Test
    fun testClear() {
        testAdapter.addReviews(createReviews(1))
        assertEquals(testAdapter.itemCount, 2)
        testAdapter.clearReviews()
        assertEquals(testAdapter.itemCount, 1)
    }

    @Test
    fun testMoreReviewsAvailable() {
        testAdapter.moreReviewsAvailable = true
        assertEquals(testAdapter.itemCount, 2)
        testAdapter.moreReviewsAvailable = false
        assertEquals(testAdapter.itemCount, 1)
    }

    @Test
    fun testNoSummary() {
        testAdapterNoSummary.addReviews(createReviews(1))
        val viewType = testAdapterNoSummary.getItemViewType(0)
        val parent = LinearLayout(context)
        assertTrue(testAdapterNoSummary.onCreateViewHolder(parent, viewType).itemView is HotelReviewRowView)
        assertEquals(testAdapter.itemCount, 1)
    }

    private fun createReviews(count: Int): List<HotelReviewsResponse.Review> {
        return List(count) { createGenericReview() }
    }

    private fun createGenericReview(): HotelReviewsResponse.Review {
        return HotelReviewsResponse.Review().apply {
            title = "test"
            reviewSubmissionTime = DateTime.now()
            reviewText = "body"
            contentLocale = "zz_ZZ"
            reviewId = "123"
        }
    }
}
