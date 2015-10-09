package com.expedia.bookings.widget

import android.content.Context
import android.view.View
import android.widget.LinearLayout
import android.widget.RatingBar
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.R
import com.expedia.util.subscribeText
import com.expedia.util.subscribeRating
import com.expedia.vm.HotelReviewRowViewModel

public class HotelReviewRowView(context: Context, vm: HotelReviewRowViewModel) : LinearLayout(context) {

    val title: TextView by bindView(R.id.review_title)
    val ratingBar: RatingBar by bindView(R.id.user_rating_bar)
    val content: TextView by bindView(R.id.content)
    val reviewer: TextView by bindView(R.id.reviewer)
    val date: TextView by bindView(R.id.date)

    init {
        View.inflate(getContext(), R.layout.hotel_review_row, this)
        vm.titleTextObservable.subscribeText(title)
        vm.ratingObservable.subscribeRating(ratingBar)
        vm.reviewerTextObservable.subscribeText(reviewer)
        vm.reviewBodyObservable.subscribeText(content)
        vm.submissionDateObservable.subscribeText(date)
    }


}




