package com.expedia.bookings.utils

import android.content.Context
import com.expedia.bookings.data.abacus.AbacusUtils
import com.expedia.bookings.data.abacus.AbacusVariant

/*
* There is no guarantee Abacus will successfully return before the app is launched.
* This introduces timing bugs with the launch screen.  This class maintains a local instance
* of pro wizard test value to ensure user experience remains consistent.
 */
class ProWizardBucketCache {
    companion object {
        private val ABACUS_FILE_NAME = "abacus_prefs"
        private val PREFS_PRO_WIZARD_BUCKET = AbacusUtils.ProWizardTest.key.toString()

        @JvmStatic fun cacheBucket(context: Context, testValue: Int) {
            val editor = context.getSharedPreferences(ABACUS_FILE_NAME, Context.MODE_PRIVATE).edit()
            editor.putInt(PREFS_PRO_WIZARD_BUCKET, testValue)
            editor.apply()
        }

        @JvmStatic fun isBucketed(context: Context): Boolean {
            val prefs = context.getSharedPreferences(ABACUS_FILE_NAME, Context.MODE_PRIVATE)
            val testValue = prefs.getInt(PREFS_PRO_WIZARD_BUCKET, AbacusVariant.NO_BUCKET.value)

            return testValue == AbacusVariant.BUCKETED.value
        }

        @JvmStatic fun getTrackingValue(context: Context): Int {
            val prefs = context.getSharedPreferences(ABACUS_FILE_NAME, Context.MODE_PRIVATE)
            val testValue = prefs.getInt(PREFS_PRO_WIZARD_BUCKET, AbacusVariant.NO_BUCKET.value)

            return testValue
        }
    }
}
