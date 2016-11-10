package com.expedia.bookings.unit

import com.expedia.bookings.data.GaiaSuggestion
import com.expedia.bookings.data.SuggestionResultType
import com.expedia.bookings.data.SuggestionV4
import com.expedia.bookings.interceptors.MockInterceptor
import com.expedia.bookings.services.SuggestionV4Services
import com.mobiata.mocke3.ExpediaDispatcher
import com.mobiata.mocke3.FileSystemOpener
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import okhttp3.mockwebserver.MockWebServer
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import rx.observers.TestSubscriber
import rx.schedulers.Schedulers
import java.io.File
import kotlin.test.assertEquals

class SuggestionV4ServicesTest {

    var server: MockWebServer = MockWebServer()
        @Rule get

    private var service: SuggestionV4Services? = null

    @Before
    fun before() {
        val logger = HttpLoggingInterceptor()
        logger.level = HttpLoggingInterceptor.Level.BODY
        val interceptor = MockInterceptor()
        service = SuggestionV4Services("http://localhost:" + server.port,
                "http://localhost:" + server.port,
                OkHttpClient.Builder().addInterceptor(logger).build(),
                interceptor, Schedulers.immediate(), Schedulers.immediate())

        givenExpediaDispatcherPrepared()
    }

    @Test
    fun testNearbySuggestionsWithOnlyAirport() {
        val test = TestSubscriber<List<SuggestionV4>>()
        val observer = service?.suggestNearbyV4("en_US", "latLng", 1, "clientId",
                SuggestionResultType.AIRPORT or SuggestionResultType.AIRPORT_METRO_CODE, "distance", "HOTELS")
        observer?.subscribe(test)

        val suggestions = test.onNextEvents[0]
        assertEquals(2, suggestions.size)
        assertEquals("AIRPORT", suggestions[0].type)
        test.assertValueCount(1)
    }

    @Test
    fun testNearbySuggestionsWithMultiCity() {
        val test = TestSubscriber<List<SuggestionV4>>()
        val observer = service?.suggestNearbyV4("en_US", "latLng", 1, "clientId",
                SuggestionResultType.MULTI_CITY, "distance", "HOTELS")
        observer?.subscribe(test)

        val suggestions = test.onNextEvents[0]
        assertEquals(2, suggestions.size)
        assertEquals("MULTICITY", suggestions[0].type)
        test.assertValueCount(1)
    }

    @Test
    fun testGaiaNearbySuggestions() {
        val suggestions = getGaiaNearbySuggestion(3.0);
        assertEquals(2, suggestions.size)
        assertSuggestionsEqual(getGaiaSuggestion(), suggestions.first())
    }

    @Test
    fun testGaiaNearbySuggestionsForLessThanThreeResults() {
        val suggestions = getGaiaNearbySuggestion(1.0);
        assertEquals(1, suggestions.size)
        assertSuggestionsEqual(getGaiaSuggestion(), suggestions.first())
    }

    @Test
    fun testGaiaNearbySuggestionsForZeroResults() {
        val suggestions = getGaiaNearbySuggestion(0.0);
        assertEquals(0, suggestions.size)
    }

    private fun getGaiaSuggestion(): GaiaSuggestion {
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

    private fun assertSuggestionsEqual(expectedSuggestion: GaiaSuggestion, actualSuggestion: GaiaSuggestion) {
        assertEquals(expectedSuggestion.gaiaID, actualSuggestion.gaiaID)
        assertEquals(expectedSuggestion.type, actualSuggestion.type)
        assertEquals(expectedSuggestion.name, actualSuggestion.name)
        assertEquals(expectedSuggestion.country, actualSuggestion.country)
        assertEquals(expectedSuggestion.position.type, actualSuggestion.position.type)
        assertEquals(expectedSuggestion.latLong.latitude, actualSuggestion.latLong.latitude)
        assertEquals(expectedSuggestion.localizedNames.first(), actualSuggestion.localizedNames.first())
    }

    private fun getGaiaNearbySuggestion(location: Double): List<GaiaSuggestion> {
        val test = TestSubscriber<List<GaiaSuggestion>>()
        val observer = service?.suggestNearbyGaia(location, location, "distance", "hotels", "en_US", 1)
        observer?.subscribe(test)

        return test.onNextEvents[0]
    }

    private fun givenExpediaDispatcherPrepared() {
        val root = File("../mocked/templates").canonicalPath
        val opener = FileSystemOpener(root)
        server.setDispatcher(ExpediaDispatcher(opener))
    }
}