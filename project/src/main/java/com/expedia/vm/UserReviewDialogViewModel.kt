package com.expedia.vm

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.expedia.bookings.BuildConfig
import com.expedia.bookings.R
import com.expedia.bookings.featureconfig.ProductFlavorFeatureConfiguration
import com.expedia.bookings.tracking.OmnitureTracking
import com.mobiata.android.util.SettingUtils
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import org.joda.time.DateTime
import org.joda.time.Period
import org.joda.time.PeriodType

class UserReviewDialogViewModel(val context: Context) {
    val reviewSubject = PublishSubject.create<Unit>()
    val reviewLinkSubject = PublishSubject.create<String>()
    val feedbackSubject = PublishSubject.create<Unit>()
    val feedbackLinkSubject = PublishSubject.create<String>()
    val noSubject = PublishSubject.create<Unit>()
    val closeSubject = PublishSubject.create<Unit>()

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

        Observable.merge(reviewSubject, feedbackSubject, noSubject).subscribe(closeSubject)
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
}
