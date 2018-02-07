package com.expedia.bookings.data;

public class Codes {
	// Intent extras
	public static final String SEARCH_PARAMS = "SEARCH_PARAMS";
	public static final String LOB_NOT_SUPPORTED = "LOB_NOT_SUPPORTED";

	//If the calling activity has already set the search params, we don't need to clear/reset them
	public static final String TAG_EXTERNAL_SEARCH_PARAMS = "TAG_EXTERNAL_SEARCH_PARAMS";

	// Indicates that we came from a deeplink, which might entail different behavior
	public static final String FROM_DEEPLINK = "TAG_FROM_DEEPLINK";
	public static final String FROM_DEEPLINK_TO_DETAILS = "TAG_FROM_DEEPLINK_TO_DETAILS";

	// Indicates we should open search automatically when launching a screen
	public static final String EXTRA_OPEN_SEARCH = "EXTRA_OPEN_SEARCH";
	public static final String EXTRA_OPEN_RESULTS = "EXTRA_OPEN_RESULTS";

	// For EF
	public final static String TRAVELER_INDEX = "TRAVELER_INDEX";
	public final static String CARS_PRODUCT_KEY = "CARS_PRODUCT_KEY";

	// From Member Only Deals Or Last Minute Deals
	public final static String DEALS = "DEALS";
}
