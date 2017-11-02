package com.expedia.vm

import com.expedia.bookings.R
import com.expedia.bookings.data.SuggestionV4
import com.expedia.bookings.data.abacus.AbacusUtils
import com.expedia.bookings.data.hotels.HotelSearchParams
import com.expedia.bookings.hotel.util.HotelSearchManager
import com.expedia.bookings.services.HotelServices
import com.expedia.bookings.services.TestObserver
import com.expedia.bookings.test.MultiBrand
import com.expedia.bookings.test.RunForBrands
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.utils.AbacusTestUtils
import com.expedia.bookings.utils.LocaleBasedDateFormatUtils
import com.expedia.bookings.utils.Ui
import com.expedia.testutils.builder.TestSuggestionV4Builder
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import org.joda.time.LocalDate
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.robolectric.RuntimeEnvironment
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@RunWith(RobolectricRunner::class)
@RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
class HotelSearchViewModelTest {

    private val context = RuntimeEnvironment.application

    private val mockSearchManager = MockSearchManager(Mockito.mock(HotelServices::class.java))
    private lateinit var testViewModel: HotelSearchViewModel

    private lateinit var suggestionBuilder: TestSuggestionV4Builder

    private lateinit var today: LocalDate
    private val testGenericSearchSubscriber = TestObserver.create<HotelSearchParams>()

    @Before
    fun setup() {
        Ui.getApplication(context).defaultHotelComponents()
        testViewModel = HotelSearchViewModel(context, mockSearchManager)

        today = testViewModel.getCalendarRules().getFirstAvailableDate()

        testViewModel.genericSearchSubject.subscribe(testGenericSearchSubscriber)
        suggestionBuilder = TestSuggestionV4Builder().regionDisplayName("diplayName")
    }

    @Test
    fun testSearchMissingRequiredParamDates() {
        triggerParams(suggestion = suggestionBuilder.build())

        val testErrorSubscriber = TestObserver.create<Unit>()
        testViewModel.errorNoDatesObservable.subscribe(testErrorSubscriber)

        testViewModel.searchObserver.onNext(Unit)
        assertEquals(1, testErrorSubscriber.valueCount())
    }

    @Test
    fun testSearchMissingRequiredParamDestination() {
        triggerParams(startDate = today, endDate = today.plusDays(1))

        val testErrorSubscriber = TestObserver.create<Unit>()
        testViewModel.errorNoDestinationObservable.subscribe(testErrorSubscriber)

        testViewModel.searchObserver.onNext(Unit)
        assertEquals(1, testErrorSubscriber.valueCount())
    }

    @Test
    fun testSearchInvalidDuration() {
        val invalidEndDate = today.plusDays(testViewModel.getCalendarRules().getMaxSearchDurationDays() + 1)

        triggerParams(suggestion = suggestionBuilder.build(),
                startDate = today, endDate = invalidEndDate)

        val testErrorSubscriber = TestObserver.create<String>()
        testViewModel.errorMaxDurationObservable.subscribe(testErrorSubscriber)

        testViewModel.searchObserver.onNext(Unit)
        val expectedErrorText = context.getString(R.string.hotel_search_range_error_TEMPLATE,
                testViewModel.getCalendarRules().getMaxSearchDurationDays())
        assertEquals(expectedErrorText, testErrorSubscriber.values()[0])
    }

    @Test
    fun testSearchDatesOutOfRange() {
        val invalidStartDate = today.plusDays(testViewModel.getCalendarRules().getMaxDateRange() + 1)
        val invalidEndDate = invalidStartDate.plusDays(1)

        triggerParams(suggestion = suggestionBuilder.build(),
                startDate = invalidStartDate, endDate = invalidEndDate)

        val testErrorSubscriber = TestObserver.create<String>()
        testViewModel.errorMaxRangeObservable.subscribe(testErrorSubscriber)

        testViewModel.searchObserver.onNext(Unit)
        val expectedErrorText = context.getString(R.string.error_date_too_far)
        assertEquals(expectedErrorText, testErrorSubscriber.values()[0])
    }

