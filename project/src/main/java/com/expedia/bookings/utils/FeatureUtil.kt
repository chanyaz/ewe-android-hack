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

fun shouldPackageForceUpdateBeVisible(context: Context): Boolean {
    return !isMidAPIEnabled(context) && isPackageForceUpdateEnabled(context)
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
}

fun isKrazyglueOnFlightsConfirmationEnabled(context: Context) : Boolean {
    return AbacusFeatureConfigManager.isUserBucketedForTest(context, AbacusUtils.EBAndroidAppFlightsKrazyglue)
}

fun isDisplayCardsOnPaymentForm(context: Context): Boolean {
    return AbacusFeatureConfigManager.isUserBucketedForTest(context, AbacusUtils.EBAndroidAppDisplayEligibleCardsOnPaymentForm)
}

fun isCreditCardMessagingForPayLaterEnabled(context: Context): Boolean {
    return AbacusFeatureConfigManager.isUserBucketedForTest(context, AbacusUtils.EBAndroidAppHotelPayLaterCreditCardMessaging) &&
            FeatureToggleUtil.isFeatureEnabled(context, R.string.pay_later_credit_card_messaging)
}

fun isBrandColorEnabled(context: Context) : Boolean {
    return FeatureToggleUtil.isUserBucketedAndFeatureEnabled(context, AbacusUtils.EBAndroidAppBrandColors, R.string.preference_enable_launch_screen_brand_colors)
}

fun isHotelMaterialForms(context: Context): Boolean {
    return AbacusFeatureConfigManager.isUserBucketedForTest(context, AbacusUtils.EBAndroidAppHotelMaterialForms) &&
            FeatureToggleUtil.isFeatureEnabled(context, R.string.preference_enable_hotel_material_forms)
}

fun isBreadcrumbsPackagesEnabled(context: Context): Boolean {
    return AbacusFeatureConfigManager.isUserBucketedForTest(context, AbacusUtils.EBAndroidAppPackagesBreadcrumbsForNav)
}

fun isBreadcrumbsMoveBundleOverviewPackagesEnabled(context: Context): Boolean {
    return AbacusFeatureConfigManager.isUserBucketedForTest(context, AbacusUtils.EBAndroidAppPackagesMoveBundleOverviewForBreadcrumbs) &&
            FeatureToggleUtil.isFeatureEnabled(context, R.string.preference_packages_breadcrumbs_move_bundle_overview)
}

fun isPackagesMISRealWorldGeoEnabled(context: Context): Boolean {
    return AbacusFeatureConfigManager.isUserBucketedForTest(context, AbacusUtils.EBAndroidAppPackagesMISRealWorldGeo)
}

fun isShowSavedCoupons(context: Context): Boolean {
    return AbacusFeatureConfigManager.isUserBucketedForTest(context, AbacusUtils.EBAndroidAppSavedCoupons)
}

fun isBackFlowFromOverviewEnabled(context: Context): Boolean {
    return (AbacusFeatureConfigManager.isUserBucketedForTest(context, AbacusUtils.PackagesBackFlowFromOverview) &&
            FeatureToggleUtil.isFeatureEnabled(context, R.string.preference_packages_back_flow_from_overview))
}

private fun isPackageForceUpdateEnabled(context: Context): Boolean {
    return AbacusFeatureConfigManager.isUserBucketedForTest(AbacusUtils.EBAndroidAppPackagesShowForceUpdateDialog) &&
            FeatureToggleUtil.isFeatureEnabled(context, R.string.preference_packages_force_upgrade_for_pss_clients)
}
