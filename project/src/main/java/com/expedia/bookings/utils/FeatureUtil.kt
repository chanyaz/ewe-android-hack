package com.expedia.bookings.utils

import android.content.Context
import com.expedia.bookings.R
import com.expedia.bookings.data.abacus.AbacusUtils
import com.expedia.bookings.featureconfig.AbacusFeatureConfigManager

fun isFlexEnabled(context: Context): Boolean {
    return AbacusFeatureConfigManager.isBucketedForTest(context, AbacusUtils.EBAndroidAppFlightFlexEnabled)
}

fun isPopulateCardholderNameEnabled(context: Context): Boolean {
    return AbacusFeatureConfigManager.isBucketedForTest(context, AbacusUtils.EBAndroidPopulateCardholderName)
}

fun isSecureIconEnabled(context: Context): Boolean {
    return AbacusFeatureConfigManager.isBucketedForTest(context, AbacusUtils.EBAndroidAppSecureCheckoutIcon)
}

fun isMidAPIEnabled(context: Context): Boolean {
    return AbacusFeatureConfigManager.isBucketedForTest(context, AbacusUtils.EBAndroidAppPackagesMidApi)
}

fun shouldPackageForceUpdateBeVisible(context: Context): Boolean {
    return !isMidAPIEnabled(context) && isPackageForceUpdateEnabled(context)
}

fun isAllowUnknownCardTypesEnabled(context: Context): Boolean {
    return AbacusFeatureConfigManager.isBucketedForTest(context, AbacusUtils.EBAndroidAppAllowUnknownCardTypes)
}

fun isAllowCheckinCheckoutDatesInlineEnabled(context: Context): Boolean {
    return AbacusFeatureConfigManager.isBucketedForTest(context, AbacusUtils.EBAndroidAppHotelCheckinCheckoutDatesInline)
}

fun isShowFlightsCheckoutWebview(context: Context): Boolean {
    return AbacusFeatureConfigManager.isBucketedForTest(context, AbacusUtils.EBAndroidAppShowFlightsCheckoutWebview)
}

fun isKrazyglueOnFlightsConfirmationEnabled(context: Context): Boolean {
    return AbacusFeatureConfigManager.isBucketedInAnyVariant(context, AbacusUtils.EBAndroidAppFlightsKrazyglue)
}

fun isDisplayCardsOnPaymentForm(context: Context): Boolean {
    return AbacusFeatureConfigManager.isBucketedForTest(context, AbacusUtils.EBAndroidAppDisplayEligibleCardsOnPaymentForm)
}

fun isCreditCardMessagingForPayLaterEnabled(context: Context): Boolean {
    return AbacusFeatureConfigManager.isBucketedForTest(context, AbacusUtils.EBAndroidAppHotelPayLaterCreditCardMessaging) &&
            FeatureToggleUtil.isFeatureEnabled(context, R.string.pay_later_credit_card_messaging)
}

fun isBrandColorEnabled(context: Context): Boolean {
    return AbacusFeatureConfigManager.isBucketedForTest(context, AbacusUtils.EBAndroidAppBrandColors)
}

fun isHotelMaterialForms(context: Context): Boolean {
    return AbacusFeatureConfigManager.isBucketedForTest(context, AbacusUtils.EBAndroidAppHotelMaterialForms)
}

fun isBreadcrumbsPackagesEnabled(context: Context): Boolean {
    return AbacusFeatureConfigManager.isBucketedForTest(context, AbacusUtils.EBAndroidAppPackagesBreadcrumbsForNav)
}

fun isBreadcrumbsMoveBundleOverviewPackagesEnabled(context: Context): Boolean {
    return AbacusFeatureConfigManager.isBucketedForTest(context, AbacusUtils.EBAndroidAppPackagesMoveBundleOverviewForBreadcrumbs)
}

fun isPackagesMISRealWorldGeoEnabled(context: Context): Boolean {
    return !isMidAPIEnabled(context) && AbacusFeatureConfigManager.isBucketedForTest(context, AbacusUtils.EBAndroidAppPackagesMISRealWorldGeo)
}

fun isFlightGreedySearchEnabled(context: Context): Boolean {
    return AbacusFeatureConfigManager.isBucketedForTest(context, AbacusUtils.EBAndroidAppFlightsGreedySearchCall)
}

fun isShowSavedCoupons(context: Context): Boolean {
    return AbacusFeatureConfigManager.isBucketedForTest(context, AbacusUtils.EBAndroidAppSavedCoupons)
}

fun isBackFlowFromOverviewEnabled(context: Context): Boolean {
    return (AbacusFeatureConfigManager.isBucketedForTest(context, AbacusUtils.PackagesBackFlowFromOverview))
}

private fun isPackageForceUpdateEnabled(context: Context): Boolean {
    return AbacusFeatureConfigManager.isBucketedForTest(context, AbacusUtils.EBAndroidAppPackagesShowForceUpdateDialog)
}

fun isDisplayFlightSeatingClassForShoppingEnabled(context: Context): Boolean {
    return AbacusFeatureConfigManager.isBucketedForTest(context, AbacusUtils.EBAndroidAppPackagesDisplayFlightSeatingClass)
}

fun isDisplayBasicEconomyTooltipForPackagesEnabled(context: Context): Boolean {
    return AbacusFeatureConfigManager.isBucketedForTest(context, AbacusUtils.EBAndroidAppPackagesDisplayBasicEconomyTooltip)
}
