package com.expedia.bookings.hotel.vm

import android.text.SpannableStringBuilder
import com.expedia.bookings.analytics.OmnitureTestUtils
import com.expedia.bookings.R
import com.expedia.bookings.analytics.AnalyticsProvider
import com.expedia.bookings.data.ApiError
import com.expedia.bookings.data.BaseHotelFilterOptions
import com.expedia.bookings.data.SuggestionV4
import com.expedia.bookings.data.hotel.DisplaySort
import com.expedia.bookings.data.hotel.UserFilterChoices
import com.expedia.bookings.data.hotels.HotelFilterOptions
import com.expedia.bookings.data.hotels.HotelSearchParams
import com.expedia.bookings.data.hotels.HotelSearchResponse
import com.expedia.bookings.data.hotels.Neighborhood
import com.expedia.bookings.hotel.util.HotelSearchManager
import com.expedia.bookings.model.HotelStayDates
import com.expedia.bookings.test.OmnitureMatchers
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.utils.LocaleBasedDateFormatUtils
import com.expedia.bookings.utils.RetrofitError
import com.expedia.bookings.utils.StrUtils
import com.expedia.testutils.JSONResourceReader
import com.squareup.phrase.Phrase
import org.hamcrest.Matchers
import org.joda.time.LocalDate
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RuntimeEnvironment
import com.expedia.bookings.services.TestObserver
import java.util.concurrent.TimeUnit
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

@RunWith(RobolectricRunner::class)
class HotelResultsViewModelTest {
    private val context = RuntimeEnvironment.application

    private lateinit var sut: HotelResultsViewModel
    private val mockSearchProvider = HotelSearchManager(null)
    private lateinit var mockAnalyticsProvider: AnalyticsProvider

    private lateinit var checkInDate: LocalDate
    private lateinit var checkOutDate: LocalDate
    private lateinit var happyParams: HotelSearchParams
    private lateinit var filterChoices: UserFilterChoices

    private val happyResponse = getHotelSearchResponse("src/test/resources/raw/hotel/hotel_happy_search_response.json")

    private lateinit var titleSubject: TestObserver<String>
    private lateinit var subtitleSubject: TestObserver<CharSequence>
    private lateinit var subtitleContDescSubject: TestObserver<String>
    private lateinit var changeDateStringSubject: TestObserver<String>
    private lateinit var guestStringSubject: TestObserver<String>
    private lateinit var searchingForHotelsDateTime: TestObserver<Unit>
    private lateinit var searchInProgressSubject: TestObserver<Unit>
    private lateinit var paramChangedSubject: TestObserver<HotelSearchParams>
    private lateinit var resultsReceivedDateTimeObservable: TestObserver<Unit>
    private lateinit var filterResultsObservable: TestObserver<HotelSearchResponse>
    private lateinit var hotelResultsObservable: TestObserver<HotelSearchResponse>

    @Before
    fun setup() {
        sut = HotelResultsViewModel(context, mockSearchProvider)
        mockAnalyticsProvider = OmnitureTestUtils.setMockAnalyticsProvider()

        checkInDate = LocalDate.now()
        checkOutDate = checkInDate.plusDays(3)

        happyParams = makeHappyParams()
        filterChoices = makeFilterChoices()

        titleSubject = TestObserver()
        sut.titleSubject.subscribe(titleSubject)
        subtitleSubject = TestObserver()
        sut.subtitleSubject.subscribe(subtitleSubject)
        subtitleContDescSubject = TestObserver()
        sut.subtitleContDescSubject.subscribe(subtitleContDescSubject)
        changeDateStringSubject = TestObserver()
        sut.changeDateStringSubject.subscribe(changeDateStringSubject)
        guestStringSubject = TestObserver()
        sut.guestStringSubject.subscribe(guestStringSubject)
        searchingForHotelsDateTime = TestObserver()
        sut.searchingForHotelsDateTime.subscribe(searchingForHotelsDateTime)
        searchInProgressSubject = TestObserver()
        sut.searchInProgressSubject.subscribe(searchInProgressSubject)
        paramChangedSubject = TestObserver()
        sut.paramChangedSubject.subscribe(paramChangedSubject)
        resultsReceivedDateTimeObservable = TestObserver()
        sut.resultsReceivedDateTimeObservable.subscribe(resultsReceivedDateTimeObservable)
        filterResultsObservable = TestObserver()
        sut.filterResultsObservable.subscribe(filterResultsObservable)
        hotelResultsObservable = TestObserver()
        sut.hotelResultsObservable.subscribe(hotelResultsObservable)
    }

