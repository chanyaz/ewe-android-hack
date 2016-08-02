package com.expedia.bookings.test.robolectric

import android.app.Activity
import com.expedia.bookings.R
import com.expedia.bookings.data.SuggestionV4
import com.expedia.bookings.data.cars.CarSearchParam
import com.expedia.vm.cars.CarSearchViewModel
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
        vm.datesObserver.onNext(Pair(LocalDate.now(), LocalDate.now().plusDays(3)))
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