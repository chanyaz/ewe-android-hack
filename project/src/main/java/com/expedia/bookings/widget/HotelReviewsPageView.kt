package com.expedia.bookings.widget

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.Context
import android.view.View
import android.widget.LinearLayout
import com.expedia.bookings.R
import com.expedia.bookings.data.abacus.AbacusUtils
import com.expedia.bookings.data.hotels.ReviewSort
import com.expedia.bookings.featureconfig.AbacusFeatureConfigManager
import com.expedia.bookings.hotel.widget.HotelReviewsRecyclerView
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.hotel.widget.HotelReviewsRecyclerView.HotelReviewsRecyclerAdapter
import com.expedia.vm.HotelReviewsAdapterViewModel
import com.expedia.vm.HotelReviewsPageViewModel

class HotelReviewsPageView(context: Context, reviewSort: ReviewSort, viewModel: HotelReviewsAdapterViewModel) : LinearLayout(context) {

    private val pageViewModel = HotelReviewsPageViewModel(context, reviewSort, viewModel)
    private val messageProgressLoading: MessageProgressView by bindView(R.id.message_progress_loading)
    private val recyclerView: HotelReviewsRecyclerView by bindView(android.R.id.list)
    private val recyclerAdapter: HotelReviewsRecyclerAdapter = HotelReviewsRecyclerAdapter(AbacusFeatureConfigManager.isBucketedForTest(context, AbacusUtils.HotelUGCReviewsBoxRatingDesign), viewModel)
    private val animation: ObjectAnimator

    init {
        tag = reviewSort
        View.inflate(getContext(), R.layout.hotel_reviews_page_widget, this)
        recyclerView.adapter = recyclerAdapter

        pageViewModel.reviewsAddedSubject.subscribe(this::reviewsAdded)
        recyclerAdapter.loadMoreObservable.subscribe(pageViewModel.loadMoreSubject)

        animation = ObjectAnimator.ofFloat(messageProgressLoading, "progress", 0f, 1f)
        animation.repeatMode = ValueAnimator.RESTART
        animation.repeatCount = ValueAnimator.INFINITE
        animation.duration = 1500
        animation.start()
    }

    private fun reviewsAdded(update: HotelReviewsPageViewModel.ReviewUpdate) {
        recyclerView.visibility = if (update.hasReviews) View.VISIBLE else View.GONE
        messageProgressLoading.visibility = if (update.hasReviews) View.GONE else View.VISIBLE
        recyclerAdapter.moreReviewsAvailable = update.moreReviews
        recyclerAdapter.addReviews(update.newReviews)
        cancelAnimation()
    }

    private fun cancelAnimation() {
        animation.cancel()
        val anim = ObjectAnimator.ofFloat(messageProgressLoading, "progress", 1f)
        anim.duration = (1500 * (1f - messageProgressLoading.progress)).toLong()
        anim.start()
    }
}
