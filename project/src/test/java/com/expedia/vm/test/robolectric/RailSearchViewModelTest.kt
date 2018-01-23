package com.expedia.vm.test.robolectric

import android.app.Activity
import com.expedia.bookings.R
import com.expedia.bookings.data.SuggestionV4
import com.expedia.bookings.data.rail.requests.RailSearchRequest
import com.expedia.bookings.data.rail.responses.RailCard
import com.expedia.bookings.services.TestObserver
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.utils.DateFormatUtils
import com.expedia.bookings.utils.LocaleBasedDateFormatUtils
import com.expedia.util.Optional
import com.expedia.vm.rail.RailSearchViewModel
import com.squareup.phrase.Phrase
import org.joda.time.DateTime
import org.joda.time.LocalDate
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.robolectric.Robolectric
import org.robolectric.RuntimeEnvironment
import kotlin.properties.Delegates
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

@RunWith(RobolectricRunner::class)
class RailSearchViewModelTest {

    lateinit var searchVM: RailSearchViewModel
    var activity: Activity by Delegates.notNull()
    val context = RuntimeEnvironment.application

    val startDate = LocalDate.now()
    val returnDate = startDate.plusDays(1)

    val expectedStartDateString = getExpectedStringFormatForDate(startDate)
    val expectedReturnDateString = getExpectedStringFormatForDate(returnDate)

    val expectedOneWayDateLabel = context.getString(R.string.select_departure_date)
    val expectedRoundTripDateLabel = context.getString(R.string.select_dates)

    @Before
    fun setup() {
        activity = Robolectric.buildActivity(Activity::class.java).create().get()
        searchVM = RailSearchViewModel(activity)
    }

    fun setupOneWay() {
        val origin = buildRailSuggestion("GBCHX")
        val destination = buildRailSuggestion("GBGLQ")
        val departDateTime = DateTime.now().plusDays(1)
        val departDate = departDateTime.toLocalDate()
        val departTime = departDateTime.toLocalTime().millisOfDay

        searchVM.railOriginObservable.onNext(origin)
        searchVM.railDestinationObservable.onNext(destination)
        searchVM.datesUpdated(departDate, null)
        searchVM.departTimeSubject.onNext(departTime)
        searchVM.returnTimeSubject.onNext(Optional(null))
        searchVM.isRoundTripSearchObservable.onNext(false)
    }

    fun setupRoundTrip() {
        val origin = buildRailSuggestion("GBCHX")
        val destination = buildRailSuggestion("GBGLQ")
        val departDateTime = DateTime.now().plusDays(1)
        val departDate = departDateTime.toLocalDate()
        val departTime = departDateTime.toLocalTime().millisOfDay
        val returnDateTime = departDateTime.plusDays(1)
        val returnDate = returnDateTime.toLocalDate()
        val returnTime = returnDateTime.toLocalTime().millisOfDay

        searchVM.railOriginObservable.onNext(origin)
        searchVM.railDestinationObservable.onNext(destination)
        searchVM.datesUpdated(departDate, returnDate)
        searchVM.departTimeSubject.onNext(departTime)
        searchVM.returnTimeSubject.onNext(Optional(returnTime))
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
        val searchParamsSubscriber = TestObserver<RailSearchRequest>()
        searchVM.searchParamsObservable.subscribe(searchParamsSubscriber)

        searchVM.searchObserver.onNext(Unit)
        assertNotNull(searchParamsSubscriber.values()[0].origin)
        assertNotNull(searchParamsSubscriber.values()[0].destination)
        assertNull(searchParamsSubscriber.values()[0].returnDate)
        assertEquals(searchVM.departTimeSubject.value.toString(), searchParamsSubscriber.values()[0].departDateTimeMillis.toString())
        assertNull(searchParamsSubscriber.values()[0].returnDateTimeMillis)
        assertFalse(searchVM.isRoundTripSearchObservable.value)
    }

    @Test
    fun testDatesValidationOneWay() {
        setupOneWay()
        val searchParamsSubscriber = TestObserver<RailSearchRequest>()
        val errorNoDatesSubscriber = TestObserver<Unit>()
        val errorMaxDurationSubscriber = TestObserver<String>()
        val errorMaxRangeSubscriber = TestObserver<String>()

        searchVM.searchParamsObservable.subscribe(searchParamsSubscriber)
        searchVM.errorNoDatesObservable.subscribe(errorNoDatesSubscriber)
        searchVM.errorMaxDurationObservable.subscribe(errorMaxDurationSubscriber)
        searchVM.errorMaxRangeObservable.subscribe(errorMaxRangeSubscriber)

        val departDate = LocalDate().plusDays(activity.resources.getInteger(R.integer.calendar_max_days_rail_search)).plusDays(1)
        searchVM.datesUpdated(departDate, null)
        searchVM.searchObserver.onNext(Unit)
        assertFalse(searchVM.isRoundTripSearchObservable.value)
        assertEquals("This date is too far out, please choose a closer date.", errorMaxRangeSubscriber.values()[0].toString())
    }

