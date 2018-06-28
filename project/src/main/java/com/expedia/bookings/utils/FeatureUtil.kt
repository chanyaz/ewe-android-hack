package com.expedia.bookings.utils

import android.content.Context
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.LoyaltyMembershipTier
import com.expedia.bookings.data.abacus.AbacusUtils
import com.expedia.bookings.data.abacus.AbacusVariant
import com.expedia.bookings.data.pos.PointOfSale
import com.expedia.bookings.data.pos.PointOfSaleId
import com.expedia.bookings.featureconfig.AbacusFeatureConfigManager
import com.expedia.bookings.featureconfig.ProductFlavorFeatureConfiguration
import com.expedia.bookings.features.Features
import java.util.Locale

fun isFlexEnabled(context: Context): Boolean {
    return AbacusFeatureConfigManager.isBucketedForTest(context, AbacusUtils.EBAndroidAppFlightFlexEnabled)
}

fun isPopulateCardholderNameEnabled(context: Context): Boolean {
    return AbacusFeatureConfigManager.isBucketedForTest(context, AbacusUtils.EBAndroidPopulateCardholderName)
}

fun isGrowthSocialSharingEnabled(context: Context): Boolean {
    return AbacusFeatureConfigManager.isBucketedForTest(context, AbacusUtils.EBAndroidAppGrowthSocialSharing)
}

fun isFHCPackageWebViewEnabled(context: Context): Boolean {
    return AbacusFeatureConfigManager.isBucketedForTest(context, AbacusUtils.EBAndroidAppPackagesWebviewFHC)
}

fun isAllowUnknownCardTypesEnabled(context: Context): Boolean {
    return AbacusFeatureConfigManager.isBucketedForTest(context, AbacusUtils.EBAndroidAppAllowUnknownCardTypes)
}

fun isAllowCheckinCheckoutDatesInlineEnabled(context: Context): Boolean {
    return AbacusFeatureConfigManager.isBucketedForTest(context, AbacusUtils.EBAndroidAppHotelCheckinCheckoutDatesInline)
}

