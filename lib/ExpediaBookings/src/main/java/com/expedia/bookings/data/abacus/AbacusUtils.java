package com.expedia.bookings.data.abacus;

import java.util.ArrayList;
import java.util.List;

public class AbacusUtils {
	/**
	 * ACTIVE KEYS
	 * <p/>
	 * When new tests need to be added just add a new key to this class
	 * Then call AbacusFeatureConfigManager.isUserBucketedForTest(Context context, int key) to check if the user is
	 * participating in the AB Test.
	 */

	public static final ABTest ExpediaAndroidAppAATestSep2015 = new ABTest(11455);
	public static final ABTest EBAndroidAppLXCategoryABTest = new ABTest(9165);
	public static final ABTest EBAndroidAppLXRTROnSearchAndDetails = new ABTest(10000);
	public static final ABTest EBAndroidAppFrequentFlierTooltip = new ABTest(24632, true);
	public static final ABTest EBAndroidAppDisabledSTPStateHotels = new ABTest(15923, true);
	public static final ABTest EBAndroidAppBringUniversalCheckoutToLX = new ABTest(12630);
	public static final ABTest EBAndroidAppLXFilterSearch = new ABTest(12689);
	public static final ABTest EBAndroidAppLXDisablePOISearch = new ABTest(13050);
	public static final ABTest EBAndroidAppFlightsConfirmationItinSharing = new ABTest(14137);
	public static final ABTest EBAndroidAppLXNavigateToSRP = new ABTest(13152);
	public static final ABTest EBAndroidAppShowAirAttachMessageOnLaunchScreen = new ABTest(13345);
	public static final ABTest EBAndroidAppLastMinuteDeals = new ABTest(15844);
	public static final ABTest EBAndroidLXMOD = new ABTest(25418);
	public static final ABTest EBAndroidLXMIP = new ABTest(25417, true);
	public static final ABTest EBAndroidAppAPIMAuth = new ABTest(14654);
	public static final ABTest EBAndroidPopulateCardholderName = new ABTest(14525);
	public static final ABTest EBAndroidAppFlightFlexEnabled = new ABTest(15247);
	public static final ABTest EBAndroidAppLXOfferLevelCancellationPolicySupport = new ABTest(15246);
	public static final ABTest EBAndroidAppAllowUnknownCardTypes = new ABTest(15457);
	public static final ABTest EBAndroidAppHotelMaterialForms = new ABTest(24870, true);
	public static final ABTest EBAndroidLXNotifications = new ABTest(24888);
	public static final ABTest EBAndroidAppSavedCoupons = new ABTest(16365, true);
	public static final ABTest EBAndroidAppFlightsFiltersPriceAndLogo = new ABTest(25526, true);
	public static final ABTest DownloadableFonts = new ABTest(25071, true);
	public static final ABTest EBAndroidAppSeatsLeftUrgencyMessaging = new ABTest(25037, true);
	public static final ABTest EBAndroidAppLxWebCheckoutView = new ABTest(25622, true);

	// Rail tests
	public static final ABTest EBAndroidRailHybridAppForDEEnabled = new ABTest(15102);
	public static final ABTest EBAndroidRailHybridAppForUKEnabled = new ABTest(15413);
	public static final ABTest EBAndroidRailHybridAppForEbookersUKEnabled = new ABTest(24804);

	// Trips tests
	public static final ABTest EBAndroidAppHotelTripTaxiCard = new ABTest(25484, true);
	public static final ABTest EBAndroidAppTripsUserReviews = new ABTest(24499, true);
	public static final ABTest EBAndroidAppTripsHotelPricing = new ABTest(25537, true);
	public static final ABTest TripFoldersFragment = new ABTest(25538, true);