    @Test
    fun testDatesValidationRoundTrip() {
        setupRoundTrip()
        val searchParamsSubscriber = TestObserver<RailSearchRequest>()
        val errorNoDatesSubscriber = TestObserver<Unit>()
        val errorMaxDurationSubscriber = TestObserver<String>()
        val errorMaxRangeSubscriber = TestObserver<String>()

        searchVM.searchParamsObservable.subscribe(searchParamsSubscriber)
        searchVM.errorNoDatesObservable.subscribe(errorNoDatesSubscriber)
        searchVM.errorMaxDurationObservable.subscribe(errorMaxDurationSubscriber)
        searchVM.errorMaxRangeObservable.subscribe(errorMaxRangeSubscriber)

        var departDate = LocalDate().plusDays(1)
        var returnDate = departDate.plusDays(31)
        searchVM.datesUpdated(departDate, returnDate)
        searchVM.searchObserver.onNext(Unit)
        assertEquals("We're sorry, but we are unable to search for round trip trains more than 30 days apart.", errorMaxDurationSubscriber.values()[0].toString())

        departDate = LocalDate().plusDays(activity.resources.getInteger(R.integer.calendar_max_days_rail_search))
        returnDate = departDate.plusDays(1)
        searchVM.datesUpdated(departDate, returnDate)
        searchVM.searchObserver.onNext(Unit)
        assertTrue(searchVM.isRoundTripSearchObservable.value)
        assertEquals("This date is too far out, please choose a closer date.", errorMaxRangeSubscriber.values()[0].toString())
    }

    @Test
    fun testInvalidRailCardsCount() {
        setupOneWay()
        val errorRailCardCountSubscriber = TestObserver<String>()

        searchVM.errorInvalidCardsCountObservable.subscribe(errorRailCardCountSubscriber)
        searchVM.getParamsBuilder().adults(1)
        searchVM.getParamsBuilder().fareQualifierList(listOf(RailCard("", "", ""), RailCard("", "", "")))
        searchVM.searchObserver.onNext(Unit)
        assertEquals("The number of railcards cannot exceed the number of travelers.", errorRailCardCountSubscriber.values()[0].toString())
    }

    @Test
    fun testCalendarToolTipOneWay() {
        val testSub = TestObserver.create<Pair<String, String>>()
        searchVM.calendarTooltipTextObservable.subscribe(testSub)
        searchVM.datesUpdated(startDate, null)

        assertEquals(context.getString(R.string.calendar_drag_to_modify), testSub.values()[0].second)
    }

    @Test
    fun testCalendarToolTipRoundTrip() {
        val testSub = TestObserver.create<Pair<String, String>>()
        searchVM.calendarTooltipTextObservable.subscribe(testSub)

        searchVM.isRoundTripSearchObservable.onNext(true)
        searchVM.datesUpdated(startDate, null)

        assertEquals(context.getString(R.string.calendar_instructions_date_range_flight_select_return_date),
                testSub.values()[0].second)

        searchVM.datesUpdated(startDate, startDate.plusDays(1))
        assertEquals(context.getString(R.string.calendar_drag_to_modify), testSub.values()[1].second)
    }

    @Test
    fun testCalendarInstructionTextOneWay() {
        val testSub = TestObserver.create<CharSequence>()
        searchVM.dateInstructionObservable.subscribe(testSub)
        searchVM.isRoundTripSearchObservable.onNext(false)

        searchVM.datesUpdated(null, null)
        assertEquals(expectedOneWayDateLabel, testSub.values()[0])

        searchVM.datesUpdated(startDate, null)
        assertEquals(expectedStartDateString, testSub.values()[1])
    }

    @Test
    fun testCalendarInstructionTextRoundTripEmpty() {

        val testSub = TestObserver.create<CharSequence>()
        searchVM.dateInstructionObservable.subscribe(testSub)
        searchVM.isRoundTripSearchObservable.onNext(true)

        searchVM.datesUpdated(null, null)
        assertEquals(expectedRoundTripDateLabel, testSub.values()[0])
    }

