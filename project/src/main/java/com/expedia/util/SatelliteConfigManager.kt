package com.expedia.util

import android.content.Context
import android.text.format.DateUtils

class SatelliteConfigManager {
    private val INVALID_TIMESTAMP: Long = -1
    private val TEST_ID = "test-ids"
    private val TIMESTAMP = "timestamp"
    private val FEATURE_CONFIG = "featureConfig"

    fun shouldCallSatellite(context: Context): Boolean {
        val fetchPref = context.getSharedPreferences(FEATURE_CONFIG, 0)
        val timestamp = fetchPref.getLong(TEST_ID, INVALID_TIMESTAMP)
        val currentTime = System.currentTimeMillis()
        val timeToLive: Long = DateUtils.DAY_IN_MILLIS
        return isTimeStampValid(timestamp, currentTime, timeToLive)
    }

    fun storeSatelliteResponse(context: Context, satelliteSearchResponse: String) {
        val prefs = context.getSharedPreferences(FEATURE_CONFIG, 0).edit()
        prefs.clear()
        prefs.apply()
        prefs.putString(TEST_ID, satelliteSearchResponse)
        prefs.putLong(TIMESTAMP, System.currentTimeMillis())
        prefs.apply()
    }

    fun isTimeStampValid(timestamp: Long, currentTime: Long, timeToLive: Long): Boolean {
        if (timestamp == INVALID_TIMESTAMP || (currentTime - timestamp) >= timeToLive) {
            return true
        }
        return false
    }
}
