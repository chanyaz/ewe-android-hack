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
import rx.observers.TestSubscriber
import rx.schedulers.Schedulers
import java.io.File
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class SuggestionV4ServicesTest {

    var server: MockWebServer = MockWebServer()
        @Rule get

    private var service: SuggestionV4Services? = null
    private lateinit var mockInterceptor: MockInterceptor
    private lateinit var essMockInterceptor: MockInterceptor
    private lateinit var gaiaMockInterceptor: MockInterceptor

    @Before
    fun before() {
        val logger = HttpLoggingInterceptor()
        logger.level = HttpLoggingInterceptor.Level.BODY
        mockInterceptor = MockInterceptor()
        essMockInterceptor = MockInterceptor()
        gaiaMockInterceptor = MockInterceptor()
        service = SuggestionV4Services("http://localhost:" + server.port,
                "http://localhost:" + server.port,
                OkHttpClient.Builder().addInterceptor(logger).build(),
                mockInterceptor, essMockInterceptor, gaiaMockInterceptor, Schedulers.immediate(), Schedulers.immediate())

        givenExpediaDispatcherPrepared()
    }

    @Test
    fun testGaiaNearbySuggestions() {
        val suggestions = getGaiaNearbySuggestion(3.0)
        assertEquals(2, suggestions.size)
        assertSuggestionsEqual(getGaiaSuggestion(), suggestions.first())
    }

    @Test
    fun testGaiaNearbySuggestionsForLessThanThreeResults() {
        val suggestions = getGaiaNearbySuggestion(1.0)
        assertEquals(1, suggestions.size)
        assertSuggestionsEqual(getGaiaSuggestion(), suggestions.first())
    }

    @Test
    fun testGaiaNearbySuggestionsForZeroResults() {
        val suggestions = getGaiaNearbySuggestion(0.0)
        assertEquals(0, suggestions.size)
    }

    @Test
    fun testGaiaNearbySuggestionsLXEnglish() {
        val suggestions = getGaiaNearbySuggestionLXEnglish(3.0)
        assertEquals(2, suggestions.size)
        assertSuggestionsEqual(getLXGaiaSuggestionEnglish(), suggestions.first())
    }

    @Test
    fun testGaiaNearbySuggestionsForLessThanThreeResultsLXFrench() {
        val suggestions = getGaiaNearbySuggestionLXFrench(1.0)
        assertEquals(1, suggestions.size)
        assertSuggestionsEqual(getLXGaiaSuggestionFrench(), suggestions.first())
    }

    @Test
    fun gaiaCallUsesCorrectInterceptors() {
        getGaiaNearbySuggestion(3.0)

        assertTrue(mockInterceptor.wasCalled())
        assertFalse(essMockInterceptor.wasCalled())
        assertTrue(gaiaMockInterceptor.wasCalled())
    }

    @Test
    fun testGetLxSuggestionsV4() {
        val testObserver = TestSubscriber<List<SuggestionV4>>()
        service?.getLxSuggestionsV4("lon", testObserver, true)

        testObserver.awaitTerminalEvent()
        testObserver.assertCompleted()
        testObserver.assertValueCount(1)
        val essSuggestions = testObserver.onNextEvents[0]
        assertEquals(essSuggestions[0].regionNames.fullName, "San Francisco, CA, United States (SFO-San Francisco Intl.)")
    }

    @Test
    fun testGaiaNearbySuggestionsForLessThanThreeResultsLXEnglish() {
        val suggestions = getGaiaNearbySuggestionLXEnglish(1.0)
        assertEquals(1, suggestions.size)
        assertSuggestionsEqual(getLXGaiaSuggestionEnglish(), suggestions.first())
    }

    @Test
    fun testGaiaNearbySuggestionsForZeroResultsLX() {
        val suggestions = getGaiaNearbySuggestionLXEnglish(0.0)
        assertEquals(0, suggestions.size)
    }

    @Test
    fun hotelSuggestionsUsesCorrectInterceptors() {
        val testObserver = TestSubscriber<List<SuggestionV4>>()

        service?.getHotelSuggestionsV4("chicago", testObserver, true, "guid")
        testObserver.awaitTerminalEvent()
        testObserver.assertCompleted()

        assertTrue(mockInterceptor.wasCalled())
        assertTrue(essMockInterceptor.wasCalled())
        assertFalse(gaiaMockInterceptor.wasCalled())
    }

    @Test
    fun airportSuggestionsUsesCorrectInterceptors() {
        val testObserver = TestSubscriber<List<SuggestionV4>>()

        service?.getAirports("chicago", true, testObserver, "guid")
        testObserver.awaitTerminalEvent()
        testObserver.assertCompleted()

        assertTrue(mockInterceptor.wasCalled())
        assertTrue(essMockInterceptor.wasCalled())
        assertFalse(gaiaMockInterceptor.wasCalled())
    }

    @Test
    fun packagesSuggestionsUsesCorrectInterceptors() {
        val testObserver = TestSubscriber<List<SuggestionV4>>()

        service?.suggestPackagesV4("chicago", true, false, testObserver)
        testObserver.awaitTerminalEvent()
        testObserver.assertCompleted()

        assertTrue(mockInterceptor.wasCalled())
        assertTrue(essMockInterceptor.wasCalled())
        assertFalse(gaiaMockInterceptor.wasCalled())
    }

    @Test
    fun railSuggestionsUsesCorrectInterceptors() {
        val testObserver = TestSubscriber<List<SuggestionV4>>()

        service?.suggestRailsV4("chicago", true, testObserver)
        testObserver.awaitTerminalEvent()
        testObserver.assertCompleted()

        assertTrue(mockInterceptor.wasCalled())
        assertTrue(essMockInterceptor.wasCalled())
        assertFalse(gaiaMockInterceptor.wasCalled())
    }

    @Test
    fun lxSuggestionsUsesCorrectInterceptors() {
        val testObserver = TestSubscriber<List<SuggestionV4>>()

        service?.getLxSuggestionsV4("chicago", testObserver, false)
        testObserver.awaitTerminalEvent()
        testObserver.assertCompleted()

        assertTrue(mockInterceptor.wasCalled())
        assertTrue(essMockInterceptor.wasCalled())
        assertFalse(gaiaMockInterceptor.wasCalled())
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