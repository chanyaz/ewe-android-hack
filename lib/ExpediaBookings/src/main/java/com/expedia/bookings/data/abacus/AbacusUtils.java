package com.expedia.bookings.data.abacus;

public class AbacusUtils {

	/**
	 * ACTIVE KEYS
	 * <p/>
	 * When new tests need to be added just add a new key to this class
	 * Then call isUserBucketedForTest(String key) to check if the user is
	 * participating in the AB Test.
	 */
	public static final String EBAndroidAATest = "ExpediaAndroidAppAATest";
	public static final String EBAndroidETPTest = "ExpediaAndroidAppETPDefaultNow";
	public static final String EBAndroidAppHISBookAboveFoldTest = "EBAndroidAppHISBookAboveFold";
	public static final String EBAndroidAppHISFreeCancellationTest = "EBAndroidAppHISFreeCancellation";

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

	public static enum HISFreeCancellationVariate {
		CONTROL,
		BUCKETED
	}

}
