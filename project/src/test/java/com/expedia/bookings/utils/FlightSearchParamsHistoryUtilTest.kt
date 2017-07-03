package com.expedia.bookings.utils

import com.expedia.bookings.data.SuggestionV4
import com.expedia.bookings.data.flights.FlightSearchParams
import com.expedia.bookings.test.robolectric.RobolectricRunner
import org.joda.time.LocalDate
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RuntimeEnvironment
import rx.observers.TestSubscriber
import java.util.concurrent.TimeUnit
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@RunWith(RobolectricRunner::class)
class FlightSearchParamsHistoryUtilTest {

    @Test
    fun testParamsSaveAndLoad() {
        val searchParamsToSave = getDummySearchParams()
        FlightSearchParamsHistoryUtil.saveFlightParams(RuntimeEnvironment.application, searchParamsToSave , { _ ->
            FlightSearchParamsHistoryUtil.loadPreviousFlightSearchParams(RuntimeEnvironment.application, { loadedSearchParams ->
                assertEquals(searchParamsToSave.flightCabinClass, loadedSearchParams.flightCabinClass)
                assertEquals(searchParamsToSave.adults, 1)
                assertEquals(searchParamsToSave.children, listOf(1,2,3))
            })
        })
    }

    @Test
    fun testParamsLoadFailure() {
        FlightSearchParamsHistoryUtil.deleteCachedFlightSearchParams(RuntimeEnvironment.application)
        val failure = TestSubscriber<Unit>()
        FlightSearchParamsHistoryUtil.loadPreviousFlightSearchParams(RuntimeEnvironment.application, {}, {
            failure.onNext(Unit)
        })
        failure.awaitValueCount(1, 2, TimeUnit.SECONDS)
        failure.assertValueCount(1)
    }


    private fun getDummySearchParams(): FlightSearchParams {
        val origin = SuggestionV4()
        val destination = SuggestionV4()
        val startDate = LocalDate.now()
        val endDate = startDate.plusDays(2)
        val paramsBuilder = FlightSearchParams.Builder(26, 369)
                .flightCabinClass("BUSINESS")
                .origin(origin)
                .destination(destination)
                .startDate(startDate)
                .adults(1)
                .children(listOf(1,2,3))
                .endDate(endDate) as FlightSearchParams.Builder

        return paramsBuilder.build()
    }
}