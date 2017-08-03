package com.expedia.util

import android.content.Context

class SatellitePref {
    fun callSatellite(context: Context): Boolean {
        val FEATURE_CONFIG = "featureConfig"
        val fetchPref = context.getSharedPreferences(FEATURE_CONFIG, 0)
        val keys = fetchPref.all
        val timestamp = keys["timestamp"].toString().toLong()
        val currentTime = System.currentTimeMillis()
        val oneDay: Long = 86400
        return timestampCheck(timestamp,currentTime,oneDay)
    }

    fun storeSatelliteResponse(context: Context, satelliteSearchResponse: String) {
        val FEATURE_CONFIG = "featureConfig"
        val prefs = context.getSharedPreferences(FEATURE_CONFIG, 0).edit()
        prefs.clear()
        prefs.apply()
        prefs.putString("test-ids", satelliteSearchResponse)
        prefs.putLong("timestamp", System.currentTimeMillis())
        prefs.apply()
    }

    fun timestampCheck(timestamp: Long, currentTime: Long, oneDay: Long): Boolean {
        if (timestamp.equals("null")) {
            return true
        } else if ((currentTime - timestamp) >= oneDay) {
            System.out.println("satellite call")
            return true
        } else {
            System.out.println("No satellite call")
            return false
        }
    }
}