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
	public static final String DISPLAY_MODAL_VIEW = "DISPLAY_MODAL_VIEW";
	public static final String DISCOUNT_RATE = "DISCOUNT_RATE";
	public static final String CONTENT_STRING = "CONTENT_STRING";
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
}
