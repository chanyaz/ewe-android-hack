import com.expedia.bookings.data.SuggestionV4
import org.junit.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class SuggestionV4Test {
    var testSuggestion = SuggestionV4()

    @Test
    fun testCurrentLocation_WithHotelId() {
        testSuggestion.gaiaId = ""
        testSuggestion.isSearchThisArea = false
        testSuggestion.hotelId = "12345"

        assertFalse(testSuggestion.isCurrentLocationSearch, "FAILURE: Hotel Id specific searches don't have a gaiaId but still should not be considered CurrentLocation")
    }

    @Test
    fun testCurrentLocation_WithValidGaiaId() {
        testSuggestion.gaiaId = "1234"

        assertFalse(testSuggestion.isCurrentLocationSearch)
    }

    @Test
    fun testCurrentLocation_SearchThisArea() {
        testSuggestion.gaiaId = ""
        testSuggestion.isSearchThisArea = true
        testSuggestion.hotelId = ""

        assertFalse(testSuggestion.isCurrentLocationSearch)
    }

    @Test
    fun testCurrentLocationSearch() {
        testSuggestion.gaiaId = ""
        testSuggestion.isSearchThisArea = false
        testSuggestion.hotelId = ""

        assertTrue(testSuggestion.isCurrentLocationSearch)
    }
}
