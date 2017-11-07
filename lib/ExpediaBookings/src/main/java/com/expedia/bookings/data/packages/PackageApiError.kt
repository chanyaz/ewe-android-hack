package com.expedia.bookings.data.packages

class PackageApiError {

    enum class Code {

        //Package search error
        search_response_null,
        pkg_unknown_error,
        pkg_destination_resolution_failed,
        pkg_origin_resolution_failed,
        pkg_flight_no_longer_available,
        pkg_invalid_checkin_checkout_dates,
        pkg_piid_expired,
        pkg_no_flights_available,
        pkg_pss_downstream_service_timeout,
        pkg_too_many_children_in_lap,
        pkg_hotel_no_longer_available,
        pkg_error_code_not_mapped,
        pkg_search_from_date_too_near,

        //TODO to be removed, needs to be handled properly
        mid_could_not_find_results,
        mid_internal_server_error,
        mid_fss_hotel_unavailable_for_red_eye_flight
    }

    val errorCode: Code = Code.pkg_unknown_error

}