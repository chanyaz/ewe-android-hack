package com.expedia.bookings.utils

import android.content.Context
import com.expedia.bookings.data.abacus.AbacusUtils
import com.expedia.bookings.data.pos.PointOfSaleId
import com.expedia.bookings.featureconfig.AbacusFeatureConfigManager
import com.expedia.bookings.featureconfig.ProductFlavorFeatureConfiguration
import java.util.Locale

fun isFlexEnabled(context: Context): Boolean {
    return AbacusFeatureConfigManager.isBucketedForTest(context, AbacusUtils.EBAndroidAppFlightFlexEnabled)
}

fun isPopulateCardholderNameEnabled(context: Context): Boolean {
    return AbacusFeatureConfigManager.isBucketedForTest(context, AbacusUtils.EBAndroidPopulateCardholderName)
}

fun isMidAPIEnabled(context: Context): Boolean {
    return AbacusFeatureConfigManager.isBucketedForTest(context, AbacusUtils.EBAndroidAppPackagesMidApi)
}

fun isFHCPackageWebViewEnabled(context: Context): Boolean {
    return AbacusFeatureConfigManager.isBucketedForTest(context, AbacusUtils.EBAndroidAppPackagesWebviewFHC)
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

fun isCreditCardMessagingForPayLaterEnabled(context: Context): Boolean {
    return AbacusFeatureConfigManager.isBucketedForTest(context, AbacusUtils.EBAndroidAppHotelPayLaterCreditCardMessaging)
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

fun showNewCreditCardExpiryFormField(context: Context): Boolean {
    return AbacusFeatureConfigManager.isBucketedForTest(context, AbacusUtils.CardExpiryDateFormField)
}

fun isRecentSearchesForFlightsEnabled(context: Context): Boolean {
    return AbacusFeatureConfigManager.isBucketedInAnyVariant(context, AbacusUtils.EBAndroidAppFlightsRecentSearch) &&
            !AbacusFeatureConfigManager.isBucketedForTest(context, AbacusUtils.EBAndroidAppFlightAdvanceSearch)
}

fun isAccountEditWebViewEnabled(context: Context): Boolean {
    return AbacusFeatureConfigManager.isBucketedForTest(context, AbacusUtils.EBAndroidAppAccountsEditWebView)
}

fun isDownloadableFontsEnabled(context: Context): Boolean {
    return AbacusFeatureConfigManager.isBucketedForTest(context, AbacusUtils.DownloadableFonts)
}

fun shouldShowRewardLaunchCard(context: Context): Boolean {
    return AbacusFeatureConfigManager.isBucketedForTest(context, AbacusUtils.RewardLaunchCard)
            && Ui.getApplication(context).appComponent().userStateManager().isUserAuthenticated()
            && ProductFlavorFeatureConfiguration.getInstance().defaultPOS == PointOfSaleId.ORBITZ
            && Locale.getDefault().language != "es"
}

fun isBottomNavigationBarEnabled(context: Context): Boolean {
    return AbacusFeatureConfigManager.isBucketedForTest(context, AbacusUtils.EBAndroidAppBottomNavTabs)
}

fun isShowClassAndBookingCodeEnabled(context: Context): Boolean {
    return AbacusFeatureConfigManager.isBucketedForTest(context, AbacusUtils.EBAndroidAppFlightsSeatClassAndBookingCode)
}

fun shouldShowUrgencyMessaging(context: Context): Boolean {
    return AbacusFeatureConfigManager.isBucketedForTest(context, AbacusUtils.EBAndroidAppSeatsLeftUrgencyMessaging)
}

fun checkIfTripFoldersEnabled(context: Context): Boolean {
    return AbacusFeatureConfigManager.isBucketedForTest(context, AbacusUtils.TripFoldersFragment)
}
