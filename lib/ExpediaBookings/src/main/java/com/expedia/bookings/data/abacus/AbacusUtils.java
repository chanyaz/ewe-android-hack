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

	public static final int EBAndroidAATest = 6714;
	public static final int EBAndroidAppLaunchScreenTest = 7369;
	public static final int EBAndroidAppSRPercentRecommend = 7373;
	public static final int EBAndroidAppFlightConfCarsXsell = 7370;
	public static final int EBAndroidAppHotelHSRSalePinTest = 7535;
	public static final int EBAndroidAppFlightConfLXXsell = 7533;
	public static final int EBAndroidAppHotelETPSearchResults = 7532;
	public static final int EBAndroidAppHotelItinLXXsell = 7612;
	public static final int EBAndroidAppHSRMapIconTest = 7772;
	public static final int EBAndroidAppCarRatesCollapseTopListing = 7769;
	public static final int EBAndroidAppCarInsuranceIncludedCKO = 7774;
	public static final int EBAndroidAppHotelHCKOTraveler = 7775;
	public static final int EBAndroidAppHotelPayLaterCouponMessaging = 7777;
	public static final int EBAndroidAppHotel3xMessaging = 7973;
	public static final int EBAndroidAppHotelCollapseAmenities = 8126;
	public static final int EBAndroidAppHotelShowAddressMapInReceipt = 8129;
	public static final int EBAndroidAppPaySuppressGoogleWallet = 8148;
	public static final int EBAndroidAppFlightMissingTravelerInfoCallout = 8128;
	public static final int EBAndroidHotelCKOMerEmailGuestOpt = 8127;
	public static final int EBAndroidAppSplitGTandActivities = 8369;
	public static final int ExpediaAndroidAppAATestSep2015 = 8706;
	public static final int EBAndroidAppHotelsABTest = 8624;
	public static final int EBAndroidAppHotelsV2SuperlativeReviewsABTest = 8688;


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

	public enum FMissingTravelerCalloutVariate {
		CONTROL,
		SINGLE_LINE_CALLOUT,
		SECOND_LINE_CALLOUT
	}

	public enum HotelSuperlativeReviewsVariate {
		CONTROL,
		WITH_COLOR_NO_SUPERLATIVES,
		NO_COLOR_WITH_SUPERLATIVES,
		WITH_COLOR_WITH_SUPERLATIVES
	}

	// Test ID's that we are bucketing the user for.
	public static List<Integer> getActiveTests() {
		List<Integer> testIDs = new ArrayList<>();
		testIDs.add(EBAndroidAATest);
		testIDs.add(EBAndroidAppLaunchScreenTest);
		testIDs.add(EBAndroidAppSRPercentRecommend);
		testIDs.add(EBAndroidAppFlightConfCarsXsell);
		testIDs.add(EBAndroidAppHotelHSRSalePinTest);
		testIDs.add(EBAndroidAppFlightConfLXXsell);
		testIDs.add(EBAndroidAppHotelETPSearchResults);
		testIDs.add(EBAndroidAppHotelItinLXXsell);
		testIDs.add(EBAndroidAppHSRMapIconTest);
		testIDs.add(EBAndroidAppCarRatesCollapseTopListing);
		testIDs.add(EBAndroidAppCarInsuranceIncludedCKO);
		testIDs.add(EBAndroidAppHotelHCKOTraveler);
		testIDs.add(EBAndroidAppHotelPayLaterCouponMessaging);
		testIDs.add(EBAndroidAppHotel3xMessaging);
		testIDs.add(EBAndroidAppHotelCollapseAmenities);
		testIDs.add(EBAndroidAppHotelShowAddressMapInReceipt);
		testIDs.add(EBAndroidAppPaySuppressGoogleWallet);
		testIDs.add(EBAndroidAppFlightMissingTravelerInfoCallout);
		testIDs.add(EBAndroidHotelCKOMerEmailGuestOpt);
		testIDs.add(EBAndroidAppSplitGTandActivities);
		testIDs.add(ExpediaAndroidAppAATestSep2015);
		testIDs.add(EBAndroidAppHotelsABTest);
		testIDs.add(EBAndroidAppHotelsV2SuperlativeReviewsABTest);
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
