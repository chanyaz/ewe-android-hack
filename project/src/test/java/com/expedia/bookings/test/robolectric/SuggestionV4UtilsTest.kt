package com.expedia.bookings.test.robolectric

import com.expedia.bookings.data.GaiaSuggestion
import com.expedia.bookings.utils.SuggestionV4Utils
import com.mobiata.android.util.IoUtils
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RuntimeEnvironment
import com.expedia.bookings.services.TestObserver
import java.util.ArrayList
import java.util.concurrent.TimeUnit
import kotlin.test.assertEquals
import kotlin.test.assertNull

@RunWith(RobolectricRunner::class)
class SuggestionV4UtilsTest {

    @Test
    fun testGaiaToSuggestionV4WhenAirportCodeIsEmpty() {
        val gaiaSuggestion = ArrayList<GaiaSuggestion>()
        gaiaSuggestion.add(getDummyGaiaSuggestion())
        val actualSuggestion = SuggestionV4Utils.convertToSuggestionV4(gaiaSuggestion).first()
        val hierarcyInfo = actualSuggestion.hierarchyInfo

        assertEquals("180000", actualSuggestion.gaiaId)
        assertEquals("multi_city_vicinity", actualSuggestion.type)
        assertEquals(77.22496, actualSuggestion.coordinates.lng)
        assertEquals(28.635308, actualSuggestion.coordinates.lat)
        assertEquals("Delhi (and vicinity)", actualSuggestion.regionNames.shortName)
        assertEquals("Delhi (and vicinity), India", actualSuggestion.regionNames.fullName)
        assertEquals("Delhi (and vicinity), India (DEL - New Delhi)", actualSuggestion.regionNames.displayName)
        assertEquals("India", hierarcyInfo!!.country.name)
        assertEquals("IND", hierarcyInfo.country.countryCode)
        assertNull(hierarcyInfo.airport!!.airportCode)
    }

    @Test
    fun testSuggestionSavedAreDistinctWhenAirportCodesPresent() {
        val dummySuggestion = getDummyGaiaSuggestion()
        dummySuggestion.airportCode = "CLT"
        checkDistinctSavedSuggestions(dummySuggestion)
    }

    @Test
    fun testSuggestionSavedAreDistinctWhenAirportCodesNotPresent() {
        checkDistinctSavedSuggestions(getDummyGaiaSuggestion())
    }

    @Test
    fun testFilteringSuggestionFromHistory() {
        val dummySuggestion = getDummyGaiaSuggestion()
        dummySuggestion.airportCode = "CLT"

        val suggestionToFilter = SuggestionV4Utils.convertToSuggestionV4(listOf(dummySuggestion)).first()
        val file = SuggestionV4Utils.RECENT_AIRPORT_SUGGESTIONS_FILE
        IoUtils.writeStringToFile(file, "[{\"coordinates\": {\"lat\": 28.635308,\"long\": -80.84296},\"gaiaId\": \"178247\",\"hierarchyInfo\": {\"airport\": {\"airportCode\": \"CLT\",\"multicity\": \"178247\"},\"country\": {\"name\": \"United States of America\"},\"rails\": {},\"isChild\": false},\"iconType\": \"HISTORY_ICON\",\"regionNames\": {\"displayName\": \"\u003cB\u003eCharl\u003c/B\u003eotte, NC (CLT - \u003cB\u003eCharl\u003c/B\u003eotte-Douglas Intl.)\",\"fullName\": \"Charlotte (and vicinity), North Carolina, United States of America\",\"shortName\": \"Charlotte (and vicinity)\"},\"type\": \"MULTICITY\",\"isMinorAirport\": false,\"isSearchThisArea\": false}]", RuntimeEnvironment.application)

        assertEquals(1, SuggestionV4Utils.loadSuggestionHistory(RuntimeEnvironment.application, file).size)
        assertEquals(0, SuggestionV4Utils.loadSuggestionHistory(RuntimeEnvironment.application, file, suggestionToFilter).size)
    }

    private fun checkDistinctSavedSuggestions(gaiaSuggestion: GaiaSuggestion) {
        val testSubscriber = TestObserver<Unit>()
        SuggestionV4Utils.testSuggestionSavedSubject.subscribe(testSubscriber)

        val gaiaSuggestions = ArrayList<GaiaSuggestion>()
        gaiaSuggestions.add(gaiaSuggestion)
        val actualSuggestion = SuggestionV4Utils.convertToSuggestionV4(gaiaSuggestions).first()
        val file = SuggestionV4Utils.RECENT_AIRPORT_SUGGESTIONS_FILE
        IoUtils.writeStringToFile(file, "[{\"coordinates\": {\"lat\": 28.635308,\"long\": -80.84296},\"gaiaId\": \"178247\",\"hierarchyInfo\": {\"airport\": {\"airportCode\": \"CLT\",\"multicity\": \"178247\"},\"country\": {\"name\": \"United States of America\"},\"rails\": {},\"isChild\": false},\"iconType\": \"HISTORY_ICON\",\"regionNames\": {\"displayName\": \"\u003cB\u003eCharl\u003c/B\u003eotte, NC (CLT - \u003cB\u003eCharl\u003c/B\u003eotte-Douglas Intl.)\",\"fullName\": \"Charlotte (and vicinity), North Carolina, United States of America\",\"shortName\": \"Charlotte (and vicinity)\"},\"type\": \"MULTICITY\",\"isMinorAirport\": false,\"isSearchThisArea\": false}]", RuntimeEnvironment.application)
        SuggestionV4Utils.saveSuggestionHistory(RuntimeEnvironment.application, actualSuggestion, file, true)
        testSubscriber.awaitValueCount(1, 1, TimeUnit.SECONDS)
        assertEquals(1, SuggestionV4Utils.loadSuggestionHistory(RuntimeEnvironment.application, file).size)
    }

    private fun getDummyGaiaSuggestion(): GaiaSuggestion {
        val suggestion = GaiaSuggestion()
        suggestion.gaiaID = "180000"
        suggestion.type = "multi_city_vicinity"
        val position = GaiaSuggestion.Position("Point", arrayOf(77.22496, 28.635308))
        val localizedNames = arrayOf(GaiaSuggestion.LocalizedName(1043, "Delhi (and vicinity)",
                "Delhi (and vicinity), India", "Delhi (and vicinity), India", "DEL-New Delhi"))
        val country = GaiaSuggestion.Country("India", "IND")
        suggestion.name = "Delhi (and vicinity), India"
        suggestion.country = country
        suggestion.position = position
        suggestion.localizedNames = localizedNames
        return suggestion
    }
}
