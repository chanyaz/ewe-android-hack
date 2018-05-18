package com.expedia.bookings.lob.lx.ui.viewmodel

import android.app.Activity
import com.expedia.bookings.R
import com.expedia.bookings.data.SuggestionV4
import com.expedia.bookings.data.lx.LxSearchParams
import com.expedia.bookings.features.Features
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.utils.LocaleBasedDateFormatUtils
import org.joda.time.LocalDate
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import com.expedia.bookings.services.TestObserver
import com.expedia.bookings.utils.Constants
import com.expedia.bookings.utils.FeatureTestUtils
import org.robolectric.RuntimeEnvironment
import kotlin.properties.Delegates
import kotlin.test.assertEquals

@RunWith(RobolectricRunner::class)
class LXSearchViewModelTests {
    private val context = RuntimeEnvironment.application
    var vm: LXSearchViewModel by Delegates.notNull()
    var activity: Activity by Delegates.notNull()

    val testStartDate = LocalDate.now()
    val testEndDate = testStartDate.plusDays(Constants.LX_CALENDAR_MAX_DATE_SELECTION)

    @Before
    fun before() {
        activity = Robolectric.buildActivity(Activity::class.java).create().get()
        activity.setTheme(R.style.V2_Theme_LX)
        Ui.getApplication(activity).defaultLXComponents()
        vm = LXSearchViewModel(activity)
    }

    @Test
    fun selectDatesAndSearch() {
        val testSubscriber = TestObserver<LxSearchParams>()
        val expected = arrayListOf<LxSearchParams>()
        val suggestion = getDummySuggestion()

        vm.searchParamsObservable.subscribe(testSubscriber)

        // Selecting a location suggestion for search, as it is a necessary parameter for search
        vm.destinationLocationObserver.onNext(suggestion)

        // Selecting only start date should search with end date as the next day
        vm.datesUpdated(LocalDate.now(), null)
        vm.searchObserver.onNext(Unit)
        expected.add(LxSearchParams.Builder()
                .destination(suggestion)
                .startDate(testStartDate)
                .endDate(testEndDate).build() as LxSearchParams)

        // Select both start date and end date and search
        vm.datesUpdated(testStartDate, testStartDate.plusDays(3))
        vm.searchObserver.onNext(Unit)
        expected.add(LxSearchParams.Builder()
                .destination(suggestion)
                .startDate(testStartDate)
                .endDate(testStartDate.plusDays(3)).build() as LxSearchParams)

        assertEquals(testSubscriber.values()[0].activityStartDate, expected[0].activityStartDate)
        assertEquals(testSubscriber.values()[1].activityEndDate, expected[1].activityEndDate)
    }

    @Test
    fun selectErrorObservablesForLXSearch() {
        val destinationErrorTestSubscriber = TestObserver<Unit>()
        val dateErrorTestSubscriber = TestObserver<Unit>()
        val searchParamsTestSubscriber = TestObserver<LxSearchParams>()

        val suggestion = getDummySuggestion()

        vm.errorNoDestinationObservable.subscribe(destinationErrorTestSubscriber)
        vm.errorNoDatesObservable.subscribe(dateErrorTestSubscriber)
        vm.searchParamsObservable.subscribe(searchParamsTestSubscriber)

        //Neither Destination nor date is selected
        vm.searchObserver.onNext(Unit)

        //Date is not selected yet
        vm.destinationLocationObserver.onNext(suggestion)
        vm.searchObserver.onNext(Unit)

        destinationErrorTestSubscriber.assertValueCount(1)
        dateErrorTestSubscriber.assertValueCount(1)
        searchParamsTestSubscriber.assertValueCount(0)
    }