    @Test
    fun testResultsReceivedDateTimeObservable() {
        mockSearchProvider.apiCompleteSubject.onNext(Unit)
        assertObserverValue(resultsReceivedDateTimeObservable, Unit, 1)
    }

    @Test
    fun testHappySearch() {
        sut.paramsSubject.onNext(happyParams)
        assertSearchParams(happyParams, sut.cachedParams!!)

        assertSearch(happyParams, 1)
        assertSearchResults(false)
    }

    @Test
    fun testLocationSearch() {
        sut.paramsSubject.onNext(happyParams)
        val suggestion = makeSuggestion("gaiaId", "regionShortName")
        sut.locationParamsSubject.onNext(suggestion)
        val expectedParams = makeParams("gaiaId", "regionShortName")
        assertSearchAndParamChanged(expectedParams, 2, "regionShortName")
        assertSearchResults(false)
    }

    @Test
    fun testLocationSearchNoCachedParams() {
        val suggestion = makeSuggestion("gaiaId", "regionShortName")
        assertFails {
            sut.locationParamsSubject.onNext(suggestion)
        }
    }

    @Test
    fun testLocationSearch_clearPrefetch() {
        val resultsSubscriber = TestObserver<HotelSearchResponse>()
        sut.hotelResultsObservable.subscribe(resultsSubscriber)

        sut.paramsSubject.onNext(happyParams)

        mockSearchProvider.searchResponseObserver.onNext(happyResponse)
        sut.locationParamsSubject.onNext(makeSuggestion("", ""))

        resultsSubscriber.awaitValueCount(1, 1, TimeUnit.SECONDS)
        resultsSubscriber.assertValueCount(1)

        sut.locationParamsSubject.onNext(makeSuggestion("", ""))
        resultsSubscriber.awaitValueCount(1, 1, TimeUnit.SECONDS)
        resultsSubscriber.assertValueCount(1)

        mockSearchProvider.successSubject.onNext(happyResponse)

        resultsSubscriber.awaitValueCount(1, 1, TimeUnit.SECONDS)
        resultsSubscriber.assertValueCount(2)
    }

    @Test
    fun testFilteredSearch() {
        sut.paramsSubject.onNext(happyParams)
        sut.filterChoicesSubject.onNext(filterChoices)
        val expectedParams = makeParams("", "")
        expectedParams.filterOptions = HotelFilterOptions().apply {
            filterHotelName = "Hyatt"
            userSort = BaseHotelFilterOptions.SortType.EXPERT_PICKS
        }
        assertSearchAndParamChanged(expectedParams, 2)
        assertSearchResults(true)
    }

    @Test
    fun testFilteredSearchNoCachedParams() {
        assertFails {
            sut.filterChoicesSubject.onNext(filterChoices)
        }
    }

    @Test
    fun testSearchWithFilterOptions() {
        val hotelParams = happyParams
        val filterOptions = HotelFilterOptions()
        filterOptions.filterHotelName = "Hyatt"
        hotelParams.filterOptions = filterOptions
        sut.paramsSubject.onNext(hotelParams)
        val expectedParams = makeParams("", "")
        expectedParams.filterOptions = HotelFilterOptions().apply {
            filterHotelName = "Hyatt"
        }

        assertSearch(expectedParams, 1)
        assertSearchResults(true)
    }

