package com.expedia.bookings.hotel.widget

import android.content.Context
import android.view.View
import android.widget.LinearLayout
import android.widget.RatingBar
import com.expedia.bookings.R
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.widget.TextView
import com.expedia.util.subscribeTextAndVisibility
import com.expedia.vm.HotelReviewRowViewModel
import com.squareup.phrase.Phrase

class HotelReviewRowView(context: Context) : LinearLayout(context) {

    val title: TextView by bindView(R.id.review_title)
    val ratingBar: RatingBar by bindView(R.id.user_rating_bar)
    val content: TextView by bindView(R.id.content)
    val reviewer: TextView by bindView(R.id.reviewer)
    val date: TextView by bindView(R.id.date)

    init {
        View.inflate(getContext(), R.layout.hotel_review_row, this)
    }

    fun bindData(vm: HotelReviewRowViewModel) {
        vm.titleTextObservable.subscribeTextAndVisibility(title)
        vm.ratingObservable.subscribe {
            ratingBar.rating = it
            setRatingBarContentDescription(it.toInt())
        }
        vm.reviewerTextObservable.subscribeTextAndVisibility(reviewer)
        vm.reviewBodyObservable.subscribeTextAndVisibility(content)
        vm.submissionDateObservable.subscribeTextAndVisibility(date)
    }

    private fun setRatingBarContentDescription(rating: Int) {
        ratingBar.contentDescription = Phrase.from(context, R.string.hotel_rating_bar_cont_desc_TEMPLATE)
                .put("rating", rating)
                .format().toString()
    }
}

