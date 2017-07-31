package com.expedia.bookings.test.robolectric

import android.app.Activity
import com.expedia.bookings.R
import com.expedia.bookings.data.SuggestionV4
import com.expedia.bookings.data.cars.CarSearchParam
import com.expedia.bookings.utils.DateFormatUtils
import com.expedia.bookings.utils.DateUtils
import com.expedia.bookings.utils.LocaleBasedDateFormatUtils
import com.expedia.vm.cars.CarSearchViewModel
import org.joda.time.DateTime
import org.joda.time.LocalDate
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import rx.observers.TestSubscriber
import kotlin.properties.Delegates
import kotlin.test.assertEquals

@RunWith(RobolectricRunner::class)
class CarSearchViewModelTest {
    var vm: CarSearchViewModel by Delegates.notNull()
    var activity : Activity by Delegates.notNull()
    val dateNow = LocalDate.now()
    @Before
    fun before() {
        activity = Robolectric.buildActivity(Activity::class.java).create().get()
        activity.setTheme(R.style.V2_Theme_Cars)
        vm = CarSearchViewModel(activity)

    }

    @Test
    fun selectDatesAndSearch() {
        val testSubscriber = TestSubscriber<CarSearchParam>()
        val expected = arrayListOf<CarSearchParam>()
        val suggestion = getDummySuggestion()

        vm.searchParamsObservable.subscribe(testSubscriber)

        // Selecting a location suggestion for search, as it is a necessary parameter for search
        vm.originLocationObserver.onNext(suggestion)

        // Select both start date and end date and search
        vm.datesUpdated(LocalDate.now(), LocalDate.now().plusDays(3))
        vm.searchObserver.onNext(Unit)

        expected.add(CarSearchParam.Builder()
                .origin(suggestion)
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusDays(3)).build() as CarSearchParam)

