package com.expedia.bookings.preference

object TripMockScenarios {
    const val TRIP_SCENARIOS_FILENAME_KEY = "TRIP_SCENARIOS_FILENAME_KEY"

    enum class Scenarios(val filename: String) {
        TRIP_FOLDERS_M1_ONLY_HOTEL("tripfolders_m1_hotel"),
        TRIP_FOLDERS_M1_ONLY_CAR("tripfolders_m1_car"),
        TRIP_FOLDERS_M1_MULTIPLE_LOB("tripfolders_m1_multiple_lob")
    }
}
