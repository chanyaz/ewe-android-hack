package com.expedia.bookings.test

import android.content.Context
import com.expedia.bookings.R
import com.expedia.bookings.data.SuggestionV4
import com.expedia.bookings.data.cars.ApiError
import com.expedia.bookings.data.flights.FlightSearchParams
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.utils.DateFormatUtils
import com.expedia.vm.packages.FlightErrorViewModel
import org.joda.time.LocalDate
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RuntimeEnvironment
import rx.observers.TestSubscriber
import kotlin.test.assertEquals

@RunWith(RobolectricRunner::class)
class FlightErrorViewModelTest {
    private fun getContext(): Context {
        return RuntimeEnvironment.application
    }

    @Test fun observableEmissionsOnSearchApiError() {
        observableEmissionsOnSearchError(ApiError.Code.FLIGHT_SEARCH_NO_RESULTS)
    }

    fun observableEmissionsOnSearchError(apiError: ApiError.Code) {
        val subjectUnderTest = FlightErrorViewModel(RuntimeEnvironment.application)

        val searchApiObservableTestSubscriber = TestSubscriber.create<ApiError.Code>()
        subjectUnderTest.searchApiErrorObserver.subscribe(searchApiObservableTestSubscriber)

        val errorMessageObservableTestSubscriber = TestSubscriber.create<String>()
        subjectUnderTest.errorMessageObservable.subscribe(errorMessageObservableTestSubscriber)

        subjectUnderTest.searchApiErrorObserver.onNext(apiError)

        errorMessageObservableTestSubscriber.assertValues(getContext().getString(R.string.error_no_result_message))
    }

    @Test fun toolBarTitleAndSubTitle() {
        val subjectUnderTest = FlightErrorViewModel(RuntimeEnvironment.application)
        val flightSearchParams = doFlightSearch()
        val expectedDate = DateFormatUtils.formatLocalDateToShortDayAndDate(flightSearchParams.departureDate)
        subjectUnderTest.paramsSubject.onNext(flightSearchParams)
        assertEquals("Select flight to Los Angles", subjectUnderTest.titleObservable.value)
        assertEquals(expectedDate + ", 1 Traveler", subjectUnderTest.subTitleObservable.value)
    }

    private fun doFlightSearch(): FlightSearchParams {
        return FlightSearchParams.Builder(26, 500)
                .origin(getDummySuggestion())
                .destination(getDummySuggestion())
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusDays(1))
                .adults(1)
                .build() as FlightSearchParams
    }

    private fun getDummySuggestion(): SuggestionV4 {
        val suggestion = SuggestionV4()
        suggestion.gaiaId = ""
        suggestion.regionNames = SuggestionV4.RegionNames()
        suggestion.regionNames.displayName = ""
        suggestion.regionNames.fullName = ""
        suggestion.regionNames.shortName = "Los Angles"
        suggestion.hierarchyInfo = SuggestionV4.HierarchyInfo()
        suggestion.hierarchyInfo!!.airport = SuggestionV4.Airport()
        suggestion.hierarchyInfo!!.airport!!.airportCode = ""
        return suggestion
    }

}
