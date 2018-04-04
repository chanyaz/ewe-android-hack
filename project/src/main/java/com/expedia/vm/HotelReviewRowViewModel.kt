package com.expedia.vm

import android.content.Context
import android.support.annotation.VisibleForTesting
import android.text.TextUtils
import com.expedia.bookings.R
import com.expedia.bookings.data.abacus.AbacusUtils
import com.expedia.bookings.data.hotels.HotelReviewsResponse.Review
import com.expedia.bookings.featureconfig.AbacusFeatureConfigManager
import com.expedia.bookings.featureconfig.ProductFlavorFeatureConfiguration
import com.expedia.bookings.hotel.data.TranslatedReview
import com.expedia.bookings.text.HtmlCompat
import com.expedia.bookings.tracking.OmnitureTracking
import com.expedia.util.endlessObserver
import io.reactivex.subjects.BehaviorSubject
import java.util.Locale

class HotelReviewRowViewModel(val context: Context) {

    val titleTextObservable = BehaviorSubject.create<String>()
    val reviewerTextObservable = BehaviorSubject.create<String>()
    val ratingObservable = BehaviorSubject.create<Float>()
    val submissionDateObservable = BehaviorSubject.create<String>()
    val reviewBodyObservable = BehaviorSubject.create<String>()
    val translateButtonTextObservable = BehaviorSubject.create<String>()
    val toggleReviewTranslationObservable = BehaviorSubject.create<String>()
    val onTranslateClick = endlessObserver<Unit> {
        review?.let { review ->
            OmnitureTracking.trackHotelReviewTranslate(showingTranslated)
            translateButtonTextObservable.onNext(context.getString(R.string.user_review_translation_loading))
            toggleReviewTranslationObservable.onNext(review.reviewId)
        }
    }

    val translatedReviewObserver = endlessObserver<TranslatedReview> { translatedReview ->
        if (translatedReview.showToUser) {
            showingTranslated = true
            updateViews(translatedReview.review)
        }
    }

    val reviewObserver = endlessObserver<Review> { review ->
        this.review = review
        showingTranslated = false
        updateViews(review)
    }

    private var showingTranslated = false
    private var review: Review? = null

    @VisibleForTesting
    fun reviewInDifferentLanguage(): Boolean {
        if (review != null && review!!.contentLocale.length >= 2) {
            val reviewLanguage = review!!.contentLocale.substring(0, 2)
            return Locale.getDefault().language != reviewLanguage
        }
        return false
    }

    @VisibleForTesting
    fun reviewHasText(): Boolean = review?.reviewText?.isNotEmpty() == true || review?.title?.isNotEmpty() == true

    private fun updateViews(review: Review) {
        titleTextObservable.onNext(HtmlCompat.fromHtml(review.title).toString())
        reviewerTextObservable.onNext(getReviewerText(review))
        ratingObservable.onNext(review.ratingOverall.toFloat())
        submissionDateObservable.onNext(getSubmissionDate(review))
        reviewBodyObservable.onNext(HtmlCompat.fromHtml(review.reviewText).toString())
        updateTranslationButton()
    }

    private fun getSubmissionDate(review: Review): String {
        val dateTime = review.reviewSubmissionTime
        val submissionDateText = ProductFlavorFeatureConfiguration.getInstance().formatDateTimeForHotelUserReviews(context, dateTime)
        return submissionDateText
    }

    private fun getReviewerText(review: Review): String {
        var nameAndLocationText = ""
        val name = review.userDisplayName
        val location = review.userLocation
        val hasName = !TextUtils.isEmpty(name)
        val hasLocation = !TextUtils.isEmpty(location)

        if (hasName && hasLocation) {
            nameAndLocationText = context.resources.getString(R.string.user_review_name_and_location_signature, name, location)
        } else if (!hasName && hasLocation) {
            nameAndLocationText = location
        } else if (hasName && !hasLocation) {
            nameAndLocationText = name
        }
        return nameAndLocationText
    }

    private fun updateTranslationButton() {
        if (reviewHasText() && reviewInDifferentLanguage() && AbacusFeatureConfigManager.isBucketedForTest(context, AbacusUtils.HotelUGCTranslations)) {
            if (showingTranslated) {
                translateButtonTextObservable.onNext(context.getString(R.string.user_review_see_original))
            } else {
                translateButtonTextObservable.onNext(context.getString(R.string.user_review_see_translation))
            }
        } else {
            translateButtonTextObservable.onNext("")
        }
    }
}
