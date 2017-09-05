package com.expedia.bookings.data.abacus;

import java.util.ArrayList;
import java.util.List;

public class AbacusUtils {

	/**
	 * ACTIVE KEYS
	 * <p/>
	 * When new tests need to be added just add a new key to this class
	 * Then call isUserBucketedForTest(int key) to check if the user is
	 * participating in the AB Test.
	 */

	public static final int EBAndroidAppCarInsuranceIncludedCKO = 7774;
	public static final int ExpediaAndroidAppAATestSep2015 = 11455;
	public static final int EBAndroidAppLXCategoryABTest = 9165;
	public static final int EBAndroidAppLXFirstActivityListingExpanded = 9467;
	public static final int EBAndroidAppHotelSecureCheckoutMessaging = 9898;
	public static final int EBAndroidAppLXRTROnSearchAndDetails = 10000;
	public static final int EBAndroidAppLXCrossSellOnHotelConfirmationTest = 10556;
	public static final int EBAndroidAppSmartLockTest = 11269;
	public static final int EBAndroidAppBringUniversalCheckoutToLX = 12630;
	public static final int EBAndroidAppLXFilterSearch = 12689;
	public static final int EBAndroidAppLXDisablePOISearch = 13050;
	public static final int EBAndroidAppUniversalCheckoutMaterialForms = 12721;
	public static final int EBAndroidAppFlightsConfirmationItinSharing = 14137;
	public static final int EBAndroidAppLXNavigateToSRP = 13152;
	public static final int EBAndroidAppCheckoutButtonText = 13480;
	public static final int EBAndroidAppShowAirAttachMessageOnLaunchScreen = 13345;
	public static final int EBAndroidAppShowMemberPricingCardOnLaunchScreen = 13697;
	public static final int EBAndroidAppHotelUpgrade = 13196;
	public static final int EBAndroidAppItinHotelRedesign = 14741;
	public static final int TripsHotelScheduledNotificationsV2 = 15315;
	public static final int EBAndroidLXVoucherRedemption = 14380;
	public static final int EBAndroidAppTripsDetailRemoveCalendar = 14201;
	public static final int EBAndroidAppFreeCancellationTooltip = 14513;
	public static final int EBAndroidAppAPIMAuth = 14654;
	public static final int EBAndroidPopulateCardholderName = 14525;
	public static final int EBAndroidAppSecureCheckoutIcon = 14202;
	public static final int EBAndroidAppFlightFlexEnabled = 15247;
	public static final int EBAndroidAppLocaleBasedDateFormatting = 15316;
	public static final int EBAndroidAppHideApacBillingAddressFields = 12622;
	public static final int ABTEST_IGNORE_DEBUG = -1;

	// Rail tests
	public static final int EBAndroidRailHybridAppForDEEnabled = 15102;

	// Flight tests
	public static final int EBAndroidAppOfferInsuranceInFlightSummary = 12268;
	public static final int EBAndroidAppFareFamilyFlightSummary = 15074;
	public static final int EBAndroidAppFlightByotSearch = 13202;
	public static final int EBAndroidAppFlightsSeatClassAndBookingCode = 12763;
	public static final int EBAndroidAppSimplifyFlightShopping = 13514;
	public static final int EBAndroidAppFlightsMoreInfoOnOverview = 13505;
	public static final int EBAndroidAppFlightsCrossSellPackageOnFSR = 14183;
	public static final int EBAndroidAppFlightAATest = 14241;
	public static final int EBAndroidAppFlightSearchFormValidation = 13843;
	public static final int EBAndroidAppFlightHideFSRInfographic = 13844;
	public static final int EBAndroidAppFlightAdvanceSearch = 15185;
	public static final int EBAndroidAppFlightRetainSearchParams = 14297;
	public static final int EBAndroidAppFlightDayPlusDateSearchForm = 14742;
	public static final int EBAndroidAppFlightSubpubChange = 15211;
	public static final int EBAndroidAppFlightSwitchFields = 14918;
	public static final int EBAndroidAppFlightTravelerFormRevamp = 14647;
	public static final int EBAndroidAppFlightSearchSuggestionLabel = 14646;
	public static final int EBAndroidAppFlightFrequentFlyerNumber = 14971;
	public static final int EBAndroidAppFlightSuggestionOnOneCharacter = 15349;
	public static final int EBAndroidAppFlightRateDetailsFromCache = 14769;