    @Test
    fun testSearchStartMaxDateStrings() {
        val testDateTextObservable = TestObserver.create<CharSequence>()
        val testDateAccessibilityObservable = TestObserver.create<CharSequence>()
        val testDateInstructionObservable = TestObserver.create<CharSequence>()
        val testCalendarTooltipTextObservable = TestObserver.create<Pair<String, String>>()
        val testCalendarTooltipContDescObservable = TestObserver.create<String>()

        testViewModel.dateTextObservable.subscribe(testDateTextObservable)
        testViewModel.dateAccessibilityObservable.subscribe(testDateAccessibilityObservable)
        testViewModel.dateInstructionObservable.subscribe(testDateInstructionObservable)
        testViewModel.calendarTooltipTextObservable.subscribe(testCalendarTooltipTextObservable)
        testViewModel.calendarTooltipContDescObservable.subscribe(testCalendarTooltipContDescObservable)

        val startDate = today.plusDays(testViewModel.getCalendarRules().getMaxDateRange())
        triggerParams(suggestion = suggestionBuilder.build(),
                startDate = startDate, endDate = null)

        val startDateString = LocaleBasedDateFormatUtils.localDateToMMMd(startDate)

        assertEquals(startDateString + " - Select check out date", testDateTextObservable.values()[0])
        assertEquals(startDateString + " - Select check out date Button. Opens dialog. ", testDateAccessibilityObservable.values()[0])
        assertEquals(startDateString + " - Select check out date", testDateInstructionObservable.values()[0])
        assertEquals(startDateString, testCalendarTooltipTextObservable.values()[0].first)
        assertEquals("Next: Select check out date", testCalendarTooltipTextObservable.values()[0].second)
        assertEquals(startDateString + ". Next: Select check out date", testCalendarTooltipContDescObservable.values()[0])
    }

    @Test
    fun testStartMaxDateCantDoSearch() {
        val testErrorNoDatesObservable = TestObserver.create<Unit>()
        testViewModel.errorNoDatesObservable.subscribe(testErrorNoDatesObservable)

        val startDate = today.plusDays(testViewModel.getCalendarRules().getMaxDateRange())
        triggerParams(suggestion = suggestionBuilder.build(),
                startDate = startDate, endDate = null)

        assertFalse(testViewModel.getParamsBuilder().hasStartAndEndDates())

        testViewModel.searchObserver.onNext(Unit)

        testGenericSearchSubscriber.assertValueCount(0)
        testErrorNoDatesObservable.assertValueCount(1)
    }

    @Test
    fun testSearchStartMaxDateMinusOneStrings() {
        val testDateTextObservable = TestObserver.create<CharSequence>()
        val testDateAccessibilityObservable = TestObserver.create<CharSequence>()
        val testDateInstructionObservable = TestObserver.create<CharSequence>()
        val testCalendarTooltipTextObservable = TestObserver.create<Pair<String, String>>()
        val testCalendarTooltipContDescObservable = TestObserver.create<String>()

        testViewModel.dateTextObservable.subscribe(testDateTextObservable)
        testViewModel.dateAccessibilityObservable.subscribe(testDateAccessibilityObservable)
        testViewModel.dateInstructionObservable.subscribe(testDateInstructionObservable)
        testViewModel.calendarTooltipTextObservable.subscribe(testCalendarTooltipTextObservable)
        testViewModel.calendarTooltipContDescObservable.subscribe(testCalendarTooltipContDescObservable)

        val startDate = today.plusDays(testViewModel.getCalendarRules().getMaxDateRange() - 1)
        triggerParams(suggestion = suggestionBuilder.build(),
                startDate = startDate, endDate = null)

        val startDateString = LocaleBasedDateFormatUtils.localDateToMMMd(startDate)

        assertEquals(startDateString + " - Select check out date", testDateTextObservable.values()[0])
        assertEquals(startDateString + " - Select check out date Button. Opens dialog. ", testDateAccessibilityObservable.values()[0])
        assertEquals(startDateString + " - Select check out date", testDateInstructionObservable.values()[0])
        assertEquals(startDateString, testCalendarTooltipTextObservable.values()[0].first)
        assertEquals("Next: Select check out date", testCalendarTooltipTextObservable.values()[0].second)
        assertEquals(startDateString + ". Next: Select check out date", testCalendarTooltipContDescObservable.values()[0])
    }

