package com.expedia.bookings.widget

import android.content.Context
import android.support.constraint.ConstraintLayout
import android.util.AttributeSet
import android.view.View
import com.expedia.bookings.R
import com.expedia.bookings.utils.bindView

class ReviewSummaryBoxRating(context: Context, attrs: AttributeSet) : ConstraintLayout(context, attrs) {
    val ratingSummaryDescriptionTextView: TextView by bindView(R.id.rating_summary_description_text_view)
    val ratingScoreBoxRatingBar: BoxRatingBar by bindView(R.id.rating_score_box_rating_bar)
    val ratingScoreTextView: TextView by bindView(R.id.rating_score_text_view)

    init {
        View.inflate(context, R.layout.review_summary_box_rating, this)
    }

    fun update(description: String, rating: Float) {
        ratingSummaryDescriptionTextView.text = description
        ratingScoreBoxRatingBar.rating = rating
        ratingScoreTextView.text = rating.toString()
    }
}
