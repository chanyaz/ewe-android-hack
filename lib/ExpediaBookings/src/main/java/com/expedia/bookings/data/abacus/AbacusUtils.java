package com.expedia.bookings.data.abacus;

import java.util.ArrayList;
import java.util.List;

public class AbacusUtils {
	public static final int ABTEST_IGNORE_DEBUG = -1;

	/**
	 * ACTIVE KEYS
	 * <p/>
	 * When new tests need to be added just add a new key to this class
	 * Then call AbacusFeatureConfigManager.isUserBucketedForTest(Context context, int key) to check if the user is
	 * participating in the AB Test.
	 */

	public static final ABTest ExpediaAndroidAppAATestSep2015 =  new ABTest(11455);
	public static final ABTest EBAndroidAppLXCategoryABTest =  new ABTest(9165);
	public static final ABTest EBAndroidAppLXFirstActivityListingExpanded = new ABTest(9467);
	public static final ABTest EBAndroidAppLXRTROnSearchAndDetails = new ABTest(10000);
	public static final ABTest EBAndroidAppLXCrossSellOnHotelConfirmationTest = new ABTest(10556);
	public static final ABTest EBAndroidAppSmartLockTest = new ABTest(11269);
	public static final ABTest EBAndroidAppDisabledSTPStateHotels = new ABTest(15923);
	public static final ABTest EBAndroidAppBringUniversalCheckoutToLX = new ABTest(12630);
	public static final ABTest EBAndroidAppLXFilterSearch = new ABTest(12689);
	public static final ABTest EBAndroidAppLXDisablePOISearch = new ABTest(13050);
	public static final ABTest EBAndroidAppUniversalCheckoutMaterialForms = new ABTest(12721);
	public static final ABTest EBAndroidAppFlightsConfirmationItinSharing = new ABTest(14137);
	public static final ABTest EBAndroidAppLXNavigateToSRP = new ABTest(13152);
	public static final ABTest EBAndroidAppCheckoutButtonText = new ABTest(13480);
	public static final ABTest EBAndroidAppShowAirAttachMessageOnLaunchScreen = new ABTest(13345);
	public static final ABTest EBAndroidAppShowMemberPricingCardOnLaunchScreen = new ABTest(13697);
	public static final ABTest EBAndroidAppHotelUpgrade = new ABTest(13196);
	public static final ABTest EBAndroidAppItinHotelRedesign = new ABTest(14741);
	public static final ABTest TripsHotelScheduledNotificationsV2 = new ABTest(15315);
	public static final ABTest TripsHotelMap = new ABTest(15383);
	public static final ABTest EBAndroidAppTripsDetailRemoveCalendar = new ABTest(14201);
	public static final ABTest EBAndroidAppFreeCancellationTooltip = new ABTest(14513);
	public static final ABTest EBAndroidAppAPIMAuth = new ABTest(14654);
	public static final ABTest EBAndroidPopulateCardholderName = new ABTest(14525);
	public static final ABTest EBAndroidAppSecureCheckoutIcon = new ABTest(16112);
	public static final ABTest EBAndroidAppFlightFlexEnabled = new ABTest(15247);
	public static final ABTest EBAndroidAppHideApacBillingAddressFields = new ABTest(12622);
	public static final ABTest EBAndroidAppLXOfferLevelCancellationPolicySupport = new ABTest(15246);
	public static final ABTest EBAndroidAppAllowUnknownCardTypes = new ABTest(15457);
	public static final ABTest EBAndroidAppShowFlightsCheckoutWebview = new ABTest(15371);
	public static final ABTest TripsFlightsNewdesign = new ABTest(14655, true);
	public static final ABTest EBAndroidAppDisplayEligibleCardsOnPaymentForm = new ABTest(15682);
	public static final ABTest EBAndroidAppHotelMaterialForms = new ABTest(16138);
	public static final ABTest TripsNewFlightAlerts = new ABTest(16205);
	public static final ABTest EBAndroidAppMIDCheckout = new ABTest(14856);