	// Flight tests
	public static final ABTest EBAndroidAppFlightByotSearch = new ABTest(25624, true);
	public static final ABTest EBAndroidAppFlightsSeatClassAndBookingCode = new ABTest(25529, true);
	public static final ABTest EBAndroidAppFlightsCrossSellPackageOnFSR = new ABTest(14183);
	public static final ABTest EBAndroidAppFlightAATest = new ABTest(14241);
	public static final ABTest EBAndroidAppFlightAdvanceSearch = new ABTest(15185);
	public static final ABTest EBAndroidAppFlightSubpubChange = new ABTest(15211);
	public static final ABTest EBAndroidAppFlightSearchSuggestionLabel = new ABTest(16366);
	public static final ABTest EBAndroidAppFlightSuggestionOnOneCharacter = new ABTest(15349);
	public static final ABTest EBAndroidAppFlightRateDetailsFromCache = new ABTest(14769, true);
	public static final ABTest EBAndroidAppFlightsKrazyglue = new ABTest(15790, true);
	public static final ABTest EBAndroidAppConfirmationToolbarXHidden = new ABTest(25171, true);
	public static final ABTest EBAndroidAppFlightsEvolable = new ABTest(16345);
	public static final ABTest EBAndroidAppFlightsBaggageWebViewHideAd = new ABTest(16334);
	public static final ABTest EBAndroidAppFlightsGreedySearchCall = new ABTest(15962, true);
	public static final ABTest EBAndroidAppFlightsAPIKongEndPoint = new ABTest(25680, true);
	public static final ABTest EBAndroidAppFLightLoadingStateV1 = new ABTest(25528, true);
	public static final ABTest EBAndroidAppFlightsRecentSearch = new ABTest(25527, true);
	public static final ABTest EBAndroidAppFlightsRichContent = new ABTest(25580, true);
	public static final ABTest EBAndroidAppFlightsUrgencyMessaging = new ABTest(25449, true);
	public static final ABTest EBAndroidFlightsNativeRateDetailsWebviewCheckout = new ABTest(25620, true);
	public static final ABTest EBAndroidAppFlightsContentHighlightInTypeahead = new ABTest(25639, true);
	public static final ABTest EBAndroidAppFlightsHolidayCalendar = new ABTest(25869, true);

	// Hotel tests
	public static final ABTest EBAndroidAppHotelPinnedSearch = new ABTest(15082);
	public static final ABTest EBAndroidAppHotelsWebCheckout = new ABTest(25618, true);
	public static final ABTest EBAndroidAppHotelCheckinCheckoutDatesInline = new ABTest(15344);
	public static final ABTest EBAndroidAppHotelPayLaterCreditCardMessaging = new ABTest(15925, true);
	public static final ABTest HotelRoomImageGallery = new ABTest(14927, true);
	public static final ABTest HotelAmenityFilter = new ABTest(24541, true);
	public static final ABTest HotelNewFilterCtaText = new ABTest(24583, true);
	public static final ABTest HotelDatelessInfosite = new ABTest(24648, true);
	public static final ABTest HotelUrgencyV2 = new ABTest(24741, true);
	public static final ABTest HotelHideMiniMapOnResult = new ABTest(16255, true);
	public static final ABTest HotelSoldOutOnHSRTreatment = new ABTest(24727, true);
	public static final ABTest HotelImageGrid = new ABTest(24841, true);
	public static final ABTest HotelResultChangeDate = new ABTest(24743, true);
	public static final ABTest HotelEarn2xMessaging = new ABTest(24742, true);
	public static final ABTest HotelSatelliteSearch = new ABTest(25534, true);
	public static final ABTest HotelGuestRatingFilter = new ABTest(25533, true);
	public static final ABTest HotelSearchResultsFloatingActionPill = new ABTest(25531, true);
	public static final ABTest HotelUGCTranslations = new ABTest(25532, true);
	public static final ABTest HotelShortlist = new ABTest(25630, true);
	public static final ABTest HotelUGCReviewsBoxRatingDesign = new ABTest(25455, true);
	public static final ABTest HotelMapSmallSoldOutPins = new ABTest(25410, true);
	public static final ABTest HotelReviewSelectRoomCta = new ABTest(25777, true);
	public static final ABTest HotelUGCSearch = new ABTest(25501, true);