fun isShowFlightsNativeRateDetailsWebviewCheckoutEnabled(context: Context): Boolean {
    return AbacusFeatureConfigManager.isBucketedForTest(context, AbacusUtils.EBAndroidFlightsNativeRateDetailsWebviewCheckout) ||
            AbacusFeatureConfigManager.isBucketedForTest(context, AbacusUtils.EBAndroidFlightsNativeRateDetailsWebviewCheckoutInEUPos)
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

fun isLxWebViewCheckoutEnabled(context: Context): Boolean {
    return AbacusFeatureConfigManager.isBucketedForTest(context, AbacusUtils.EBAndroidAppLxWebCheckoutView)
}

fun isFlightGreedySearchEnabled(context: Context): Boolean {
    return AbacusFeatureConfigManager.isBucketedForTest(context, AbacusUtils.EBAndroidAppFlightsGreedySearchCall)
}

fun isShowSavedCoupons(context: Context): Boolean {
    return AbacusFeatureConfigManager.isBucketedForTest(context, AbacusUtils.EBAndroidAppSavedCoupons)
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

fun shouldShowRewardLaunchCard(context: Context): Boolean {
    return !shouldShowJoinRewardsLaunchCard(context)
            && Ui.getApplication(context).appComponent().userStateManager().isUserAuthenticated()
            && ProductFlavorFeatureConfiguration.getInstance().defaultPOS == PointOfSaleId.ORBITZ
            && Locale.getDefault().language != "es"
}

fun shouldShowJoinRewardsLaunchCard(context: Context): Boolean {
    val userStateManager = Ui.getApplication(context).appComponent().userStateManager()

    return AbacusFeatureConfigManager.isBucketedForTest(context, AbacusUtils.JoinRewardsLaunchCard)
            && userStateManager.isUserAuthenticated()
            && userStateManager.getCurrentUserLoyaltyTier() == LoyaltyMembershipTier.NONE
            && PointOfSale.getPointOfSale().shouldShowJoinRewardsCard()
            && Locale.getDefault().language != "es"
}

fun isBottomNavigationBarEnabled(context: Context): Boolean {
    return LaunchNavBucketCache.isBucketed(context)
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

fun isFlightsUrgencyMeassagingEnabled(context: Context): Boolean {
    return AbacusFeatureConfigManager.isBucketedForTest(context, AbacusUtils.EBAndroidAppFlightsUrgencyMessaging)
}

fun isHideMiniMapOnResultBucketed(context: Context): Boolean {
    return AbacusFeatureConfigManager.isBucketedForTest(context, AbacusUtils.HotelHideMiniMapOnResult)
}

fun isRichContentEnabled(context: Context): Boolean {
    return AbacusFeatureConfigManager.isBucketedInAnyVariant(context, AbacusUtils.EBAndroidAppFlightsRichContent)
}

fun isPackagesHSRPriceDisplayEnabled(context: Context): Boolean {
    return AbacusFeatureConfigManager.isBucketedForTest(context, AbacusUtils.EBAndroidAppPackagesHSRPriceDisplay)
}

fun isRichContentShowAmenityEnabled(): Boolean {
    val richContentVariant = Db.sharedInstance.abacusResponse.variateForTest(AbacusUtils.EBAndroidAppFlightsRichContent)
    return (richContentVariant == AbacusVariant.ONE.value || richContentVariant == AbacusVariant.THREE.value)
}

fun isRichContentShowRouteScoreEnabled(): Boolean {
    val richContentVariant = Db.sharedInstance.abacusResponse.variateForTest(AbacusUtils.EBAndroidAppFlightsRichContent)
    return (richContentVariant == AbacusVariant.TWO.value || richContentVariant == AbacusVariant.THREE.value)
}

fun shouldShowCustomerFirstGuarantee(context: Context): Boolean {
    return AbacusFeatureConfigManager.isBucketedForTest(context, AbacusUtils.CustomerFirstGuarantee) &&
            PointOfSale.getPointOfSale().shouldShowCustomerFirstGuarantee()
}

fun isNewSignInEnabled(context: Context): Boolean {
    return AbacusFeatureConfigManager.isBucketedForTest(context, AbacusUtils.EBAndroidAppAccountNewSignIn)
}

fun isServerSideFilteringEnabledForPackages(context: Context): Boolean {
    return AbacusFeatureConfigManager.isBucketedInAnyVariant(context, AbacusUtils.EBAndroidAppPackagesServerSideFiltering)
}

fun isHolidayCalendarEnabled(context: Context): Boolean {
    return AbacusFeatureConfigManager.isBucketedForTest(context, AbacusUtils.EBAndroidAppFlightsHolidayCalendar)
}

fun isBetterSavingsOnRDScreenEnabledForPackages(context: Context): Boolean {
    return AbacusFeatureConfigManager.isBucketedForTest(context, AbacusUtils.EBAndroidAppPackagesBetterSavingsOnRateDetails)
}

fun isLXMultipleDatesSearchEnabled(): Boolean {
    return Features.all.lxMultipleDatesSearch.enabled()
}

fun isActivityCountHeaderViewEnabled(): Boolean {
    return Features.all.lxActivityResultsHeader.enabled()
}

fun isPackagesSearchFormRenameToFromEnabled(context: Context): Boolean {
    return AbacusFeatureConfigManager.isBucketedForTest(context, AbacusUtils.EBAndroidAppPackagesSearchFormRenameToFrom)
}

fun isHighlightSortFilterOnPackagesEnabled(context: Context): Boolean {
    return AbacusFeatureConfigManager.isBucketedForTest(context, AbacusUtils.EBAndroidAppPackagesHighlightSortFilter)
}

fun isRichContentForPackagesEnabled(context: Context): Boolean {
    return AbacusFeatureConfigManager.isBucketedInAnyVariant(context, AbacusUtils.EBAndroidAppPackagesRichContent)
}

fun isRichContentShowAmenityForPackagesEnabled(): Boolean {
    val richContentVariant = Db.sharedInstance.abacusResponse.variateForTest(AbacusUtils.EBAndroidAppPackagesRichContent)
    return (richContentVariant == AbacusVariant.ONE.value || richContentVariant == AbacusVariant.THREE.value)
}

fun isRichContentShowRouteScoreForPackagesEnabled(): Boolean {
    val richContentVariant = Db.sharedInstance.abacusResponse.variateForTest(AbacusUtils.EBAndroidAppPackagesRichContent)
    return (richContentVariant == AbacusVariant.TWO.value || richContentVariant == AbacusVariant.THREE.value)
}

fun isHotMipRedesignEnabled(): Boolean {
    return Features.all.hotMipRedesign.enabled()
}

fun isGlassboxForPackagesEnabled(): Boolean {
    return Features.all.packagesGlassbox.enabled()
}

fun isGlassboxEnabled(): Boolean {
    return Features.all.glassBox.enabled()
}

fun islXEssRegionTypeCallEnabled(): Boolean {
    return Features.all.lxEssRegionTypeCall.enabled()
}
