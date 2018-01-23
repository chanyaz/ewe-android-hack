package com.expedia.bookings.utils

import android.content.Context
import android.support.annotation.VisibleForTesting
import java.util.UUID

class UniqueIdentifierHelper {

    companion object {
        private var uniqueID: String = ""
        private val PREF_DEVICE_ID = "PREF_DEVICE_ID"

        @JvmStatic
        @Synchronized
        fun getID(context: Context): String {
            if (uniqueID.isEmpty()) {
                val sharedPrefs = context.getSharedPreferences(
                        PREF_DEVICE_ID, Context.MODE_PRIVATE)
                uniqueID = sharedPrefs.getString(PREF_DEVICE_ID, "")
                if (uniqueID.isEmpty()) {
                    createUniqueID(context)
                }
            }
            return uniqueID
        }

        @VisibleForTesting
        fun createUniqueID(context: Context) {
            uniqueID = UUID.randomUUID().toString()
            val sharedPrefs = context.getSharedPreferences(
                    PREF_DEVICE_ID, Context.MODE_PRIVATE)
            val editor = sharedPrefs.edit()
            editor.putString(PREF_DEVICE_ID, uniqueID)
            editor.apply()
        }
    }
}
