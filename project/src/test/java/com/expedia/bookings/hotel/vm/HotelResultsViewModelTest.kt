package com.expedia.bookings.hotel.vm

import com.expedia.bookings.R
import com.expedia.bookings.data.ApiError
import com.expedia.bookings.data.SuggestionV4
import com.expedia.bookings.data.hotel.UserFilterChoices
import com.expedia.bookings.data.hotels.HotelSearchParams
import com.expedia.bookings.data.hotels.HotelSearchResponse
import com.expedia.bookings.hotel.provider.HotelSearchProvider
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.utils.DateUtils
import com.expedia.testutils.JSONResourceReader
import org.joda.time.LocalDate
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RuntimeEnvironment
import rx.observers.TestSubscriber
import java.util.concurrent.TimeUnit
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@RunWith(RobolectricRunner::class)
class HotelResultsViewModelTest {
    val context = RuntimeEnvironment.application

    lateinit var sut: HotelResultsViewModel
    val mockSearchProvider = HotelSearchProvider(null)

    val happyParams = makeHappyParams()
    val filterParams = makeFilterParams()
    lateinit var checkInDate: LocalDate
    lateinit var checkOutDate: LocalDate

    val happyResponse = getHotelSearchResponse("src/test/resources/raw/hotel/hotel_happy_search_response.json")

    @Before
    fun setup() {
        sut = HotelResultsViewModel(context, mockSearchProvider)
    }

    @Test
    fun happySearch() {
        val resultsSubscriber = TestSubscriber<HotelSearchResponse>()

        sut.hotelResultsObservable.subscribe(resultsSubscriber)
        sut.paramsSubject.onNext(happyParams)
        mockSearchProvider.successSubject.onNext(happyResponse)

        resultsSubscriber.awaitValueCount(1, 1, TimeUnit.SECONDS)

        resultsSubscriber.assertNoTerminalEvent()
        resultsSubscriber.assertNoErrors()
        resultsSubscriber.assertValueCount(1)
    }

    @Test
    fun locationSearch() {
        val resultsSubscriber = TestSubscriber<HotelSearchResponse>()
        sut.hotelResultsObservable.subscribe(resultsSubscriber)

        sut.paramsSubject.onNext(happyParams)
        sut.locationParamsSubject.onNext(makeSuggestion("", ""))

        mockSearchProvider.successSubject.onNext(happyResponse)

        resultsSubscriber.awaitValueCount(1, 1, TimeUnit.SECONDS)
        resultsSubscriber.assertNoTerminalEvent()
        resultsSubscriber.assertNoErrors()
        resultsSubscriber.assertValueCount(1)
    }

    @Test
    fun filteredSearch() {
        val filteredResultsSubscriber = TestSubscriber<HotelSearchResponse>()
        sut.filterResultsObservable.subscribe(filteredResultsSubscriber)

        sut.paramsSubject.onNext(happyParams)
        sut.filterParamsSubject.onNext(filterParams)

        mockSearchProvider.successSubject.onNext(happyResponse)

        filteredResultsSubscriber.awaitValueCount(1, 1, TimeUnit.SECONDS)
        filteredResultsSubscriber.assertValueCount(1)
        filteredResultsSubscriber.assertNoTerminalEvent()
    }

    @Test
    fun testFilterAfterPinned() {
        happyParams.suggestion.hotelId = "12345"
        assertTrue(happyParams.isPinnedSearch())

        sut.paramsSubject.onNext(happyParams)
        sut.filterParamsSubject.onNext(filterParams)

        assertFalse(sut.getSearchParams()!!.isPinnedSearch(), "FAILURE: Filter search params should never be pinned.")
        assertTrue(happyParams.isPinnedSearch(), "FAILURE : The original params should not be mutated")
    }

    @Test
    fun errorResponseCallsErrorObservable() {
        val testSubscriber = TestSubscriber<ApiError>()
        sut.errorObservable.subscribe(testSubscriber)

        mockSearchProvider.errorSubject.onNext(ApiError(ApiError.Code.HOTEL_SEARCH_NO_RESULTS))

        testSubscriber.awaitValueCount(1, 1, TimeUnit.SECONDS)
        testSubscriber.assertValueCount(1)
        assertEquals(ApiError.Code.HOTEL_SEARCH_NO_RESULTS, testSubscriber.onNextEvents[0].errorCode)
    }

