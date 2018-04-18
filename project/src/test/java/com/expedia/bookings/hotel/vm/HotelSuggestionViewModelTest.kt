package com.expedia.bookings.hotel.vm

import com.expedia.bookings.R
import com.expedia.bookings.data.SuggestionV4
import com.expedia.bookings.data.travelgraph.SearchInfo
import com.expedia.bookings.data.travelgraph.TravelerInfo
import com.expedia.bookings.services.TestObserver
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.utils.FontCache
import com.expedia.bookings.utils.LocaleBasedDateFormatUtils
import com.expedia.testutils.builder.TestSuggestionV4Builder
import org.joda.time.LocalDate
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RuntimeEnvironment
import kotlin.test.assertEquals

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
        val iconContentDescription = if (testVM.isIconContentDescriptionRequired()) "HISTORY_ICON" else ""
        testIconContentDescriptionObserver.assertValue(iconContentDescription)
    }

    @Test
    fun testRecentSearchIcon() {
        val suggestion = TestSuggestionV4Builder().regionDisplayName("notnull")
                .iconType(SuggestionV4.IconType.RECENT_SEARCH_ICON).build()
        testVM.bind(suggestion)
        testIconObserver.assertValue(R.drawable.recents)
        val iconContentDescription = if (testVM.isIconContentDescriptionRequired()) "RECENT_SEARCH_ICON" else ""
        testIconContentDescriptionObserver.assertValue(iconContentDescription)
    }

    @Test
    fun testCurrentLocationIcon() {
        val suggestion = TestSuggestionV4Builder().regionDisplayName("notnull")
                .iconType(SuggestionV4.IconType.CURRENT_LOCATION_ICON).build()
        testVM.bind(suggestion)
        testIconObserver.assertValue(R.drawable.ic_suggest_current_location)
        val iconContentDescription = if (testVM.isIconContentDescriptionRequired()) "CURRENT_LOCATION_ICON" else ""
        testIconContentDescriptionObserver.assertValue(iconContentDescription)
    }

    @Test
    fun testGoogleIcon() {
        val suggestion = TestSuggestionV4Builder().regionDisplayName("notnull")
                .iconType(SuggestionV4.IconType.MAGNIFYING_GLASS_ICON).build()
        testVM.bind(suggestion)
        testIconObserver.assertValue(R.drawable.google_search)
        val iconContentDescription = if (testVM.isIconContentDescriptionRequired()) "MAGNIFYING_GLASS_ICON" else ""
        testIconContentDescriptionObserver.assertValue(iconContentDescription)
    }

    @Test
    fun testHotelOnlyIcon() {
        val suggestion = TestSuggestionV4Builder().regionDisplayName("notnull")
                .type("HOTEL").build()
        testVM.bind(suggestion)
        testIconObserver.assertValue(R.drawable.hotel_suggest)
        val iconContentDescription = if (testVM.isIconContentDescriptionRequired()) "HOTEL_ICON" else ""
        testIconContentDescriptionObserver.assertValue(iconContentDescription)
    }

    @Test
    fun testAirportIcon() {
        val suggestion = TestSuggestionV4Builder().regionDisplayName("notnull")
                .type("AIRPORT").build()
        testVM.bind(suggestion)
        testIconObserver.assertValue(R.drawable.airport_suggest)
        val iconContentDescription = if (testVM.isIconContentDescriptionRequired()) "AIRPORT_ICON" else ""
        testIconContentDescriptionObserver.assertValue(iconContentDescription)
    }

    @Test
    fun testDefaultIcon() {
        val suggestion = TestSuggestionV4Builder().regionDisplayName("notnull").build()
        testVM.bind(suggestion)
        testIconObserver.assertValue(R.drawable.search_type_icon)
        val iconContentDescription = if (testVM.isIconContentDescriptionRequired()) "SEARCH_TYPE_ICON" else ""
        testIconContentDescriptionObserver.assertValue(iconContentDescription)
    }

    @Test
    fun testTitleFontWithNoSubtitle() {
        val testFontSubscriber = TestObserver.create<FontCache.Font>()
        testVM.titleFontObservable.subscribe(testFontSubscriber)

        val suggestion = TestSuggestionV4Builder().regionDisplayName("Chicago").build()
        testVM.bind(suggestion)

        assertEquals(FontCache.Font.ROBOTO_REGULAR, testFontSubscriber.values()[0])
    }

    @Test
    fun testTitleFontWithSubtitle() {
        val testFontSubscriber = TestObserver.create<FontCache.Font>()
        testVM.titleFontObservable.subscribe(testFontSubscriber)

        val suggestion = TestSuggestionV4Builder().regionDisplayName("Chicago").build()
        val startDate = LocalDate.now()
        val endDate = startDate.plusDays(1)

        val searchInfo = SearchInfo(suggestion, startDate, endDate, TravelerInfo())
        testVM.bind(searchInfo)

        assertEquals(FontCache.Font.ROBOTO_MEDIUM, testFontSubscriber.values()[0])
    }
}
