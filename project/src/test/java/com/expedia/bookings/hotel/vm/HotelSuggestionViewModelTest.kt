package com.expedia.bookings.hotel.vm

import com.expedia.bookings.R
import com.expedia.bookings.data.SuggestionV4
import com.expedia.bookings.data.travelgraph.SearchInfo
import com.expedia.bookings.data.travelgraph.TravelerInfo
import com.expedia.bookings.services.TestObserver
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.utils.LocaleBasedDateFormatUtils
import com.expedia.testutils.builder.TestSuggestionV4Builder
import org.joda.time.LocalDate
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RuntimeEnvironment

@RunWith(RobolectricRunner::class)
class HotelSuggestionViewModelTest {
    private val testVM = HotelSuggestionViewModel(RuntimeEnvironment.application)
    private val testIconObserver = TestObserver<Int>()
    private val testIconContentDescriptionObserver = TestObserver<String>()

    @Before
    fun setup() {
        testVM.iconObservable.subscribe(testIconObserver)
        testVM.iconContentDescriptionObservable.subscribe(testIconContentDescriptionObserver)
    }

    @Test
    fun testSearchInfoSubtitle() {
        val suggestion = TestSuggestionV4Builder().regionDisplayName("notnull")
                .iconType(SuggestionV4.IconType.RECENT_SEARCH_ICON).build()
        val startDate = LocalDate.now()
        val endDate = startDate.plusDays(1)
        val startDateString = LocaleBasedDateFormatUtils.localDateToMMMd(startDate)
        val endDateString = LocaleBasedDateFormatUtils.localDateToMMMd(endDate)

        val expectedString = "$startDateString – $endDateString   1 Night   1 Guest"
        val searchInfo = SearchInfo(suggestion, startDate, endDate, TravelerInfo())
        val subtitleObserver = TestObserver<String>()
        testVM.subtitleObservable.subscribe(subtitleObserver)
        testVM.bind(searchInfo)

        subtitleObserver.assertValue(expectedString)
    }

    @Test
    fun testNoSubtitleForSuggestion() {
        val suggestion = TestSuggestionV4Builder().regionDisplayName("notnull")
                .iconType(SuggestionV4.IconType.HISTORY_ICON).build()
        val subtitleObserver = TestObserver<String>()
        testVM.subtitleObservable.subscribe(subtitleObserver)
        testVM.bind(suggestion)
        subtitleObserver.assertValue("")
    }

    @Test
    fun testHistoryItemIcon() {
        val suggestion = TestSuggestionV4Builder().regionDisplayName("notnull")
                .iconType(SuggestionV4.IconType.HISTORY_ICON).build()
        testVM.bind(suggestion)
        testIconObserver.assertValue(R.drawable.search_type_icon)
        testIconContentDescriptionObserver.assertValue("HISTORY_ICON")
    }

    @Test
    fun testRecentSearchIcon() {
        val suggestion = TestSuggestionV4Builder().regionDisplayName("notnull")
                .iconType(SuggestionV4.IconType.RECENT_SEARCH_ICON).build()
        testVM.bind(suggestion)
        testIconObserver.assertValue(R.drawable.recents)
        testIconContentDescriptionObserver.assertValue("RECENT_SEARCH_ICON")
    }

    @Test
    fun testCurrentLocationIcon() {
        val suggestion = TestSuggestionV4Builder().regionDisplayName("notnull")
                .iconType(SuggestionV4.IconType.CURRENT_LOCATION_ICON).build()
        testVM.bind(suggestion)
        testIconObserver.assertValue(R.drawable.ic_suggest_current_location)
        testIconContentDescriptionObserver.assertValue("CURRENT_LOCATION_ICON")
    }

    @Test
    fun testGoogleIcon() {
        val suggestion = TestSuggestionV4Builder().regionDisplayName("notnull")
                .iconType(SuggestionV4.IconType.MAGNIFYING_GLASS_ICON).build()
        testVM.bind(suggestion)
        testIconObserver.assertValue(R.drawable.google_search)
        testIconContentDescriptionObserver.assertValue("MAGNIFYING_GLASS_ICON")
    }

    @Test
    fun testHotelOnlyIcon() {
        val suggestion = TestSuggestionV4Builder().regionDisplayName("notnull")
                .type("HOTEL").build()
        testVM.bind(suggestion)
        testIconObserver.assertValue(R.drawable.hotel_suggest)
        testIconContentDescriptionObserver.assertValue("HOTEL_ICON")
    }

    @Test
    fun testAirportIcon() {
        val suggestion = TestSuggestionV4Builder().regionDisplayName("notnull")
                .type("AIRPORT").build()
        testVM.bind(suggestion)
        testIconObserver.assertValue(R.drawable.airport_suggest)
        testIconContentDescriptionObserver.assertValue("AIRPORT_ICON")
    }

    @Test
    fun testDefaultIcon() {
        val suggestion = TestSuggestionV4Builder().regionDisplayName("notnull").build()
        testVM.bind(suggestion)
        testIconObserver.assertValue(R.drawable.search_type_icon)
        testIconContentDescriptionObserver.assertValue("SEARCH_TYPE_ICON")
    }
}
