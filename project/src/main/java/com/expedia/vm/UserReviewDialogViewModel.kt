package com.expedia.vm

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.expedia.bookings.BuildConfig
import com.expedia.bookings.R
import com.expedia.bookings.tracking.OmnitureTracking
import com.mobiata.android.util.SettingUtils
import org.joda.time.DateTime
import rx.subjects.PublishSubject

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
            feedbackLinkSubject.onNext(scheme +"://supportEmail")
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

    private fun startIntent(link: String) {
        val intent = Intent(Intent.ACTION_VIEW)
        intent.data = Uri.parse(link)
        context.startActivity(intent)
        SettingUtils.save(context, R.string.preference_user_has_seen_review_prompt, true)
        SettingUtils.save(context, R.string.preference_date_last_review_prompt_shown, DateTime.now().millis)
    }
}
