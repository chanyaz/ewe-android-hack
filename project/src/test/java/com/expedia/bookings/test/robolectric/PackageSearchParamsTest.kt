package com.expedia.bookings.test.robolectric

import android.app.Activity
import com.expedia.bookings.R
import com.expedia.bookings.data.SuggestionV4
import com.expedia.bookings.data.packages.PackageSearchParams
import com.expedia.bookings.utils.Ui
import com.expedia.vm.packages.PackageSearchViewModel
import org.joda.time.LocalDate
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import com.expedia.bookings.services.TestObserver
import kotlin.properties.Delegates
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

@RunWith(RobolectricRunner::class)
class PackageSearchParamsTest {
    var vm: PackageSearchViewModel by Delegates.notNull()
    var activity: Activity by Delegates.notNull()

    @Before
    fun before() {
        activity = Robolectric.buildActivity(Activity::class.java).create().get()
        Ui.getApplication(activity).defaultTravelerComponent()
        vm = PackageSearchViewModel(activity)
    }

    @Test
    fun testNumberOfGuests() {
        val params = PackageSearchParams.Builder(activity.resources.getInteger(R.integer.calendar_max_days_hotel_stay),
                activity.resources.getInteger(R.integer.max_calendar_selectable_date_range))
                .origin(getDummySuggestion("123"))
                .destination(getDummySuggestion("456"))
                .adults(1)
                .children(listOf(10, 2))
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusDays(1))
                .build() as PackageSearchParams

