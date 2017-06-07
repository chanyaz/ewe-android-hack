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
	public static final int EBAndroidAppTripsUserReviews = 13257;
	public static final int EBAndroidAppUniversalCheckoutMaterialForms = 12721;
	public static final int EBAndroidAppFlightsConfirmationItinSharing = 14137;
	public static final int EBAndroidAppTripsNewSignInPage = 13890;
	public static final int EBAndroidAppDisabledSTPState = 13825;
	public static final int EBAndroidAppShowSignInCardOnLaunchScreen = 13191;
	public static final int EBAndroidAppLXNavigateToSRP = 13152;
	public static final int EBAndroidAppWebViewCheckout = 13741;
	public static final int EBAndroidAppCheckoutButtonText = 13480;
	public static final int EBAndroidAppShowAirAttachMessageOnLaunchScreen = 13345;
	public static final int EBAndroidAppShowMemberPricingCardOnLaunchScreen = 13697;
	public static final int EBAndroidAppShowPopularHotelsCardOnLaunchScreen = 13314;
	public static final int EBAndroidAppHotelUpgrade = 13196;
	public static final int EBAndroidCheckoutPaymentTravelerInfo = 13280;
	public static final int EBAndroidAppLaunchShowGuestItinCard = 13455;
	public static final int EBAndroidAppLaunchShowActiveItinCard = 13357;
	public static final int EBAndroidAppItinCrystalSkin = 13812;
	public static final int EBAndroidAppUserOnboarding = 13548;
	public static final int EBAndroidLXVoucherRedemption = 14380;
	public static final int EBAndroidAppTripsDetailRemoveCalendar = 14201;
	public static final int EBAndroidAppFreeCancellationTooltip = 14513;

	public static final int ABTEST_IGNORE_DEBUG = -1;

	// Flight tests
	public static final int EBAndroidAppOfferInsuranceInFlightSummary = 12268;
	public static final int EBAndroidAppFlightByotSearch = 13202;
	public static final int EBAndroidAppFlightRateDetailExpansion = 12637;
	public static final int EBAndroidAppFlightsSeatClassAndBookingCode = 12763;
	public static final int EBAndroidAppSimplifyFlightShopping = 13514;
	public static final int EBAndroidAppFlightStaticSortFilter = 13842;
	public static final int EBAndroidAppFlightsMoreInfoOnOverview = 13505;
	public static final int EBAndroidAppFlightsCrossSellPackageOnFSR = 14183;
	public static final int EBAndroidAppFlightAATest = 14241;
	public static final int EBAndroidAppFlightSearchFormValidation = 13843;
	public static final int EBAndroidAppFlightHideFSRInfographic = 13844;
	public static final int EBAndroidAppFlightRetainSearchParams = 14297;


	// Hotel Tests
	public static final int EBAndroidAppHotelRoomRateExpanded = 13246;
	public static final int EBAndroidAppHotelImageLoadLatency = 12908;
	public static final int EBAndroidAppHotelLoyaltyEarnMessage = 13179;
	public static final int EBAndroidAppHotelUrgencyMessage = 13277;
	public static final int EBAndroidAppHotelDetailsGalleryPeak = 13415;
	public static final int EBAndroidAppHotelResultsSortFaq = 13264;
	public static final int EBAndroidAppHotelResultsCardReadability = 13554;
	public static final int EBAndroidAppHotelLPASEndpoint = 13929;
	public static final int EBAndroidAppHotelNoStickyETP = 13904;
	public static final int EBAndroidAppHotelPinnedSearch = 13563;
	public static final int EBAndroidAppHotelGroupRoomRate = 14591;
	public static final int EBAndroidAppHotelHideSearch = 14271;
	public static final int EBAndroidAppHotelSortCallToAction = 14332;

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

	// Packages Tests
	public static final int EBAndroidAppPackagesRemoveBundleOverview = 13655;

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
		testIDs.add(EBAndroidAppFlightRateDetailExpansion);
		testIDs.add(EBAndroidAppBringUniversalCheckoutToLX);
		testIDs.add(EBAndroidAppLXFilterSearch);
		testIDs.add(EBAndroidAppWebViewCheckout);
		testIDs.add(EBAndroidAppCheckoutButtonText);
		testIDs.add(EBAndroidAppLXDisablePOISearch);
		testIDs.add(EBAndroidAppTripsUserReviews);
		testIDs.add(EBAndroidAppFlightsSeatClassAndBookingCode);
		testIDs.add(EBAndroidAppUniversalCheckoutMaterialForms);
		testIDs.add(EBAndroidAppFreeCancellationTooltip);
		testIDs.add(EBAndroidAppFlightsConfirmationItinSharing);
		testIDs.add(EBAndroidAppHotelRoomRateExpanded);
		testIDs.add(EBAndroidAppHotelImageLoadLatency);
		testIDs.add(EBAndroidAppTripsNewSignInPage);
		testIDs.add(EBAndroidAppFlightByotSearch);
		testIDs.add(EBAndroidAppShowSignInCardOnLaunchScreen);
		testIDs.add(EBAndroidAppShowAirAttachMessageOnLaunchScreen);
		testIDs.add(EBAndroidAppHotelLoyaltyEarnMessage);
		testIDs.add(EBAndroidAppHotelUrgencyMessage);
		testIDs.add(EBAndroidAppDisabledSTPState);
		testIDs.add(EBAndroidAppLXNavigateToSRP);
		testIDs.add(EBAndroidAppHotelLPASEndpoint);
		testIDs.add(EBAndroidAppShowMemberPricingCardOnLaunchScreen);
		testIDs.add(EBAndroidAppShowPopularHotelsCardOnLaunchScreen);
		testIDs.add(EBAndroidAppHotelUpgrade);
		testIDs.add(EBAndroidCheckoutPaymentTravelerInfo);
		testIDs.add(EBAndroidAppLaunchShowGuestItinCard);
		testIDs.add(EBAndroidAppLaunchShowActiveItinCard);
		testIDs.add(EBAndroidAppHotelDetailsGalleryPeak);
		testIDs.add(EBAndroidAppHotelResultsSortFaq);
		testIDs.add(EBAndroidAppFlightsMoreInfoOnOverview);
		testIDs.add(EBAndroidAppFlightsCrossSellPackageOnFSR);
		testIDs.add(EBAndroidAppSimplifyFlightShopping);
		testIDs.add(EBAndroidAppHotelResultsCardReadability);
		testIDs.add(EBAndroidAppHotelNoStickyETP);
		testIDs.add(EBAndroidAppItinCrystalSkin);
		testIDs.add(EBAndroidAppFlightStaticSortFilter);
		testIDs.add(EBAndroidAppCarsWebViewUK);
		testIDs.add(EBAndroidAppCarsWebViewTVLY);
		testIDs.add(EBAndroidAppCarsWebViewUS);
		testIDs.add(EBAndroidAppCarsWebViewCT);
		testIDs.add(EBAndroidAppCarsWebViewEB);
		testIDs.add(EBAndroidAppCarsWebViewAUNZ);
		testIDs.add(EBAndroidAppCarsWebViewCA);
		testIDs.add(EBAndroidAppCarsWebViewOB);
		testIDs.add(EBAndroidAppCarsWebViewEMEA);
		testIDs.add(EBAndroidAppUserOnboarding);
		testIDs.add(EBAndroidAppPackagesRemoveBundleOverview);
		testIDs.add(EBAndroidAppFlightAATest);
		testIDs.add(EBAndroidAppFlightSearchFormValidation);
		testIDs.add(EBAndroidAppHotelPinnedSearch);
		testIDs.add(EBAndroidAppHotelGroupRoomRate);
		testIDs.add(EBAndroidAppFlightHideFSRInfographic);
		testIDs.add(EBAndroidLXVoucherRedemption);
		testIDs.add(EBAndroidAppHotelHideSearch);
		testIDs.add(EBAndroidAppTripsDetailRemoveCalendar);
		testIDs.add(EBAndroidAppFlightRetainSearchParams);
		testIDs.add(EBAndroidAppHotelSortCallToAction);
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
