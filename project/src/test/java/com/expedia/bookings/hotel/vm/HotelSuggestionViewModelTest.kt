package com.expedia.bookings.hotel.vm

import com.expedia.bookings.R
import com.expedia.bookings.data.SuggestionV4
import com.expedia.bookings.services.TestObserver
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.testutils.builder.TestSuggestionV4Builder
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(RobolectricRunner::class)
class HotelSuggestionViewModelTest {
    private val testVM = HotelSuggestionViewModel()
    private val testObserver = TestObserver<Int>()

    @Before
    fun setup() {
        testVM.iconObservable.subscribe(testObserver)
    }

    @Test
    fun testHistoryItemIcon() {
        val suggestion = TestSuggestionV4Builder().regionDisplayName("notnull")
                .iconType(SuggestionV4.IconType.HISTORY_ICON).build()
        testVM.bind(suggestion)
        testObserver.assertValue(R.drawable.search_type_icon)
    }

    @Test
    fun testRecentSearchIcon() {
        val suggestion = TestSuggestionV4Builder().regionDisplayName("notnull")
                .iconType(SuggestionV4.IconType.RECENT_SEARCH_ICON).build()
        testVM.bind(suggestion)
        testObserver.assertValue(R.drawable.recents)
    }

    @Test
    fun testCurrentLocationIcon() {
        val suggestion = TestSuggestionV4Builder().regionDisplayName("notnull")
                .iconType(SuggestionV4.IconType.CURRENT_LOCATION_ICON).build()
        testVM.bind(suggestion)
        testObserver.assertValue(R.drawable.ic_suggest_current_location)
    }

    @Test
    fun testGoogleIcon() {
        val suggestion = TestSuggestionV4Builder().regionDisplayName("notnull")
                .iconType(SuggestionV4.IconType.MAGNIFYING_GLASS_ICON).build()
        testVM.bind(suggestion)
        testObserver.assertValue(R.drawable.google_search)
    }

    @Test
    fun testHotelOnlyIcon() {
        val suggestion = TestSuggestionV4Builder().regionDisplayName("notnull")
                .type("HOTEL").build()
        testVM.bind(suggestion)
        testObserver.assertValue(R.drawable.hotel_suggest)
    }

    @Test
    fun testAirportIcon() {
        val suggestion = TestSuggestionV4Builder().regionDisplayName("notnull")
                .type("AIRPORT").build()
        testVM.bind(suggestion)
        testObserver.assertValue(R.drawable.airport_suggest)
    }

    @Test
    fun testDefaultIcon() {
        val suggestion = TestSuggestionV4Builder().regionDisplayName("notnull").build()
        testVM.bind(suggestion)
        testObserver.assertValue(R.drawable.search_type_icon)
    }
}
