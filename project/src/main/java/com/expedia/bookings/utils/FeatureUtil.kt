package com.expedia.bookings.utils

import android.content.Context
import com.expedia.bookings.R
import com.expedia.bookings.data.abacus.AbacusUtils
import com.expedia.bookings.featureconfig.AbacusFeatureConfigManager

fun isFlexEnabled(): Boolean {
    return AbacusFeatureConfigManager.isUserBucketedForTest(AbacusUtils.EBAndroidAppFlightFlexEnabled)
}

fun isPopulateCardholderNameEnabled(context: Context): Boolean {
    return AbacusFeatureConfigManager.isUserBucketedForTest(context, AbacusUtils.EBAndroidPopulateCardholderName)
}

fun isSecureIconEnabled(context: Context): Boolean {
    return AbacusFeatureConfigManager.isUserBucketedForTest(context, AbacusUtils.EBAndroidAppSecureCheckoutIcon)
}

fun isFrequentFlyerNumberForFlightsEnabled(): Boolean {
    return AbacusFeatureConfigManager.isUserBucketedForTest(AbacusUtils.EBAndroidAppFlightFrequentFlyerNumber)
}

fun isMidAPIEnabled(context: Context): Boolean {
    return FeatureToggleUtil.isUserBucketedAndFeatureEnabled(context, AbacusUtils.EBAndroidAppPackagesMidApi, R.string.preference_packages_mid_api)
}

fun isHideApacBillingFieldsEnabled(): Boolean {
    return AbacusFeatureConfigManager.isUserBucketedForTest(AbacusUtils.EBAndroidAppHideApacBillingAddressFields)
}

fun isAllowUnknownCardTypesEnabled(context: Context): Boolean {
    return AbacusFeatureConfigManager.isUserBucketedForTest(AbacusUtils.EBAndroidAppAllowUnknownCardTypes)
}

fun isAllowCheckinCheckoutDatesInlineEnabled(): Boolean {
    return AbacusFeatureConfigManager.isUserBucketedForTest(AbacusUtils.EBAndroidAppHotelCheckinCheckoutDatesInline)
}

fun isShowFlightsCheckoutWebview(context: Context): Boolean {
    return AbacusFeatureConfigManager.isUserBucketedForTest(AbacusUtils.EBAndroidAppShowFlightsCheckoutWebview)
            && FeatureToggleUtil.isFeatureEnabled(context, R.string.preference_show_flights_checkout_webview)
}

fun isKrazyglueOnFlightsConfirmationEnabled(context: Context) : Boolean {
    return AbacusFeatureConfigManager.isUserBucketedForTest(context, AbacusUtils.EBAndroidAppFlightsKrazyglue)
            && FeatureToggleUtil.isFeatureEnabled(context, R.string.preference_enable_krazy_glue_on_flights_confirmation)
}

fun isDisplayCardsOnPaymentForm(context: Context): Boolean {
    return AbacusFeatureConfigManager.isUserBucketedForTest(context, AbacusUtils.EBAndroidAppDisplayEligibleCardsOnPaymentForm)
}

fun isCreditCardMessagingForPayLaterEnabled(context: Context): Boolean {
    return AbacusFeatureConfigManager.isUserBucketedForTest(context, AbacusUtils.EBAndroidAppHotelPayLaterCreditCardMessaging) &&
            FeatureToggleUtil.isFeatureEnabled(context, R.string.pay_later_credit_card_messaging)
}

fun isHotelMaterialForms(context: Context): Boolean {
    return AbacusFeatureConfigManager.isUserBucketedForTest(context, AbacusUtils.EBAndroidAppHotelMaterialForms) &&
            FeatureToggleUtil.isFeatureEnabled(context, R.string.preference_enable_hotel_material_forms)
}
