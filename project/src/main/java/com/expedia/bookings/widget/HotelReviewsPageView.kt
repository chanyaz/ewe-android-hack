package com.expedia.bookings.widget

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.Context
import android.view.View
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TableLayout
import com.expedia.bookings.R
import com.expedia.bookings.activity.ExpediaBookingApp
import com.expedia.bookings.utils.bindView
import com.expedia.util.notNullAndObservable
import com.expedia.util.subscribeVisibility
import com.expedia.vm.HotelReviewsPageViewModel

public class HotelReviewsPageView(context: Context) : LinearLayout(context) {

    val reviewsTable: TableLayout by bindView(R.id.reviews_table)
    val summaryContainer: LinearLayout by bindView(R.id.summary_container)
    val messageProgressLoading: MessageProgressView by bindView(R.id.message_progress_loading)
    val reviewsScrollviewContainer: ScrollView by bindView(R.id.reviews_scrollview_container)
    val bottomProgressBar: ProgressBar by bindView(R.id.more_reviews_progressbar)
    val animation: ObjectAnimator

    init {
        View.inflate(getContext(), R.layout.hotel_reviews_page_widget, this)

        animation = ObjectAnimator.ofFloat(messageProgressLoading, "progress", 0f, 1f)
        animation.repeatMode = ValueAnimator.RESTART
        animation.repeatCount = ValueAnimator.INFINITE
        animation.setDuration(1500)
        animation.start()
    }

    var viewModel: HotelReviewsPageViewModel by notNullAndObservable { vm ->
        vm.reviewsScrollviewContainerObservable.subscribeVisibility(reviewsScrollviewContainer)
        vm.messageProgressLoadingObservable.subscribeVisibility(messageProgressLoading)
        vm.messageProgressLoadingAnimationObservable.subscribe {
            animation.cancel()
            val anim = ObjectAnimator.ofFloat(messageProgressLoading, "progress", 1f)
            anim.setDuration((1500 * (1f - messageProgressLoading.progress)).toLong())
            anim.start()
        }
        if (ExpediaBookingApp.isAutomation()) {
            bottomProgressBar.visibility = View.GONE
        } else {
            vm.moreReviewsAvailableObservable.subscribeVisibility(bottomProgressBar)
        }
    }
}
