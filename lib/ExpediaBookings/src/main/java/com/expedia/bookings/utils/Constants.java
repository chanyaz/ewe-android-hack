package com.expedia.bookings.utils;

public class Constants {
	public static final String PRODUCT_FLIGHT = "flight";
	public static final String PRODUCT_HOTEL = "hotel";
	public static final int HOTEL_REQUEST_CODE = 101;
	public static final String RAW_TEXT_SEARCH = "RAW_TEXT_SEARCH";
	public static final String DEFAULT_HIDE_FIELDS_CONFIG_ID = "5";
	public static final int PERMISSION_REQUEST_LOCATION = 7;
	public static final int PACKAGE_FLIGHT_OUTBOUND_REQUEST_CODE = 102;
	public static final int PACKAGE_FLIGHT_RETURN_REQUEST_CODE = 103;
	public static final int PACKAGE_API_ERROR_RESULT_CODE = 104;
	public static final int FLIGHT_REQUEST_CODE = 105;
	public static final int PACKAGE_PARAMS_NULL_RESTORE = 106;
	public static final String APP_DATABASE_NAME = "app-database";

	//For more info on Package trip type https://confluence/display/POS/GetPackages+API
	public static final String PACKAGE_TRIP_TYPE = "2"; // 2 mean BYOT (build your own trip)
	public static final String NUMBER_OF_ROOMS = "1";
	public static final String PACKAGE_HOTEL_DELTA_PRICE_TYPE = "per night all travelers";
	public static final String PACKAGE_CHANGE_HOTEL = "CHANGE_HOTEL";
	public static final String PACKAGE_CHANGE_FLIGHT = "CHANGE_FLIGHT";
	public static final String PACKAGE_FILTER_CHANGE_FLIGHT = "filterChangeFlight";
	public static final String PACKAGE_HOTEL_OFFERS_ERROR = "package hotel offers error";
	public static final String PACKAGE_HOTEL_OFFERS_ERROR_KEY = "package hotel offers error key";
	public static final String PACKAGE_HOTEL_DID_INFOSITE_CALL_FAIL = "package hotel did infosite call fail";
	public static final String PACKAGE_API_ERROR = "packageApiError";

	// itin airline code for check in
	public static final String ITIN_CHECK_IN_AIRLINE_NAME = "ITIN_CHECK_IN_AIRLINE_NAME";
	public static final String ITIN_CHECK_IN_AIRLINE_CODE = "ITIN_CHECK_IN_AIRLINE_CODE";
	public static final String ITIN_CHECK_IN_CONFIRMATION_CODE = "ITIN_CHECK_IN_CONFIRMATION_CODE";
	public static final int ITIN_CHECK_IN_WEBPAGE_CODE = 21;
	public static final String ITIN_FLIGHT_TRIP_LEGS = "ITIN_FLIGHT_TRIP_LEGS";
	public static final String ITIN_IS_SPLIT_TICKET = "ITIN_IS_SPLIT_TICKET";

	public static final String TAG_CALENDAR_DIALOG = "TAG_CALENDAR_DIALOG";
	// constants for prefs changes
	public static final int REQUEST_SETTINGS = 11;
	public static final int RESULT_NO_CHANGES = 1;
	public static final int RESULT_CHANGED_PREFS = 2;
	public static final String PACKAGE_LOAD_HOTEL_ROOM = "LOAD_HOTEL_ROOM";
	public static final String PACKAGE_LOAD_OUTBOUND_FLIGHT = "LOAD_OUTBOUND_FLIGHT";
	public static final String PACKAGE_LOAD_INBOUND_FLIGHT = "LOAD_INBOUND_FLIGHT";
	public static final String REQUEST = "REQUEST";
	public static final String INTENT_PERFORM_HOTEL_SEARCH = "INTENT_PERFORM_HOTEL_SEARCH";
	// customer returned from hotel itin web view
	public static final int ITIN_WEBVIEW_REFRESH_ON_EXIT_CODE = 61;
	public static final String ITIN_WEBVIEW_REFRESH_ON_EXIT_TRIP_NUMBER = "ITIN_WEBVIEW_REFRESH_ON_EXIT_TRIP_NUMBER";

	public static final String RAIL_CLIENT_CODE = "1002";
	public static final String RAIL_STANDARD_FARE_CLASS = "Standard";

	public static final String AIRLINE_SQUARE_LOGO_BASE_URL = "https://images.trvl-media.com/media/content/expus/graphics/static_content/fusion/v0.1b/images/airlines/s/**_sq.jpg";

	// Smart Offer Service
	public static final float SOS_IMAGE_SHIFT = -0.2f;

	//Save hotels map state
	public static final String HOTELS_MAP_STATE = "HOTELS_MAP_STATE";

	//	New User Onboarding
	public static final int SWIPE_MIN_DISTANCE = 120;
	public static final int SWIPE_THRESHOLD_VELOCITY = 250;
	public static final String ONBOARDING_BOOKING_PAGE_URL = "https://images.trvl-media.com/mobiata/mobile/apps/ExpediaBooking/Onboarding/expedia-onboarding-ad-1.jpg";
	public static final String ONBOARDING_TRIP_PAGE_URL = "https://images.trvl-media.com/mobiata/mobile/apps/ExpediaBooking/Onboarding/expedia-onboarding-ad-2.jpg";
	public static final String ONBOARDING_REWARD_PAGE_URL = "https://images.trvl-media.com/mobiata/mobile/apps/ExpediaBooking/Onboarding/expedia-onboarding-ad-3.jpg";

