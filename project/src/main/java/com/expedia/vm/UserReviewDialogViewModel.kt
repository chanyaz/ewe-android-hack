package com.expedia.vm

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.expedia.bookings.BuildConfig
import com.expedia.bookings.R
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.abacus.AbacusUtils
import com.expedia.bookings.featureconfig.ProductFlavorFeatureConfiguration
import com.expedia.bookings.tracking.OmnitureTracking
import com.mobiata.android.util.SettingUtils
import org.joda.time.DateTime
import org.joda.time.Period
import org.joda.time.PeriodType
import rx.subjects.PublishSubject

class UserReviewDialogViewModel(val context: Context) {
    val reviewSubject = PublishSubject.create<Unit>()
    val reviewLinkSubject = PublishSubject.create<String>()
    val feedbackSubject = PublishSubject.create<Unit>()
    val feedbackLinkSubject = PublishSubject.create<String>()
    val noSubject = PublishSubject.create<Unit>()
    val closeSubject = PublishSubject.create<Unit>()

    val titleTextSubject = PublishSubject.create<String>()
    val reviewTextSubject = PublishSubject.create<String>()
    val feedbackTextSubject = PublishSubject.create<String>()
    val closeTextSubject = PublishSubject.create<String>()

    init {
        reviewSubject.subscribe {
            val packageName = context.packageName
            reviewLinkSubject.onNext("market://details?id=" + packageName)
            OmnitureTracking.trackItinAppRatingClickReview()
        }
        feedbackSubject.subscribe {
            val scheme = BuildConfig.DEEPLINK_SCHEME
            feedbackLinkSubject.onNext(scheme + "://reviewFeedbackEmail")
            OmnitureTracking.trackItinAppRatingClickFeedback()
        }
        reviewLinkSubject.subscribe { link ->
            startIntent(link)
        }
        feedbackLinkSubject.subscribe { link ->
            startIntent(link)
        }
        noSubject.subscribe {
            SettingUtils.save(context, R.string.preference_user_has_seen_review_prompt, true)
            SettingUtils.save(context, R.string.preference_date_last_review_prompt_shown, DateTime.now().millis)
            OmnitureTracking.trackItinAppRatingClickNo()
        }

        rx.Observable.merge(reviewSubject, feedbackSubject, noSubject).subscribe(closeSubject)
    }

    fun bindText() {
        if (isBucketedForTest()) {
            titleTextSubject.onNext(context.getString(R.string.dialog_app_rating_title_alt))
            reviewTextSubject.onNext(context.getString(R.string.dialog_app_rating_review_button_alt))
            feedbackTextSubject.onNext(context.getString(R.string.dialog_app_rating_feedback_button_alt))
            closeTextSubject.onNext(context.getString(R.string.dialog_app_rating_no_button_alt))
        } else {
            titleTextSubject.onNext(context.getString(R.string.dialog_app_rating_title))
            reviewTextSubject.onNext(context.getString(R.string.dialog_app_rating_review_button))
            feedbackTextSubject.onNext(context.getString(R.string.dialog_app_rating_feedback_button))
            closeTextSubject.onNext(context.getString(R.string.dialog_app_rating_no_button))
        }
    }

    private fun startIntent(link: String) {
        val intent = Intent(Intent.ACTION_VIEW)
        intent.data = Uri.parse(link)
        context.startActivity(intent)
        SettingUtils.save(context, R.string.preference_user_has_seen_review_prompt, true)
        SettingUtils.save(context, R.string.preference_date_last_review_prompt_shown, DateTime.now().millis)
    }

    companion object {
        @JvmStatic
        fun shouldShowReviewDialog(context: Context): Boolean {
            val hasShownUserReview = SettingUtils.get(context, R.string.preference_user_has_seen_review_prompt, false)
            val hasBookedHotelOrFlight = SettingUtils.get(context, R.string.preference_user_has_booked_hotel_or_flight, false)
            val lastDate = DateTime(SettingUtils.get(context, R.string.preference_date_last_review_prompt_shown, DateTime.now().millis))
            val hasBeenAtLeast3Months = Period(lastDate, DateTime.now(), PeriodType.yearMonthDayTime()).months >= 3

            if (ProductFlavorFeatureConfiguration.getInstance().shouldShowUserReview() && (!hasShownUserReview || hasBeenAtLeast3Months) && hasBookedHotelOrFlight) {
                OmnitureTracking.trackItinUserRating()
                return true
            }
            return false
        }
    }

    private fun isBucketedForTest() : Boolean {
        return Db.getAbacusResponse().isUserBucketedForTest(AbacusUtils.EBAndroidAppTripsUserReviews)
    }
}