    @Test
    fun testStartMaxDateMinusOneCanDoSearch() {
        val testErrorNoDatesObservable = TestObserver.create<Unit>()
        testViewModel.errorNoDatesObservable.subscribe(testErrorNoDatesObservable)

        val startDate = today.plusDays(testViewModel.getCalendarRules().getMaxDateRange() - 1)
        val endDate = startDate.plusDays(1)
        triggerParams(suggestion = suggestionBuilder.build(),
                startDate = startDate, endDate = null)

        testViewModel.searchObserver.onNext(Unit)

        testGenericSearchSubscriber.assertValueCount(1)
        assertEquals(startDate, testGenericSearchSubscriber.values()[0].startDate)
        assertEquals(endDate, testGenericSearchSubscriber.values()[0].endDate)
        testErrorNoDatesObservable.assertValueCount(0)
    }

    @Test
    fun testSearchNullStartEndDateStrings() {
        val testDateTextObservable = TestObserver.create<CharSequence>()
        val testDateAccessibilityObservable = TestObserver.create<CharSequence>()
        val testDateInstructionObservable = TestObserver.create<CharSequence>()
        val testCalendarTooltipTextObservable = TestObserver.create<Pair<String, String>>()
        val testCalendarTooltipContDescObservable = TestObserver.create<String>()

        testViewModel.dateTextObservable.subscribe(testDateTextObservable)
        testViewModel.dateAccessibilityObservable.subscribe(testDateAccessibilityObservable)
        testViewModel.dateInstructionObservable.subscribe(testDateInstructionObservable)
        testViewModel.calendarTooltipTextObservable.subscribe(testCalendarTooltipTextObservable)
        testViewModel.calendarTooltipContDescObservable.subscribe(testCalendarTooltipContDescObservable)

        triggerParams(suggestion = suggestionBuilder.build(),
                startDate = null, endDate = null)

        assertEquals("Select dates", testDateTextObservable.values()[0])
        assertEquals("Select dates Button. Opens dialog. ", testDateAccessibilityObservable.values()[0])
        assertEquals("Select check in date", testDateInstructionObservable.values()[0])
        assertEquals("Select dates", testCalendarTooltipTextObservable.values()[0].first)
        assertEquals("Next: Select check out date", testCalendarTooltipTextObservable.values()[0].second)
        assertEquals("Select dates", testCalendarTooltipContDescObservable.values()[0])
    }

    @Test
    fun testNullStartEndDateCantDoSearch() {
        val testErrorNoDatesObservable = TestObserver.create<Unit>()
        testViewModel.errorNoDatesObservable.subscribe(testErrorNoDatesObservable)

        triggerParams(suggestion = suggestionBuilder.build(),
                startDate = null, endDate = null)

        testViewModel.searchObserver.onNext(Unit)

        testGenericSearchSubscriber.assertValueCount(0)
        testErrorNoDatesObservable.assertValueCount(1)
    }

    @Test
    fun testSearchGoodStartEndDateStrings() {
        val testDateTextObservable = TestObserver.create<CharSequence>()
        val testDateAccessibilityObservable = TestObserver.create<CharSequence>()
        val testDateInstructionObservable = TestObserver.create<CharSequence>()
        val testCalendarTooltipTextObservable = TestObserver.create<Pair<String, String>>()
        val testCalendarTooltipContDescObservable = TestObserver.create<String>()

        testViewModel.dateTextObservable.subscribe(testDateTextObservable)
        testViewModel.dateAccessibilityObservable.subscribe(testDateAccessibilityObservable)
        testViewModel.dateInstructionObservable.subscribe(testDateInstructionObservable)
        testViewModel.calendarTooltipTextObservable.subscribe(testCalendarTooltipTextObservable)
        testViewModel.calendarTooltipContDescObservable.subscribe(testCalendarTooltipContDescObservable)

        val startDate = today.plusDays(testViewModel.getCalendarRules().getMaxDateRange() - 1)
        val endDate = startDate.plusDays(1)
        triggerParams(suggestion = suggestionBuilder.build(),
                startDate = startDate, endDate = endDate)

        val startDateString = LocaleBasedDateFormatUtils.localDateToMMMd(startDate)
        val endDateString = LocaleBasedDateFormatUtils.localDateToMMMd(endDate)

        assertEquals(startDateString + " - " + endDateString + " (1 night)", testDateTextObservable.values()[0])
        assertEquals("Select dates Button. Opens dialog. " + startDateString + " to " + endDateString + " (1 night)", testDateAccessibilityObservable.values()[0])
        assertEquals(startDateString + " - " + endDateString + " (1 night)", testDateInstructionObservable.values()[0])
        assertEquals(startDateString + " - " + endDateString, testCalendarTooltipTextObservable.values()[0].first)
        assertEquals("Drag to modify", testCalendarTooltipTextObservable.values()[0].second)
        assertEquals(startDateString + " to " + endDateString + ". Select dates again to modify", testCalendarTooltipContDescObservable.values()[0])
    }

