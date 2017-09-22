package com.expedia.util

import android.content.Context

class ForceBucketPref {

    companion object {
        private val PREF_FORCE_BUCKETED = "PREF_FORCE_BUCKETED"
        private val FORCE_BUCKET_PREFERENCES = "ForceBucketPrefs"

        @JvmStatic fun isForceBucketed(context: Context): Boolean {
            return context.getSharedPreferences(FORCE_BUCKET_PREFERENCES, 0).getBoolean(PREF_FORCE_BUCKETED, false)
        }

        @JvmStatic fun setUserForceBucketed(context: Context, isForceBucketed: Boolean) {
            val editor = context.getSharedPreferences(FORCE_BUCKET_PREFERENCES, Context.MODE_PRIVATE).edit()

            //clear currently set forced bucketing on reset
            if (!isForceBucketed) {
                editor.clear()
            }

            editor.putBoolean(PREF_FORCE_BUCKETED, isForceBucketed)
            editor.apply()
        }

        @JvmStatic fun saveForceBucketedTestKeyValue(context: Context, key: Int, value: Int) {
            if (isForceBucketed(context)) {
                val editor = context.getSharedPreferences(FORCE_BUCKET_PREFERENCES, Context.MODE_PRIVATE).edit()
                editor.putInt(key.toString(), value)
                editor.apply()
            }
        }

        @JvmStatic fun getForceBucketedTestValue(context: Context, key: Int, defValue: Int): Int {
            return context.getSharedPreferences(FORCE_BUCKET_PREFERENCES, 0).getInt(key.toString(), defValue)
        }
    }
}