    @Test
    fun testFilteredSearchOverrideOriginalFilter() {
        val hotelParams = happyParams
        val filterOptions = HotelFilterOptions().apply {
            filterHotelName = "Ttayh"
            filterStarRatings = listOf(40)
            filterGuestRatings = listOf(4)
            filterPrice = HotelSearchParams.PriceRange(20, 200)
            filterVipOnly = false
            filterByNeighborhood = Neighborhood().apply {
                name = "name2"
                id = "id2"
            }
            amenities.add(2)
            userSort = BaseHotelFilterOptions.SortType.PRICE
        }
        hotelParams.filterOptions = filterOptions
        sut.paramsSubject.onNext(hotelParams)
        assertSearch(hotelParams, 1)

        val neighborhood = Neighborhood().apply {
            name = "name"
            id = "id"
        }

        val userFilterChoices = UserFilterChoices().apply {
            userSort = DisplaySort.DISTANCE
            isVipOnlyAccess = true
            hotelStarRating = UserFilterChoices.StarRatings(five = true)
            hotelGuestRating = UserFilterChoices.GuestRatings(five = true)
            name = "Hyatt"
            minPrice = 10
            maxPrice = 100
            amenities.add(1)
            neighborhoods.add(neighborhood)
        }

        sut.filterChoicesSubject.onNext(userFilterChoices)
        val expectedParams = makeParams("", "")
        expectedParams.filterOptions = HotelFilterOptions().apply {
            filterHotelName = "Hyatt"
            filterStarRatings = listOf(50)
            filterGuestRatings = listOf(5)
            filterPrice = HotelSearchParams.PriceRange(10, 100)
            filterVipOnly = true
            filterByNeighborhood = neighborhood
            amenities.add(1)
            userSort = BaseHotelFilterOptions.SortType.DISTANCE
        }
        assertSearchAndParamChanged(expectedParams, 2)
        assertSearchResults(true)
    }

    @Test
    fun testFilterAfterPinned() {
        happyParams.suggestion.hotelId = "12345"
        assertTrue(happyParams.isPinnedSearch())

        sut.paramsSubject.onNext(happyParams)
        sut.filterChoicesSubject.onNext(filterChoices)

        assertFalse(sut.getSearchParams()!!.isPinnedSearch(), "FAILURE: Filter search params should never be pinned.")
        assertTrue(happyParams.isPinnedSearch(), "FAILURE : The original params should not be mutated")
    }

    @Test
    fun testSearchWithChangeDate() {
        sut.paramsSubject.onNext(happyParams)
        val startDate = checkInDate.plusDays(2)
        val endDate = checkOutDate.plusDays(2)
        val hotelStayDates = HotelStayDates(startDate, endDate)
        sut.dateChangedParamsSubject.onNext(hotelStayDates)
        val expectedParams = makeParams("", "", startDate, endDate)
        assertSearchAndParamChanged(expectedParams, 2)
        assertSearchResults(false)
    }

    @Test
    fun testSearchWithChangeDateNoCachedParams() {
        val startDate = checkInDate.plusDays(2)
        val endDate = checkOutDate.plusDays(2)
        val hotelStayDates = HotelStayDates(startDate, endDate)
        assertFails {
            sut.dateChangedParamsSubject.onNext(hotelStayDates)
        }
    }

    @Test
    fun errorResponseCallsErrorObservable() {
        val testSubscriber = TestObserver<ApiError>()
        sut.searchApiErrorObservable.subscribe(testSubscriber)

        mockSearchProvider.errorSubject.onNext(ApiError(ApiError.Code.HOTEL_SEARCH_NO_RESULTS))

        testSubscriber.awaitValueCount(1, 1, TimeUnit.SECONDS)
        testSubscriber.assertValueCount(1)
        assertEquals(ApiError.Code.HOTEL_SEARCH_NO_RESULTS, testSubscriber.values()[0].getErrorCode())
    }

    @Test
    fun titleSubjectSetToRegionShortName() {
        val regionShortName = "New York"
        val testSubscriber = TestObserver<String>()
        sut.titleSubject.subscribe(testSubscriber)

        sut.paramsSubject.onNext(makeParams("", regionShortName))

        testSubscriber.assertValueCount(1)
        testSubscriber.assertNotTerminated()
        testSubscriber.assertValue(regionShortName)
    }

