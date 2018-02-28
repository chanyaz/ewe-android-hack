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
	public static final ABTest EBAndroidAppLXFirstActivityListingExpanded = new ABTest(9467);
	public static final ABTest EBAndroidAppLXRTROnSearchAndDetails = new ABTest(10000);
	public static final ABTest EBAndroidAppLXCrossSellOnHotelConfirmationTest = new ABTest(10556);
	public static final ABTest EBAndroidAppDisabledSTPStateHotels = new ABTest(15923, true);
	public static final ABTest EBAndroidAppBringUniversalCheckoutToLX = new ABTest(12630);
	public static final ABTest EBAndroidAppLXFilterSearch = new ABTest(12689);
	public static final ABTest EBAndroidAppLXDisablePOISearch = new ABTest(13050);
	public static final ABTest EBAndroidAppFlightsConfirmationItinSharing = new ABTest(14137);
	public static final ABTest EBAndroidAppLXNavigateToSRP = new ABTest(13152);
	public static final ABTest EBAndroidAppShowAirAttachMessageOnLaunchScreen = new ABTest(13345);
	public static final ABTest EBAndroidAppLastMinuteDeals = new ABTest(15844);
	public static final ABTest EBAndroidLXMOD = new ABTest(16098);
	public static final ABTest EBAndroidAppFreeCancellationTooltip = new ABTest(14513);
	public static final ABTest EBAndroidAppAPIMAuth = new ABTest(14654);
	public static final ABTest EBAndroidPopulateCardholderName = new ABTest(14525);
	public static final ABTest EBAndroidAppSecureCheckoutIcon = new ABTest(16112);
	public static final ABTest EBAndroidAppFlightFlexEnabled = new ABTest(15247);
	public static final ABTest EBAndroidAppLXOfferLevelCancellationPolicySupport = new ABTest(15246);
	public static final ABTest EBAndroidAppAllowUnknownCardTypes = new ABTest(15457);
	public static final ABTest EBAndroidAppShowFlightsCheckoutWebview = new ABTest(15371, true);
	public static final ABTest EBAndroidAppDisplayEligibleCardsOnPaymentForm = new ABTest(15682);
	public static final ABTest EBAndroidAppHotelMaterialForms = new ABTest(24870, true);
	public static final ABTest EBAndroidLXNotifications = new ABTest(24888);
	public static final ABTest EBAndroidAppSavedCoupons = new ABTest(16365, true);
	public static final ABTest EBAndroidAppFlightsFiltersPriceAndLogo = new ABTest(24758, true);

	// Rail tests
	public static final ABTest EBAndroidRailHybridAppForDEEnabled = new ABTest(15102);
	public static final ABTest EBAndroidRailHybridAppForUKEnabled = new ABTest(15413);
	public static final ABTest EBAndroidRailHybridAppForEbookersUKEnabled = new ABTest(24804);

	// Trips tests
	public static final ABTest TripsNewFlightAlerts = new ABTest(16205, true);
	public static final ABTest TripsFlightsNewDesign = new ABTest(14655, true);
	public static final ABTest EBAndroidAppTripsMessageHotel = new ABTest(16176, true);
	public static final ABTest TripsHotelsM2 = new ABTest(24601, true);
	public static final ABTest EBAndroidAppTripsUserReviews = new ABTest(24499, true);

	// Flight tests
	public static final ABTest EBAndroidAppOfferInsuranceInFlightSummary = new ABTest(12268);
	public static final ABTest EBAndroidAppFlightByotSearch = new ABTest(13202);
	public static final ABTest EBAndroidAppFlightsSeatClassAndBookingCode = new ABTest(12763);
	public static final ABTest EBAndroidAppSimplifyFlightShopping = new ABTest(13514);
	public static final ABTest EBAndroidAppFlightsMoreInfoOnOverview = new ABTest(13505);
	public static final ABTest EBAndroidAppFlightsCrossSellPackageOnFSR = new ABTest(14183);
	public static final ABTest EBAndroidAppFlightAATest = new ABTest(14241);
	public static final ABTest EBAndroidAppFlightAdvanceSearch = new ABTest(15185);
	public static final ABTest EBAndroidAppFlightSubpubChange = new ABTest(15211);
	public static final ABTest EBAndroidAppFlightSwitchFields = new ABTest(14918);
	public static final ABTest EBAndroidAppFlightTravelerFormRevamp = new ABTest(14647);
	public static final ABTest EBAndroidAppFlightSearchSuggestionLabel = new ABTest(16366);
	public static final ABTest EBAndroidAppFlightSuggestionOnOneCharacter = new ABTest(15349);
	public static final ABTest EBAndroidAppFlightRateDetailsFromCache = new ABTest(14769);
	public static final ABTest EBAndroidAppFlightsSearchResultCaching = new ABTest(14888);
	public static final ABTest EBAndroidAppFlightsKrazyglue = new ABTest(15790, true);
	public static final ABTest EBAndroidAppFlightsEvolable = new ABTest(16345);
	public static final ABTest EBAndroidAppFlightsDeltaPricing = new ABTest(15602);
	public static final ABTest EBAndroidAppFlightsBaggageWebViewHideAd = new ABTest(16334);
	public static final ABTest EBAndroidAppFlightsGreedySearchCall = new ABTest(15962, true);
	public static final ABTest EBAndroidAppFlightsAPIKongEndPoint = new ABTest(16382, true);
	public static final ABTest EBAndroidAppFLightLoadingStateV1 = new ABTest(24766, true);
	public static final ABTest EBAndroidAppFlightsRecentSearch = new ABTest(24765, true);

	public static final ABTest EBAndroidAppHotelPinnedSearch = new ABTest(15082);
	public static final ABTest EBAndroidAppHotelSuperSearch = new ABTest(14911);
	public static final ABTest EBAndroidAppHotelsWebCheckout = new ABTest(15823);
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
	public static final ABTest HotelRecentSearch = new ABTest(15832, true);
	public static final ABTest HotelResultChangeDate = new ABTest(24783, true);
	public static final ABTest HotelEarn2xMessaging = new ABTest(24742, true);

	// Cars Web View Tests
	public static final ABTest EBAndroidAppCarsFlexView = new ABTest(14632);
	public static final ABTest EBAndroidAppCarsAATest = new ABTest(15311);

	// Packages Tests
	public static final ABTest EBAndroidAppPackagesMidApi = new ABTest(14856, true);
	public static final ABTest PackagesTitleChange = new ABTest(15787);
	public static final ABTest EBAndroidAppPackagesEnablePOS = new ABTest(24551);
	public static final ABTest EBAndroidAppPackagesBreadcrumbsForNav = new ABTest(15835);
	public static final ABTest EBAndroidAppPackagesMoveBundleOverviewForBreadcrumbs = new ABTest(16223);
	public static final ABTest EBAndroidAppPackagesMISRealWorldGeo = new ABTest(16314);
	public static final ABTest EBAndroidAppPackagesShowForceUpdateDialog = new ABTest(16283);
	public static final ABTest PackagesBackFlowFromOverview = new ABTest(16163);
	public static final ABTest EBAndroidAppPackagesDisplayFlightSeatingClass = new ABTest(16300);
	public static final ABTest EBAndroidAppPackagesDisplayBasicEconomyTooltip = new ABTest(24678);
	public static final ABTest EBAndroidAppPackagesFlightCabinClass = new ABTest(24803, true);
	public static final ABTest CardExpiryDateFormField = new ABTest(24734, true);

	// Account
	public static final ABTest EBAndroidAppAccountRecaptcha = new ABTest(15652, true);
	public static final ABTest EBAndroidAppAccountsAPIKongEndPoint = new ABTest(24860, true);

	// Launch
	public static final ABTest EBAndroidAppBrandColors = new ABTest(15846);
	public static final ABTest MesoAd = new ABTest(15842, true);

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
		testIDs.add(EBAndroidAppLXFirstActivityListingExpanded.getKey());
		testIDs.add(EBAndroidAppLXRTROnSearchAndDetails.getKey());
		testIDs.add(EBAndroidAppLXCrossSellOnHotelConfirmationTest.getKey());
		testIDs.add(EBAndroidAppDisabledSTPStateHotels.getKey());
		testIDs.add(EBAndroidAppOfferInsuranceInFlightSummary.getKey());
		testIDs.add(EBAndroidAppBringUniversalCheckoutToLX.getKey());
		testIDs.add(EBAndroidAppLXFilterSearch.getKey());
		testIDs.add(EBAndroidAppFlightsFiltersPriceAndLogo.getKey());
		testIDs.add(EBAndroidAppLXDisablePOISearch.getKey());
		testIDs.add(EBAndroidAppFlightsSeatClassAndBookingCode.getKey());
		testIDs.add(EBAndroidAppFreeCancellationTooltip.getKey());
		testIDs.add(EBAndroidAppFlightsConfirmationItinSharing.getKey());
		testIDs.add(EBAndroidAppFlightByotSearch.getKey());
		testIDs.add(EBAndroidAppShowAirAttachMessageOnLaunchScreen.getKey());
		testIDs.add(EBAndroidAppLXNavigateToSRP.getKey());
		testIDs.add(EBAndroidAppLastMinuteDeals.getKey());
		testIDs.add(EBAndroidAppFlightsMoreInfoOnOverview.getKey());
		testIDs.add(EBAndroidAppFlightsCrossSellPackageOnFSR.getKey());
		testIDs.add(PackagesTitleChange.getKey());
		testIDs.add(EBAndroidAppSimplifyFlightShopping.getKey());
		testIDs.add(EBAndroidAppHotelsWebCheckout.getKey());
		testIDs.add(EBAndroidAppCarsFlexView.getKey());
		testIDs.add(EBAndroidAppFlightAATest.getKey());
		testIDs.add(EBAndroidAppHotelPinnedSearch.getKey());
		testIDs.add(EBAndroidLXMOD.getKey());
		testIDs.add(EBAndroidAppFlightAdvanceSearch.getKey());
		testIDs.add(EBAndroidAppHotelSuperSearch.getKey());
		testIDs.add(EBAndroidAppAPIMAuth.getKey());
		testIDs.add(EBAndroidAppAccountsAPIKongEndPoint.getKey());
		testIDs.add(EBAndroidPopulateCardholderName.getKey());
		testIDs.add(EBAndroidAppSecureCheckoutIcon.getKey());
		testIDs.add(EBAndroidAppPackagesMidApi.getKey());
		testIDs.add(EBAndroidAppFlightFlexEnabled.getKey());
		testIDs.add(EBAndroidAppFlightSubpubChange.getKey());
		testIDs.add(EBAndroidAppFlightSwitchFields.getKey());
		testIDs.add(EBAndroidAppFlightTravelerFormRevamp.getKey());
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
		testIDs.add(EBAndroidAppShowFlightsCheckoutWebview.getKey());
		testIDs.add(EBAndroidAppFlightsEvolable.getKey());
		testIDs.add(TripsFlightsNewDesign.getKey());
		testIDs.add(EBAndroidAppDisplayEligibleCardsOnPaymentForm.getKey());
		testIDs.add(EBAndroidAppFlightsSearchResultCaching.getKey());
		testIDs.add(EBAndroidAppBrandColors.getKey());
		testIDs.add(MesoAd.getKey());
		testIDs.add(EBAndroidAppFlightsKrazyglue.getKey());
		testIDs.add(EBAndroidAppFlightsDeltaPricing.getKey());
		testIDs.add(HotelRoomImageGallery.getKey());
		testIDs.add(EBAndroidAppAccountRecaptcha.getKey());
		testIDs.add(EBAndroidAppHotelMaterialForms.getKey());
		testIDs.add(TripsNewFlightAlerts.getKey());
		testIDs.add(EBAndroidAppPackagesBreadcrumbsForNav.getKey());
		testIDs.add(EBAndroidAppPackagesMoveBundleOverviewForBreadcrumbs.getKey());
		testIDs.add(EBAndroidAppPackagesMISRealWorldGeo.getKey());
		testIDs.add(EBAndroidAppFlightsGreedySearchCall.getKey());
		testIDs.add(EBAndroidAppFlightsBaggageWebViewHideAd.getKey());
		testIDs.add(EBAndroidLXNotifications.getKey());
		testIDs.add(EBAndroidAppPackagesShowForceUpdateDialog.getKey());
		testIDs.add(EBAndroidAppSavedCoupons.getKey());
		testIDs.add(PackagesBackFlowFromOverview.getKey());
		testIDs.add(EBAndroidAppFlightsAPIKongEndPoint.getKey());
		testIDs.add(EBAndroidAppTripsMessageHotel.getKey());
		testIDs.add(HotelAmenityFilter.getKey());
		testIDs.add(EBAndroidAppPackagesDisplayFlightSeatingClass.getKey());
		testIDs.add(TripsHotelsM2.getKey());
		testIDs.add(HotelNewFilterCtaText.getKey());
		testIDs.add(HotelDatelessInfosite.getKey());
		testIDs.add(HotelUrgencyV2.getKey());
		testIDs.add(EBAndroidAppFLightLoadingStateV1.getKey());
		testIDs.add(HotelHideMiniMapOnResult.getKey());
		testIDs.add(EBAndroidAppFlightsRecentSearch.getKey());
		testIDs.add(EBAndroidAppPackagesDisplayBasicEconomyTooltip.getKey());
		testIDs.add(HotelSoldOutOnHSRTreatment.getKey());
		testIDs.add(HotelImageGrid.getKey());
		testIDs.add(HotelRecentSearch.getKey());
		testIDs.add(HotelResultChangeDate.getKey());
		testIDs.add(HotelEarn2xMessaging.getKey());
		testIDs.add(CardExpiryDateFormField.getKey());
		testIDs.add(EBAndroidAppPackagesFlightCabinClass.getKey());
		testIDs.add(EBAndroidAppTripsUserReviews.getKey());
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
