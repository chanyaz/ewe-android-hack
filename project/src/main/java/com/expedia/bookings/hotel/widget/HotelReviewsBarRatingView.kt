package com.expedia.bookings.hotel.widget

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import com.expedia.bookings.R
import com.expedia.bookings.extensions.subscribeVisibility
import com.expedia.bookings.hotel.vm.HotelReviewsSummaryBoxRatingViewModel
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.widget.ReviewSummaryBoxRating
import com.expedia.util.notNullAndObservable

class HotelReviewsBarRatingView(context: Context, attributeSet: AttributeSet) : LinearLayout(context, attributeSet) {

    private val roomCleanlinessReviewSummaryBoxRating: ReviewSummaryBoxRating by bindView(R.id.room_cleanliness_review_summary_box_rating)
    private val roomComfortReviewSummaryBoxRating: ReviewSummaryBoxRating by bindView(R.id.room_comfort_review_summary_box_rating)
    private val serviceStaffReviewSummaryBoxRating: ReviewSummaryBoxRating by bindView(R.id.service_staff_review_summary_box_rating)
    private val hotelConditionReviewSummaryBoxRating: ReviewSummaryBoxRating by bindView(R.id.hotel_condition_review_summary_box_rating)

    var viewModel: HotelReviewsSummaryBoxRatingViewModel by notNullAndObservable { vm ->
        vm.barRatingViewVisibility.subscribeVisibility(this)
        vm.roomCleanlinessObservable.subscribe { summary -> roomCleanlinessReviewSummaryBoxRating.update(summary.description, summary.rating) }
        vm.roomComfortObservable.subscribe { summary -> roomComfortReviewSummaryBoxRating.update(summary.description, summary.rating) }
        vm.serviceStaffObservable.subscribe { summary -> serviceStaffReviewSummaryBoxRating.update(summary.description, summary.rating) }
        vm.hotelConditionObservable.subscribe { summary -> hotelConditionReviewSummaryBoxRating.update(summary.description, summary.rating) }
    }

    init {
        View.inflate(context, R.layout.hotel_reviews_bar_rating_view, this)
    }
}