    @Test
    fun subtitleSubjectSet() {
        val expectedSubtitle = LocaleBasedDateFormatUtils.localDateToMMMd(checkInDate) + " - " + LocaleBasedDateFormatUtils.localDateToMMMd(checkOutDate) + ", 1 guest"
        val testSubscriber = TestObserver<CharSequence>()
        sut.subtitleSubject.subscribe(testSubscriber)

        sut.paramsSubject.onNext(happyParams)

        testSubscriber.assertValueCount(1)
        testSubscriber.assertNotTerminated()
        assertEquals(expectedSubtitle, testSubscriber.values()[0].toString())
    }

    @Test
    fun noResultsMap() {
        val regionShortName = context.getString(R.string.visible_map_area)
        val params = makeParams("", regionShortName)
        val testSubscriber = TestObserver<ApiError>()
        sut.searchApiErrorObservable.subscribe(testSubscriber)

        sut.paramsSubject.onNext(params)
        mockSearchProvider.noResultsSubject.onNext(Unit)

        testSubscriber.awaitValueCount(1, 1, TimeUnit.SECONDS)
        testSubscriber.assertValueCount(1)
        assertEquals(ApiError(ApiError.Code.HOTEL_MAP_SEARCH_NO_RESULTS), testSubscriber.values()[0])
    }

    @Test
    fun noResultsFilter() {
        val testSubscriber = TestObserver<ApiError>()
        sut.searchApiErrorObservable.subscribe(testSubscriber)

        sut.paramsSubject.onNext(happyParams)
        sut.filterChoicesSubject.onNext(filterChoices)

        mockSearchProvider.noResultsSubject.onNext(Unit)

        testSubscriber.awaitValueCount(1, 1, TimeUnit.SECONDS)
        testSubscriber.assertValueCount(1)
        assertEquals(ApiError(ApiError.Code.HOTEL_FILTER_NO_RESULTS), testSubscriber.values()[0])
    }

    @Test
    fun noResultsGeneral() {
        val testSubscriber = TestObserver<ApiError>()
        sut.searchApiErrorObservable.subscribe(testSubscriber)

        sut.paramsSubject.onNext(happyParams)

        mockSearchProvider.noResultsSubject.onNext(Unit)

        testSubscriber.awaitValueCount(1, 1, TimeUnit.SECONDS)
        testSubscriber.assertValueCount(1)
        assertEquals(ApiError(ApiError.Code.HOTEL_SEARCH_NO_RESULTS), testSubscriber.values()[0])
    }

    @Test
    fun pinnedSearchSuccessPinnedSearch() {
        val testErrorSubscriber = TestObserver<ApiError>()
        val testResultsSubscriber = TestObserver<HotelSearchResponse>()

        sut.searchApiErrorObservable.subscribe(testErrorSubscriber)
        sut.hotelResultsObservable.subscribe(testResultsSubscriber)

        val pinnedResponse = getHotelSearchResponse("src/test/resources/raw/hotel/hotel_happy_search_response_pinned.json")
        mockSearchProvider.successSubject.onNext(pinnedResponse)

        testResultsSubscriber.awaitValueCount(1, 1, TimeUnit.SECONDS)
        testResultsSubscriber.assertValueCount(1)
        assertEquals(pinnedResponse, testResultsSubscriber.values()[0])

        testErrorSubscriber.assertValueCount(0)
    }

    @Test
    fun pinnedSearchSuccessPinnedSearchNotFound() {
        val testErrorSubscriber = TestObserver<ApiError>()
        val testResultsSubscriber = TestObserver<HotelSearchResponse>()

        sut.searchApiErrorObservable.subscribe(testErrorSubscriber)
        sut.hotelResultsObservable.subscribe(testResultsSubscriber)

        val noPinnedResponse = getHotelSearchResponse("src/test/resources/raw/hotel/hotel_happy_search_response_no_pinned.json")
        mockSearchProvider.successSubject.onNext(noPinnedResponse)

        testResultsSubscriber.assertValueCount(0)

        testErrorSubscriber.awaitValueCount(1, 1, TimeUnit.SECONDS)
        testErrorSubscriber.assertValueCount(1)
        assertEquals(ApiError(ApiError.Code.HOTEL_PINNED_NOT_FOUND), testErrorSubscriber.values()[0])
    }