	// Rail tests
	public static final ABTest EBAndroidRailHybridAppForDEEnabled = new ABTest(15102);
	public static final ABTest EBAndroidRailHybridAppForUKEnabled = new ABTest(15413);

	// Flight tests
	public static final ABTest EBAndroidAppOfferInsuranceInFlightSummary = new ABTest(12268);
	public static final ABTest EBAndroidAppFareFamilyFlightSummary = new ABTest(15074);
	public static final ABTest EBAndroidAppFlightByotSearch = new ABTest(13202);
	public static final ABTest EBAndroidAppFlightsSeatClassAndBookingCode = new ABTest(12763);
	public static final ABTest EBAndroidAppSimplifyFlightShopping = new ABTest(13514);
	public static final ABTest EBAndroidAppFlightsMoreInfoOnOverview = new ABTest(13505);
	public static final ABTest EBAndroidAppFlightsCrossSellPackageOnFSR = new ABTest(14183);
	public static final ABTest EBAndroidAppFlightAATest = new ABTest(14241);
	public static final ABTest EBAndroidAppFlightSearchFormValidation = new ABTest(13843);
	public static final ABTest EBAndroidAppFlightAdvanceSearch = new ABTest(15185);
	public static final ABTest EBAndroidAppFlightSubpubChange = new ABTest(15211);
	public static final ABTest EBAndroidAppFlightSwitchFields = new ABTest(14918);
	public static final ABTest EBAndroidAppFlightTravelerFormRevamp = new ABTest(14647);
	public static final ABTest EBAndroidAppFlightSearchSuggestionLabel = new ABTest(14646);
	public static final ABTest EBAndroidAppFlightFrequentFlyerNumber = new ABTest(14971);
	public static final ABTest EBAndroidAppFlightSuggestionOnOneCharacter = new ABTest(15349);
	public static final ABTest EBAndroidAppFlightRateDetailsFromCache = new ABTest(14769);
	public static final ABTest EBAndroidAppFlightsSearchResultCaching = new ABTest(14888);
	public static final ABTest EBAndroidAppFlightsKrazyglue = new ABTest(15790);
	public static final ABTest EBAndroidAppFlightsEvolable = new ABTest(15732);
	public static final ABTest EBAndroidAppFlightsDeltaPricing = new ABTest(15602);
	public static final ABTest EBAndroidAppFlightsFrenchLegalBaggageInfo = new ABTest(16031);

	// Hotel Tests
	public static final ABTest EBAndroidAppHotelUrgencyMessage = new ABTest(13277);
	public static final ABTest EBAndroidAppHotelPinnedSearch = new ABTest(15082);
	public static final ABTest EBAndroidAppHotelGroupRoomRate = new ABTest(14591);
	public static final ABTest HotelAutoSuggestSameAsWeb = new ABTest(15637, true);
	public static final ABTest EBAndroidAppHotelGreedySearch = new ABTest(15228);
	public static final ABTest EBAndroidAppHotelSuperSearch = new ABTest(14911);
	public static final ABTest EBAndroidAppHotelHideStrikethroughPrice = new ABTest(14863);
	public static final ABTest EBAndroidAppHotelsWebCheckout = new ABTest(15823);
	public static final ABTest EBAndroidAppHotelPriceDescriptorProminence = new ABTest(15137);
	public static final ABTest EBAndroidAppHotelCheckinCheckoutDatesInline = new ABTest(15344);
	public static final ABTest HotelShowSoldOutResults = new ABTest(15730);
	public static final ABTest EBAndroidAppHotelPayLaterCreditCardMessaging = new ABTest(15925);
	public static final ABTest HotelEnableInfositeChangeDate = new ABTest(15220, true);
	public static final ABTest HotelRoomImageGallery = new ABTest(14927, true);

	// Cars Web View Tests
	public static final ABTest EBAndroidAppCarsFlexView = new ABTest(14632);
	public static final ABTest EBAndroidAppCarsAATest = new ABTest(15311);

