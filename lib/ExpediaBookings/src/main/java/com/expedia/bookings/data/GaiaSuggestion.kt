package com.expedia.bookings.data

import com.expedia.bookings.data.cars.LatLong
import com.google.gson.annotations.SerializedName

class GaiaSuggestion() {

    @SerializedName("id")
    lateinit var gaiaID: String
    lateinit var type: String
    lateinit var name: String

    lateinit var position: Position
    lateinit var localizedNames: Array<LocalizedName>
    lateinit var country: Country

    @SerializedName("isMajor")
    var isMajorAirport: Boolean = false

    @SerializedName("iataCode")
    var airportCode: String? = null

    @SerializedName("ancestors")
    var regionId: Array<RegionId>? = null

    data class Position(
        val type: String,
        val coordinates: Array<Double>
    )

    val latLong: LatLong by lazy {
        LatLong(position.coordinates[1], position.coordinates[0])
    }

    data class LocalizedName(
        @SerializedName("lcid")
        val languageIdentifier: Int,
        @SerializedName("value")
        val shortName: String,
        @SerializedName("extendedValue")
        val fullName: String,
        val friendlyName: String,
        var airportName: String? = null
    )

    data class Country(
        val name: String,
        var code: String? = null
    )

    data class RegionId(
        val id: String,
        val type: String
    )
}
