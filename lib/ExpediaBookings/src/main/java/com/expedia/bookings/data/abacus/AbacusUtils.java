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
	//TODO: Update key
	public static final String EBAndroidETPTest = "ExpediaAndroidAppETPDefaultNow";
	//TODO: Sync on key
	public static final String EBAndroidHotelBookButtonPlacementTest = "EBAndroidAppHISBookAboveFold";

	public static enum Variante {
		CONTROL,
		BUCKETED
	}

	public static enum BookingVariante {
		CONTROL,
		BOOK_ABOVE_FOLD,
	 	SELECT_ROOM_ABOVE_FOLD
	}

}