    @Test
    fun testLXSearchMultipleDates() {
        vm.datesUpdated(null, null)
        assertEquals(context.resources.getString(R.string.select_start_date), vm.dateTextObservable.value.toString().trim())
        assertEquals(context.resources.getString(R.string.lx_search_start_date_button_cont_desc), vm.dateAccessibilityObservable.value.toString().trim())

        vm.datesUpdated(testStartDate, null)
        var startDateText = LocaleBasedDateFormatUtils.localDateToMMMMd(testStartDate)
        assertEquals(startDateText, vm.dateTextObservable.value.toString().trim())
        assertEquals(context.resources.getString(R.string.lx_search_start_date_button_cont_desc) + " " + startDateText, vm.dateAccessibilityObservable.value.toString())

        FeatureTestUtils.enableFeature(context, Features.all.lxMultipleDatesSearch)
        startDateText = LocaleBasedDateFormatUtils.localDateToMMMd(testStartDate)
        val endDateText = LocaleBasedDateFormatUtils.localDateToMMMd(testEndDate)
        val testCalendarToolTipTextObservable = TestObserver.create<Pair<String, String>>()
        vm.calendarTooltipTextObservable.subscribe(testCalendarToolTipTextObservable)

        val testCalendarTooltipContDescObservable = TestObserver.create<String>()
        vm.calendarTooltipContDescObservable.subscribe(testCalendarTooltipContDescObservable)

        vm.datesUpdated(null, null)
        assertEquals(context.resources.getString(R.string.select_dates), vm.dateTextObservable.value.toString().trim())
        assertEquals(context.resources.getString(R.string.base_search_dates_button_cont_desc), vm.dateAccessibilityObservable.value.toString().trim())
        assertEquals(context.resources.getString(R.string.select_dates_proper_case), testCalendarToolTipTextObservable.values()[0].first)
        assertEquals(context.getString(R.string.lx_calendar_tooltip_bottom), testCalendarToolTipTextObservable.values()[0].second)
        assertEquals(context.resources.getString(R.string.select_dates_proper_case), testCalendarTooltipContDescObservable.values()[0])

        vm.datesUpdated(testStartDate, null)
        assertEquals("$startDateText - Select trip end date", vm.dateTextObservable.value.toString().trim())
        assertEquals(startDateText + " - Select trip end date Button. Opens dialog. ", vm.dateAccessibilityObservable.value.toString())
        assertEquals(startDateText, testCalendarToolTipTextObservable.values()[1].first)
        assertEquals("$startDateText. " + context.resources.getString(R.string.lx_calendar_tooltip_bottom), testCalendarTooltipContDescObservable.values()[1])

        vm.datesUpdated(testStartDate, testEndDate)
        assertEquals("$startDateText - $endDateText (15 Days)", vm.dateTextObservable.value.toString().trim())
        assertEquals(context.resources.getString(R.string.base_search_dates_button_cont_desc) + " " + "$startDateText to $endDateText (15 Days)", vm.dateAccessibilityObservable.value.toString())
        assertEquals("$startDateText - $endDateText", testCalendarToolTipTextObservable.values()[2].first)
        assertEquals(context.getString(R.string.lx_calendar_tooltip_maximum_days_limit), testCalendarToolTipTextObservable.values()[2].second)
        assertEquals("$startDateText to $endDateText. Select dates again to modify", testCalendarTooltipContDescObservable.values()[2])

        val newTestEndDate = testStartDate.plusDays(3)
        val newTestEndDateText = LocaleBasedDateFormatUtils.localDateToMMMd(newTestEndDate)
        vm.datesUpdated(testStartDate, newTestEndDate)
        assertEquals("$startDateText - $newTestEndDateText (4 Days)", vm.dateTextObservable.value.toString().trim())
        assertEquals(context.resources.getString(R.string.base_search_dates_button_cont_desc) + " " + "$startDateText to $newTestEndDateText (4 Days)", vm.dateAccessibilityObservable.value.toString())
        assertEquals("$startDateText - $newTestEndDateText", testCalendarToolTipTextObservable.values()[3].first)
        assertEquals(context.getString(R.string.calendar_drag_to_modify), testCalendarToolTipTextObservable.values()[3].second)
        assertEquals("$startDateText to $newTestEndDateText. Select dates again to modify", testCalendarTooltipContDescObservable.values()[3])
    }

    @Test
    fun testComputeDateInstructionText() {
        assertEquals("Select date", vm.getDateInstructionText(null, null))

        FeatureTestUtils.enableFeature(context, Features.all.lxMultipleDatesSearch)
        assertEquals(context.resources.getString(R.string.select_lx_trip_start_dates), vm.getDateInstructionText(null, null))

        val startDateText = LocaleBasedDateFormatUtils.localDateToMMMd(testStartDate)
        val endDateText = LocaleBasedDateFormatUtils.localDateToMMMd(testEndDate)

        assertEquals("$startDateText - Select trip end date", vm.getDateInstructionText(testStartDate, null))
        assertEquals("$startDateText - $endDateText (15 Days)", vm.getDateInstructionText(testStartDate, testEndDate))
    }

    @Test
    fun testGetToolTipContDesc() {
        FeatureTestUtils.enableFeature(context, Features.all.lxMultipleDatesSearch)
        assertEquals(context.getString(R.string.select_dates_proper_case), vm.getToolTipContDesc(null, null))

        val startDateText = LocaleBasedDateFormatUtils.localDateToMMMd(testStartDate!!)
        assertEquals("$startDateText. Select trip end date (optional)", vm.getToolTipContDesc(testStartDate, null))

        val endDateText = LocaleBasedDateFormatUtils.localDateToMMMd(testEndDate!!)
        assertEquals("$startDateText to $endDateText. Select dates again to modify", vm.getToolTipContDesc(testStartDate, testEndDate))
    }

    @Test
    fun testGetNoOfDaysText() {
        FeatureTestUtils.enableFeature(context, Features.all.lxMultipleDatesSearch)
        val actualText = vm.getNoOfDaysText(testStartDate, testEndDate).trim()
        assertEquals("15 Days", actualText)
    }

    @Test
    fun testDestinationShortNameValue() {
        val testSubscriber = TestObserver<String>()
        val expected = "Las Cruces (and vicinity)"
        val suggestion = getDummySuggestion()

        vm.destinationShortNameObservable.subscribe(testSubscriber)
        vm.destinationShortNameObservable.onNext(suggestion.regionNames.shortName)

        assertEquals(testSubscriber.values()[0], expected)
    }

    private fun getDummySuggestion(): SuggestionV4 {
        val suggestion = SuggestionV4()
        suggestion.gaiaId = ""
        suggestion.regionNames = SuggestionV4.RegionNames()
        suggestion.regionNames.displayName = ""
        suggestion.regionNames.fullName = "Las Cruces (and vicinity), New Mexico, United States of America"
        suggestion.regionNames.shortName = "Las Cruces (and vicinity)"
        return suggestion
    }
}
