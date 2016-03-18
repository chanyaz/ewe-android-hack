package com.expedia.bookings.utils;

public class Constants {
	public static final String PRODUCT_FLIGHT = "flight";
	public static final int HOTEL_REQUEST_CODE = 101;
	public static final int FLIGHT_REQUEST_CODE = 102;
	public static final String RAW_TEXT_SEARCH = "RAW_TEXT_SEARCH";
	public static final int PERMISSION_REQUEST_LOCATION = 7;
	public static final int PERMISSION_REQUEST_LOCATION_WITH_RATIONALE = 8;
	public static final int PACKAGE_FLIGHT_DEPARTURE_REQUEST_CODE = 102;
	public static final int PACKAGE_FLIGHT_ARRIVAL_REQUEST_CODE = 103;
	//For more info on Package trip type https://confluence/display/POS/GetPackages+API
	public static final String PACKAGE_TRIP_TYPE = "2"; // 2 mean BYOT (build your own trip)
	public static final String NUMBER_OF_ROOMS = "1";
	public static final String PACKAGE_HOTEL_DELTA_PRICE_TYPE = "per night all travelers";
	public static final String CLIENT_LOG_MATERIAL_HOTEL_SEARCH = "page.Material.Hotel.Search";
	public static final String PACKAGE_CHANGE_HOTEL = "CHANGE_HOTEL";
	public static final String PACKAGE_CHANGE_FLIGHT = "CHANGE_FLIGHT";
	public static final String PACKAGE_FILTER_CHANGE_FLIGHT = "filterChangeFlight";

	// itin airline code for check in
	public static final String ITIN_CHECK_IN_CODE = "ITIN_CHECK_IN_CODE";
	public static final String ITIN_CHECK_IN_CONFIRMATION_CODE = "ITIN_CHECK_IN_CONFIRMATION_CODE";
	public static final int ITIN_CHECK_IN_WEBPAGE_CODE = 21;
	public static final String ITIN_FLIGHT_TRIP_LEGS = "ITIN_FLIGHT_TRIP_LEGS";
	public static final String ITIN_IS_SPLIT_TICKET = "ITIN_IS_SPLIT_TICKET";

	public static final String TAG_CALENDAR_DIALOG = "TAG_CALENDAR_DIALOG";
}