	// Packages Tests
	public static final ABTest EBAndroidAppPackagesMidApi = new ABTest(14856);
	public static final ABTest PackagesTitleChange = new ABTest(15787);
	public static final ABTest EBAndroidAppPackagesEnablePOS = new ABTest(15452);
	public static final ABTest EBAndroidAppPackagesBreadcrumbsForNav = new ABTest(15835);

	// Account
	public static final ABTest EBAndroidAppAccountSinglePageSignUp = new ABTest(13923);
	public static final ABTest EBAndroidAppAccountRecaptcha = new ABTest(15652);

	// Launch
	public static final ABTest ProWizardTest = new ABTest(16136);
	public static final ABTest HolidayFun = new ABTest(15996);

	// Soft Prompt
	public static final ABTest EBAndroidAppSoftPromptLocation = new ABTest(15119);

	public enum DefaultVariant {
		CONTROL,
		BUCKETED
	}

	public enum DefaultTwoVariant {
		CONTROL,
		VARIANT1,
		VARIANT2
	}

	public enum DefaultThreeVariant {
		CONTROL,
		VARIANT1,
		VARIANT2,
		VARIANT3
	}

	public enum LaunchScreenAirAttachVariant {
		CONTROL,
		UP_TO_XX_PERCENT_OFF,
		BECAUSE_YOU_BOOKED_A_FLIGHT
	}

