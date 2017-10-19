package com.expedia.bookings.utils

import com.tune.Tune
import com.tune.TuneDeeplinkListener
import com.tune.TuneDeeplinker
import com.tune.TuneEvent

class MockTune: Tune() {
    val deepLinker = TuneDeeplinker("advertiser_id", "conversion_key", "package_name")
    private var userId = ""
    private var googleUserId = ""
    private var existingUser = false
    private var twitterUserId = ""
    private var referralUrl = ""

    override fun setUserId(userId: String?) {
        this.userId = userId ?: ""
    }

    override fun getUserId(): String = userId

    override fun setGoogleUserId(userId: String?) {
        googleUserId = userId ?: ""
    }

    override fun getGoogleUserId(): String = googleUserId

    override fun setTwitterUserId(userId: String?) {
        twitterUserId = userId ?: ""
    }

    override fun getTwitterUserId(): String = twitterUserId

    override fun setReferralUrl(url: String?) {
        referralUrl = url ?: ""
    }

    override fun getReferralUrl(): String = referralUrl

    override fun setDebugMode(debug: Boolean) {}

    override fun setExistingUser(existing: Boolean) {
        existingUser = existing
    }

    override fun getExistingUser(): Boolean = existingUser

    override fun registerDeeplinkListener(listener: TuneDeeplinkListener?) {
        deepLinker.setListener(listener)
    }

    override fun measureEvent(eventData: TuneEvent?) { TODO("Stub to ensure nothing gets sent while testing.") }
}