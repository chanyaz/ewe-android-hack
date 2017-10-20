package com.expedia.bookings.utils

import android.content.Context
import com.expedia.bookings.R
import com.expedia.bookings.data.abacus.AbacusUtils
import com.expedia.bookings.featureconfig.AbacusFeatureConfigManager

fun isMaterialFormsEnabled(): Boolean {
    return AbacusFeatureConfigManager.isUserBucketedForTest(AbacusUtils.EBAndroidAppUniversalCheckoutMaterialForms)
}

fun isFlexEnabled(): Boolean {
    return AbacusFeatureConfigManager.isUserBucketedForTest(AbacusUtils.EBAndroidAppFlightFlexEnabled)
}

fun isPopulateCardholderNameEnabled(context: Context): Boolean {
    return AbacusFeatureConfigManager.isUserBucketedForTest(context, AbacusUtils.EBAndroidPopulateCardholderName)
}

fun isSecureIconEnabled(context: Context): Boolean {
    return AbacusFeatureConfigManager.isUserBucketedForTest(AbacusUtils.EBAndroidAppSecureCheckoutIcon)
            && FeatureToggleUtil.isFeatureEnabled(context, R.string.preference_enable_secure_icon)
}

fun isFrequentFlyerNumberForFlightsEnabled(): Boolean {
    return AbacusFeatureConfigManager.isUserBucketedForTest(AbacusUtils.EBAndroidAppFlightFrequentFlyerNumber)&& isMaterialFormsEnabled()
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

fun isDisplayCardsOnPaymentForm(context: Context): Boolean {
    return AbacusFeatureConfigManager.isUserBucketedForTest(AbacusUtils.EBAndroidAppDisplayEligibleCardsOnPaymentForm)
            && FeatureToggleUtil.isFeatureEnabled(context, R.string.preference_display_eligible_cards_on_payment_form)
}

fun isKrazyglueOnFlightsConfirmationEnabled(context: Context) : Boolean {
    return AbacusFeatureConfigManager.isUserBucketedForTest(context, AbacusUtils.EBAndroidAppFlightsKrazyglue)
            && FeatureToggleUtil.isFeatureEnabled(context, R.string.preference_enable_krazy_glue_on_flights_confirmation)
}

fun isCreditCardMessagingForPayLaterEnabled(context: Context): Boolean {
    return AbacusFeatureConfigManager.isUserBucketedForTest(context, AbacusUtils.EBAndroidAppHotelPayLaterCreditCardMessaging) &&
            FeatureToggleUtil.isFeatureEnabled(context, R.string.pay_later_credit_card_messaging)
}

