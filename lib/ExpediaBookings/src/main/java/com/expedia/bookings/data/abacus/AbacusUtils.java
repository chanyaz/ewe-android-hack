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

	public static final int EBAndroidAppSRPercentRecommend = 7373;
	public static final int EBAndroidAppHotelHSRSalePinTest = 7535;
	public static final int EBAndroidAppHotelETPSearchResults = 7532;
	public static final int EBAndroidAppHSRMapIconTest = 7772;
	public static final int EBAndroidAppCarInsuranceIncludedCKO = 7774;
	public static final int EBAndroidAppHotelHCKOTraveler = 7775;
	public static final int EBAndroidAppHotelPayLaterCouponMessaging = 7777;
	public static final int EBAndroidAppHotel3xMessaging = 7973;
	public static final int EBAndroidAppHotelCollapseAmenities = 8126;
	public static final int EBAndroidAppHotelShowAddressMapInReceipt = 8129;
	public static final int EBAndroidHotelCKOMerEmailGuestOpt = 8127;
	public static final int EBAndroidAppSplitGTandActivities = 8369;
	public static final int ExpediaAndroidAppAATestSep2015 = 8706;
	public static final int EBAndroidAppHotelsV2SuperlativeReviewsABTest = 8688;
	public static final int EBAndroidAppLXCategoryABTest = 9165;
	public static final int EBAndroidAppHotelRecentSearchTest = 8692;
	public static final int EBAndroidAppLXFirstActivityListingExpanded = 9467;
	public static final int EBAndroidAppHotelResultMapTest = 9474;
	public static final int EBAndroidAppHotelTravelerTest = 9478;
	public static final int EBAndroidAppHotelShowExampleNamesTest = 9475;
	public static final int EBAndroidAppHotelPriceBreakDownTest = 9477;
	public static final int EBAndroidAppSignInMessagingTest = 9476;
	public static final int EBAndroidAppHotelsMemberDealTest = 9044;
	public static final int EBAndroidAppHotelCKOCreditDebitTest = 9642;
	public static final int EBAndroidAppFlightsRoundtripMessageTest = 9636;
	public static final int EBAndroidAppLXRecommendedActivitiesTest = 9639;
	public static final int EBAndroidAppFlightsNumberOfTicketsUrgencyTest = 9897;
	public static final int EBAndroidAppHotelSecureCheckoutMessaging = 9898;
	public static final int EBAndroidAppShowSignInOnLaunch = 8687;
	public static final int EBAndroidAppLXRTROnSearchAndDetails = 10000;
	public static final int EBAndroidAppFeedsOnLaunch = 10065;
	public static final int EBAndroidAppLaunchScreenTest = 10639;
	public static final int INS_AddInsuranceOnMobileAppFlights = 10001; // TODO: change to properly named, Android-specific test
	public static final int EBAndroidAppLXCrossSellOnHotelConfirmationTest = 10556;

	public static final int ABTEST_IGNORE_DEBUG = -1;

	public enum DefaultVariate {
		CONTROL,
		BUCKETED
	}

	public enum HSearchInfluenceMessagingVariate {
		CONTROL,
		WORKING_HARD,
		SEARCHING_HUNDREDS,
		NO_TEXT
	}

	public enum HISMapIconVariate {
		CONTROL,
		MAP_PIN,
		TEXT_ONLY
	}

	public enum HotelSuperlativeReviewsVariate {
		CONTROL,
		WITH_COLOR_NO_SUPERLATIVES,
		NO_COLOR_WITH_SUPERLATIVES,
		WITH_COLOR_WITH_SUPERLATIVES
	}

	public enum HotelPriceBreakDownVariate {
		CONTROL,
		DUE_TODAY,
		TRIP_TOTAL
	}

	public enum HotelSignInMessagingVariate {
		CONTROL,
		EXCLUSIVE_MEMBER_MESSAGE,
		TRIPLE_POINT_MESSAGE,
		TRIP_ALERT_MESSAGE
	}

	// Test ID's that we are bucketing the user for.
	public static List<Integer> getActiveTests() {
		List<Integer> testIDs = new ArrayList<>();
		testIDs.add(EBAndroidAppSRPercentRecommend);
		testIDs.add(EBAndroidAppHotelHSRSalePinTest);
		testIDs.add(EBAndroidAppHotelETPSearchResults);
		testIDs.add(EBAndroidAppHSRMapIconTest);
		testIDs.add(EBAndroidAppCarInsuranceIncludedCKO);
		testIDs.add(EBAndroidAppHotelHCKOTraveler);
		testIDs.add(EBAndroidAppHotelPayLaterCouponMessaging);
		testIDs.add(EBAndroidAppHotel3xMessaging);
		testIDs.add(EBAndroidAppHotelCollapseAmenities);
		testIDs.add(EBAndroidAppHotelShowAddressMapInReceipt);
		testIDs.add(EBAndroidHotelCKOMerEmailGuestOpt);
		testIDs.add(EBAndroidAppSplitGTandActivities);
		testIDs.add(ExpediaAndroidAppAATestSep2015);
		testIDs.add(EBAndroidAppHotelsV2SuperlativeReviewsABTest);
		testIDs.add(EBAndroidAppLXCategoryABTest);
		testIDs.add(EBAndroidAppHotelRecentSearchTest);
		testIDs.add(EBAndroidAppLXFirstActivityListingExpanded);
		testIDs.add(EBAndroidAppHotelResultMapTest);
		testIDs.add(EBAndroidAppHotelTravelerTest);
		testIDs.add(EBAndroidAppHotelShowExampleNamesTest);
		testIDs.add(EBAndroidAppHotelPriceBreakDownTest);
		testIDs.add(EBAndroidAppSignInMessagingTest);
		testIDs.add(EBAndroidAppHotelsMemberDealTest);
		testIDs.add(EBAndroidAppHotelCKOCreditDebitTest);
		testIDs.add(EBAndroidAppFlightsRoundtripMessageTest);
		testIDs.add(EBAndroidAppLXRecommendedActivitiesTest);
		testIDs.add(EBAndroidAppFlightsNumberOfTicketsUrgencyTest);
		testIDs.add(EBAndroidAppHotelSecureCheckoutMessaging);
		testIDs.add(EBAndroidAppShowSignInOnLaunch);
		testIDs.add(EBAndroidAppLXRTROnSearchAndDetails);
		testIDs.add(EBAndroidAppFeedsOnLaunch);
		testIDs.add(EBAndroidAppLaunchScreenTest);
		testIDs.add(INS_AddInsuranceOnMobileAppFlights);
		testIDs.add(EBAndroidAppLXCrossSellOnHotelConfirmationTest);
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
