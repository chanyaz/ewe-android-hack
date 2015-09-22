package com.expedia.bookings.test

import com.expedia.bookings.data.cars.LatLong
import com.expedia.bookings.data.cars.Suggestion
import com.expedia.bookings.data.hotels.SuggestionV4
import org.junit.Test
import kotlin.test.assertEquals

public class HotelSuggestionV4Test {

    @Test
    fun suggestionV1ToV4() {
        var v1 = Suggestion();
        v1.gaiaId = "1"
        v1.type = "CITY"
        v1.displayName = "SFO - San Francisco"
        v1.shortName = "SFO"
        v1.fullName = "San Francisco"
        v1.latLong = LatLong(10.0, 10.0)
        v1.iconType = Suggestion.IconType.CURRENT_LOCATION_ICON
        var v4 = SuggestionV4.convertV1toV4(v1)
        assertEquals(v1.gaiaId, v4.gaiaId)
        assertEquals(v1.type, v4.type)
        assertEquals(v1.displayName, v4.regionNames.displayName)
        assertEquals(v1.shortName, v4.regionNames.shortName)
        assertEquals(v1.fullName, v4.regionNames.fullName)
        assertEquals(v1.latLong.lat, v4.coordinates.lat)
        assertEquals(v1.latLong.lng, v4.coordinates.lng)
        assertEquals(SuggestionV4.IconType.CURRENT_LOCATION_ICON, v4.iconType)
    }

}