	// Hotel Tests
	public static final int EBAndroidAppHotelRoomRateExpanded = 13246;
	public static final int EBAndroidAppHotelUrgencyMessage = 13277;
	public static final int EBAndroidAppHotelPinnedSearch = 15082;
	public static final int EBAndroidAppHotelGroupRoomRate = 14591;
	public static final int EBAndroidAppHotelHideSearch = 14271;
	public static final int EBAndroidAppHotelSortCallToAction = 14923;
	public static final int EBAndroidAppHotelAutoSuggestSameAsWeb = 14483;
	public static final int EBAndroidAppHotelThrottleGalleryAnimation = 14785;
	public static final int EBAndroidAppHotelGreedySearch = 15228;
	public static final int EBAndroidAppHotelSuperSearch = 14911;
	public static final int EBAndroidAppHotelHideStrikethroughPrice = 14863;
	public static final int EBAndroidAppHotelsWebCheckout = 14761;


	// Cars Web View Tests
	public static final int EBAndroidAppCarsWebViewUK = 12913;
	public static final int EBAndroidAppCarsWebViewTVLY = 13658;
	public static final int EBAndroidAppCarsWebViewUS = 13648;
	public static final int EBAndroidAppCarsWebViewCT = 13830;
	public static final int EBAndroidAppCarsWebViewEB = 13826;
	public static final int EBAndroidAppCarsWebViewAUNZ = 13828;
	public static final int EBAndroidAppCarsWebViewCA = 13829;
	public static final int EBAndroidAppCarsWebViewOB = 13660;
	public static final int EBAndroidAppCarsWebViewEMEA = 13827;
	public static final int EBAndroidAppCarsWebViewNewPOS = 15271;
	public static final int EBAndroidAppCarsFlexView = 14632;
	public static final int EBAndroidAppCarsAATest = 15311;

	// Packages Tests
	public static final int EBAndroidAppPackagesMidApi = 14856;
	public static final int EBAndroidAppPackagesTitleChange = 14953;
	public static final int EBAndroidAppPackagesEnableJapanPOS = 14900;

	// Account
	public static final int EBAndroidAppAccountSinglePageSignUp = 13923;

	// Launch
	public static final int ProWizardTest = 14687;

