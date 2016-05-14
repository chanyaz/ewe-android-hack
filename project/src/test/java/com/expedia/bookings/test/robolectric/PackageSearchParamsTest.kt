package com.expedia.bookings.test.robolectric

import android.app.Activity
import com.expedia.bookings.R
import com.expedia.bookings.data.SuggestionV4
import com.expedia.bookings.data.packages.PackageSearchParams
import com.expedia.vm.packages.PackageSearchViewModel
import org.joda.time.LocalDate
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import rx.observers.TestSubscriber
import kotlin.properties.Delegates
import kotlin.test.assertEquals

@RunWith(RobolectricRunner::class)
class PackageSearchParamsTest {
    var vm: PackageSearchViewModel by Delegates.notNull()
    private var LOTS_MORE: Long = 100
    var activity : Activity by Delegates.notNull()

    @Before
    fun before() {
        activity = Robolectric.buildActivity(Activity::class.java).create().get()
        vm = PackageSearchViewModel(activity)
    }

    @Test
    fun testNumberOfGuests() {
        val params = PackageSearchParams.Builder(activity.resources.getInteger(R.integer.calendar_max_days_hotel_stay),
                activity.resources.getInteger(R.integer.calendar_max_package_selectable_date_range))
                .origin(getDummySuggestion("123"))
                .destination(getDummySuggestion("456"))
                .adults(1)
                .children(listOf(10,2))
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusDays(1))
                .build() as PackageSearchParams

        assertEquals(3, params.guests)
    }

    @Test
    fun testGuestString() {
        val params = PackageSearchParams.Builder(activity.resources.getInteger(R.integer.calendar_max_days_hotel_stay),
                activity.resources.getInteger(R.integer.calendar_max_package_selectable_date_range))
                .origin(getDummySuggestion("123"))
                .destination(getDummySuggestion("456"))
                .adults(1)
                .children(listOf(10,2))
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusDays(1))
                .build() as PackageSearchParams

        assertEquals("1,10,2", params.guestString)
    }

    @Test
    fun testChildrenString() {
        val params = PackageSearchParams.Builder(activity.resources.getInteger(R.integer.calendar_max_days_hotel_stay),
                activity.resources.getInteger(R.integer.calendar_max_package_selectable_date_range))
                .origin(getDummySuggestion("123"))
                .destination(getDummySuggestion("456"))
                .adults(1)
                .children(listOf(10,2))
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusDays(1))
                .build() as PackageSearchParams

        assertEquals("10,2", params.childrenString)
    }

    @Test
    fun testDateAndOriginValidation() {
        val searchParamsSubscriber = TestSubscriber<PackageSearchParams>()
        val noOriginSubscriber = TestSubscriber<Unit>()
        val noDatesSubscriber = TestSubscriber<Unit>()
        val maxRangeSubscriber = TestSubscriber<String>()
        val expectedSearchParams = arrayListOf<PackageSearchParams>()
        val expectedOrigins = arrayListOf<Unit>()
        val expectedDates = arrayListOf<Unit>()
        val expectedRangeErrors = arrayListOf("This date is too far out, please choose a closer date.", "This date is too far out, please choose a closer date.")
        val origin = getDummySuggestion("123")
        val destination = getDummySuggestion("456")

        vm.searchParamsObservable.subscribe(searchParamsSubscriber)
        vm.errorNoDatesObservable.subscribe(noDatesSubscriber)
        vm.errorMaxRangeObservable.subscribe(maxRangeSubscriber)
        vm.errorNoDestinationObservable.subscribe(noOriginSubscriber)

        // Selecting a location suggestion for search, as it is a necessary parameter for search
        vm.originLocationObserver.onNext(origin)
        // Selecting a location suggestion for search, as it is a necessary parameter for search
        vm.destinationLocationObserver.onNext(destination)

        // When neither start date nor end date are selected, search should fire a no notes error
        vm.datesObserver.onNext(Pair(null, null))
        vm.searchObserver.onNext(Unit)
        expectedDates.add(Unit)

        // Selecting only start date should search with end date as the next day
        vm.datesObserver.onNext(Pair(LocalDate.now(), null))
        vm.searchObserver.onNext(Unit)
        expectedSearchParams.add(PackageSearchParams.Builder(activity.resources.getInteger(R.integer.calendar_max_days_hotel_stay),
                activity.resources.getInteger(R.integer.calendar_max_package_selectable_date_range))
                .origin(origin)
                .destination(destination)
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusDays(1)).build() as PackageSearchParams)


        // Select days beyond 329
        vm.datesObserver.onNext(Pair(LocalDate.now().plusDays(329), LocalDate.now().plusDays(330)))
        vm.searchObserver.onNext(Unit)

        // Select days beyond 329 but same day
        vm.datesObserver.onNext(Pair(LocalDate.now().plusDays(329), LocalDate.now().plusDays(329)))
        vm.searchObserver.onNext(Unit)

        // Select both start date and end date and search
        vm.datesObserver.onNext(Pair(LocalDate.now(), LocalDate.now().plusDays(3)))
        vm.searchObserver.onNext(Unit)
        expectedSearchParams.add(PackageSearchParams.Builder(activity.resources.getInteger(R.integer.calendar_max_days_hotel_stay),
                activity.resources.getInteger(R.integer.calendar_max_package_selectable_date_range))
                .origin(origin)
                .destination(destination)
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusDays(3)).build() as PackageSearchParams)

        // When no origin or destination, search should fire no origin error
        vm.suggestionTextChangedObserver.onNext(true)
        expectedOrigins.add(Unit)
        vm.searchObserver.onNext(Unit)
        vm.originLocationObserver.onNext(origin)
        vm.suggestionTextChangedObserver.onNext(false)
        expectedOrigins.add(Unit)
        vm.searchObserver.onNext(Unit)

        searchParamsSubscriber.requestMore(LOTS_MORE)
        assertEquals(expectedSearchParams[0].checkOut, searchParamsSubscriber.onNextEvents[0].checkOut)
        assertEquals(expectedSearchParams[1].checkOut, searchParamsSubscriber.onNextEvents[1].checkOut)
        noDatesSubscriber.requestMore(LOTS_MORE)
        noDatesSubscriber.assertReceivedOnNext(expectedDates)
        maxRangeSubscriber.requestMore(LOTS_MORE)
        maxRangeSubscriber.assertReceivedOnNext(expectedRangeErrors)
        noOriginSubscriber.requestMore(LOTS_MORE)
        noOriginSubscriber.assertReceivedOnNext(expectedOrigins)
    }

    private fun getDummySuggestion(code: String): SuggestionV4 {
        val suggestion = SuggestionV4()
        suggestion.gaiaId = ""
        suggestion.regionNames = SuggestionV4.RegionNames()
        suggestion.regionNames.displayName = ""
        suggestion.regionNames.fullName = ""
        suggestion.regionNames.shortName = ""
        val hierarchyInfo = SuggestionV4.HierarchyInfo()
        val airport =  SuggestionV4.Airport();
        airport.airportCode = "";
        airport.multicity = code;
        hierarchyInfo.airport = airport
        suggestion.hierarchyInfo = hierarchyInfo;
        return suggestion
    }
}
