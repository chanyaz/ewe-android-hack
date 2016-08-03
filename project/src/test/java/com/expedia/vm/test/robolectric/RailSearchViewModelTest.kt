package com.expedia.vm.test.robolectric;

import android.app.Activity
import android.content.Context
import com.expedia.bookings.data.SuggestionV4
import com.expedia.bookings.data.rail.requests.RailSearchRequest
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.vm.rail.RailSearchViewModel
import org.joda.time.LocalDate
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.robolectric.Robolectric
import rx.observers.TestSubscriber
import kotlin.properties.Delegates
import kotlin.test.*


@RunWith(RobolectricRunner::class)
class RailSearchViewModelTest {

    lateinit var searchVM: RailSearchViewModel
    var activity: Activity by Delegates.notNull()

    @Before
    fun setup() {
        searchVM = RailSearchViewModel(getContext())
    }

    fun setupOneWay() {
        val origin = buildRailSuggestion("GBCHX")
        val destination = buildRailSuggestion("GBGLQ")
        val departDate = LocalDate().plusDays(1)
        val departTime = departDate.toDateTimeAtStartOfDay().millis

        searchVM.railOriginObservable.onNext(origin)
        searchVM.railDestinationObservable.onNext(destination)
        searchVM.datesObserver.onNext(Pair(departDate, null))
        searchVM.timesObservable.onNext(Pair(departTime, null))
        searchVM.isRoundTripSearchObservable.onNext(false)
    }

    fun setupRoundTrip() {
        val origin = buildRailSuggestion("GBCHX")
        val destination = buildRailSuggestion("GBGLQ")
        val departDate = LocalDate().plusDays(1)
        val departTime = departDate.toDateTimeAtStartOfDay().millis
        val returnDate = departDate.plusDays(1)
        val returnTime = returnDate.toDateTimeAtStartOfDay().millis

        searchVM.railOriginObservable.onNext(origin)
        searchVM.railDestinationObservable.onNext(destination)
        searchVM.datesObserver.onNext(Pair(departDate, returnDate))
        searchVM.timesObservable.onNext(Pair(departTime, returnTime))
        searchVM.isRoundTripSearchObservable.onNext(true)
    }

    fun buildRailSuggestion(stationCode: String): SuggestionV4 {
        val suggestion = Mockito.mock(SuggestionV4::class.java)
        val hierarchyInfo = SuggestionV4.HierarchyInfo()
        val rails = Mockito.mock(SuggestionV4.Rails::class.java)
        rails.stationCode = stationCode
        hierarchyInfo.rails = rails
        suggestion.hierarchyInfo = hierarchyInfo
        return suggestion
    }

    @Test
    fun testOneWayParams() {
        setupOneWay()
        val searchParamsSubscriber = TestSubscriber<RailSearchRequest>()
        searchVM.searchParamsObservable.subscribe(searchParamsSubscriber)

        searchVM.searchObserver.onNext(Unit)
        assertNotNull(searchParamsSubscriber.onNextEvents[0].origin)
        assertNotNull(searchParamsSubscriber.onNextEvents[0].destination)
        assertEquals(searchVM.datesObservable.value.first.toString(), searchParamsSubscriber.onNextEvents[0].departDate.toString())
        assertNull(searchParamsSubscriber.onNextEvents[0].returnDate)
        assertEquals(searchVM.timesObservable.value.first.toString(), searchParamsSubscriber.onNextEvents[0].departTime.toString())
        assertNull(searchParamsSubscriber.onNextEvents[0].returnTime)
        assertFalse(searchVM.isRoundTripSearchObservable.value)
    }

    @Test
    fun testDatesValidationOneWay() {
        setupOneWay()
        val searchParamsSubscriber = TestSubscriber<RailSearchRequest>()
        val errorNoDatesSubscriber = TestSubscriber<Unit>()
        val errorMaxDurationSubscriber = TestSubscriber<String>()
        val errorMaxRangeSubscriber = TestSubscriber<String>()

        searchVM.searchParamsObservable.subscribe(searchParamsSubscriber)
        searchVM.errorNoDatesObservable.subscribe(errorNoDatesSubscriber)
        searchVM.errorMaxDurationObservable.subscribe(errorMaxDurationSubscriber)
        searchVM.errorMaxRangeObservable.subscribe(errorMaxRangeSubscriber)

        val departDate = LocalDate().plusDays(3301)
        searchVM.datesObserver.onNext(Pair(departDate, null))
        searchVM.searchObserver.onNext(Unit)
        assertFalse(searchVM.isRoundTripSearchObservable.value)
        assertEquals("This date is too far out, please choose a closer date.", errorMaxRangeSubscriber.onNextEvents[0].toString())
    }

    @Test
    fun testDatesValidationRoundTrip() {
        setupRoundTrip()
        val searchParamsSubscriber = TestSubscriber<RailSearchRequest>()
        val errorNoDatesSubscriber = TestSubscriber<Unit>()
        val errorMaxDurationSubscriber = TestSubscriber<String>()
        val errorMaxRangeSubscriber = TestSubscriber<String>()

        searchVM.searchParamsObservable.subscribe(searchParamsSubscriber)
        searchVM.errorNoDatesObservable.subscribe(errorNoDatesSubscriber)
        searchVM.errorMaxDurationObservable.subscribe(errorMaxDurationSubscriber)
        searchVM.errorMaxRangeObservable.subscribe(errorMaxRangeSubscriber)

        var departDate = LocalDate().plusDays(1)
        var returnDate = departDate.plusDays(31)
        searchVM.datesObserver.onNext(Pair(departDate, returnDate))
        searchVM.searchObserver.onNext(Unit)
        assertEquals("We're sorry, but we are unable to search for round trip trains more than 30 days apart.", errorMaxDurationSubscriber.onNextEvents[0].toString())

        departDate = LocalDate().plusDays(330)
        returnDate = departDate.plusDays(1)
        searchVM.datesObserver.onNext(Pair(departDate, returnDate))
        searchVM.searchObserver.onNext(Unit)
        assertTrue(searchVM.isRoundTripSearchObservable.value)
        assertEquals("This date is too far out, please choose a closer date.", errorMaxRangeSubscriber.onNextEvents[0].toString())
    }

    private fun getContext(): Context {
        val activity = Robolectric.buildActivity(Activity::class.java).create().get()
        return activity
    }
}
