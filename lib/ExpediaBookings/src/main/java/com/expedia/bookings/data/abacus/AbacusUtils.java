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
	public static final int EBAndroidAppShowSignInFormOnLaunch = 8687;
	public static final int EBAndroidAppLXRTROnSearchAndDetails = 10000;
	public static final int EBAndroidAppSignInButtonYellow = 13002;
	public static final int EBAndroidAppFeedsOnLaunch = 10065;
	public static final int EBAndroidAppHotelSearchScreenSoldOutTest = 10554;
	public static final int EBAndroidAppLXCrossSellOnHotelConfirmationTest = 10556;
	public static final int EBAndroidAppHotelFavoriteTest = 10989;
	public static final int EBAndroidAppSmartLockTest = 11269;
	public static final int EBAndroidAppRemoveExpiredCreditCards = 13046;
	public static final int EBAndroidAppHotelResultsPerceivedInstantTest = 10555;
	public static final int EBAndroidAppFlightsCreateTripPriceChangeAlert = 12992;
	public static final int EBAndroidItinHotelGallery = 12465;
	public static final int EBAndroidAppFlightUrgencyMessage = 12768;
	public static final int EBAndroidAppFlightRateDetailExpansion = 12637;
	public static final int EBAndroidAppBringUniversalCheckoutToLX = 12630;
	public static final int EBAndroidAppLXFilterSearch = 12689;
	public static final int EBAndroidAppLXDisablePOISearch = 13050;
	public static final int EBAndroidAppTripsUserReviews = 13257;
	public static final int EBAndroidAppFlightsSeatClassAndBookingCode = 12763;
	public static final int EBAndroidAppUniversalCheckoutMaterialForms = 12721;
	public static final int EBAndroidAppFlightByotSearch = 13202;
	public static final int EBAndroidAppMaterialFlightSearchRoundTripMessage = 12765;
	public static final int EBAndroidAppHotelColorSwitch = 13247;
	public static final int EBAndroidAppHotelRoomRateExpanded = 13246;
	public static final int EBAndroidAppMaterialFlightDistanceOnDetails = 12766;
	public static final int EBAndroidAppHotelImageLoadLatency = 12908;
	public static final int EBAndroidAppHotelPriceProminence = 12974;
	public static final int EBAndroidAppFlightPremiumClass = 13035;
	public static final int EBAndroidAppTripsNewSignInPage = 13023;
	public static final int EBAndroidAppHotelHideNoReviewRating = 13079;
	public static final int EBAndroidAppHotelMemberPricingBadge = 13098;
	public static final int EBAndroidAppTripsHotelSoftChangeWebView = 13026;
	public static final int EBAndroidAppHotelLoyaltyEarnMessage = 13179;
	public static final int EBAndroidAppHotelUrgencyMessage = 13277;
	public static final int EBAndroidAppDisabledSTPState = 13825;
	public static final int EBAndroidAppShowSignInCardOnLaunchScreen = 13191;
	public static final int EBAndroidAppLXNavigateToSRP = 13152;
	public static final int EBAndroidAppWebViewCheckout = 13741;
	public static final int EBAndroidAppCheckoutButtonText = 13480;
	public static final int EBAndroidAppHotelRemoveAutoFocusAndAdvanceOnSearch = 13178;
	public static final int EBAndroidAppShowAirAttachMessageOnLaunchScreen = 13345;
	public static final int EBAndroidAppShowMemberPricingCardOnLaunchScreen = 13697;
	public static final int EBAndroidAppShowPopularHotelsCardOnLaunchScreen = 13314;
	public static final int EBAndroidAppShowCarWebView = 12913;
	public static final int EBAndroidAppHotelUpgrade = 13196;
	public static final int EBAndroidCheckoutPaymentTravelerInfo = 13280;
	public static final int EBAndroidAppLaunchShowGuestItinCard = 13455;
	public static final int EBAndroidAppLaunchShowActiveItinCard = 13357;
	public static final int EBAndroidAppHotelDetailsGalleryPeak = 13415;
	public static final int EBAndroidAppHotelResultsSortFaq = 13264;
	public static final int EBAndroidAppSimplifyFlightShopping = 13514;
	public static final int EBAndroidAppHotelResultsCardReadability = 13554;
	public static final int EBAndroidAppLOBAccentuating = 13718;
	public static final int EBAndroidAppShowCarWebViewOrbitz = 13660;
	public static final int EBAndroidAppShowCarWebViewTvly = 13658;
	public static final int ABTEST_IGNORE_DEBUG = -1;

	public enum DefaultVariant {
		CONTROL,
		BUCKETED
	}

	public enum DefaultTwoVariant {
		CONTROL,
		VARIANT1,
		VARIANT2
	}

	public enum LaunchScreenAirAttachVariant {
		CONTROL,
		UP_TO_XX_PERCENT_OFF,
		BECAUSE_YOU_BOOKED_A_FLIGHT
	}

	// Test ID's that we are bucketing the user for.
	public static List<Integer> getActiveTests() {
		List<Integer> testIDs = new ArrayList<>();
		testIDs.add(EBAndroidAppCarInsuranceIncludedCKO);
		testIDs.add(ExpediaAndroidAppAATestSep2015);
		testIDs.add(EBAndroidAppLXCategoryABTest);
		testIDs.add(EBAndroidAppLXFirstActivityListingExpanded);
		testIDs.add(EBAndroidAppHotelSecureCheckoutMessaging);
		testIDs.add(EBAndroidAppShowSignInFormOnLaunch);
		testIDs.add(EBAndroidAppLXRTROnSearchAndDetails);
		testIDs.add(EBAndroidAppFeedsOnLaunch);
		testIDs.add(EBAndroidAppHotelSearchScreenSoldOutTest);
		testIDs.add(EBAndroidAppHotelResultsPerceivedInstantTest);
		testIDs.add(EBAndroidAppSignInButtonYellow);
		testIDs.add(EBAndroidAppLXCrossSellOnHotelConfirmationTest);
		testIDs.add(EBAndroidAppSmartLockTest);
		testIDs.add(EBAndroidAppHotelFavoriteTest);
		testIDs.add(EBAndroidAppFlightsCreateTripPriceChangeAlert);
		testIDs.add(EBAndroidItinHotelGallery);
		testIDs.add(EBAndroidAppFlightRateDetailExpansion);
		testIDs.add(EBAndroidAppFlightUrgencyMessage);
		testIDs.add(EBAndroidAppBringUniversalCheckoutToLX);
		testIDs.add(EBAndroidAppLXFilterSearch);
		testIDs.add(EBAndroidAppWebViewCheckout);
		testIDs.add(EBAndroidAppCheckoutButtonText);
		testIDs.add(EBAndroidAppLXDisablePOISearch);
		testIDs.add(EBAndroidAppTripsUserReviews);
		testIDs.add(EBAndroidAppFlightsSeatClassAndBookingCode);
		testIDs.add(EBAndroidAppUniversalCheckoutMaterialForms);
		testIDs.add(EBAndroidAppMaterialFlightSearchRoundTripMessage);
		testIDs.add(EBAndroidAppHotelColorSwitch);
		testIDs.add(EBAndroidAppHotelRoomRateExpanded);
		testIDs.add(EBAndroidAppRemoveExpiredCreditCards);
		testIDs.add(EBAndroidAppMaterialFlightDistanceOnDetails);
		testIDs.add(EBAndroidAppHotelImageLoadLatency);
		testIDs.add(EBAndroidAppHotelPriceProminence);
		testIDs.add(EBAndroidAppFlightPremiumClass);
		testIDs.add(EBAndroidAppTripsNewSignInPage);
		testIDs.add(EBAndroidAppHotelHideNoReviewRating);
		testIDs.add(EBAndroidAppFlightByotSearch);
		testIDs.add(EBAndroidAppShowSignInCardOnLaunchScreen);
		testIDs.add(EBAndroidAppShowAirAttachMessageOnLaunchScreen);
		testIDs.add(EBAndroidAppHotelMemberPricingBadge);
		testIDs.add(EBAndroidAppHotelLoyaltyEarnMessage);
		testIDs.add(EBAndroidAppHotelUrgencyMessage);
		testIDs.add(EBAndroidAppDisabledSTPState);
		testIDs.add(EBAndroidAppTripsHotelSoftChangeWebView);
		testIDs.add(EBAndroidAppLXNavigateToSRP);
		testIDs.add(EBAndroidAppHotelRemoveAutoFocusAndAdvanceOnSearch);
		testIDs.add(EBAndroidAppShowMemberPricingCardOnLaunchScreen);
		testIDs.add(EBAndroidAppShowPopularHotelsCardOnLaunchScreen);
		testIDs.add(EBAndroidAppShowCarWebView);
		testIDs.add(EBAndroidAppHotelUpgrade);
		testIDs.add(EBAndroidCheckoutPaymentTravelerInfo);
		testIDs.add(EBAndroidAppLaunchShowGuestItinCard);
		testIDs.add(EBAndroidAppLaunchShowActiveItinCard);
		testIDs.add(EBAndroidAppHotelDetailsGalleryPeak);
		testIDs.add(EBAndroidAppHotelResultsSortFaq);
		testIDs.add(EBAndroidAppSimplifyFlightShopping);
		testIDs.add(EBAndroidAppHotelResultsCardReadability);
		testIDs.add(EBAndroidAppLOBAccentuating);
		testIDs.add(EBAndroidAppShowCarWebViewOrbitz);
		testIDs.add(EBAndroidAppShowCarWebViewTvly);
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
