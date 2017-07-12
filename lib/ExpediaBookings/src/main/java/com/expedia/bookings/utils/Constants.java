package com.expedia.bookings.utils;

public class Constants {
	public static final String PRODUCT_FLIGHT = "flight";
	public static final String PRODUCT_HOTEL = "hotel";
	public static final String PRODUCT_PACKAGE_LOADING = "packageLoading";
	public static final int HOTEL_REQUEST_CODE = 101;
	public static final String RAW_TEXT_SEARCH = "RAW_TEXT_SEARCH";
	public static final int PERMISSION_REQUEST_LOCATION = 7;
	public static final int PERMISSION_REQUEST_LOCATION_WITH_RATIONALE = 8;
	public static final int PACKAGE_FLIGHT_OUTBOUND_REQUEST_CODE = 102;
	public static final int PACKAGE_FLIGHT_RETURN_REQUEST_CODE = 103;
	public static final int PACKAGE_API_ERROR_RESULT_CODE = 104;
	public static final int FLIGHT_REQUEST_CODE = 105;
	public static final int PACKAGE_PARAMS_NULL_RESTORE = 106;

	//For more info on Package trip type https://confluence/display/POS/GetPackages+API
	public static final String PACKAGE_TRIP_TYPE = "2"; // 2 mean BYOT (build your own trip)
	public static final String NUMBER_OF_ROOMS = "1";
	public static final String PACKAGE_HOTEL_DELTA_PRICE_TYPE = "per night all travelers";
	public static final String PACKAGE_CHANGE_HOTEL = "CHANGE_HOTEL";
	public static final String PACKAGE_CHANGE_FLIGHT = "CHANGE_FLIGHT";
	public static final String PACKAGE_FILTER_CHANGE_FLIGHT = "filterChangeFlight";
	public static final String PACKAGE_HOTEL_OFFERS_ERROR = "package hotel offers error";
	public static final String PACKAGE_API_ERROR = "packageApiError";

	// itin airline code for check in
	public static final String ITIN_CHECK_IN_AIRLINE_NAME = "ITIN_CHECK_IN_AIRLINE_NAME";
	public static final String ITIN_CHECK_IN_AIRLINE_CODE = "ITIN_CHECK_IN_AIRLINE_CODE";
	public static final String ITIN_CHECK_IN_CONFIRMATION_CODE = "ITIN_CHECK_IN_CONFIRMATION_CODE";
	public static final int ITIN_CHECK_IN_WEBPAGE_CODE = 21;
	public static final String ITIN_FLIGHT_TRIP_LEGS = "ITIN_FLIGHT_TRIP_LEGS";
	public static final String ITIN_IS_SPLIT_TICKET = "ITIN_IS_SPLIT_TICKET";

	public static final String TAG_CALENDAR_DIALOG = "TAG_CALENDAR_DIALOG";
	public static final String TAG_RATING_DIALOG = "TAG_RATING_DIALOG";
	// constants for prefs changes
	public static final int REQUEST_SETTINGS = 11;
	public static final int RESULT_NO_CHANGES = 1;
	public static final int RESULT_CHANGED_PREFS = 2;
	// customer returned from room cancellation web view
	public static final int ITIN_CANCEL_ROOM_WEBPAGE_CODE = 31;
	// customer returned from room soft change web view
	public static final int ITIN_SOFT_CHANGE_WEBPAGE_CODE = 41;
	// customer returned from room upgrade web view
	public static final int ITIN_ROOM_UPGRADE_WEBPAGE_CODE = 51;
	public static final String ITIN_SOFT_CHANGE_TRIP_ID = "ITIN_SOFT_CHANGE_TRIP_ID";
	public static final String ITIN_CANCEL_ROOM_BOOKING_TRIP_ID = "ITIN_CANCEL_ROOM_BOOKING_TRIP_ID";
	public static final String ITIN_ROOM_UPGRADE_TRIP_ID = "ITIN_ROOM_UPGRADE_TRIP_ID";
	public static final String PACKAGE_LOAD_HOTEL_ROOM = "LOAD_HOTEL_ROOM";
	public static final String PACKAGE_LOAD_OUTBOUND_FLIGHT = "LOAD_OUTBOUND_FLIGHT";
	public static final String PACKAGE_LOAD_INBOUND_FLIGHT = "LOAD_INBOUND_FLIGHT";
	public static final String REQUEST = "REQUEST";
	public static final String INTENT_PERFORM_HOTEL_SEARCH = "INTENT_PERFORM_HOTEL_SEARCH";

	public static final String RAIL_CLIENT_CODE = "1002";
	public static final String RAIL_STANDARD_FARE_CLASS = "Standard";

	public static final String AIRLINE_SQUARE_LOGO_BASE_URL = "https://images.trvl-media.com/media/content/expus/graphics/static_content/fusion/v0.1b/images/airlines/s/**_sq.jpg";

	public static final int PERMISSION_WRITE_EXTERNAL_STORAGE_BUGSHAKER = 11;

	public static final double PRICE_CHANGE_NOTIFY_CUTOFF = .01;

	public static final String DEEPLINK_KEYWORD = "/mobile/deeplink";

	// Member Only Deals
	public static final String MOD_SITE_ID = "1";
	public static final String MOD_PRODUCT_TYPE = "Hotel";
	public static final String MOD_GROUP_BY = "destination";
	public static final int MOD_DESTINATION_LIMIT = 10;
	public static final String MOD_CLIENT_ID = "ebad";
	public static final String MOD_DESTINATION_IMAGE_BASE_URL = "https://a.travel-assets.com/dynamic_images/{regionId}.jpg";
	public static final float MOD_IMAGE_SHIFT = -0.2f;

	//Save hotels map state
	public static final String HOTELS_MAP_STATE = "HOTELS_MAP_STATE";

	//	New User Onboarding
	public static final int SWIPE_MIN_DISTANCE = 120;
	public static final int SWIPE_THRESHOLD_VELOCITY = 250;
	public static final String ONBOARDING_BOOKING_PAGE_URL = "https://media.expedia.com/mobiata/mobile/apps/ExpediaBooking/Onboarding/expedia-onboarding-ad-1.jpg";
	public static final String ONBOARDING_TRIP_PAGE_URL = "https://media.expedia.com/mobiata/mobile/apps/ExpediaBooking/Onboarding/expedia-onboarding-ad-2.jpg";
	public static final String ONBOARDING_REWARD_PAGE_URL = "https://media.expedia.com/mobiata/mobile/apps/ExpediaBooking/Onboarding/expedia-onboarding-ad-3.jpg";

	//Business Region
	public static final String ASIA_PACIFIC_REGION = "APAC";

	public static final String FEATURE_SUBPUB = "SubPub";
}
