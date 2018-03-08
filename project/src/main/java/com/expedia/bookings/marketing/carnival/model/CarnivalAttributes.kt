package com.expedia.bookings.marketing.carnival.model

object CarnivalConstants {
    // App launch
    const val APP_OPEN_LAUNCH_RELAUNCH = "app_open_launch_relaunch"
    const val APP_OPEN_LAUNCH_RELAUNCH_LOCATION_ENABLED = "${APP_OPEN_LAUNCH_RELAUNCH}_location_enabled"
    const val APP_OPEN_LAUNCH_RELAUNCH_USERID = "${APP_OPEN_LAUNCH_RELAUNCH}_userid"
    const val APP_OPEN_LAUNCH_RELAUNCH_USER_EMAIL = "${APP_OPEN_LAUNCH_RELAUNCH}_user_email"
    const val APP_OPEN_LAUNCH_RELAUNCH_SIGN_IN = "${APP_OPEN_LAUNCH_RELAUNCH}_sign-in"
    const val APP_OPEN_LAUNCH_RELAUNCH_BOOKED_PRODUCT = "${APP_OPEN_LAUNCH_RELAUNCH}_booked_product"
    const val APP_OPEN_LAUNCH_RELAUNCH_LOYALTY_TIER = "${APP_OPEN_LAUNCH_RELAUNCH}_loyalty_tier"
    const val APP_OPEN_LAUNCH_RELAUNCH_LAST_LOCATION = "${APP_OPEN_LAUNCH_RELAUNCH}_last_location"
    const val APP_OPEN_LAUNCH_RELAUNCH_NOTIFICATION_TYPE = "${APP_OPEN_LAUNCH_RELAUNCH}_notification_type"
    const val APP_OPEN_LAUNCH_RELAUNCH_POS = "${APP_OPEN_LAUNCH_RELAUNCH}_pos"

    // Search flight
    const val SEARCH_FLIGHT = "search_flight"
    const val SEARCH_FLIGHT_DESTINATION = "${SEARCH_FLIGHT}_destination"
    const val SEARCH_FLIGHT_NUMBER_OF_ADULTS = "${SEARCH_FLIGHT}_number_of_adults"
    const val SEARCH_FLIGHT_DEPARTURE_DATE = "${SEARCH_FLIGHT}_departure_date"

    // Checkout start flight
    const val CHECKOUT_START_FLIGHT = "checkout_start_flight"
    const val CHECKOUT_START_FLIGHT_DESTINATION = "${CHECKOUT_START_FLIGHT}_destination"
    const val CHECKOUT_START_FLIGHT_AIRLINE = "${CHECKOUT_START_FLIGHT}_airline"
    const val CHECKOUT_START_FLIGHT_FLIGHT_NUMBER = "${CHECKOUT_START_FLIGHT}_flight_number"
    const val CHECKOUT_START_FLIGHT_NUMBER_OF_ADULTS = "${CHECKOUT_START_FLIGHT}_number_of_adults"
    const val CHECKOUT_START_FLIGHT_DEPARTURE_DATE = "${CHECKOUT_START_FLIGHT}_departure_date"
    const val CHECKOUT_START_FLIGHT_LENGTH_OF_FLIGHT = "${CHECKOUT_START_FLIGHT}_length_of_flight"

    // Confirmation flight
    const val CONFIRMATION_FLIGHT = "confirmation_flight"
    const val CONFIRMATION_FLIGHT_DESTINATION = "${CONFIRMATION_FLIGHT}_destination"
    const val CONFIRMATION_FLIGHT_AIRLINE = "${CONFIRMATION_FLIGHT}_airline"
    const val CONFIRMATION_FLIGHT_FLIGHT_NUMBER = "${CONFIRMATION_FLIGHT}_flight_number"
    const val CONFIRMATION_FLIGHT_NUMBER_OF_ADULTS = "${CONFIRMATION_FLIGHT}_number_of_adults"
    const val CONFIRMATION_FLIGHT_DEPARTURE_DATE = "${CONFIRMATION_FLIGHT}_departure_date"
    const val CONFIRMATION_FLIGHT_LENGTH_OF_FLIGHT = "${CONFIRMATION_FLIGHT}_length_of_flight"

