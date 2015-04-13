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
	public static final int EBAndroidETPTest = 6800;
	public static final int EBAndroidAppHISBookAboveFoldTest = 6815;
	public static final int EBAndroidAppHISFreeCancellationTest = 6801;
	public static final int EBAndroidAppHISSwipablePhotosTest = 7021;
	public static final int EBAndroidAppFlightCKOFreeCancelationTest = 7005;
	public static final int EBAndroidAppHSearchInfluenceMessagingTest = 7017;
	public static final int EBAndroidAppLaunchScreenTest = 7369;
	public static final int EBAndroidAppAddORToForm = 7372;
	public static final int EBAndroidAppSRPercentRecommend = 7373;
	public static final int EBAndroidAppFlightConfCarsXsell = 7370;

	public static final int ABTEST_IGNORE_DEBUG = -1;

	public static enum DefaultVariate {
		CONTROL,
		BUCKETED
	}

	public static enum HISBookAboveFoldVariate {
		CONTROL,
		BOOK_ABOVE_FOLD,
		SELECT_ROOM_ABOVE_FOLD
	}

	public static enum HSearchInfluenceMessagingVariate {
		CONTROL,
		WORKING_HARD,
		SEARCHING_HUNDREDS,
		NO_TEXT
	}

	// Test ID's that we are bucketing the user for.
	public static List<Integer> getActiveTests() {
		List<Integer> testIDs = new ArrayList<>();
		testIDs.add(EBAndroidAATest);
		testIDs.add(EBAndroidETPTest);
		testIDs.add(EBAndroidAppHISBookAboveFoldTest);
		testIDs.add(EBAndroidAppHISFreeCancellationTest);
		testIDs.add(EBAndroidAppHISSwipablePhotosTest);
		testIDs.add(EBAndroidAppFlightCKOFreeCancelationTest);
		testIDs.add(EBAndroidAppHSearchInfluenceMessagingTest);
		testIDs.add(EBAndroidAppLaunchScreenTest);
		testIDs.add(EBAndroidAppAddORToForm);
		testIDs.add(EBAndroidAppSRPercentRecommend);
		testIDs.add(EBAndroidAppFlightConfCarsXsell);
		return testIDs;
	}

}
