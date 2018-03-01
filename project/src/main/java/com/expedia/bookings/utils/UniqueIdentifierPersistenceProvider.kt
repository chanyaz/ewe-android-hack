package com.expedia.bookings.utils

import android.content.Context

class UniqueIdentifierPersistenceProvider(private val context: Context) : StringPersistenceProvider {
    private val deviceIdFileName = "PREF_DEVICE_ID"

    override fun getString(key: String, defaultValue: String): String {
        val sharedPrefs = context.getSharedPreferences(deviceIdFileName, Context.MODE_PRIVATE)
        return sharedPrefs.getString(key, defaultValue)
    }

    override fun putString(key: String, value: String) {
        val sharedPrefs = context.getSharedPreferences(deviceIdFileName, Context.MODE_PRIVATE)
        sharedPrefs.edit().putString(key, value).apply()
    }
}
