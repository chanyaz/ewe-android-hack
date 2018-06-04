package com.expedia.bookings.tracking

sealed class ApiCallFailing(val apiCall: String, val errorCode: String) {
    // Flights
    class FlightSearch(val code: String) : ApiCallFailing("FLIGHT_SEARCH", code)
    class FlightCreateTrip(val code: String) : ApiCallFailing("FLIGHT_CREATE_TRIP", code)
    class FlightCheckout(val code: String) : ApiCallFailing("FLIGHT_CHECKOUT", code)

    // Packages
    class PackageHotelSearch(val code: String) : ApiCallFailing("PACKAGE_HOTEL_SEARCH", code)
    class PackageHotelInfosite(val code: String) : ApiCallFailing("PACKAGE_HOTEL_INFOSITE", code)
    class PackageHotelRoom(val code: String) : ApiCallFailing("PACKAGE_HOTEL_ROOM", code)
    class PackageFlightOutbound(val code: String) : ApiCallFailing("PACKAGE_FLIGHT_OUTBOUND", code)
    class PackageFlightInbound(val code: String) : ApiCallFailing("PACKAGE_FLIGHT_INBOUND", code)
    class PackageHotelChange(val code: String) : ApiCallFailing("PACKAGE_HOTEL_SEARCH_CHANGE", code)
    class PackageHotelInfositeChange(val code: String) : ApiCallFailing("PACKAGE_HOTEL_INFOSITE_CHANGE", code)
    class PackageHotelRoomChange(val code: String) : ApiCallFailing("PACKAGE_HOTEL_ROOM_CHANGE", code)
    class PackageFlightOutboundChange(val code: String) : ApiCallFailing("PACKAGE_FLIGHT_OUTBOUND_CHANGE", code)
    class PackageFlightInboundChange(val code: String) : ApiCallFailing("PACKAGE_FLIGHT_INBOUND_CHANGE", code)
    class PackageFilterSearch(val code: String) : ApiCallFailing("PACKAGE_FILTERS_SEARCH", code)

    // Confirmation
    class ConfirmationPaymentSummaryMissing : ApiCallFailing("TRIP_DETAILS", "PAYMENT_SUMMARY_MISSING")

    fun getErrorStringForTracking() = "$errorCode|$apiCall"
}
