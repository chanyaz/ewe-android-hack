package com.expedia.bookings.test.robolectric

import android.app.Activity
import com.expedia.bookings.R
import com.expedia.bookings.data.BaseHotelFilterOptions
import com.expedia.bookings.data.SuggestionV4
import com.expedia.bookings.data.hotels.HotelSearchParams
import com.expedia.bookings.data.packages.PackageHotelFilterOptions
import com.expedia.bookings.data.packages.PackageSearchParams
import com.expedia.bookings.packages.vm.PackageSearchViewModel
import com.expedia.bookings.services.TestObserver
import com.expedia.bookings.utils.Ui
import org.joda.time.LocalDate
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import kotlin.properties.Delegates
import kotlin.test.assertEquals
import kotlin.test.assertFalse
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
    fun testNumberOfGuestsForMultipleRooms() {
        val paramsBuilder = PackageSearchParams.Builder(activity.resources.getInteger(R.integer.calendar_max_days_hotel_stay),
                activity.resources.getInteger(R.integer.max_calendar_selectable_date_range))
                .origin(getDummySuggestion("123"))
                .destination(getDummySuggestion("456"))
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusDays(1)) as PackageSearchParams.Builder

        val paramsForSingleRoomOnlyAdults = paramsBuilder.multiRoomAdults(mapOf(1 to 1)).multiRoomChildren(emptyMap()).build()
        assertEquals(1, paramsForSingleRoomOnlyAdults.guests)

        val paramsForSingleRoomMultipleAdultsAndSingleChild = paramsBuilder.multiRoomAdults(mapOf(1 to 2)).multiRoomChildren(mapOf(1 to listOf(10))).build()
        assertEquals(3, paramsForSingleRoomMultipleAdultsAndSingleChild.guests)

        val paramsForSingleRoomMultipleAdultsAndMultipleChildren = paramsBuilder.multiRoomAdults(mapOf(1 to 2)).multiRoomChildren(mapOf(1 to listOf(10, 14))).build()
        assertEquals(4, paramsForSingleRoomMultipleAdultsAndMultipleChildren.guests)

        val paramsForMultipleRoomAdultsOnly = paramsBuilder.multiRoomAdults(mapOf(1 to 1, 2 to 2)).multiRoomChildren(emptyMap()).build()
        assertEquals(3, paramsForMultipleRoomAdultsOnly.guests)

        val paramsForMultipleRoomMultipleAdultsAndSingleChild = paramsBuilder.multiRoomAdults(mapOf(1 to 2, 2 to 1)).multiRoomChildren(mapOf(1 to listOf(10))).build()
        assertEquals(4, paramsForMultipleRoomMultipleAdultsAndSingleChild.guests)

        val paramsForMultipleRoomMultipleAdultsAndMultipleChildren = paramsBuilder.multiRoomAdults(mapOf(1 to 2, 2 to 1)).multiRoomChildren(mapOf(1 to listOf(10, 14), 2 to listOf(14))).build()
        assertEquals(6, paramsForMultipleRoomMultipleAdultsAndMultipleChildren.guests)
    }

    @Test
    fun testEmptyChildString() {
        val paramsBuilder = PackageSearchParams.Builder(activity.resources.getInteger(R.integer.calendar_max_days_hotel_stay),
                activity.resources.getInteger(R.integer.max_calendar_selectable_date_range))
                .origin(getDummySuggestion("123"))
                .destination(getDummySuggestion("456"))
                .adults(1)
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusDays(1))

        var params = paramsBuilder.build() as PackageSearchParams
        assertNull(params.childAges)
    }

    @Test
    fun testAdultsAndChildString() {
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

        assertEquals(1, params.adults)
        assertEquals("1", params.adultsQueryParam)
        assertEquals("10,2", params.childAges)
        assertNull(params.infantsInSeats)
    }

    @Test
    fun testAdultChildAgesStringForMultipleRooms() {
        val paramsBuilder = PackageSearchParams.Builder(activity.resources.getInteger(R.integer.calendar_max_days_hotel_stay),
                activity.resources.getInteger(R.integer.max_calendar_selectable_date_range))
                .infantSeatingInLap(false)
                .origin(getDummySuggestion("123"))
                .destination(getDummySuggestion("456"))
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusDays(1)) as PackageSearchParams.Builder

        val paramsForSingleRoomOnlyAdults = paramsBuilder.multiRoomAdults(mapOf(1 to 1)).multiRoomChildren(emptyMap()).build()
        checkAdultsAndChildrenQueryString(paramsForSingleRoomOnlyAdults, "1", null)

        val paramsForSingleRoomMultipleAdultsAndSingleChild = paramsBuilder.multiRoomAdults(mapOf(1 to 2)).multiRoomChildren(mapOf(1 to listOf(10))).build()
        checkAdultsAndChildrenQueryString(paramsForSingleRoomMultipleAdultsAndSingleChild, "2", "10__")

        val paramsForSingleRoomMultipleAdultsAndMultipleChildren = paramsBuilder.multiRoomAdults(mapOf(1 to 2)).multiRoomChildren(mapOf(1 to listOf(10, 14))).build()
        checkAdultsAndChildrenQueryString(paramsForSingleRoomMultipleAdultsAndMultipleChildren, "2", "10,14__")

        val paramsForMultipleRoomAdultsOnly = paramsBuilder.multiRoomAdults(mapOf(1 to 1, 2 to 2)).multiRoomChildren(emptyMap()).build()
        checkAdultsAndChildrenQueryString(paramsForMultipleRoomAdultsOnly, "1,2", null)

        val paramsForMultipleRoomMultipleAdultsAndSingleChild = paramsBuilder.multiRoomAdults(mapOf(1 to 2, 2 to 1)).multiRoomChildren(mapOf(2 to listOf(10))).build()
        checkAdultsAndChildrenQueryString(paramsForMultipleRoomMultipleAdultsAndSingleChild, "2,1", "_10_")

        val paramsForMultipleRoomMultipleAdultsAndMultipleChildren = paramsBuilder.multiRoomAdults(mapOf(1 to 1, 2 to 1, 3 to 1)).multiRoomChildren(mapOf(1 to listOf(10), 3 to listOf(14))).build()
        checkAdultsAndChildrenQueryString(paramsForMultipleRoomMultipleAdultsAndMultipleChildren, "1,1,1", "10__14")

        val paramsForMultipleRoomMultipleAdultsAndMultipleChildrenInSecondRoom = paramsBuilder.multiRoomAdults(mapOf(1 to 2, 2 to 1)).multiRoomChildren(mapOf(2 to listOf(10, 14))).build()
        checkAdultsAndChildrenQueryString(paramsForMultipleRoomMultipleAdultsAndMultipleChildrenInSecondRoom, "2,1", "_10,14_")
    }

    private fun checkAdultsAndChildrenQueryString(params: PackageSearchParams, expectedAdults: String, expectedChildren: String?) {
        assertEquals(expectedAdults, params.adultsQueryParam)
        assertEquals(expectedChildren, params.childAges)
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

        assertEquals("10,2", params.childAges)
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
    fun testIsOriginSameAsDestinationIsTrue() {
        val builder = PackageSearchParams.Builder(activity.resources.getInteger(R.integer.calendar_max_days_hotel_stay),
                activity.resources.getInteger(R.integer.max_calendar_selectable_date_range))
        val origin = getDummySuggestion("123")
        val destination = getDummySuggestion("456")
        origin.hierarchyInfo?.airport?.multicity = null
        destination.hierarchyInfo?.airport?.multicity = null
        builder.origin(origin)
                .destination(destination)
                .adults(1)
                .children(listOf(10, 2))
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusDays(1))
                .build() as PackageSearchParams

        assertTrue(builder.isOriginSameAsDestination())
    }

    @Test
    fun testIsOriginSameAsDestinationIsFalse() {
        val builder = PackageSearchParams.Builder(activity.resources.getInteger(R.integer.calendar_max_days_hotel_stay),
                activity.resources.getInteger(R.integer.max_calendar_selectable_date_range))
        builder.origin(getDummySuggestion("123"))
                .destination(getDummySuggestion("124"))
                .adults(1)
                .children(listOf(10, 2))
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusDays(1))
                .build() as PackageSearchParams

        assertFalse(builder.isOriginSameAsDestination())
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

    @Test
    fun testPagingMethod() {
        val builder = PackageSearchParams.Builder(activity.resources.getInteger(R.integer.calendar_max_days_hotel_stay),
                activity.resources.getInteger(R.integer.max_calendar_selectable_date_range))
                .paging(0)
                .origin(getDummySuggestion("123"))
                .destination(getDummySuggestion("456"))
                .startDate(LocalDate.now().plusDays(329))
                .endDate(LocalDate.now().plusDays(330))

        val params = builder.build() as PackageSearchParams
        assertEquals(0, params.pageIndex)
        assertEquals(200, params.pageSize)

        params.updatePageIndex()

        assertEquals(1, params.pageIndex)
        assertEquals(200, params.pageSize)

        params.resetPageIndex()

        assertEquals(0, params.pageIndex)
        assertEquals(200, params.pageSize)
    }

    @Test
    fun testFilterOptions() {
        val name = "Hyatt"
        val builder = PackageSearchParams.Builder(activity.resources.getInteger(R.integer.calendar_max_days_hotel_stay),
                activity.resources.getInteger(R.integer.max_calendar_selectable_date_range))
                .hotelName(name)
                .starRatings(listOf(1, 2))
                .vipOnly(true)
                .userSort(BaseHotelFilterOptions.SortType.PACKAGE_SAVINGS)
                .origin(getDummySuggestion("123"))
                .destination(getDummySuggestion("456"))
                .adults(1)
                .children(listOf(10, 2))
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusDays(1)) as PackageSearchParams.Builder

        val params = builder.build()
        val map = params.filterOptions!!.getFiltersQueryMap()

        assertEquals(4, map.size)
        assertEquals(name, map["hotelName"])
        assertEquals("1,2", map["stars"])
        assertEquals("true", map["vipOnly"])
        assertEquals("PACKAGE_SAVINGS", map["hotelSortOrder"])
        assertEquals(params.getHotelsSortOrder(), BaseHotelFilterOptions.SortType.PACKAGE_SAVINGS)
    }

    @Test
    fun testGetDefaultSortType() {
        val params = PackageSearchParams.Builder(activity.resources.getInteger(R.integer.calendar_max_days_hotel_stay),
                activity.resources.getInteger(R.integer.max_calendar_selectable_date_range))
                .origin(getDummySuggestion("123"))
                .destination(getDummySuggestion("456"))
                .adults(1)
                .children(listOf(10, 2))
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusDays(1))
                .build() as PackageSearchParams

        assertEquals(params.getHotelsSortOrder(), BaseHotelFilterOptions.SortType.EXPERT_PICKS)
    }

    @Test
    fun testAddPackageFilterParamsHappy() {
        val packageParams = getPackageParams()
        packageParams.filterOptions = buildFilterOptions()
        val hotelParams = packageParams.convertToHotelSearchParams(26, 329)
        assertPackageAndHotelFilterOptionsEqual(packageParams, hotelParams)
        assertEquals(packageParams.filterOptions!!.filterHotelName, "Test_Hotel")
        assertEquals(packageParams.filterOptions!!.filterStarRatings, listOf(2))
        assertEquals(packageParams.filterOptions!!.filterVipOnly, true)
        assertEquals(packageParams.filterOptions!!.userSort, BaseHotelFilterOptions.SortType.EXPERT_PICKS)
    }

    @Test
    fun testAddPackageFilterParamsEmptyHotelName() {
        val packageParams = getPackageParams()
        packageParams.filterOptions = buildFilterOptions("")
        val hotelParams = packageParams.convertToHotelSearchParams(26, 329)
        assertPackageAndHotelFilterOptionsEqual(packageParams, hotelParams)
        assertEquals(packageParams.filterOptions!!.filterStarRatings, listOf(2))
        assertEquals(packageParams.filterOptions!!.filterVipOnly, true)
        assertEquals(packageParams.filterOptions!!.filterHotelName, "")
        assertEquals(packageParams.filterOptions!!.userSort, BaseHotelFilterOptions.SortType.EXPERT_PICKS)
    }

    @Test
    fun testAddPackageFilterParamsNullHotelName() {
        val packageParams = getPackageParams()
        packageParams.filterOptions = buildFilterOptions(null)
        val hotelParams = packageParams.convertToHotelSearchParams(26, 329)
        assertPackageAndHotelFilterOptionsEqual(packageParams, hotelParams)
        assertNull(packageParams.filterOptions!!.filterHotelName)
        assertEquals(packageParams.filterOptions!!.filterStarRatings, listOf(2))
        assertEquals(packageParams.filterOptions!!.filterVipOnly, true)
        assertEquals(packageParams.filterOptions!!.userSort, BaseHotelFilterOptions.SortType.EXPERT_PICKS)
    }

    @Test
    fun testAddPackageFilterParamsEmptyStarRatings() {
        val packageParams = getPackageParams()
        packageParams.filterOptions = buildFilterOptions(starRatings = listOf())
        val hotelParams = packageParams.convertToHotelSearchParams(26, 329)
        assertPackageAndHotelFilterOptionsEqual(packageParams, hotelParams)
        assertEquals(packageParams.filterOptions!!.filterHotelName, "Test_Hotel")
        assertEquals(packageParams.filterOptions!!.filterStarRatings, listOf())
        assertEquals(packageParams.filterOptions!!.filterVipOnly, true)
        assertEquals(packageParams.filterOptions!!.userSort, BaseHotelFilterOptions.SortType.EXPERT_PICKS)
    }

    @Test
    fun testAddPackageFilterParamsNullSort() {
        val packageParams = getPackageParams()
        packageParams.filterOptions = buildFilterOptions(userSelectedSort = null)
        val hotelParams = packageParams.convertToHotelSearchParams(26, 329)
        assertPackageAndHotelFilterOptionsEqual(packageParams, hotelParams)
        assertEquals(packageParams.filterOptions!!.filterHotelName, "Test_Hotel")
        assertEquals(packageParams.filterOptions!!.filterStarRatings, listOf(2))
        assertEquals(packageParams.filterOptions!!.filterVipOnly, true)
        assertNull(packageParams.filterOptions!!.userSort)
    }

    @Test
    fun testAddPackageFilterParamsVIPFalse() {
        val packageParams = getPackageParams()
        packageParams.filterOptions = buildFilterOptions(vipOnly = false)
        val hotelParams = packageParams.convertToHotelSearchParams(26, 329)
        assertPackageAndHotelFilterOptionsEqual(packageParams, hotelParams)
        assertEquals(packageParams.filterOptions!!.filterHotelName, "Test_Hotel")
        assertEquals(packageParams.filterOptions!!.filterStarRatings, listOf(2))
        assertEquals(packageParams.filterOptions!!.filterVipOnly, false)
        assertEquals(packageParams.filterOptions!!.userSort, BaseHotelFilterOptions.SortType.EXPERT_PICKS)
    }

    private fun assertPackageAndHotelFilterOptionsEqual(packageParams: PackageSearchParams, hotelParams: HotelSearchParams) {
        assertEquals(packageParams.filterOptions!!.filterHotelName, hotelParams.filterOptions!!.filterHotelName)
        assertEquals(packageParams.filterOptions!!.filterStarRatings, hotelParams.filterOptions!!.filterStarRatings)
        assertEquals(packageParams.filterOptions!!.filterVipOnly, hotelParams.filterOptions!!.filterVipOnly)
        assertEquals(packageParams.filterOptions!!.userSort, hotelParams.filterOptions!!.userSort)
    }

    private fun getPackageParams(): PackageSearchParams {
        val packageParams = PackageSearchParams.Builder(26, 329)
                .flightCabinClass("coach")
                .infantSeatingInLap(true)
                .children(listOf(16, 10, 1))
                .origin(getDummySuggestion("Seattle"))
                .destination(getDummySuggestion("London"))
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusDays(1)).build() as PackageSearchParams
        return packageParams
    }

    private fun buildFilterOptions(hotelName: String? = "Test_Hotel",
                                   starRatings: List<Int> = listOf(2),
                                   vipOnly: Boolean = true,
                                   userSelectedSort: BaseHotelFilterOptions.SortType? = BaseHotelFilterOptions.SortType.EXPERT_PICKS): PackageHotelFilterOptions {
        return PackageHotelFilterOptions().apply {
            filterHotelName = hotelName
            filterStarRatings = starRatings
            filterVipOnly = vipOnly
            userSort = userSelectedSort
        }
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
