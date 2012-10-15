package com.expedia.bookings.data;

public class Codes {
	// Intent extras
	public static final String SESSION = "SESSION";
	public static final String SEARCH_PARAMS = "SEARCH_PARAMS";
	public static final String PROPERTY_LOCATION_PREFIX = "USE_CURRENT_LOCATION";
	public static final String PROPERTY = "PROPERTY";
	public static final String SELECTED_IMAGE = "SELECTED_IMAGE";
	public static final String AVAILABILITY_RESPONSE = "AVAILABILITY_RESPONSE";
	public static final String RATE = "RATE";
	public static final String RATE_RULES = "RATE_RULES";
	public static final String BILLING_INFO = "BILLING_INFO";
	public static final String BOOKING_RESPONSE = "BOOKING_RESPONSE";
	public static final String SEARCH_ERROR = "SEARCH_ERROR";
	public static final String WIDGET_LAST_LOCATION = "WIDGET_LAST_LOCATION";
	public static final String APP_WIDGET_ID = "APP_WIDGET_ID";
	public static final String BRANDING_TITLE = "BRANDING_TITLE";
	public static final String BRANDING_SAVINGS = "BRANDING_SAVINGS";
	public static final String DISTANCE_OF_MAX_SAVINGS = "DISTANCE_OF_MAX_SAVINGS";
	public static final String SHOW_BRANDING = "SHOW_BRANDING";
	public static final String OPENED_FROM_WIDGET = "OPENED_FROM_WIDGET";
	public static final String DISPLAY_MODAL_VIEW = "DISPLAY_MODAL_VIEW";
	public static final String DISCOUNT_RATE = "DISCOUNT_RATE";

	// Used in onNewIntent(), if the calling Activity wants the SearchActivity to start fresh
	public static final String EXTRA_NEW_SEARCH = "EXTRA_NEW_SEARCH";

	//If the calling activity has already set the search params, we don't need to clear/reset them
	public static final String TAG_EXTERNAL_SEARCH_PARAMS = "TAG_EXTERNAL_SEARCH_PARAMS";

	// HockeyApp app id, used for communicating with servers to fetch new versions, send crash reports
	public static final String HOCKEY_APP_ID = "abe5838f6a36a535ef94f53ab2f1a659";

	// For EF
	public final static String TRAVELER_INDEX = "TRAVELER_INDEX";
}
