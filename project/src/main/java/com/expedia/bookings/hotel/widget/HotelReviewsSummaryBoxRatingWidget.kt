package com.expedia.bookings.hotel.widget

import android.content.Context
import android.support.v4.content.ContextCompat
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import com.expedia.bookings.R
import com.expedia.bookings.extensions.subscribeText
import com.expedia.bookings.hotel.vm.HotelReviewsSummaryBoxRatingViewModel
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.widget.TextView

class HotelReviewsSummaryBoxRatingWidget(context: Context, attrs: AttributeSet?) : LinearLayout(context, attrs) {
    val viewModel: HotelReviewsSummaryBoxRatingViewModel

    private val guestRatingTextView: TextView by bindView(R.id.guest_rating_text_view)
    private val guestRatingRecommendationTextView: TextView by bindView(R.id.guest_rating_recommendation_text_view)
    private val reviewRatingBarView: HotelReviewsBarRatingView by bindView(R.id.hotel_reviews_bar_rating_view)

    init {
        View.inflate(context, R.layout.hotel_reviews_summary_box_rating_widget, this)
        orientation = LinearLayout.VERTICAL
        layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
        setBackgroundColor(ContextCompat.getColor(context, R.color.hotel_reviews_summary_background_color))

        viewModel = HotelReviewsSummaryBoxRatingViewModel(context)
        initViewModelSubscriptions()
        reviewRatingBarView.viewModel = viewModel
    }

    private fun initViewModelSubscriptions() {
        viewModel.guestRatingObservable.subscribeText(guestRatingTextView)
        viewModel.guestRatingRecommendationObservable.subscribeText(guestRatingRecommendationTextView)
    }
}
