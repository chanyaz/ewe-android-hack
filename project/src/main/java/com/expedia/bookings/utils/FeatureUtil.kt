package com.expedia.bookings.utils

import android.content.Context
import com.expedia.bookings.R
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.abacus.AbacusUtils

fun isMaterialFormsEnabled(): Boolean {
    return Db.getAbacusResponse().isUserBucketedForTest(AbacusUtils.EBAndroidAppUniversalCheckoutMaterialForms)
}

fun isFlexEnabled(): Boolean {
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
            && isMaterialFormsEnabled()
}

fun isHideFormFieldsEnabled(context: Context): Boolean {
    return FeatureToggleUtil.isFeatureEnabled(context, R.string.preference_hide_form_fields_based_on_billing_country_address)
}

fun isMidAPIEnabled(context: Context): Boolean {
    return FeatureToggleUtil.isUserBucketedAndFeatureEnabled(context, AbacusUtils.EBAndroidAppPackagesMidApi, R.string.preference_packages_mid_api)
}

fun isHideApacBillingFieldsEnabled(): Boolean {
    return Db.getAbacusResponse().isUserBucketedForTest(AbacusUtils.EBAndroidAppHideApacBillingAddressFields)
}

fun isAllowUnknownCardTypesEnabled(context: Context): Boolean {
    return Db.getAbacusResponse().isUserBucketedForTest(AbacusUtils.EBAndroidAppAllowUnknownCardTypes)
            && FeatureToggleUtil.isFeatureEnabled(context, R.string.preference_allow_unknown_card_types)
}
