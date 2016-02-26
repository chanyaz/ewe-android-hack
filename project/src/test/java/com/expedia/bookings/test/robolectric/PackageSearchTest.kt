package com.expedia.bookings.test.robolectric

import android.app.Activity
import com.expedia.bookings.R
import com.expedia.bookings.data.SuggestionV4
import com.expedia.bookings.data.packages.PackageSearchParams
import com.expedia.vm.PackageSearchViewModel
import org.joda.time.LocalDate
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import rx.observers.TestSubscriber
import kotlin.properties.Delegates

@RunWith(RobolectricRunner::class)
class PackageSearchTest {
    public var vm: PackageSearchViewModel by Delegates.notNull()
    private var LOTS_MORE: Long = 100
    var activity : Activity by Delegates.notNull()

    @Before
    fun before() {
        activity = Robolectric.buildActivity(Activity::class.java).create().get()
        vm = PackageSearchViewModel(activity)
    }

    @Test
    fun selectDatesAndSearch() {
        val searchParamsSubscriber = TestSubscriber<PackageSearchParams>()
        val noOriginSubscriber = TestSubscriber<Boolean>()
        val noDatesSubscriber = TestSubscriber<Unit>()
        val expectedSearchParams = arrayListOf<PackageSearchParams>()
        val expectedOrigins = arrayListOf<Boolean>()
        val expectedDates = arrayListOf<Unit>()
        val origin = getDummySuggestion()
        val destination = getDummySuggestion()

        vm.searchParamsObservable.subscribe(searchParamsSubscriber)
        vm.errorNoDatesObservable.subscribe(noDatesSubscriber)
        vm.errorNoOriginObservable.subscribe(noOriginSubscriber)

        // Selecting a location suggestion for search, as it is a necessary parameter for search
        vm.originObserver.onNext(origin)
        // Selecting a location suggestion for search, as it is a necessary parameter for search
        vm.destinationObserver.onNext(destination)

        // When neither start date nor end date are selected, search should fire a no notes error
        vm.datesObserver.onNext(Pair(null, null))
        vm.searchObserver.onNext(Unit)
        expectedDates.add(Unit)

        // Selecting only start date should search with end date as the next day
        vm.datesObserver.onNext(Pair(LocalDate.now(), null))
        vm.searchObserver.onNext(Unit)
        expectedSearchParams.add(PackageSearchParams.Builder(activity.resources.getInteger(R.integer.calendar_max_days_hotel_stay)).origin(origin).destination(destination).checkIn(LocalDate.now()).checkOut(LocalDate.now().plusDays(1)).build())

        // Select both start date and end date and search
        vm.datesObserver.onNext(Pair(LocalDate.now(), LocalDate.now().plusDays(3)))
        vm.searchObserver.onNext(Unit)
        expectedSearchParams.add(PackageSearchParams.Builder(activity.resources.getInteger(R.integer.calendar_max_days_hotel_stay)).origin(origin).destination(destination).checkIn(LocalDate.now()).checkOut(LocalDate.now().plusDays(3)).build())

        // When no origin or destination, search should fire no origin error
        vm.suggestionTextChangedObserver.onNext(true)
        expectedOrigins.add(false)
        vm.searchObserver.onNext(Unit)
        vm.originObserver.onNext(origin)
        vm.suggestionTextChangedObserver.onNext(false)
        expectedOrigins.add(true)
        vm.searchObserver.onNext(Unit)

        searchParamsSubscriber.requestMore(LOTS_MORE)
        searchParamsSubscriber.assertReceivedOnNext(expectedSearchParams)
        noDatesSubscriber.requestMore(LOTS_MORE)
        noDatesSubscriber.assertReceivedOnNext(expectedDates)
        noOriginSubscriber.requestMore(LOTS_MORE)
        noOriginSubscriber.assertReceivedOnNext(expectedOrigins)
    }

    private fun getDummySuggestion(): SuggestionV4 {
        val suggestion = SuggestionV4()
        suggestion.gaiaId = ""
        suggestion.regionNames = SuggestionV4.RegionNames()
        suggestion.regionNames.displayName = ""
        suggestion.regionNames.fullName = ""
        suggestion.regionNames.shortName = ""
        val hierarchyInfo = SuggestionV4.HierarchyInfo()
        val airport =  SuggestionV4.Airport();
        airport.airportCode = "";
        hierarchyInfo.airport = airport
        suggestion.hierarchyInfo = hierarchyInfo;
        return suggestion
    }
}
