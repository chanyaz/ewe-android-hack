package com.expedia.testutils.builder

import com.expedia.bookings.data.SuggestionV4

class TestSuggestionV4Builder() {
    private val suggestion = SuggestionV4()

    init {
        suggestion.regionNames = SuggestionV4.RegionNames()
        suggestion.coordinates = SuggestionV4.LatLng()
    }

    fun type(type: String): TestSuggestionV4Builder {
        suggestion.type = type
        return this
    }

    fun regionDisplayName(name: String): TestSuggestionV4Builder {
        suggestion.regionNames.displayName = name
        return this
    }

    fun regionShortName(name: String): TestSuggestionV4Builder {
        suggestion.regionNames.shortName = name
        return this
    }

    fun coordinates(lat: Double, long: Double): TestSuggestionV4Builder {
        suggestion.coordinates.lat = lat
        suggestion.coordinates.lng = long
        return this
    }

    fun hotelId(hotelId: String): TestSuggestionV4Builder {
        suggestion.hotelId = hotelId
        return this
    }

    fun gaiaId(gaiaId: String): TestSuggestionV4Builder {
        suggestion.gaiaId = gaiaId
        return this
    }

    fun build() : SuggestionV4 {
        return suggestion
    }
}