	// Cars Web View Tests
	public static final ABTest EBAndroidAppCarsFlexView = new ABTest(14632);
	public static final ABTest EBAndroidAppCarsAATest = new ABTest(15311);

	// Packages Tests
	public static final ABTest PackagesTitleChange = new ABTest(15787);
	public static final ABTest EBAndroidAppPackagesEnablePOS = new ABTest(24551);
	public static final ABTest EBAndroidAppPackagesMoveBundleOverviewForBreadcrumbs = new ABTest(16223);
	public static final ABTest EBAndroidAppPackagesShowForceUpdateDialog = new ABTest(16283);
	public static final ABTest EBAndroidAppPackagesFFPremiumClass = new ABTest(25557, true);
	public static final ABTest CardExpiryDateFormField = new ABTest(24734, true);
	public static final ABTest EBAndroidAppPackagesWebviewFHC = new ABTest(25555, true);
	public static final ABTest EBAndroidAppPackagesAATest = new ABTest(25714, true);
	public static final ABTest EBAndroidAppPackagesServerSideFiltering = new ABTest(25814, true);
	public static final ABTest EBAndroidAppPackagesBetterSavingsOnRateDetails = new ABTest(26122, true);
	public static final ABTest EBAndroidAppPackagesHSRPriceDisplay = new ABTest(25885, true);

	// Account
	public static final ABTest EBAndroidAppAccountRecaptcha = new ABTest(15652, true);
	public static final ABTest EBAndroidAppAccountsAPIKongEndPoint = new ABTest(24860, true);
	public static final ABTest EBAndroidAppAccountsEditWebView = new ABTest(25566, true);
	public static final ABTest EBAndroidAppAccountNewSignIn = new ABTest(24581, true);

	// Launch
	public static final ABTest EBAndroidAppBrandColors = new ABTest(15846);
	public static final ABTest MesoAd = new ABTest(25552, true);
	public static final ABTest RewardLaunchCard = new ABTest(25001, true);
	public static final ABTest EBAndroidAppBottomNavTabs = new ABTest(25565, true);
	public static final ABTest DisableSignInPageAsFirstScreen = new ABTest(25030, true);
	public static final ABTest CustomerFirstGuarantee = new ABTest(25382, true);
	public static final ABTest JoinRewardsLaunchCard = new ABTest(26022, true);

	// Growth
	public static final ABTest EBAndroidAppGrowthSocialSharing = new ABTest(26166, true);

	// Soft Prompt
	public static final ABTest EBAndroidAppSoftPromptLocation = new ABTest(15119);

	public enum LaunchScreenAirAttachVariant {
		CONTROL,
		UP_TO_XX_PERCENT_OFF,
		BECAUSE_YOU_BOOKED_A_FLIGHT
	}