    @Test
    fun titleSubjectSetToRegionShortName() {
        val regionShortName = "New York"
        val testSubscriber = TestSubscriber<String>()
        sut.titleSubject.subscribe(testSubscriber)

        sut.paramsSubject.onNext(makeParams("", regionShortName))

        testSubscriber.assertValueCount(1)
        testSubscriber.assertNoTerminalEvent()
        testSubscriber.assertValue(regionShortName)
    }

    @Test
    fun subtitleSubjectSet() {
        val expectedSubtitle = DateUtils.localDateToMMMd(checkInDate) + " - " + DateUtils.localDateToMMMd(checkOutDate) + ", 1 Guest"
        val testSubscriber = TestSubscriber<CharSequence>()
        sut.subtitleSubject.subscribe(testSubscriber)

        sut.paramsSubject.onNext(happyParams)

        testSubscriber.assertValueCount(1)
        testSubscriber.assertNoTerminalEvent()
        assertEquals(expectedSubtitle, testSubscriber.onNextEvents[0].toString())
    }

    @Test
    fun mapResultsObservable() {
        val regionShortName = context.getString(R.string.visible_map_area)
        val params = makeParams("", regionShortName)
        val testSubscriber = TestSubscriber<HotelSearchResponse>()
        sut.mapResultsObservable.subscribe(testSubscriber)

        sut.paramsSubject.onNext(params)
        mockSearchProvider.successSubject.onNext(happyResponse)

        testSubscriber.awaitValueCount(1, 1, TimeUnit.SECONDS)
        testSubscriber.assertValueCount(1)
        testSubscriber.assertNoTerminalEvent()
    }

    @Test
    fun noResultsMap() {
        val regionShortName = context.getString(R.string.visible_map_area)
        val params = makeParams("", regionShortName)
        val testSubscriber = TestSubscriber<ApiError>()
        sut.errorObservable.subscribe(testSubscriber)

        sut.paramsSubject.onNext(params)
        mockSearchProvider.noResultsSubject.onNext(Unit)

        testSubscriber.awaitValueCount(1, 1, TimeUnit.SECONDS)
        testSubscriber.assertValueCount(1)
        assertEquals(ApiError(ApiError.Code.HOTEL_MAP_SEARCH_NO_RESULTS), testSubscriber.onNextEvents[0])
    }

    @Test
    fun noResultsFilter() {
        val testSubscriber = TestSubscriber<ApiError>()
        sut.errorObservable.subscribe(testSubscriber)

        sut.paramsSubject.onNext(happyParams)
        sut.filterParamsSubject.onNext(filterParams)

        mockSearchProvider.noResultsSubject.onNext(Unit)

        testSubscriber.awaitValueCount(1, 1, TimeUnit.SECONDS)
        testSubscriber.assertValueCount(1)
        assertEquals(ApiError(ApiError.Code.HOTEL_FILTER_NO_RESULTS), testSubscriber.onNextEvents[0])
    }

    @Test
    fun noResultsGeneral() {
        val testSubscriber = TestSubscriber<ApiError>()
        sut.errorObservable.subscribe(testSubscriber)

        sut.paramsSubject.onNext(happyParams)

        mockSearchProvider.noResultsSubject.onNext(Unit)

        testSubscriber.awaitValueCount(1, 1, TimeUnit.SECONDS)
        testSubscriber.assertValueCount(1)
        assertEquals(ApiError(ApiError.Code.HOTEL_SEARCH_NO_RESULTS), testSubscriber.onNextEvents[0])
    }

    private fun makeHappyParams(): HotelSearchParams {
        return makeParams("", "")
    }

    private fun makeParams(gaiaId: String, regionShortName: String): HotelSearchParams {
        checkInDate = LocalDate.now()
        checkOutDate = checkInDate.plusDays(3)
        val suggestion = makeSuggestion(gaiaId, regionShortName)
        val hotelSearchParams = HotelSearchParams.Builder(3, 500).destination(suggestion).startDate(checkInDate).endDate(checkOutDate).build() as HotelSearchParams

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

    private fun makeFilterParams(): UserFilterChoices {
        val filterParams = UserFilterChoices()
        filterParams.name = "Hyatt"
        return filterParams
    }

    private fun getHotelSearchResponse(filePath: String) : HotelSearchResponse {
        val resourceReader = JSONResourceReader(filePath)
        val searchResponse = resourceReader.constructUsingGson(HotelSearchResponse::class.java)
        return searchResponse
    }
}