	// Soft Prompt
	public static final int EBAndroidAppSoftPromptLocation = 15119;

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
		testIDs.add(EBAndroidAppCarInsuranceIncludedCKO);
		testIDs.add(ExpediaAndroidAppAATestSep2015);
		testIDs.add(EBAndroidAppLXCategoryABTest);
		testIDs.add(EBAndroidAppLXFirstActivityListingExpanded);
		testIDs.add(EBAndroidAppHotelSecureCheckoutMessaging);
		testIDs.add(EBAndroidAppLXRTROnSearchAndDetails);
		testIDs.add(EBAndroidAppLXCrossSellOnHotelConfirmationTest);
		testIDs.add(EBAndroidAppSmartLockTest);
		testIDs.add(EBAndroidAppOfferInsuranceInFlightSummary);
		testIDs.add(EBAndroidAppFareFamilyFlightSummary);
		testIDs.add(EBAndroidAppBringUniversalCheckoutToLX);
		testIDs.add(EBAndroidAppLXFilterSearch);
		testIDs.add(EBAndroidAppCheckoutButtonText);
		testIDs.add(EBAndroidAppLXDisablePOISearch);
		testIDs.add(EBAndroidAppFlightsSeatClassAndBookingCode);
		testIDs.add(EBAndroidAppUniversalCheckoutMaterialForms);
		testIDs.add(EBAndroidAppFreeCancellationTooltip);
		testIDs.add(EBAndroidAppFlightsConfirmationItinSharing);
		testIDs.add(EBAndroidAppHotelRoomRateExpanded);
		testIDs.add(EBAndroidAppFlightByotSearch);
		testIDs.add(EBAndroidAppShowAirAttachMessageOnLaunchScreen);
		testIDs.add(EBAndroidAppHotelUrgencyMessage);
		testIDs.add(EBAndroidAppLXNavigateToSRP);
		testIDs.add(EBAndroidAppShowMemberPricingCardOnLaunchScreen);
		testIDs.add(EBAndroidAppHotelUpgrade);
		testIDs.add(EBAndroidAppFlightsMoreInfoOnOverview);
		testIDs.add(EBAndroidAppFlightsCrossSellPackageOnFSR);
		testIDs.add(EBAndroidAppPackagesTitleChange);
		testIDs.add(EBAndroidAppSimplifyFlightShopping);
		testIDs.add(EBAndroidAppItinHotelRedesign);
		testIDs.add(EBAndroidAppCarsWebViewUK);
		testIDs.add(EBAndroidAppCarsWebViewTVLY);
		testIDs.add(EBAndroidAppHotelsWebCheckout);
		testIDs.add(EBAndroidAppCarsWebViewUS);
		testIDs.add(EBAndroidAppCarsWebViewCT);
		testIDs.add(EBAndroidAppCarsWebViewEB);
		testIDs.add(EBAndroidAppCarsWebViewAUNZ);
		testIDs.add(EBAndroidAppCarsWebViewCA);
		testIDs.add(EBAndroidAppCarsWebViewOB);
		testIDs.add(EBAndroidAppCarsWebViewEMEA);
		testIDs.add(EBAndroidAppCarsWebViewNewPOS);
		testIDs.add(EBAndroidAppCarsFlexView);
		testIDs.add(EBAndroidAppFlightAATest);
		testIDs.add(EBAndroidAppFlightSearchFormValidation);
		testIDs.add(EBAndroidAppHotelPinnedSearch);
		testIDs.add(EBAndroidAppHotelGroupRoomRate);
		testIDs.add(EBAndroidAppFlightHideFSRInfographic);
		testIDs.add(EBAndroidLXVoucherRedemption);
		testIDs.add(EBAndroidAppHotelHideSearch);
		testIDs.add(EBAndroidAppTripsDetailRemoveCalendar);
		testIDs.add(EBAndroidAppFlightAdvanceSearch);
		testIDs.add(EBAndroidAppFlightRetainSearchParams);
		testIDs.add(EBAndroidAppHotelSortCallToAction);
		testIDs.add(EBAndroidAppFlightDayPlusDateSearchForm);
		testIDs.add(EBAndroidAppHotelAutoSuggestSameAsWeb);
		testIDs.add(EBAndroidAppHotelGreedySearch);
		testIDs.add(EBAndroidAppHotelSuperSearch);
		testIDs.add(EBAndroidAppHotelHideStrikethroughPrice);
		testIDs.add(EBAndroidAppHotelThrottleGalleryAnimation);
		testIDs.add(EBAndroidAppAPIMAuth);
		testIDs.add(EBAndroidAppFlightFrequentFlyerNumber);
		testIDs.add(EBAndroidAppAccountSinglePageSignUp);
		testIDs.add(ProWizardTest);
		testIDs.add(EBAndroidPopulateCardholderName);
		testIDs.add(EBAndroidAppSecureCheckoutIcon);
		testIDs.add(EBAndroidAppPackagesMidApi);
		testIDs.add(EBAndroidAppFlightFlexEnabled);
		testIDs.add(EBAndroidAppFlightSubpubChange);
		testIDs.add(EBAndroidAppFlightSwitchFields);
		testIDs.add(EBAndroidAppFlightTravelerFormRevamp);
		testIDs.add(EBAndroidRailHybridAppForDEEnabled);
		testIDs.add(EBAndroidAppFlightSearchSuggestionLabel);
		testIDs.add(EBAndroidAppPackagesEnableJapanPOS);
		testIDs.add(EBAndroidAppCarsAATest);
		testIDs.add(EBAndroidAppLocaleBasedDateFormatting);
		testIDs.add(EBAndroidAppHideApacBillingAddressFields);
		testIDs.add(EBAndroidAppFlightSuggestionOnOneCharacter);
		testIDs.add(EBAndroidAppFlightRateDetailsFromCache);
		testIDs.add(TripsHotelScheduledNotificationsV2);
		testIDs.add(EBAndroidAppSoftPromptLocation);
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