        assertEquals(3, params.guests)
    }

    @Test
    fun testGuestString() {
        val params = PackageSearchParams.Builder(activity.resources.getInteger(R.integer.calendar_max_days_hotel_stay),
                activity.resources.getInteger(R.integer.max_calendar_selectable_date_range))
                .origin(getDummySuggestion("123"))
                .destination(getDummySuggestion("456"))
                .adults(1)
                .children(listOf(10, 2))
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusDays(1))
                .build() as PackageSearchParams

        assertEquals("1,10,2", params.guestString)
    }

    @Test
    fun testChildAgesString() {
        val params = PackageSearchParams.Builder(activity.resources.getInteger(R.integer.calendar_max_days_hotel_stay),
                activity.resources.getInteger(R.integer.max_calendar_selectable_date_range))
                .infantSeatingInLap(false)
                .origin(getDummySuggestion("123"))
                .destination(getDummySuggestion("456"))
                .adults(1)
                .children(listOf(10, 2))
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusDays(1))
                .build() as PackageSearchParams

        assertEquals("10,2", params.childAges)
        assertNull(params.infantsInSeats)
    }

    @Test
    fun testInfantInSeats() {
        val params = PackageSearchParams.Builder(activity.resources.getInteger(R.integer.calendar_max_days_hotel_stay),
                activity.resources.getInteger(R.integer.max_calendar_selectable_date_range))
                .infantSeatingInLap(false)
                .origin(getDummySuggestion("123"))
                .destination(getDummySuggestion("456"))
                .adults(1)
                .children(listOf(10, 1))
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusDays(1))
                .build() as PackageSearchParams

        assertEquals("10,1", params.childAges)
        assertNotNull(params.infantsInSeats)
        assertTrue(params.infantsInSeats!!)
    }

    @Test
    fun testOriginIdString() {
        val params = PackageSearchParams.Builder(activity.resources.getInteger(R.integer.calendar_max_days_hotel_stay),
                activity.resources.getInteger(R.integer.max_calendar_selectable_date_range))
                .origin(getDummySuggestion("123"))
                .destination(getDummySuggestion("456"))
                .adults(1)
                .children(listOf(10, 2))
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusDays(1))
                .build() as PackageSearchParams

        val paramsMap = params.toQueryMap()
        assertEquals("1011", paramsMap["originId"])
        assertEquals("1011", params.originId)
    }

    @Test
    fun testOriginIdStringWhenMultiCityNull() {
        val dummyOriginSuggestion = getDummySuggestion("123")
        dummyOriginSuggestion.hierarchyInfo?.airport?.multicity = null

        val params = PackageSearchParams.Builder(activity.resources.getInteger(R.integer.calendar_max_days_hotel_stay),
                activity.resources.getInteger(R.integer.max_calendar_selectable_date_range))
                .origin(dummyOriginSuggestion)
                .destination(getDummySuggestion("456"))
                .adults(1)
                .children(listOf(10, 2))
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusDays(1))
                .build() as PackageSearchParams

        val paramsMap = params.toQueryMap()
        assertEquals("1011", paramsMap["originId"])
        assertEquals("1011", params.originId)
    }

    @Test
    fun testDestinationIdString() {
        val params = PackageSearchParams.Builder(activity.resources.getInteger(R.integer.calendar_max_days_hotel_stay),
                activity.resources.getInteger(R.integer.max_calendar_selectable_date_range))
                .origin(getDummySuggestion("123"))
                .destination(getDummySuggestion("456"))
                .adults(1)
                .children(listOf(10, 2))
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusDays(1))
                .build() as PackageSearchParams

        val paramsMap = params.toQueryMap()
        assertEquals("1011", paramsMap["destinationId"])
        assertEquals("1011", params.destinationId)
    }

    @Test
    fun testDestinationIdStringWhenSuggestionTypePOI() {
        val destinationDummySuggestion = getDummySuggestion("456")
        destinationDummySuggestion.type = "POI"
        val params = PackageSearchParams.Builder(activity.resources.getInteger(R.integer.calendar_max_days_hotel_stay),
                activity.resources.getInteger(R.integer.max_calendar_selectable_date_range))
                .origin(getDummySuggestion("123"))
                .destination(destinationDummySuggestion)
                .adults(1)
                .children(listOf(10, 2))
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusDays(1))
                .build() as PackageSearchParams

        val paramsMap = params.toQueryMap()
        assertEquals("1011", paramsMap["destinationId"])
        assertEquals("1011", params.destinationId)
    }

    @Test
    fun testChildrenString() {
        val params = PackageSearchParams.Builder(activity.resources.getInteger(R.integer.calendar_max_days_hotel_stay),
                activity.resources.getInteger(R.integer.max_calendar_selectable_date_range))
                .origin(getDummySuggestion("123"))
                .destination(getDummySuggestion("456"))
                .adults(1)
                .children(listOf(10, 2))
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusDays(1))
                .build() as PackageSearchParams

        assertEquals("10,2", params.childrenString)
    }

    @Test
    fun testFlightCabinClass() {
        val params = PackageSearchParams.Builder(activity.resources.getInteger(R.integer.calendar_max_days_hotel_stay),
                activity.resources.getInteger(R.integer.max_calendar_selectable_date_range))
                .flightCabinClass("coach")
                .origin(getDummySuggestion("123"))
                .destination(getDummySuggestion("456"))
                .adults(1)
                .children(listOf(10, 2))
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusDays(1))
                .build() as PackageSearchParams

        assertEquals("coach", params.flightCabinClass)
    }

    @Test
    fun testDateAndOriginValidation() {
        val searchParamsSubscriber = TestObserver<PackageSearchParams>()
        val noOriginSubscriber = TestObserver<Unit>()
        val noDatesSubscriber = TestObserver<Unit>()
        val maxRangeSubscriber = TestObserver<String>()
        val expectedSearchParams = arrayListOf<PackageSearchParams>()
        val expectedOrigins = arrayListOf<Unit>()
        val expectedDates = arrayListOf<Unit>()
        val expectedRangeErrors = arrayListOf("This date is too far out, please choose a closer date.")
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
        vm.datesUpdated(null, null)
        vm.searchObserver.onNext(Unit)
        expectedDates.add(Unit)

        // Selecting only start date should search with end date as the next day
        vm.datesUpdated(LocalDate.now(), null)
        vm.searchObserver.onNext(Unit)
        expectedSearchParams.add(PackageSearchParams.Builder(activity.resources.getInteger(R.integer.calendar_max_days_hotel_stay),
                activity.resources.getInteger(R.integer.max_calendar_selectable_date_range))
                .origin(origin)
                .destination(destination)
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusDays(1)).build() as PackageSearchParams)

        // Select days beyond 329
        vm.datesUpdated(LocalDate.now().plusDays(330), LocalDate.now().plusDays(331))
        vm.searchObserver.onNext(Unit)

        // Select days at 329
        vm.datesUpdated(LocalDate.now().plusDays(329), LocalDate.now().plusDays(330))
        vm.searchObserver.onNext(Unit)
        expectedSearchParams.add(PackageSearchParams.Builder(activity.resources.getInteger(R.integer.calendar_max_days_hotel_stay),
                activity.resources.getInteger(R.integer.max_calendar_selectable_date_range))
                .origin(origin)
                .destination(destination)
                .startDate(LocalDate.now().plusDays(329))
                .endDate(LocalDate.now().plusDays(330)).build() as PackageSearchParams)

        // Select days at 329
        vm.datesUpdated(LocalDate.now().plusDays(329), null)
        vm.searchObserver.onNext(Unit)
        expectedSearchParams.add(PackageSearchParams.Builder(activity.resources.getInteger(R.integer.calendar_max_days_hotel_stay),
                activity.resources.getInteger(R.integer.max_calendar_selectable_date_range))
                .origin(origin)
                .destination(destination)
                .startDate(LocalDate.now().plusDays(329))
                .endDate(LocalDate.now().plusDays(330)).build() as PackageSearchParams)

        // Select both start date and end date and search
        vm.datesUpdated(LocalDate.now(), LocalDate.now().plusDays(3))
        vm.searchObserver.onNext(Unit)
        expectedSearchParams.add(PackageSearchParams.Builder(activity.resources.getInteger(R.integer.calendar_max_days_hotel_stay),
                activity.resources.getInteger(R.integer.max_calendar_selectable_date_range))
                .origin(origin)
                .destination(destination)
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusDays(3)).build() as PackageSearchParams)

        assertEquals(expectedSearchParams[0].endDate, searchParamsSubscriber.values()[0].endDate)
        assertEquals(expectedSearchParams[1].endDate, searchParamsSubscriber.values()[1].endDate)
        assertEquals(expectedSearchParams[2].endDate, searchParamsSubscriber.values()[2].endDate)
        assertEquals(expectedSearchParams[3].endDate, searchParamsSubscriber.values()[3].endDate)
        noDatesSubscriber.assertValueSequence(expectedDates)
        maxRangeSubscriber.assertValueSequence(expectedRangeErrors)
        noOriginSubscriber.assertValueSequence(expectedOrigins)
    }

    private fun getDummySuggestion(code: String): SuggestionV4 {
        val suggestion = SuggestionV4()
        suggestion.gaiaId = "1011"
        suggestion.regionNames = SuggestionV4.RegionNames()
        suggestion.regionNames.displayName = ""
        suggestion.regionNames.fullName = ""
        suggestion.regionNames.shortName = ""
        val hierarchyInfo = SuggestionV4.HierarchyInfo()
        val airport = SuggestionV4.Airport()
        airport.airportCode = ""
        airport.multicity = code
        hierarchyInfo.airport = airport
        suggestion.hierarchyInfo = hierarchyInfo
        return suggestion
    }
}
