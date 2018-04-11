package com.expedia.bookings.hotel.widget

import android.content.Context
import android.view.View
import android.widget.LinearLayout
import android.widget.RatingBar
import com.expedia.bookings.R
import com.expedia.bookings.data.abacus.AbacusUtils
import com.expedia.bookings.extensions.subscribeOnClick
import com.expedia.bookings.extensions.subscribeTextAndVisibility
import com.expedia.bookings.featureconfig.AbacusFeatureConfigManager
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.widget.TextView
import com.expedia.vm.HotelReviewRowViewModel
import com.squareup.phrase.Phrase

class HotelReviewRowView(context: Context) : LinearLayout(context) {

    val title: TextView by bindView(R.id.review_title)
    val ratingBar: RatingBar by bindView(R.id.user_rating_bar)
    val userReviewRating: TextView by bindView(R.id.user_review_rating_text)
    val userReviewRatingOutOfFive: TextView by bindView(R.id.user_review_rating_out_of_five)
    val content: TextView by bindView(R.id.content)
    val reviewer: TextView by bindView(R.id.reviewer)
    val date: TextView by bindView(R.id.date)
    private val translateButton: TextView by bindView(R.id.hotel_review_row_translate)

    init {
        View.inflate(getContext(), R.layout.hotel_review_row, this)
        if (isHotelUGCReviewsBoxRatingDesignEnabled()) {
            userReviewRating.visibility = View.VISIBLE
            userReviewRatingOutOfFive.visibility = View.VISIBLE
        } else {
            ratingBar.visibility = View.VISIBLE
        }
    }

    fun bindData(vm: HotelReviewRowViewModel) {
        vm.titleTextObservable.subscribeTextAndVisibility(title)
        vm.ratingObservable.subscribe {
            ratingBar.rating = it
            setRatingBarContentDescription(it.toInt())
        }
        vm.userReviewRatingTextObservable.subscribe {
            userReviewRating.text = it.toString()
            setRatingBarContentDescription(it.toInt())
        }
        vm.reviewerTextObservable.subscribeTextAndVisibility(reviewer)
        vm.reviewBodyObservable.subscribeTextAndVisibility(content)
        vm.submissionDateObservable.subscribeTextAndVisibility(date)
        vm.translateButtonTextObservable.subscribeTextAndVisibility(translateButton)
        translateButton.subscribeOnClick(vm.onTranslateClick)
    }

    private fun setRatingBarContentDescription(rating: Int) {
        val reviewContentDescription = Phrase.from(context, R.string.hotel_rating_bar_cont_desc_TEMPLATE)
                .put("rating", rating)
                .format().toString()

        if (isHotelUGCReviewsBoxRatingDesignEnabled()) {
            ratingBar.contentDescription = reviewContentDescription
        } else {
            userReviewRating.contentDescription = reviewContentDescription
        }
    }

    private fun isHotelUGCReviewsBoxRatingDesignEnabled(): Boolean {
        return AbacusFeatureConfigManager.isBucketedForTest(context, AbacusUtils.HotelUGCReviewsBoxRatingDesign)
    }
}