    @Test
    fun testGoodStartEndDateCanDoSearch() {
        val testErrorNoDatesObservable = TestObserver.create<Unit>()
        testViewModel.errorNoDatesObservable.subscribe(testErrorNoDatesObservable)

        val startDate = today.plusDays(testViewModel.getCalendarRules().getMaxDateRange() - 1)
        val endDate = startDate.plusDays(1)
        triggerParams(suggestion = suggestionBuilder.build(),
                startDate = startDate, endDate = endDate)

        testViewModel.searchObserver.onNext(Unit)
        assertEquals(startDate, testGenericSearchSubscriber.values()[0].startDate)
        assertEquals(endDate, testGenericSearchSubscriber.values()[0].endDate)
        testErrorNoDatesObservable.assertValueCount(0)
    }

    @Test
    fun testHotelIdSearch() {
        val expectedId = "12345"
        val suggestion = suggestionBuilder.hotelId(expectedId).build()
        triggerParams(suggestion = suggestion, startDate = today, endDate = today.plusDays(1))

        val testSubscriber = TestObserver.create<HotelSearchParams>()
        testViewModel.hotelIdSearchSubject.subscribe(testSubscriber)

        testViewModel.searchObserver.onNext(Unit)
        assertEquals(1, testSubscriber.valueCount())
        assertEquals(expectedId, testSubscriber.values()[0].suggestion.hotelId)
        assertEquals(0, testGenericSearchSubscriber.valueCount())
    }

    @Test
    fun testRawTextSearch() {
        val suggestion = suggestionBuilder.type("RAW_TEXT_SEARCH").build()
        triggerParams(suggestion = suggestion, startDate = today, endDate = today.plusDays(1))

        val testSubscriber = TestObserver.create<HotelSearchParams>()
        testViewModel.rawTextSearchSubject.subscribe(testSubscriber)

        testViewModel.searchObserver.onNext(Unit)
        assertEquals(1, testSubscriber.valueCount())
        assertEquals(0, testGenericSearchSubscriber.valueCount())
    }

    @Test
    fun testGenericSearch() {
        triggerParams(suggestion = suggestionBuilder.build(),
                startDate = today, endDate = today.plusDays(1))

        val errors = Observable.merge(testViewModel.errorMaxRangeObservable, testViewModel.errorMaxDurationObservable,
                testViewModel.errorNoDestinationObservable, testViewModel.errorNoDatesObservable)
        val testSubscriber = TestObserver.create<Any>()
        errors.subscribe(testSubscriber)

        testViewModel.searchObserver.onNext(Unit)

        assertEquals(0, testSubscriber.valueCount())
        assertEquals(1, testGenericSearchSubscriber.valueCount())
    }

    @Test
    fun testPrefetchDisabled_invalidParams() {
        AbacusTestUtils.updateABTest(AbacusUtils.EBAndroidAppHotelGreedySearch, 1)

        val testSubscriber = TestObserver.create<Unit>()
        mockSearchManager.searchCalledSubject.subscribe(testSubscriber)

        triggerParams(startDate = today, endDate = today.plusDays(1))
        assertEquals(0, testSubscriber.valueCount())
    }

    @Test
    fun testPrefetchDisabled_hotelIdSearch() {
        AbacusTestUtils.updateABTest(AbacusUtils.EBAndroidAppHotelGreedySearch, 1)

        val testSubscriber = TestObserver.create<Unit>()
        mockSearchManager.searchCalledSubject.subscribe(testSubscriber)

        val suggestion = suggestionBuilder.hotelId("12345").build()
        triggerParams(suggestion = suggestion, startDate = today, endDate = today.plusDays(1))
        assertEquals(0, testSubscriber.valueCount())
    }

