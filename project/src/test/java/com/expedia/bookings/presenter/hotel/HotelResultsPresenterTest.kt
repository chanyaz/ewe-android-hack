package com.expedia.bookings.presenter.hotel

import android.view.View
import com.expedia.bookings.R
import com.expedia.bookings.data.SuggestionV4
import com.expedia.bookings.data.hotels.HotelSearchParams
import com.expedia.bookings.data.hotels.HotelSearchResponse
import com.expedia.bookings.hotel.util.HotelSearchManager
import com.expedia.bookings.hotel.vm.HotelResultsViewModel
import com.expedia.bookings.services.TestObserver
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.utils.Ui
import com.expedia.testutils.JSONResourceReader
import org.joda.time.LocalDate
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RuntimeEnvironment
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@RunWith(RobolectricRunner::class)
class HotelResultsPresenterTest {

    private val context = RuntimeEnvironment.application

    private lateinit var resultsPresenter: HotelResultsPresenter
    private lateinit var resultsViewModel: HotelResultsViewModel

    private lateinit var paramsTestSubscriber: TestObserver<HotelSearchParams>
    private lateinit var hotelResultsTestSubscriber: TestObserver<HotelSearchResponse>

    @Before
    fun before() {
        Ui.getApplication(context).defaultHotelComponents()
        context.setTheme(R.style.Theme_Hotels_Default)
        resultsPresenter = View.inflate(context, R.layout.hotel_results_presenter_stub, null) as HotelResultsPresenter
        resultsViewModel = HotelResultsViewModel(context, HotelSearchManager(null))
        resultsPresenter.viewModel = resultsViewModel
        paramsTestSubscriber = TestObserver()
        resultsViewModel.paramsSubject.subscribe(paramsTestSubscriber)
        hotelResultsTestSubscriber = TestObserver()
        resultsViewModel.hotelResultsObservable.subscribe(hotelResultsTestSubscriber)
    }

    @Test
    fun testShowUnfilteredResultsUsingAdapterResults() {
        val searchResponse = createHotelResponse()
        resultsPresenter.adapter.resultsSubject.onNext(searchResponse)

        resultsPresenter.showUnfilteredResults()

        paramsTestSubscriber.assertValueCount(0)
        hotelResultsTestSubscriber.assertValue(searchResponse)
    }

    @Test
    fun testShowUnfilteredResultsNoCachedUnfilteredResponseTriggerSearch() {
        val searchParams = createSearchParamsWithFilters()
        resultsViewModel.paramsSubject.onNext(searchParams)

        paramsTestSubscriber.assertValuesAndClear(searchParams)

        resultsPresenter.showUnfilteredResults()

        assertEquals(LocalDate.now(), resultsViewModel.cachedParams!!.startDate)
        assertEquals(LocalDate.now().plusDays(1), resultsViewModel.cachedParams!!.endDate)
        assertTrue(resultsViewModel.cachedParams!!.filterOptions!!.isEmpty())
        paramsTestSubscriber.assertValue(resultsViewModel.cachedParams)
        hotelResultsTestSubscriber.assertValueCount(0)
    }

    @Test
    fun testShowUnfilteredResultsDifferentDateTriggerNewSearch() {
        val searchParams = createSearchParams()
        resultsViewModel.paramsSubject.onNext(searchParams)
        val searchResponse = createHotelResponse()
        resultsPresenter.listResultsObserver.onNext(searchResponse)

        paramsTestSubscriber.assertValuesAndClear(searchParams)

        val filterSearchParams = createSearchParamsWithFilters(LocalDate.now().plusDays(1), LocalDate.now().plusDays(2))
        resultsViewModel.paramsSubject.onNext(filterSearchParams)

        paramsTestSubscriber.assertValuesAndClear(filterSearchParams)

        resultsPresenter.showUnfilteredResults()

        assertEquals(LocalDate.now().plusDays(1), resultsViewModel.cachedParams!!.startDate)
        assertEquals(LocalDate.now().plusDays(2), resultsViewModel.cachedParams!!.endDate)
        assertTrue(resultsViewModel.cachedParams!!.filterOptions!!.isEmpty())
        paramsTestSubscriber.assertValue(resultsViewModel.cachedParams)
        hotelResultsTestSubscriber.assertValueCount(0)
    }

    @Test
    fun testShowUnfilteredResultsUsingCachedUnfilteredResponse() {
        val searchParams = createSearchParams()
        resultsViewModel.paramsSubject.onNext(searchParams)
        val searchResponse = createHotelResponse()
        resultsPresenter.listResultsObserver.onNext(searchResponse)

        paramsTestSubscriber.assertValuesAndClear(searchParams)

        val filterSearchParams = createSearchParamsWithFilters()
        resultsViewModel.paramsSubject.onNext(filterSearchParams)
        val filterSearchResponse = createHotelResponse()
        filterSearchResponse.isFilteredResponse = true
        resultsPresenter.listResultsObserver.onNext(filterSearchResponse)

        paramsTestSubscriber.assertValuesAndClear(filterSearchParams)

        resultsPresenter.showUnfilteredResults()

        assertEquals(LocalDate.now(), resultsViewModel.cachedParams!!.startDate)
        assertEquals(LocalDate.now().plusDays(1), resultsViewModel.cachedParams!!.endDate)
        assertTrue(resultsViewModel.cachedParams!!.filterOptions!!.isEmpty())
        paramsTestSubscriber.assertValueCount(0)
        hotelResultsTestSubscriber.assertValue(searchResponse)
    }

    private fun createSearchParams(startDate: LocalDate = LocalDate.now(), endDate: LocalDate = LocalDate.now().plusDays(1)): HotelSearchParams {
        val suggestion = SuggestionV4()
        suggestion.gaiaId = "gaiaId"
        return HotelSearchParams.Builder(0, 500)
                .startDate(startDate)
                .endDate(endDate)
                .destination(suggestion).build() as HotelSearchParams
    }

    private fun createSearchParamsWithFilters(startDate: LocalDate = LocalDate.now(), endDate: LocalDate = LocalDate.now().plusDays(1)): HotelSearchParams {
        val suggestion = SuggestionV4()
        suggestion.gaiaId = "gaiaId"
        return HotelSearchParams.Builder(0, 500)
                .hotelName("hotelName")
                .startDate(startDate)
                .endDate(endDate)
                .destination(suggestion).build() as HotelSearchParams
    }

    private fun createHotelResponse(): HotelSearchResponse {
        val resourceReader = JSONResourceReader("../lib/mocked/templates/m/api/hotel/search/happy.json")
        return resourceReader.constructUsingGson(HotelSearchResponse::class.java)
    }
}