    @Test
    fun subTitleContDesc() {
        val testContDescSub = TestObserver.create<String>()
        val expectedText = Phrase.from(context, R.string.start_to_end_plus_guests_cont_desc_TEMPLATE)
                .put("startdate", LocaleBasedDateFormatUtils.localDateToMMMd(happyParams.checkIn))
                .put("enddate", LocaleBasedDateFormatUtils.localDateToMMMd(happyParams.checkOut))
                .put("guests", StrUtils.formatGuestString(context, happyParams.guests))
                .format().toString()

        sut.subtitleContDescSubject.subscribe(testContDescSub)

        sut.paramsSubject.onNext(happyParams)
        assertEquals(expectedText, testContDescSub.values()[0],
                "FAILURE: Expected 'to' text to be explicit")
    }

    @Test
    fun testResultsRxNetworkErrorTracking() {
        mockSearchProvider.retrofitErrorSubject.onNext(RetrofitError.NO_INTERNET)

        assertResultsErrorTracking("NetworkError")
    }

    @Test
    fun testResultsRxTimeOutErrorTracking() {
        mockSearchProvider.retrofitErrorSubject.onNext(RetrofitError.TIMEOUT)

        assertResultsErrorTracking("NetworkTimeOut")
    }

    @Test
    fun testResultsRxUnknownErrorTracking() {
        mockSearchProvider.retrofitErrorSubject.onNext(RetrofitError.UNKNOWN)

        assertResultsErrorTracking("UnknownRetrofitError")
    }

    // TODO: create test for error pinned search

    private fun makeHappyParams(): HotelSearchParams {
        return makeParams("", "")
    }

    private fun makeParams(gaiaId: String, regionShortName: String, startDate: LocalDate = checkInDate, endDate: LocalDate = checkOutDate): HotelSearchParams {
        val suggestion = makeSuggestion(gaiaId, regionShortName)
        val hotelSearchParams = HotelSearchParams.Builder(3, 500).destination(suggestion).startDate(startDate).endDate(endDate).build() as HotelSearchParams

        return hotelSearchParams
    }

    private fun makeSuggestion(gaiaId: String, regionShortName: String): SuggestionV4 {
        val suggestion = SuggestionV4()
        suggestion.gaiaId = gaiaId
        suggestion.regionNames = SuggestionV4.RegionNames()
        suggestion.regionNames.displayName = ""
        suggestion.regionNames.fullName = ""
        suggestion.regionNames.shortName = regionShortName
        suggestion.coordinates = SuggestionV4.LatLng()

        return suggestion
    }

    private fun makeFilterChoices(): UserFilterChoices {
        val filterParams = UserFilterChoices()
        filterParams.name = "Hyatt"
        return filterParams
    }

    private fun getHotelSearchResponse(filePath: String): HotelSearchResponse {
        val resourceReader = JSONResourceReader(filePath)
        val searchResponse = resourceReader.constructUsingGson(HotelSearchResponse::class.java)
        return searchResponse
    }

    private fun assertResultsErrorTracking(errorMessage: String) {
        OmnitureTestUtils.assertStateTracked("App.Hotels.Search.NoResults",
                Matchers.allOf(
                        OmnitureMatchers.withProps(mapOf(2 to "hotels", 36 to errorMessage)),
                        OmnitureMatchers.withEvars(mapOf(2 to "D=c2", 18 to "App.Hotels.Search.NoResults"))),
                mockAnalyticsProvider)
    }

    private fun <T> assertObserverValue(observer: TestObserver<T>, value: T, count: Int) {
        observer.awaitValueCount(count, 1, TimeUnit.SECONDS)
        observer.assertNotTerminated()
        observer.assertNoErrors()
        observer.assertValueCount(count)
        observer.assertValueAt(count - 1, value)
    }

