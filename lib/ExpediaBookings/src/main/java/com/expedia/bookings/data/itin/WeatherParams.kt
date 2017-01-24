package com.expedia.bookings.data.hotels

data class WeatherParams(val brand: String, val latitude: Double, val longitude: Double) {

    fun getQuery(): String {
        return latitude.toString() + "," + longitude.toString()
    }

}