	public enum ItinShareButton {
		CONTROL,
		SHARE_ICON_BUTTON,
		SHARE_TEXT_BUTTON
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
		testIDs.add(EBAndroidAppSmartLockTest.getKey());
		testIDs.add(EBAndroidAppOfferInsuranceInFlightSummary.getKey());
		testIDs.add(EBAndroidAppFareFamilyFlightSummary.getKey());
		testIDs.add(EBAndroidAppBringUniversalCheckoutToLX.getKey());
		testIDs.add(EBAndroidAppLXFilterSearch.getKey());
		testIDs.add(EBAndroidAppCheckoutButtonText.getKey());
		testIDs.add(EBAndroidAppLXDisablePOISearch.getKey());
		testIDs.add(EBAndroidAppFlightsSeatClassAndBookingCode.getKey());
		testIDs.add(EBAndroidAppUniversalCheckoutMaterialForms.getKey());
		testIDs.add(EBAndroidAppFreeCancellationTooltip.getKey());
		testIDs.add(EBAndroidAppFlightsConfirmationItinSharing.getKey());
		testIDs.add(EBAndroidAppFlightByotSearch.getKey());
		testIDs.add(EBAndroidAppShowAirAttachMessageOnLaunchScreen.getKey());
		testIDs.add(EBAndroidAppHotelUrgencyMessage.getKey());
		testIDs.add(EBAndroidAppLXNavigateToSRP.getKey());
		testIDs.add(EBAndroidAppShowMemberPricingCardOnLaunchScreen.getKey());
		testIDs.add(EBAndroidAppHotelUpgrade.getKey());
		testIDs.add(EBAndroidAppFlightsMoreInfoOnOverview.getKey());
		testIDs.add(EBAndroidAppFlightsCrossSellPackageOnFSR.getKey());
		testIDs.add(PackagesTitleChange.getKey());
		testIDs.add(EBAndroidAppSimplifyFlightShopping.getKey());
		testIDs.add(EBAndroidAppItinHotelRedesign.getKey());
		testIDs.add(EBAndroidAppHotelsWebCheckout.getKey());
		testIDs.add(EBAndroidAppCarsFlexView.getKey());
		testIDs.add(EBAndroidAppFlightAATest.getKey());
		testIDs.add(EBAndroidAppFlightSearchFormValidation.getKey());
		testIDs.add(EBAndroidAppHotelPinnedSearch.getKey());
		testIDs.add(EBAndroidAppHotelGroupRoomRate.getKey());
		testIDs.add(EBAndroidAppTripsDetailRemoveCalendar.getKey());
		testIDs.add(EBAndroidAppFlightAdvanceSearch.getKey());
		testIDs.add(HotelAutoSuggestSameAsWeb.getKey());
		testIDs.add(EBAndroidAppHotelGreedySearch.getKey());
		testIDs.add(EBAndroidAppHotelSuperSearch.getKey());
		testIDs.add(EBAndroidAppHotelHideStrikethroughPrice.getKey());
		testIDs.add(EBAndroidAppAPIMAuth.getKey());
		testIDs.add(EBAndroidAppFlightFrequentFlyerNumber.getKey());
		testIDs.add(EBAndroidAppAccountSinglePageSignUp.getKey());
		testIDs.add(ProWizardTest.getKey());
		testIDs.add(HolidayFun.getKey());
		testIDs.add(EBAndroidPopulateCardholderName.getKey());
		testIDs.add(EBAndroidAppSecureCheckoutIcon.getKey());
		testIDs.add(EBAndroidAppPackagesMidApi.getKey());
		testIDs.add(EBAndroidAppFlightFlexEnabled.getKey());
		testIDs.add(EBAndroidAppFlightSubpubChange.getKey());
		testIDs.add(EBAndroidAppFlightSwitchFields.getKey());
		testIDs.add(EBAndroidAppFlightTravelerFormRevamp.getKey());
		testIDs.add(EBAndroidRailHybridAppForDEEnabled.getKey());
		testIDs.add(EBAndroidRailHybridAppForUKEnabled.getKey());
		testIDs.add(EBAndroidAppFlightSearchSuggestionLabel.getKey());
		testIDs.add(EBAndroidAppPackagesEnablePOS.getKey());
		testIDs.add(EBAndroidAppCarsAATest.getKey());
		testIDs.add(EBAndroidAppHideApacBillingAddressFields.getKey());
		testIDs.add(EBAndroidAppFlightSuggestionOnOneCharacter.getKey());
		testIDs.add(EBAndroidAppFlightRateDetailsFromCache.getKey());
		testIDs.add(TripsHotelScheduledNotificationsV2.getKey());
		testIDs.add(EBAndroidAppSoftPromptLocation.getKey());
		testIDs.add(EBAndroidAppHotelPriceDescriptorProminence.getKey());
		testIDs.add(EBAndroidAppLXOfferLevelCancellationPolicySupport.getKey());
		testIDs.add(EBAndroidAppAllowUnknownCardTypes.getKey());
		testIDs.add(EBAndroidAppHotelCheckinCheckoutDatesInline.getKey());
		testIDs.add(EBAndroidAppShowFlightsCheckoutWebview.getKey());
		testIDs.add(TripsHotelMap.getKey());
		testIDs.add(EBAndroidAppFlightsEvolable.getKey());
		testIDs.add(TripsFlightsNewdesign.getKey());
		testIDs.add(EBAndroidAppDisplayEligibleCardsOnPaymentForm.getKey());
		testIDs.add(EBAndroidAppFlightsSearchResultCaching.getKey());
		testIDs.add(HotelShowSoldOutResults.getKey());
		testIDs.add(EBAndroidAppFlightsKrazyglue.getKey());
		testIDs.add(EBAndroidAppFlightsDeltaPricing.getKey());
		testIDs.add(HotelEnableInfositeChangeDate.getKey());
		testIDs.add(HotelRoomImageGallery.getKey());
		testIDs.add(EBAndroidAppFlightsFrenchLegalBaggageInfo.getKey());
		testIDs.add(EBAndroidAppAccountRecaptcha.getKey());
		testIDs.add(EBAndroidAppHotelMaterialForms.getKey());
		testIDs.add(TripsNewFlightAlerts.getKey());
		testIDs.add(EBAndroidAppMIDCheckout.getKey());
		testIDs.add(EBAndroidAppPackagesBreadcrumbsForNav.getKey());

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