        assertEquals(testSubscriber.onNextEvents[0].startDate, expected[0].startDate)
    }

    @Test
    fun selectErrorObservablesForCarSearch() {
        val destinationErrorTestSubscriber = TestSubscriber<Unit>()
        val dateErrorTestSubscriber = TestSubscriber<Unit>()
        val searchParamsTestSubscriber = TestSubscriber<CarSearchParam>()


        val suggestion = getDummySuggestion()

        vm.errorNoDestinationObservable.subscribe(destinationErrorTestSubscriber)
        vm.errorNoDatesObservable.subscribe(dateErrorTestSubscriber)
        vm.searchParamsObservable.subscribe(searchParamsTestSubscriber)

        //Neither Destination nor date is selected
        vm.searchObserver.onNext(Unit)

        //Date is not selected yet
        vm.originLocationObserver.onNext(suggestion)
        vm.searchObserver.onNext(Unit)

        destinationErrorTestSubscriber.assertValueCount(1)
        dateErrorTestSubscriber.assertValueCount(1)
        searchParamsTestSubscriber.assertValueCount(0)
    }

    @Test
    fun testOnTimesChanged() {
        val dateTextTestSubscriber = TestSubscriber<CharSequence>()
        val dateAccessibilityTestSubscriber = TestSubscriber<CharSequence>()
        vm.dateTextObservable.subscribe(dateTextTestSubscriber)
        vm.dateAccessibilityObservable.subscribe(dateAccessibilityTestSubscriber)
        
        //when start date is null
        vm.onTimesChanged(Pair(36000, 4800000))
        assertEquals(dateTextTestSubscriber.onNextEvents[0].toString(),"Select pick-up and drop-off dates")
        assertEquals(dateAccessibilityTestSubscriber.onNextEvents[0].toString(),"Select travel dates. Button. Opens dialog")

        //when start date is not null
        val dates = Pair(dateNow,  dateNow.plusDays(3))
        vm.datesUpdated(dates.first, dates.second)
        vm.onTimesChanged(Pair(36000, 4800000))

        var expectedDateText = dateFormatter(dateNow, dateNow.plusDays(3), Pair(36000, 4800000), false )
        assertEquals(expectedDateText, dateTextTestSubscriber.onNextEvents[1].toString());

        var expectedDateTextAccessbility = dateFormatter(dateNow, dateNow.plusDays(3), Pair(36000, 4800000), true )
        assertEquals("Select pick-up and drop-off dates Button. Opens dialog. " + expectedDateTextAccessbility,
                dateAccessibilityTestSubscriber.onNextEvents[1].toString())

    }
    @Test
    fun testOnDatesChangedValidation() {
        val dateInstructionTestSubscriber = TestSubscriber<CharSequence>()
        val calendarTooltipTextTestSubscriber = TestSubscriber<Pair<String, String>>()
        vm.dateInstructionObservable.subscribe(dateInstructionTestSubscriber)
        vm.calendarTooltipTextObservable.subscribe(calendarTooltipTextTestSubscriber)

        //When start date and end date are null
        vm.datesUpdated(null, null)
        assertEquals(dateInstructionTestSubscriber.onNextEvents[0].toString(), "Select pick-up date")
        assertEquals(calendarTooltipTextTestSubscriber.onNextEvents[0].first, "Select dates")
        assertEquals(calendarTooltipTextTestSubscriber.onNextEvents[0].second, "Next: Select drop-off date")

        //when start date is not null and end date is null
        vm.datesUpdated(dateNow.plusDays(3), null)
        assertEquals(dateInstructionTestSubscriber.onNextEvents[1].toString(), LocaleBasedDateFormatUtils.localDateToMMMd(dateNow.plusDays(3))
                + " - Select drop-off date")
        assertEquals(calendarTooltipTextTestSubscriber.onNextEvents[1].first, LocaleBasedDateFormatUtils.localDateToMMMd(dateNow.plusDays(3)))
        assertEquals(calendarTooltipTextTestSubscriber.onNextEvents[1].second, "Next: Select drop-off date")

        //when start date and end date are not null
        vm.datesUpdated(dateNow, dateNow.plusDays(3))
        assertEquals(dateInstructionTestSubscriber.onNextEvents[2].toString(), LocaleBasedDateFormatUtils.localDateToMMMd(dateNow)
                + " - " + LocaleBasedDateFormatUtils.localDateToMMMd(dateNow.plusDays(3)))
        assertEquals(calendarTooltipTextTestSubscriber.onNextEvents[2].first, LocaleBasedDateFormatUtils.localDateToMMMd(dateNow)
                + " - " + LocaleBasedDateFormatUtils.localDateToMMMd(dateNow.plusDays(3)))
        assertEquals(calendarTooltipTextTestSubscriber.onNextEvents[2].second, "Drag to modify")

    }

    @Test
    fun testStartDate() {
        assertEquals(LocalDate.now(), vm.getFirstAvailableDate(), "Start Date is Today")
    }

    private fun dateFormatter(startDateTime: LocalDate, endDateTime: LocalDate, times: Pair<Int, Int>, isContentDescription: Boolean): String{
        val (startMillis, endMillis) = times
        var formattedString = DateFormatUtils.formatStartEndDateTimeRange(activity, DateUtils.localDateAndMillisToDateTime(startDateTime, startMillis),
                DateUtils.localDateAndMillisToDateTime(endDateTime, endMillis), isContentDescription)
        return formattedString
    }
    private fun getDummySuggestion(): SuggestionV4 {
        val suggestion = SuggestionV4()
        suggestion.gaiaId = ""
        suggestion.regionNames = SuggestionV4.RegionNames()
        suggestion.regionNames.displayName = "SanFransisco"
        suggestion.regionNames.fullName = "SanFransisco"
        suggestion.regionNames.shortName = "SanFransisco"
        suggestion.coordinates = SuggestionV4.LatLng();
        suggestion.coordinates.lat = 37.61594
        suggestion.coordinates.lng = -122.387996
        suggestion.type = ""
        suggestion.isMinorAirport = true
        return suggestion
    }
}