package com.expedia.vm

import com.expedia.bookings.R
import com.expedia.bookings.data.SuggestionV4
import com.expedia.bookings.data.abacus.AbacusUtils
import com.expedia.bookings.data.hotels.HotelSearchParams
import com.expedia.bookings.hotel.util.HotelSearchManager
import com.expedia.bookings.services.HotelServices
import com.expedia.bookings.test.MultiBrand
import com.expedia.bookings.test.RunForBrands
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.utils.AbacusTestUtils
import com.expedia.bookings.utils.Ui
import com.expedia.testutils.builder.TestSuggestionV4Builder
import org.joda.time.LocalDate
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.robolectric.RuntimeEnvironment
import rx.Observable
import rx.observers.TestSubscriber
import rx.subjects.PublishSubject
import kotlin.test.assertEquals

@RunWith(RobolectricRunner::class)
@RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
class HotelSearchViewModelTest {
    private val context = RuntimeEnvironment.application

    private val mockSearchManager = MockSearchManager(Mockito.mock(HotelServices::class.java))
    private lateinit var testViewModel: HotelSearchViewModel

    private lateinit var suggestionBuilder: TestSuggestionV4Builder

    private val today = LocalDate.now()
    private val testGenericSearchSubscriber = TestSubscriber.create<HotelSearchParams>()

    @Before
    fun setup() {
        Ui.getApplication(context).defaultHotelComponents()
        testViewModel = HotelSearchViewModel(context, mockSearchManager)

        testViewModel.genericSearchSubject.subscribe(testGenericSearchSubscriber)
        suggestionBuilder = TestSuggestionV4Builder().regionDisplayName("diplayName")
    }

    @Test
    fun testSearchMissingRequiredParamDates() {
        triggerParams(suggestion = suggestionBuilder.build())

        val testErrorSubscriber = TestSubscriber.create<Unit>()
        testViewModel.errorNoDatesObservable.subscribe(testErrorSubscriber)

        testViewModel.searchObserver.onNext(Unit)
        assertEquals(1, testErrorSubscriber.onNextEvents.size)
    }

    @Test
    fun testSearchMissingRequiredParamDestination() {
        triggerParams(startDate = today, endDate = today.plusDays(1))

        val testErrorSubscriber = TestSubscriber.create<Unit>()
        testViewModel.errorNoDestinationObservable.subscribe(testErrorSubscriber)

        testViewModel.searchObserver.onNext(Unit)
        assertEquals(1, testErrorSubscriber.onNextEvents.size)
    }

    @Test
    fun testSearchInvalidDuration() {
        val expectedErrorText = context.getString(R.string.hotel_search_range_error_TEMPLATE,
                testViewModel.getMaxSearchDurationDays())
        val invalidEndDate = today.plusDays(testViewModel.getMaxSearchDurationDays() + 1)

        triggerParams(suggestion = suggestionBuilder.build(),
                startDate = today, endDate = invalidEndDate)

        val testErrorSubscriber = TestSubscriber.create<String>()
        testViewModel.errorMaxDurationObservable.subscribe(testErrorSubscriber)

        testViewModel.searchObserver.onNext(Unit)
        assertEquals(expectedErrorText, testErrorSubscriber.onNextEvents[0])
    }

    @Test
    fun testSearchDatesOutOfRange() {
        val invalidStartDate = today.plusDays(testViewModel.getMaxDateRange() + 1)
        val invalidEndDate = invalidStartDate.plusDays(1)

        triggerParams(suggestion = suggestionBuilder.build(),
                startDate = invalidStartDate, endDate = invalidEndDate)

        val testErrorSubscriber = TestSubscriber.create<String>()
        testViewModel.errorMaxRangeObservable.subscribe(testErrorSubscriber)

        testViewModel.searchObserver.onNext(Unit)
        val expectedErrorText = context.getString(R.string.error_date_too_far)
        assertEquals(expectedErrorText, testErrorSubscriber.onNextEvents[0])
    }

    @Test
    fun testHotelIdSearch() {
        val expectedId = "12345"
        val suggestion = suggestionBuilder.hotelId(expectedId).build()
        triggerParams(suggestion = suggestion, startDate = today, endDate = today.plusDays(1))

        val testSubscriber = TestSubscriber.create<HotelSearchParams>()
        testViewModel.hotelIdSearchSubject.subscribe(testSubscriber)

        testViewModel.searchObserver.onNext(Unit)
        assertEquals(1, testSubscriber.onNextEvents.size)
        assertEquals(expectedId, testSubscriber.onNextEvents[0].suggestion.hotelId)
        assertEquals(0, testGenericSearchSubscriber.onNextEvents.size)
    }

