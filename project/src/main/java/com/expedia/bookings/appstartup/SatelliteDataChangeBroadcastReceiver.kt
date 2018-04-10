package com.expedia.bookings.appstartup

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.expedia.bookings.appstartup.persistence.SharedPreferencesSplashScreenAnimationProvider
import com.expedia.bookings.features.Features

class SatelliteDataChangeBroadcastReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val sharedPreferencesSplashScreenAnimationProvider = SharedPreferencesSplashScreenAnimationProvider(context)
        sharedPreferencesSplashScreenAnimationProvider.put(Features.all.showSplashLoadingAnimationScreen.enabled())
    }
}
