package com.expedia.bookings.unit

import com.expedia.bookings.data.GaiaSuggestion
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
import rx.Observer
import rx.observers.TestObserver
import rx.observers.TestSubscriber
import rx.schedulers.Schedulers
import java.io.File
import kotlin.test.assertEquals
import kotlin.test.assertFalse

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
                interceptor, interceptor, Schedulers.immediate(), Schedulers.immediate())

        givenExpediaDispatcherPrepared()
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

    @Test
    fun testGaiaNearbySuggestionsLXEnglish() {
        val suggestions = getGaiaNearbySuggestionLXEnglish(3.0);
        assertEquals(2, suggestions.size)
        assertSuggestionsEqual(getLXGaiaSuggestionEnglish(), suggestions.first())
    }

    @Test
    fun testGaiaNearbySuggestionsForLessThanThreeResultsLXFrench() {
        val suggestions = getGaiaNearbySuggestionLXFrench(1.0);
        assertEquals(1, suggestions.size)
        assertSuggestionsEqual(getLXGaiaSuggestionFrench(), suggestions.first())
    }

    @Test
    fun testGetLxSuggestionsV4() {
        val testObserver = TestSubscriber<List<SuggestionV4>>()
        service?.getLxSuggestionsV4("lon","expedia.app.android.phone", testObserver, "en_US", true)

        testObserver.awaitTerminalEvent()
        testObserver.assertCompleted()
        testObserver.assertValueCount(1)
        val essSuggestions = testObserver.onNextEvents[0]
        assertEquals(essSuggestions[0].regionNames.fullName, "San Francisco, CA, United States (SFO-San Francisco Intl.)")
    }

    @Test
    fun testGaiaNearbySuggestionsForLessThanThreeResultsLXEnglish() {
        val suggestions = getGaiaNearbySuggestionLXEnglish(1.0);
        assertEquals(1, suggestions.size)
        assertSuggestionsEqual(getLXGaiaSuggestionEnglish(), suggestions.first())
    }

    @Test
    fun testGaiaNearbySuggestionsForZeroResultsLX() {
        val suggestions = getGaiaNearbySuggestionLXEnglish(0.0);
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

    private fun getLXGaiaSuggestionEnglish(): GaiaSuggestion {
        val suggestion = GaiaSuggestion()
        suggestion.gaiaID = "553248623251959093"
        suggestion.type = "city"
        val position = GaiaSuggestion.Position("Point", arrayOf(77.08858686, 28.48940909))
        val localizedNames = arrayOf(GaiaSuggestion.LocalizedName(1033, "DLF Phase II",
                "DLF Phase II, Gurgaon, India", "DLF Phase II, Gurgaon"))
        val country = GaiaSuggestion.Country("India", null)
        suggestion.name = "DLF Phase II"
        suggestion.country = country
        suggestion.position = position
        suggestion.localizedNames = localizedNames

        return suggestion
    }

    private fun getLXGaiaSuggestionFrench(): GaiaSuggestion {
        val suggestion = getLXGaiaSuggestionEnglish()
        val localizedNames = arrayOf(GaiaSuggestion.LocalizedName(1036, "DLF Phase II",
                "DLF Phase II, Gurgaon, Inde", "DLF Phase II, Gurgaon"))
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

    private fun getGaiaNearbySuggestionLXEnglish(location: Double): List<GaiaSuggestion> {
        val test = TestSubscriber<List<GaiaSuggestion>>()
        val observer = service?.suggestNearbyGaia(location, location, "distance", "lx", "en_US", 1)
        observer?.subscribe(test)

        return test.onNextEvents[0]
    }

    private fun getGaiaNearbySuggestionLXFrench(location: Double): List<GaiaSuggestion> {
        val test = TestSubscriber<List<GaiaSuggestion>>()
        val observer = service?.suggestNearbyGaia(location, location, "distance", "lx", "fr_FR", 1)
        observer?.subscribe(test)

        return test.onNextEvents[0]
    }

    private fun givenExpediaDispatcherPrepared() {
        val root = File("../mocked/templates").canonicalPath
        val opener = FileSystemOpener(root)
        server.setDispatcher(ExpediaDispatcher(opener))
    }
}