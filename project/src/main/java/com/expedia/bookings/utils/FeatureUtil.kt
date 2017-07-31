package com.expedia.bookings.utils

import android.content.Context
import com.expedia.bookings.R
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.abacus.AbacusUtils

fun isMaterialFormsEnabled(): Boolean {
    return Db.getAbacusResponse().isUserBucketedForTest(AbacusUtils.EBAndroidAppUniversalCheckoutMaterialForms)
}

fun isDisabledSTPStateEnabled(): Boolean {
    return Db.getAbacusResponse().isUserBucketedForTest(AbacusUtils.EBAndroidAppDisabledSTPState)
}
fun isFlexEnabled(context: Context): Boolean {
    return Db.getAbacusResponse().isUserBucketedForTest(AbacusUtils.EBAndroidAppFlightFlexEnabled)
}

fun isPopulateCardholderNameEnabled(context: Context): Boolean {
    return FeatureToggleUtil.isFeatureEnabled(context, R.string.preference_populate_cardholder_name)
            && Db.getAbacusResponse().isUserBucketedForTest(AbacusUtils.EBAndroidPopulateCardholderName)
}

fun isSecureIconEnabled(context: Context): Boolean {
    return Db.getAbacusResponse().isUserBucketedForTest(AbacusUtils.EBAndroidAppSecureCheckoutIcon)
            && FeatureToggleUtil.isFeatureEnabled(context, R.string.preference_enable_secure_icon)
}

fun isFrequentFlyerNumberForFlightsEnabled(context: Context): Boolean {
    return FeatureToggleUtil.isUserBucketedAndFeatureEnabled(context,
            AbacusUtils.EBAndroidAppFlightFrequentFlyerNumber,
            R.string.preference_enable_flights_frequent_flyer_number)
}