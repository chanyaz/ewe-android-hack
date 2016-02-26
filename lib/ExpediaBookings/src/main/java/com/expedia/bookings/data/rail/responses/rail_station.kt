package com.expedia.bookings.data.rail.responses

class RailStation(val stationCode: String, val stationName: String, val stationType: String, val stationCity: String) {
    fun shortStationCode() : String {
        return stationCode.substring(2); //TODO - it would be nice to somehow get the code without the "GB" prefix
    }
}