	//Flight API feature override constants
	public static final String FEATURE_SUBPUB = "SubPub";
	public static final String FEATURE_EVOLABLE = "GetEvolable";
	public static final String FEATURE_FLEX = "Flex";

	//Location Permission Prompt Limit
	public static final int LOCATION_PROMPT_LIMIT = 3;

	// Krazyglue
	public static final String KRAZY_GLUE_PARTNER_ID = "expedia-hot-mobile-conf";
	public static final String KRAZY_GLUE_API_KEY = "99e4957f-c45f-4f90-993f-329b32e53ca1";
	public static final String KRAZY_GLUE_BASE_URL = "/xsell-api/1.0/offers";

	//Meso
	public static final String MESO_DEV_URL_PATH = "/11850712/expedia.us_en/home/all/ANDROID1";
	public static final String MESO_PROD_URL_PATH = "/23171577/expedia.us_en/home/all/ANDROID1";
	public static final String MESO_DEV_HOTEL_TEMPLATEID = "11751981";
	public static final String MESO_DEV_DESTINATION_TEMPLATEID = "11749411";
	public static final String MESO_PROD_HOTEL_TEMPLATEID = "11754433";
	public static final String MESO_PROD_DESTINATION_TEMPLATEID = "11754755";

	public static final String MESO_LAS_VEGAS_BG_URL = "https://a.travel-assets.com/dynamic_images/178276.jpg";
	public static final String MESO_LOS_ANGELES_BG_URL = "https://a.travel-assets.com/dynamic_images/178280.jpg";
	public static final String MESO_MIAMI_BG_URL = "https://a.travel-assets.com/dynamic_images/178286.jpg";
	public static final String MESO_CANCUN_BG_URL = "https://a.travel-assets.com/dynamic_images/179995.jpg";
	public static final String MESO_SAN_DIEGO_BG_URL = "https://a.travel-assets.com/dynamic_images/3073.jpg";

	public static final String MESO_LAS_VEGAS_WEBVIEW_URL = "https://viewfinder.expedia.com/features/vintage-las-vegas";
	public static final String MESO_LOS_ANGELES_WEBVIEW_URL = "https://viewfinder.expedia.com/los-angeles/a-perfect-day-in-los-angeles";
	public static final String MESO_MIAMI_WEBVIEW_URL = "https://viewfinder.expedia.com/miami/top-10-things-to-do-in-miami";
	public static final String MESO_CANCUN_WEBVIEW_URL = "https://viewfinder.expedia.com/features/cancun-itineraries-schedule";
	public static final String MESO_SAN_DIEGO_WEBVIEW_URL = "https://viewfinder.expedia.com/san-diego/top-10-things-to-do-in-san-diego";

	//LX
	public static final int LX_MIN_DISCOUNT_PERCENTAGE = 5;
	public static final int LX_CALENDAR_MAX_DATE_SELECTION = 14;
	public static final String LX_AIR_HOTEL_MIP = "AirHotelAttachMip";
	public static final String LX_HOTEL_MIP = "HotelAttachMip";
	public static final String LX_AIR_MIP = "AirAttachMip";
	public static final String MOD_PROMO_TYPE = "MOD";

	public static final String NO_INTERNET_ERROR_CODE = "NO_INTERNET";
	public static final String UNKNOWN_ERROR_CODE = "UNKNOWN_ERROR";
	public static final String ARG_HTML_DATA = "ARG_HTML_DATA";
	public static final String ARG_ORIGINAL_URL = "ARG_ORIGINAL_URL";
	public static final String ARG_BASE_URL = "ARG_BASE_URL";
	public static final String ARG_USE_WEB_VIEW_TITLE = "ARG_USE_WEB_VIEW_TITLE";

	//Carnival
	public static final String CARNIVAL_MESSAGE_DATA = "carnival_message";
	public static final String CARNIVAL_IN_APP_BUTTON1_LABEL = "showButtonTitle";
	public static final String CARNIVAL_IN_APP_BUTTON2_LABEL = "cancelButtonTitle";
	public static final String CARNIVAL_TITLE = "dealTitle";
	public static final String CARNIVAL_DEAL_INSTRUCTIONS = "dealInstructions";
	public static final String CARNIVAL_PROMO_CODE_TEXT = "codeText";
	public static final String CARNIVAL_DETAILS_TITLE = "detailsTitle";
	public static final String CARNIVAL_DETAILS_DESCRIPTION = "detailsDescription";
	public static final String CARNIVAL_TERMS_TITLE = "termsTitle";
	public static final String CARNIVAL_TERMS_DESCRIPTION = "termsDescription";
	public static final String CARNIVAL_SHOP_BUTTON_TITLE = "shopButtonTitle";
	public static final String CARNIVAL_DEEPLINK = "deeplink";

	public static final int DEFAULT_MAX_OFFER_COUNT = 1600;
	public static final int BYOT_MAX_OFFER_COUNT = 6400;

	public static final int HOTEL_REVIEWS_PAGE_SIZE = 25;
}