    @Test
    fun testRawTextSearch() {
        val suggestion = suggestionBuilder.type("RAW_TEXT_SEARCH").build()
        triggerParams(suggestion = suggestion, startDate = today, endDate = today.plusDays(1))

        val testSubscriber = TestSubscriber.create<HotelSearchParams>()
        testViewModel.rawTextSearchSubject.subscribe(testSubscriber)

        testViewModel.searchObserver.onNext(Unit)
        assertEquals(1, testSubscriber.onNextEvents.size)
        assertEquals(0, testGenericSearchSubscriber.onNextEvents.size)
    }

    @Test
    fun testGenericSearch() {
        triggerParams(suggestion = suggestionBuilder.build(),
                startDate = today, endDate = today.plusDays(1))

        val errors = Observable.merge(testViewModel.errorMaxRangeObservable, testViewModel.errorMaxDurationObservable,
                testViewModel.errorNoDestinationObservable, testViewModel.errorNoDatesObservable)
        val testSubscriber = TestSubscriber.create<Any>()
        errors.subscribe(testSubscriber)

        testViewModel.searchObserver.onNext(Unit)

        assertEquals(0, testSubscriber.onNextEvents.size)
        assertEquals(1, testGenericSearchSubscriber.onNextEvents.size)
    }

    @Test
    fun testPrefetchDisabled_invalidParams() {
        AbacusTestUtils.updateABTest(AbacusUtils.EBAndroidAppHotelGreedySearch, 1)

        val testSubscriber = TestSubscriber.create<Unit>()
        mockSearchManager.searchCalledSubject.subscribe(testSubscriber)

        triggerParams(startDate = today, endDate = today.plusDays(1))
        assertEquals(0, testSubscriber.onNextEvents.size)
    }

    @Test
    fun testPrefetchDisabled_hotelIdSearch() {
        AbacusTestUtils.updateABTest(AbacusUtils.EBAndroidAppHotelGreedySearch, 1)

        val testSubscriber = TestSubscriber.create<Unit>()
        mockSearchManager.searchCalledSubject.subscribe(testSubscriber)

        val suggestion = suggestionBuilder.hotelId("12345").build()
        triggerParams(suggestion = suggestion, startDate = today, endDate = today.plusDays(1))
        assertEquals(0, testSubscriber.onNextEvents.size)
    }

    @Test
    fun testPrefetchDisabled_rawTextSearch() {
        AbacusTestUtils.updateABTest(AbacusUtils.EBAndroidAppHotelGreedySearch, 1)

        val testSubscriber = TestSubscriber.create<Unit>()
        mockSearchManager.searchCalledSubject.subscribe(testSubscriber)

        val suggestion = suggestionBuilder.type("RAW_TEXT_SEARCH").build()
        triggerParams(suggestion = suggestion, startDate = today, endDate = today.plusDays(1))
        assertEquals(0, testSubscriber.onNextEvents.size)
    }

    @Test
    fun testPrefetchDisabled_advancedSearchOptions() {
        AbacusTestUtils.updateABTest(AbacusUtils.EBAndroidAppHotelGreedySearch, 1)

        val testSubscriber = TestSubscriber.create<Unit>()
        mockSearchManager.searchCalledSubject.subscribe(testSubscriber)
        testViewModel.getParamsBuilder().hotelName("ADVANCED_OPTION")
        triggerParams(suggestion = suggestionBuilder.build(),
                startDate = today, endDate = today.plusDays(1))
        assertEquals(0, testSubscriber.onNextEvents.size)
    }

    @Test
    fun testPrefetchEnabled() {
        AbacusTestUtils.updateABTest(AbacusUtils.EBAndroidAppHotelGreedySearch, 1)

        val testSubscriber = TestSubscriber.create<Unit>()
        mockSearchManager.searchCalledSubject.subscribe(testSubscriber)
        triggerParams(suggestion = suggestionBuilder.build(),
                startDate = today, endDate = today.plusDays(1))

        assertEquals(1, testSubscriber.onNextEvents.size)

        testViewModel.destinationLocationObserver.onNext(suggestionBuilder.build())
        assertEquals(2, testSubscriber.onNextEvents.size, "Error: Expected another search after destination change")

        testViewModel.datesUpdated(today.plusDays(1), today.plusDays(2))
        assertEquals(3, testSubscriber.onNextEvents.size, "Error: Expected another search after dates change")
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