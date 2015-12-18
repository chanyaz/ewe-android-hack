package com.expedia.bookings.widget

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.Context
import android.view.View
import android.widget.LinearLayout
import com.expedia.bookings.R
import com.expedia.bookings.activity.ExpediaBookingApp
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.widget.HotelReviewsRecyclerView.HotelReviewsRecyclerAdapter
import com.expedia.util.notNullAndObservable
import com.expedia.util.subscribeVisibility
import com.expedia.vm.HotelReviewsPageViewModel

public class HotelReviewsPageView(context: Context) : LinearLayout(context) {

    val messageProgressLoading: MessageProgressView by bindView(R.id.message_progress_loading)
    val recyclerView: HotelReviewsRecyclerView by bindView(android.R.id.list)
    val recyclerAdapter: HotelReviewsRecyclerAdapter = HotelReviewsRecyclerAdapter()
    val animation: ObjectAnimator

    init {
        View.inflate(getContext(), R.layout.hotel_reviews_page_widget, this)
        recyclerView.adapter = recyclerAdapter

        animation = ObjectAnimator.ofFloat(messageProgressLoading, "progress", 0f, 1f)
        animation.repeatMode = ValueAnimator.RESTART
        animation.repeatCount = ValueAnimator.INFINITE
        animation.setDuration(1500)
        animation.start()
    }

    var viewModel: HotelReviewsPageViewModel by notNullAndObservable { vm ->
        vm.reviewsListObservable.subscribeVisibility(recyclerView)
        vm.messageProgressLoadingObservable.subscribeVisibility(messageProgressLoading)
        vm.messageProgressLoadingAnimationObservable.subscribe {
            animation.cancel()
            val anim = ObjectAnimator.ofFloat(messageProgressLoading, "progress", 1f)
            anim.setDuration((1500 * (1f - messageProgressLoading.progress)).toLong())
            anim.start()
        }
        if (ExpediaBookingApp.isAutomation()) {
            recyclerAdapter.moreReviewsAvailable = false
        } else {
            vm.moreReviewsAvailableObservable.subscribe { moreReviewsAvailable ->
                recyclerAdapter.moreReviewsAvailable = moreReviewsAvailable
            }
        }
    }
}
