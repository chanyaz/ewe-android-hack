package com.expedia.bookings.widget

import android.content.Context
import android.support.v4.content.ContextCompat
import android.view.View
import android.widget.LinearLayout
import com.expedia.bookings.R
import com.expedia.bookings.extensions.setTextAndVisibility
import com.expedia.bookings.utils.bindView
import com.expedia.vm.HotelReviewsSummaryBoxRatingViewModel

class HotelReviewsSummaryBoxRatingWidget(context: Context) : LinearLayout(context) {

    val guestRatingTextView: TextView by bindView(R.id.guest_rating_text_view)
    val guestRatingRecommendationTextView: TextView by bindView(R.id.guest_rating_recommendation_text_view)
    val numberOfReviewsTextView: TextView by bindView(R.id.number_of_reviews_text_view)

    val roomCleanlinessReviewSummaryBoxRating: ReviewSummaryBoxRating by bindView(R.id.room_cleanliness_review_summary_box_rating)
    val roomComfortReviewSummaryBoxRating: ReviewSummaryBoxRating by bindView(R.id.room_comfort_review_summary_box_rating)
    val serviceStaffReviewSummaryBoxRating: ReviewSummaryBoxRating by bindView(R.id.service_staff_review_summary_box_rating)
    val hotelConditionReviewSummaryBoxRating: ReviewSummaryBoxRating by bindView(R.id.hotel_condition_review_summary_box_rating)

    init {
        View.inflate(context, R.layout.hotel_reviews_summary_box_rating_widget, this)
        orientation = LinearLayout.VERTICAL
        layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
        setBackgroundColor(ContextCompat.getColor(context, R.color.hotel_reviews_summary_background_color))
    }

    fun bindData(vm: HotelReviewsSummaryBoxRatingViewModel) {
        guestRatingTextView.text = vm.guestRatingString
        guestRatingRecommendationTextView.text = vm.guestRatingRecommendationString
        numberOfReviewsTextView.setTextAndVisibility(vm.numberOfReviewsString)

        roomCleanlinessReviewSummaryBoxRating.update(vm.roomCleanlinessReviewSummary.description, vm.roomCleanlinessReviewSummary.rating)
        roomComfortReviewSummaryBoxRating.update(vm.roomComfortReviewSummary.description, vm.roomComfortReviewSummary.rating)
        serviceStaffReviewSummaryBoxRating.update(vm.serviceStaffReviewSummary.description, vm.serviceStaffReviewSummary.rating)
        hotelConditionReviewSummaryBoxRating.update(vm.hotelConditionReviewSummary.description, vm.hotelConditionReviewSummary.rating)
    }
}