    // Search hotel
    const val SEARCH_HOTEL = "search_hotel"
    const val SEARCH_HOTEL_DESTINATION = "${SEARCH_HOTEL}_destination"
    const val SEARCH_HOTEL_NUMBER_OF_ADULTS = "${SEARCH_HOTEL}_number_of_adults"
    const val SEARCH_HOTEL_CHECK_IN_DATE = "${SEARCH_HOTEL}_check-in_date"
    const val SEARCH_HOTEL_LENGTH_OF_STAY = "${SEARCH_HOTEL}_length_of_stay"

    // Product view hotel
    const val PRODUCT_VIEW_HOTEL = "product_view_hotel"
    const val PRODUCT_VIEW_HOTEL_DESTINATION = "${PRODUCT_VIEW_HOTEL}_destination"
    const val PRODUCT_VIEW_HOTEL_HOTEL_NAME = "${PRODUCT_VIEW_HOTEL}_hotel_name"
    const val PRODUCT_VIEW_HOTEL_NUMBER_OF_ADULTS = "${PRODUCT_VIEW_HOTEL}_number_of_adults"
    const val PRODUCT_VIEW_HOTEL_CHECK_IN_DATE = "${PRODUCT_VIEW_HOTEL}_check-in_date"
    const val PRODUCT_VIEW_HOTEL_LENGTH_OF_STAY = "${PRODUCT_VIEW_HOTEL}_length_of_stay"

    // Checkout start hotel
    const val CHECKOUT_START_HOTEL = "checkout_start_hotel"
    const val CHECKOUT_START_HOTEL_DESTINATION = "${CHECKOUT_START_HOTEL}_destination"
    const val CHECKOUT_START_HOTEL_HOTEL_NAME = "${CHECKOUT_START_HOTEL}_hotel_name"
    const val CHECKOUT_START_HOTEL_NUMBER_OF_ADULTS = "${CHECKOUT_START_HOTEL}_number_of_adults"
    const val CHECKOUT_START_HOTEL_CHECK_IN_DATE = "${CHECKOUT_START_HOTEL}_check-in_date"
    const val CHECKOUT_START_HOTEL_LENGTH_OF_STAY = "${CHECKOUT_START_HOTEL}_length_of_stay"

    // Confirmation hotel
    const val CONFIRMATION_HOTEL = "confirmation_hotel"
    const val CONFIRMATION_HOTEL_DESTINATION = "${CONFIRMATION_HOTEL}_destination"
    const val CONFIRMATION_HOTEL_HOTEL_NAME = "${CONFIRMATION_HOTEL}_hotel_name"
    const val CONFIRMATION_HOTEL_NUMBER_OF_ADULTS = "${CONFIRMATION_HOTEL}_number_of_adults"
    const val CONFIRMATION_HOTEL_CHECK_IN_DATE = "${CONFIRMATION_HOTEL}_check-in_date"
    const val CONFIRMATION_HOTEL_LENGTH_OF_STAY = "${CONFIRMATION_HOTEL}_length_of_stay"

    // Confirmation lx (local experience)
    const val CONFIRMATION_LX = "confirmation_lx"
    const val CONFIRMATION_LX_ACTIVITY_NAME = "${CONFIRMATION_LX}_activity_name"
    const val CONFIRMATION_LX_DATE_OF_ACTIVITY = "${CONFIRMATION_LX}_date_of_activity"

    // Confirmation package
    const val CONFIRMATION_PKG = "confirmation_pkg"
    const val CONFIRMATION_PKG_DESTINATION = "${CONFIRMATION_PKG}_destination"
    const val CONFIRMATION_PKG_DEPARTURE_DATE = "${CONFIRMATION_PKG}_departure_date"
    const val CONFIRMATION_PKG_LENGTH_OF_STAY = "${CONFIRMATION_PKG}_length_of_stay"

    // Confirmation rail
    const val CONFIRMATION_RAIL = "confirmation_rail"
    const val CONFIRMATION_RAIL_DESTINATION = "${CONFIRMATION_RAIL}_destination"
    const val CONFIRMATION_RAIL_DEPARTURE_DATE = "${CONFIRMATION_RAIL}_departure_date"
}

object CarnivalNotificationTypeConstants {
    const val MKTG = "MKTG"
    const val SERV = "SERV"
    const val PROMO = "PROMO"
}
