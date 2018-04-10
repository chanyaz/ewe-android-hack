package com.expedia.bookings.appstartup.persistence

import android.content.Context

class SharedPreferencesSplashScreenAnimationProvider(private val context: Context) : SplashScreenAnimationProvider {
    private val splashScreenAnimationSharedPreferencesName = "splashScreenSharedPreferencesInstance"
    private val shouldSplashScreenAnimationRunKey = "shouldSplashScreenAnimationRunKey"

    override fun shouldSplashAnimationRun(): Boolean {
        val sharedPref = context.getSharedPreferences(splashScreenAnimationSharedPreferencesName, Context.MODE_PRIVATE)
        return sharedPref.getBoolean(shouldSplashScreenAnimationRunKey, false)
    }

    override fun put(shouldSplashAnimationRun: Boolean) {
        val sharedPref = context.getSharedPreferences(splashScreenAnimationSharedPreferencesName, Context.MODE_PRIVATE)
        val editor = sharedPref.edit()
        editor.putBoolean(shouldSplashScreenAnimationRunKey, shouldSplashAnimationRun)
        editor.commit()
    }
}