    @Test
    fun testCalendarInstructionTextRoundTripStartSelected() {
        val expectedInstructionText = Phrase.from(context, R.string.select_return_date_TEMPLATE)
                .put("startdate", expectedStartDateString)
                .format().toString()

        val testSub = TestObserver.create<CharSequence>()
        searchVM.dateInstructionObservable.subscribe(testSub)
        searchVM.isRoundTripSearchObservable.onNext(true)

        searchVM.datesUpdated(startDate, null)
        assertEquals(expectedInstructionText, testSub.values()[0])
    }

    @Test
    fun testCalendarInstructionTextRoundTripBothDatesComplete() {

        val expectedInstructionText = Phrase.from(context, R.string.start_dash_end_date_range_TEMPLATE)
                .put("startdate", expectedStartDateString).put("enddate", expectedReturnDateString)
                .format().toString()

        val testSub = TestObserver.create<CharSequence>()
        searchVM.dateInstructionObservable.subscribe(testSub)
        searchVM.isRoundTripSearchObservable.onNext(true)

        searchVM.datesUpdated(startDate, returnDate)
        assertEquals(expectedInstructionText, testSub.values()[0])
    }

    @Test
    fun testDateTextEmptyOneWay() {
        val testSub = TestObserver.create<CharSequence>()

        searchVM.dateTextObservable.subscribe(testSub)
        searchVM.resetDatesAndTimes()

        assertEquals(expectedOneWayDateLabel, testSub.values()[0])
    }

    @Test
    fun testDateTextOneWay() {
        val testSub = TestObserver.create<CharSequence>()
        searchVM.dateTextObservable.subscribe(testSub)
        searchVM.datesUpdated(startDate, null)
        searchVM.onTimesChanged(Pair(0, 0))

        assertEquals(getExpectedStringFormatForDateTime(startDate, null, false), testSub.values()[0])
    }

    @Test
    fun testDateTextOneWayContDesc() {
        val testSub = TestObserver.create<CharSequence>()
        searchVM.dateAccessibilityObservable.subscribe(testSub)
        searchVM.datesUpdated(startDate, null)
        searchVM.onTimesChanged(Pair(0, 0))

        val expectedText = getExpectedAccessibilityText(expectedOneWayDateLabel,
                getExpectedStringFormatForDateTime(startDate, null, false))
        assertEquals(expectedText, testSub.values()[0])
    }

    @Test
    fun testDateTextEmptyRoundTrip() {
        val testSub = TestObserver.create<CharSequence>()
        searchVM.isRoundTripSearchObservable.onNext(true)
        searchVM.dateTextObservable.subscribe(testSub)
        searchVM.resetDatesAndTimes()

        assertEquals(expectedRoundTripDateLabel, testSub.values()[0])
    }

    @Test
    fun testDateTextEmptyOneWayContDesc() {
        val testSub = TestObserver.create<CharSequence>()

        searchVM.dateAccessibilityObservable.subscribe(testSub)
        searchVM.resetDatesAndTimes()

        assertEquals(getExpectedAccessibilityText(expectedOneWayDateLabel, ""), testSub.values()[0])
    }

    @Test
    fun testDateTextEmptyRoundTripContDesc() {
        val testSub = TestObserver.create<CharSequence>()
        searchVM.isRoundTripSearchObservable.onNext(true)
        searchVM.dateAccessibilityObservable.subscribe(testSub)
        searchVM.resetDatesAndTimes()

        assertEquals(getExpectedAccessibilityText(expectedRoundTripDateLabel, ""), testSub.values()[0])
    }

    @Test
    fun testDateTextRoundTripContDesc() {
        val testSub = TestObserver.create<CharSequence>()
        searchVM.isRoundTripSearchObservable.onNext(true)
        searchVM.dateAccessibilityObservable.subscribe(testSub)
        searchVM.datesUpdated(startDate, returnDate)
        searchVM.onTimesChanged(Pair(0, 0))

        val expectedText = getExpectedAccessibilityText(expectedRoundTripDateLabel,
                getExpectedStringFormatForDateTime(startDate, returnDate, true))

        assertEquals(expectedText, testSub.values()[0])
    }

    private fun getExpectedAccessibilityText(expectedLabel: String, expectedDuration: String): String {
        return Phrase.from(context, R.string.search_dates_cont_desc_TEMPLATE)
                .put("dates_label", expectedLabel)
                .put("duration_description", expectedDuration).format().toString()
    }

    private fun getExpectedStringFormatForDate(date: LocalDate): String {
        return LocaleBasedDateFormatUtils.localDateToMMMd(date)
    }

    private fun getExpectedStringFormatForDateTime(startDate: LocalDate?, endDate: LocalDate?, forRoundTrip: Boolean): String {
        return DateFormatUtils.formatRailDateTimeRange(context, startDate, 0, endDate, 0, forRoundTrip)
    }
}
