package com.expedia.bookings.widget

import android.content.Context
import android.text.TextUtils
import android.view.View
import android.widget.LinearLayout
import android.widget.RatingBar
import com.expedia.bookings.data.Review
import com.expedia.bookings.featureconfig.ProductFlavorFeatureConfiguration
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.R
import com.expedia.util.subscribe
import com.expedia.vm.HotelReviewRowViewModel

public class HotelReviewRowView(context: Context, vm: HotelReviewRowViewModel) : LinearLayout(context) {

    val title: TextView by bindView(R.id.review_title)
    val ratingBar: RatingBar by bindView(R.id.user_rating_bar)
    val content: TextView by bindView(R.id.content)
    val reviewer: TextView by bindView(R.id.reviewer)
    val date: TextView by bindView(R.id.date)

    init {
        View.inflate(getContext(), R.layout.hotel_review_row, this)
        vm.titleTextObservable.subscribe(title)
        vm.ratingObservable.subscribe(ratingBar)
        vm.reviewerTextObservable.subscribe(reviewer)
        vm.reviewBodyObservable.subscribe(content)
        vm.submissionDateObservable.subscribe(date)
    }


}




