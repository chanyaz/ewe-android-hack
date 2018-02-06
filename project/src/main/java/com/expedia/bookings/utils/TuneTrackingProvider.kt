package com.expedia.bookings.utils

import android.app.Application
import android.content.Context
import android.content.Intent
import android.net.Uri
import com.expedia.bookings.ADMS_Measurement
import com.expedia.bookings.BuildConfig
import com.expedia.bookings.R
import com.expedia.bookings.data.user.User
import com.expedia.bookings.data.user.UserStateManager
import com.mobiata.android.Log
import com.mobiata.android.util.SettingUtils
import com.tune.Tune
import com.tune.TuneDeeplinkListener
import com.tune.TuneEvent

interface TuneTrackingProvider : TuneDeeplinkListener {
    val authenticatedUser: User?
    val tuid: String
    val membershipTier: String?
    val isUserLoggedInValue: String
    var posData: String
    var facebookReferralUrlString: String
    fun trackEvent(event: TuneEvent)
}

class TuneTrackingProviderImpl(private val tune: Tune, app: Application, private val userStateManager: UserStateManager, shouldSetExistingUserForTune: Boolean = false) : TuneTrackingProvider {
    val context: Context = app.applicationContext

    override val authenticatedUser: User?
        get() = if (userStateManager.isUserAuthenticated()) userStateManager.userSource.user else null
    override val tuid: String
        get() = authenticatedUser?.tuidString ?: ""
    override val membershipTier: String?
        get() = userStateManager.getCurrentUserLoyaltyTier().toApiValue()
    override val isUserLoggedInValue: String
        get() = if (userStateManager.isUserAuthenticated()) "1" else "0"
    private val olderOrbitzVersionWasInstalled: Boolean
        get() = context.getSharedPreferences("loginPreferences", Context.MODE_PRIVATE).contains("anonId")
    override var posData: String
        get() = tune.twitterUserId
        set(value) { tune.twitterUserId = value }
    override var facebookReferralUrlString: String
        get() = tune.referralUrl
        set(value) { tune.referralUrl = value }

    init {
        if (shouldSetExistingUserForTune && olderOrbitzVersionWasInstalled) {
            tune.existingUser = true
        }

        tune.userId = ADMS_Measurement().visitorID
        tune.googleUserId = userStateManager.userSource.user?.expediaUserId ?: ""
        tune.setDebugMode(BuildConfig.DEBUG && SettingUtils.get(context, context.getString(R.string.preference_enable_tune), false))
        tune.registerDeeplinkListener(this)
    }

    override fun trackEvent(event: TuneEvent) {
        tune.measureEvent(event)
    }

    override fun didReceiveDeeplink(deeplink: String?) {
        Log.d("Deferred deeplink received: " + deeplink)

        if (!deeplink.isNullOrEmpty()) {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(deeplink))
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        }
    }

    override fun didFailDeeplink(error: String?) {
        Log.d("Deferred deeplink error: " + error)
    }
}