    @Test
    fun testPrefetchDisabled_rawTextSearch() {
        AbacusTestUtils.updateABTest(AbacusUtils.EBAndroidAppHotelGreedySearch, 1)

        val testSubscriber = TestObserver.create<Unit>()
        mockSearchManager.searchCalledSubject.subscribe(testSubscriber)

        val suggestion = suggestionBuilder.type("RAW_TEXT_SEARCH").build()
        triggerParams(suggestion = suggestion, startDate = today, endDate = today.plusDays(1))
        assertEquals(0, testSubscriber.valueCount())
    }

    @Test
    fun testPrefetchDisabled_advancedSearchOptions() {
        AbacusTestUtils.updateABTest(AbacusUtils.EBAndroidAppHotelGreedySearch, 1)

        val testSubscriber = TestObserver.create<Unit>()
        mockSearchManager.searchCalledSubject.subscribe(testSubscriber)
        testViewModel.getParamsBuilder().hotelName("ADVANCED_OPTION")
        triggerParams(suggestion = suggestionBuilder.build(),
                startDate = today, endDate = today.plusDays(1))
        assertEquals(0, testSubscriber.valueCount())
    }

    @Test
    fun testPrefetchDisabled_deeplink() {
        AbacusTestUtils.updateABTest(AbacusUtils.EBAndroidAppHotelGreedySearch, 1)

        val testSubscriber = TestObserver.create<Unit>()
        mockSearchManager.searchCalledSubject.subscribe(testSubscriber)
        testViewModel.ignoreGreedyForDeepLink = true
        triggerParams(suggestion = suggestionBuilder.build(),
                startDate = today, endDate = today.plusDays(1))
        assertEquals(0, testSubscriber.values().size)
    }

    @Test
    fun testPrefetchEnabled() {
        AbacusTestUtils.updateABTest(AbacusUtils.EBAndroidAppHotelGreedySearch, 1)

        val testSubscriber = TestObserver.create<Unit>()
        mockSearchManager.searchCalledSubject.subscribe(testSubscriber)
        triggerParams(suggestion = suggestionBuilder.build(),
                startDate = today, endDate = today.plusDays(1))

        assertEquals(1, testSubscriber.valueCount())

        testViewModel.destinationLocationObserver.onNext(suggestionBuilder.build())
        assertEquals(2, testSubscriber.valueCount(), "Error: Expected another search after destination change")

        testViewModel.datesUpdated(today.plusDays(1), today.plusDays(2))
        assertEquals(3, testSubscriber.valueCount(), "Error: Expected another search after dates change")
    }

    @Test
    fun testShopWithPointsToggle() {
        testViewModel.shopWithPointsViewModel.isShopWithPointsAvailableObservable.onNext(true)

        val testSubscriber = TestObserver<HotelSearchParams>()
        testViewModel.genericSearchSubject.subscribe(testSubscriber)

        triggerParams(suggestion = suggestionBuilder.build(),
                startDate = LocalDate.now(), endDate = LocalDate.now().plusDays(1))

        testViewModel.searchObserver.onNext(Unit)

        assertTrue(testSubscriber.values()[0].shopWithPoints)

        testViewModel.shopWithPointsViewModel.shopWithPointsToggleObservable.onNext(false)
        testViewModel.searchObserver.onNext(Unit)

        assertFalse(testSubscriber.values()[1].shopWithPoints)

        testViewModel.shopWithPointsViewModel.shopWithPointsToggleObservable.onNext(true)
        testViewModel.searchObserver.onNext(Unit)

        assertTrue(testSubscriber.values()[2].shopWithPoints)
    }

    private class MockSearchManager(hotelServices: HotelServices) : HotelSearchManager(hotelServices) {
        val searchCalledSubject = PublishSubject.create<Unit>()

        override fun doSearch(params: HotelSearchParams, prefetchSearch: Boolean) {
            searchCalledSubject.onNext(Unit)
            //do nothing
        }
    }

    private fun triggerParams(suggestion: SuggestionV4? = null,
                              startDate: LocalDate? = null,
                              endDate: LocalDate? = null) {
        suggestion?.let { testViewModel.destinationLocationObserver.onNext(it) }
        testViewModel.datesUpdated(startDate, endDate)
    }
}
