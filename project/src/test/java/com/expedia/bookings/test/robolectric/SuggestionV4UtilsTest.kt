package com.expedia.bookings.test.robolectric

import com.expedia.bookings.data.GaiaSuggestion
import com.expedia.bookings.utils.SuggestionV4Utils
import org.junit.runner.RunWith
import java.util.ArrayList
import kotlin.test.assertEquals
import kotlin.test.assertNull

@RunWith(RobolectricRunner::class)
class SuggestionV4UtilsTest {

    fun testGaiaToSuggestionV4WhenAirportCodeIsEmpty() {
        val gaiaSuggestion = ArrayList<GaiaSuggestion>()
        gaiaSuggestion.add(getDummyGaiaSuggestion())
        var actualSuggestion = SuggestionV4Utils.convertToSuggestionV4(gaiaSuggestion).first()
        val hierarcyInfo = actualSuggestion.hierarchyInfo

        assertEquals("18000", actualSuggestion.gaiaId)
        assertEquals("multi_city_vicinity", actualSuggestion.type)
        assertEquals(77.22496, actualSuggestion.coordinates.lng)
        assertEquals(28.635308, actualSuggestion.coordinates.lat)
        assertEquals("Delhi (and vicinity)", actualSuggestion.regionNames.shortName)
        assertEquals("Delhi (and vicinity), India", actualSuggestion.regionNames.fullName)
        assertEquals("Delhi (and vicinity), India", actualSuggestion.regionNames.displayName)
        assertEquals("India", hierarcyInfo!!.country.name)
        assertEquals("IND", hierarcyInfo.country.countryCode)
        assertNull(hierarcyInfo.airport)
    }

    private fun getDummyGaiaSuggestion(): GaiaSuggestion {
        val suggestion = GaiaSuggestion()
        suggestion.gaiaID = "180000"
        suggestion.type = "multi_city_vicinity"
        val position = GaiaSuggestion.Position("Point", arrayOf(77.22496, 28.635308))
        val localizedNames = arrayOf(GaiaSuggestion.LocalizedName(1043, "Delhi (and vicinity)",
                "Delhi (and vicinity), India", "Delhi (and vicinity), India", "DEL"))
        val country = GaiaSuggestion.Country("India", "IND")
        suggestion.name = "Delhi (and vicinity), India"
        suggestion.country = country
        suggestion.position = position
        suggestion.localizedNames = localizedNames
        return suggestion
    }
}