	// Test ID's that we are bucketing the user for.
	public static List<Integer> getActiveTests() {
		List<Integer> testIDs = new ArrayList<>();
		testIDs.add(EBAndroidAppHotelPayLaterCreditCardMessaging.getKey());
		testIDs.add(ExpediaAndroidAppAATestSep2015.getKey());
		testIDs.add(EBAndroidAppLXCategoryABTest.getKey());
		testIDs.add(EBAndroidAppLXRTROnSearchAndDetails.getKey());
		testIDs.add(EBAndroidAppFrequentFlierTooltip.getKey());
		testIDs.add(EBAndroidAppDisabledSTPStateHotels.getKey());
		testIDs.add(EBAndroidAppBringUniversalCheckoutToLX.getKey());
		testIDs.add(EBAndroidAppLXFilterSearch.getKey());
		testIDs.add(EBAndroidAppFlightsFiltersPriceAndLogo.getKey());
		testIDs.add(EBAndroidAppLXDisablePOISearch.getKey());
		testIDs.add(EBAndroidAppFlightsSeatClassAndBookingCode.getKey());
		testIDs.add(EBAndroidAppFlightsConfirmationItinSharing.getKey());
		testIDs.add(EBAndroidAppFlightByotSearch.getKey());
		testIDs.add(EBAndroidAppShowAirAttachMessageOnLaunchScreen.getKey());
		testIDs.add(EBAndroidFlightsNativeRateDetailsWebviewCheckout.getKey());
		testIDs.add(EBAndroidAppLXNavigateToSRP.getKey());
		testIDs.add(EBAndroidAppLastMinuteDeals.getKey());
		testIDs.add(EBAndroidAppFlightsCrossSellPackageOnFSR.getKey());
		testIDs.add(EBAndroidAppFlightsUrgencyMessaging.getKey());
		testIDs.add(PackagesTitleChange.getKey());
		testIDs.add(EBAndroidAppHotelsWebCheckout.getKey());
		testIDs.add(EBAndroidAppCarsFlexView.getKey());
		testIDs.add(EBAndroidAppFlightAATest.getKey());
		testIDs.add(EBAndroidAppHotelPinnedSearch.getKey());
		testIDs.add(EBAndroidLXMOD.getKey());
		testIDs.add(EBAndroidLXMIP.getKey());
		testIDs.add(EBAndroidAppFlightAdvanceSearch.getKey());
		testIDs.add(EBAndroidAppAPIMAuth.getKey());
		testIDs.add(EBAndroidAppAccountsAPIKongEndPoint.getKey());
		testIDs.add(EBAndroidPopulateCardholderName.getKey());
		testIDs.add(EBAndroidAppFlightFlexEnabled.getKey());
		testIDs.add(EBAndroidAppFlightSubpubChange.getKey());
		testIDs.add(EBAndroidRailHybridAppForDEEnabled.getKey());
		testIDs.add(EBAndroidRailHybridAppForUKEnabled.getKey());
		testIDs.add(EBAndroidRailHybridAppForEbookersUKEnabled.getKey());
		testIDs.add(EBAndroidAppFlightSearchSuggestionLabel.getKey());
		testIDs.add(EBAndroidAppPackagesEnablePOS.getKey());
		testIDs.add(EBAndroidAppCarsAATest.getKey());
		testIDs.add(EBAndroidAppFlightSuggestionOnOneCharacter.getKey());
		testIDs.add(EBAndroidAppFlightRateDetailsFromCache.getKey());
		testIDs.add(EBAndroidAppSoftPromptLocation.getKey());
		testIDs.add(EBAndroidAppLXOfferLevelCancellationPolicySupport.getKey());
		testIDs.add(EBAndroidAppAllowUnknownCardTypes.getKey());
		testIDs.add(EBAndroidAppHotelCheckinCheckoutDatesInline.getKey());
		testIDs.add(EBAndroidAppFlightsEvolable.getKey());
		testIDs.add(EBAndroidAppConfirmationToolbarXHidden.getKey());
		testIDs.add(TripFoldersFragment.getKey());
		testIDs.add(EBAndroidAppBrandColors.getKey());
		testIDs.add(MesoAd.getKey());
		testIDs.add(RewardLaunchCard.getKey());
		testIDs.add(JoinRewardsLaunchCard.getKey());
		testIDs.add(CustomerFirstGuarantee.getKey());
		testIDs.add(DisableSignInPageAsFirstScreen.getKey());
		testIDs.add(EBAndroidAppFlightsKrazyglue.getKey());
		testIDs.add(HotelRoomImageGallery.getKey());
		testIDs.add(EBAndroidAppAccountRecaptcha.getKey());
		testIDs.add(EBAndroidAppAccountsEditWebView.getKey());
		testIDs.add(EBAndroidAppHotelMaterialForms.getKey());
		testIDs.add(EBAndroidAppPackagesMoveBundleOverviewForBreadcrumbs.getKey());
		testIDs.add(EBAndroidAppFlightsGreedySearchCall.getKey());
		testIDs.add(EBAndroidAppFlightsBaggageWebViewHideAd.getKey());
		testIDs.add(EBAndroidLXNotifications.getKey());
		testIDs.add(EBAndroidAppPackagesShowForceUpdateDialog.getKey());
		testIDs.add(EBAndroidAppSavedCoupons.getKey());
		testIDs.add(EBAndroidAppFlightsAPIKongEndPoint.getKey());
		testIDs.add(HotelAmenityFilter.getKey());
		testIDs.add(EBAndroidAppTripsHotelPricing.getKey());
		testIDs.add(EBAndroidAppHotelTripTaxiCard.getKey());
		testIDs.add(HotelNewFilterCtaText.getKey());
		testIDs.add(HotelDatelessInfosite.getKey());
		testIDs.add(HotelUrgencyV2.getKey());
		testIDs.add(EBAndroidAppFLightLoadingStateV1.getKey());
		testIDs.add(HotelHideMiniMapOnResult.getKey());
		testIDs.add(EBAndroidAppFlightsRecentSearch.getKey());
		testIDs.add(EBAndroidAppFlightsHolidayCalendar.getKey());
		testIDs.add(EBAndroidAppFlightsRichContent.getKey());
		testIDs.add(HotelSoldOutOnHSRTreatment.getKey());
		testIDs.add(HotelImageGrid.getKey());
		testIDs.add(HotelResultChangeDate.getKey());
		testIDs.add(HotelEarn2xMessaging.getKey());
		testIDs.add(CardExpiryDateFormField.getKey());
		testIDs.add(EBAndroidAppPackagesFFPremiumClass.getKey());
		testIDs.add(EBAndroidAppBottomNavTabs.getKey());
		testIDs.add(EBAndroidAppTripsUserReviews.getKey());
		testIDs.add(DownloadableFonts.getKey());
		testIDs.add(HotelSatelliteSearch.getKey());
		testIDs.add(EBAndroidAppSeatsLeftUrgencyMessaging.getKey());
		testIDs.add(EBAndroidAppPackagesWebviewFHC.getKey());
		testIDs.add(HotelGuestRatingFilter.getKey());
		testIDs.add(HotelSearchResultsFloatingActionPill.getKey());
		testIDs.add(HotelUGCTranslations.getKey());
		testIDs.add(EBAndroidAppGrowthSocialSharing.getKey());
		testIDs.add(HotelShortlist.getKey());
		testIDs.add(HotelUGCReviewsBoxRatingDesign.getKey());
		testIDs.add(EBAndroidAppFlightsContentHighlightInTypeahead.getKey());
		testIDs.add(EBAndroidAppAccountNewSignIn.getKey());
		testIDs.add(HotelMapSmallSoldOutPins.getKey());
		testIDs.add(EBAndroidAppLxWebCheckoutView.getKey());
		testIDs.add(EBAndroidAppPackagesAATest.getKey());
		testIDs.add(EBAndroidAppPackagesServerSideFiltering.getKey());
		testIDs.add(HotelReviewSelectRoomCta.getKey());
		testIDs.add(EBAndroidAppPackagesBetterSavingsOnRateDetails.getKey());
		testIDs.add(HotelUGCSearch.getKey());
		testIDs.add(EBAndroidAppPackagesHSRPriceDisplay.getKey());
		return testIDs;
	}

	public static String getAnalyticsString(AbacusTest test) {
		String analyticsString;
		if (test == null) {
			analyticsString = "";
		}
		else {
			// User is bucketed and the test is live, log ex: 7143.23456.1
			analyticsString = String.format("%s.%s.%s", test.id, test.instanceId, test.value);
		}

		return analyticsString;
	}

	public static String appendString(String key) {
		if (key == null || key.length() == 0) {
			return "";
		}
		else {
			return String.format("%s|", key);
		}
	}
}