    private fun <T> assertNoObserver(observer: TestObserver<T>) {
        observer.assertNotTerminated()
        observer.assertNoErrors()
        observer.assertValueCount(0)
    }

    private fun assertSearch(params: HotelSearchParams, count: Int, title: String = "") {
        assertSearchParams(params, sut.cachedParams!!)

        assertObserverValue(titleSubject, title, count)
        val startDateString = LocaleBasedDateFormatUtils.localDateToMMMd(params.startDate)
        val endDateString = LocaleBasedDateFormatUtils.localDateToMMMd(params.endDate!!)
        assertObserverValue(subtitleSubject, SpannableStringBuilder("$startDateString - $endDateString, 1 guest"), count)
        assertObserverValue(subtitleContDescSubject, "$startDateString to $endDateString, 1 guest", count)
        assertObserverValue(changeDateStringSubject, "$startDateString - $endDateString (3 nights)", count)
        assertObserverValue(guestStringSubject, "1 guest", count)
        assertObserverValue(searchingForHotelsDateTime, Unit, count)
        assertObserverValue(searchInProgressSubject, Unit, count)
    }

    private fun assertSearchAndParamChanged(params: HotelSearchParams, count: Int, title: String = "") {
        assertSearch(params, count, title)
        assertSearchParams(params, paramChangedSubject.values().last())
    }

    private fun assertSearchResults(isFilter: Boolean) {
        mockSearchProvider.successSubject.onNext(happyResponse)

        if (isFilter) {
            assertObserverValue(filterResultsObservable, happyResponse, 1)
            assertNoObserver(hotelResultsObservable)
        } else {
            assertNoObserver(filterResultsObservable)
            assertObserverValue(hotelResultsObservable, happyResponse, 1)
        }
    }

    private fun assertSearchParams(expectedSearchParams: HotelSearchParams, searchParams: HotelSearchParams) {
        if (expectedSearchParams === searchParams) {
            return
        }

        assertEquals(expectedSearchParams.suggestion, searchParams.suggestion)
        assertEquals(expectedSearchParams.checkIn, searchParams.checkIn)
        assertEquals(expectedSearchParams.checkOut, searchParams.checkOut)
        assertEquals(expectedSearchParams.adults, searchParams.adults)
        assertEquals(expectedSearchParams.children, searchParams.children)
        assertEquals(expectedSearchParams.shopWithPoints, searchParams.shopWithPoints)
        assertEquals(expectedSearchParams.sortType, searchParams.sortType)
        assertEquals(expectedSearchParams.mctc, searchParams.mctc)
        assertEquals(expectedSearchParams.forPackage, searchParams.forPackage)

        if (expectedSearchParams.filterOptions == null) {
            assertNull(searchParams.filterOptions)
        } else {
            assertNotNull(searchParams.filterOptions)
            val expectedFilterOptions = expectedSearchParams.filterOptions!!
            val filterOptions = searchParams.filterOptions!!

            assertEquals(expectedFilterOptions.filterGuestRatings, filterOptions.filterGuestRatings)
            assertEquals(expectedFilterOptions.filterPrice, filterOptions.filterPrice)
            assertEquals(expectedFilterOptions.filterByNeighborhood, filterOptions.filterByNeighborhood)
            assertEquals(expectedFilterOptions.filterHotelName, filterOptions.filterHotelName)
            assertEquals(expectedFilterOptions.filterStarRatings, filterOptions.filterStarRatings)
            assertEquals(expectedFilterOptions.filterVipOnly, filterOptions.filterVipOnly)
            assertEquals(expectedFilterOptions.userSort, filterOptions.userSort)
        }

        assertEquals(expectedSearchParams.enableSponsoredListings, searchParams.enableSponsoredListings)
        assertEquals(expectedSearchParams.updateSearchDestination, searchParams.updateSearchDestination)
        assertEquals(expectedSearchParams.isDatelessSearch, searchParams.isDatelessSearch)
    }
}
