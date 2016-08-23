package com.expedia.bookings.test.robolectric

import com.expedia.bookings.R
import com.expedia.bookings.data.ApiError
import com.expedia.bookings.data.LineOfBusiness
import com.expedia.bookings.data.SuggestionV4
import com.expedia.bookings.data.hotels.HotelSearchParams
import com.expedia.bookings.data.hotels.HotelSearchResponse
import com.expedia.bookings.test.MockHotelServiceTestRule
import com.expedia.bookings.utils.DateUtils
import com.expedia.vm.hotel.HotelResultsViewModel
import org.joda.time.LocalDate
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RuntimeEnvironment
import rx.observers.TestSubscriber
import java.util.concurrent.TimeUnit
import kotlin.test.assertEquals

@RunWith(RobolectricRunner::class)
class HotelResultsViewModelTest {

    val context = RuntimeEnvironment.application
    val mockHotelServiceTestRule: MockHotelServiceTestRule = MockHotelServiceTestRule()
        @Rule get

    lateinit var sut: HotelResultsViewModel
    lateinit var checkInDate: LocalDate
    lateinit var checkOutDate: LocalDate

    @Before
    fun setup() {
        sut = HotelResultsViewModel(context, mockHotelServiceTestRule.services!!, LineOfBusiness.HOTELS, null)
    }

    @Test
    fun happySearch() {
        val resultsSubscriber = TestSubscriber<HotelSearchResponse>()
        sut.hotelResultsObservable.subscribe(resultsSubscriber)

        sut.paramsSubject.onNext(makeHappyParams())

        resultsSubscriber.awaitTerminalEvent(1, TimeUnit.SECONDS)
        resultsSubscriber.assertNoTerminalEvent()
        resultsSubscriber.assertNoErrors()
        resultsSubscriber.assertValueCount(1)
    }

    @Test
    fun locationSearch() {
        val resultsSubscriber = TestSubscriber<HotelSearchResponse>()
        sut.hotelResultsObservable.subscribe(resultsSubscriber)

        sut.paramsSubject.onNext(makeHappyParams())
        sut.locationParamsSubject.onNext(makeSuggestion("", ""))

        resultsSubscriber.awaitTerminalEvent(1, TimeUnit.SECONDS)
        resultsSubscriber.assertNoTerminalEvent()
        resultsSubscriber.assertNoErrors()
        resultsSubscriber.assertValueCount(2)
    }

    @Test
    fun errorResponseCallsErrorObservable() {
        val paramsForErrorResp = makeParams("mock_error", "")
        val testSubscriber = TestSubscriber<ApiError>()
        sut.errorObservable.subscribe(testSubscriber)

        sut.paramsSubject.onNext(paramsForErrorResp)

        testSubscriber.awaitTerminalEvent(1, TimeUnit.SECONDS)
        testSubscriber.assertValueCount(1)
        testSubscriber.assertNoTerminalEvent()
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
        val params = makeHappyParams()
        val expectedSubtitle = DateUtils.localDateToMMMd(checkInDate) + " - " + DateUtils.localDateToMMMd(checkOutDate) + ", 1 Guest"
        val testSubscriber = TestSubscriber<CharSequence>()
        sut.subtitleSubject.subscribe(testSubscriber)

        sut.paramsSubject.onNext(params)

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

        testSubscriber.awaitTerminalEvent(1, TimeUnit.SECONDS)
        testSubscriber.assertValueCount(1)
        testSubscriber.assertNoTerminalEvent()
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
}
