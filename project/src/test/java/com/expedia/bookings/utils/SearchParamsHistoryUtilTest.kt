package com.expedia.bookings.utils

import com.expedia.bookings.R
import com.expedia.bookings.data.SuggestionV4
import com.expedia.bookings.data.flights.FlightSearchParams
import com.expedia.bookings.data.packages.PackageSearchParams
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.mobiata.android.util.SettingUtils
import org.joda.time.LocalDate
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RuntimeEnvironment
import rx.observers.TestSubscriber
import java.util.concurrent.TimeUnit
import kotlin.test.assertEquals

@RunWith(RobolectricRunner::class)
class SearchParamsHistoryUtilTest {

    val context = RuntimeEnvironment.application

    @Test
    fun testFlightParamsSaveAndLoad() {
        val searchParamsToSave = getDummyFlightSearchParams()
        SearchParamsHistoryUtil.saveFlightParams(context, searchParamsToSave , { _ ->
            SearchParamsHistoryUtil.loadPreviousFlightSearchParams(RuntimeEnvironment.application, { loadedSearchParams ->
                assertEquals(searchParamsToSave.flightCabinClass, loadedSearchParams.flightCabinClass)
                assertEquals(searchParamsToSave.adults, 1)
                assertEquals(searchParamsToSave.children, listOf(1,2,3))
            })
        })
    }

    @Test
    fun testFlightParamsLoadFailure() {
        SearchParamsHistoryUtil.deleteCachedSearchParams(context)
        val failure = TestSubscriber<Unit>()
        SearchParamsHistoryUtil.loadPreviousFlightSearchParams(RuntimeEnvironment.application, {}, {
            failure.onNext(Unit)
        })
        failure.awaitValueCount(1, 2, TimeUnit.SECONDS)
        failure.assertValueCount(1)
    }

    @Test
    fun testPackageParamsSaveAndLoad() {
        val searchParamsToSave = getDummyPackageSearchParams()
        SearchParamsHistoryUtil.savePackageParams(RuntimeEnvironment.application, searchParamsToSave , { _ ->
            SearchParamsHistoryUtil.loadPreviousFlightSearchParams(RuntimeEnvironment.application, { loadedSearchParams ->
                assertEquals(searchParamsToSave.startDate, loadedSearchParams.startDate)
                assertEquals(searchParamsToSave.endDate, loadedSearchParams.endDate)
                assertEquals(searchParamsToSave.adults, 1)
                assertEquals(searchParamsToSave.children, listOf(1,2,3))
            })
        })
    }


    private fun getDummyFlightSearchParams(): FlightSearchParams {
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

    private fun getDummyPackageSearchParams(): PackageSearchParams {
        val origin = SuggestionV4()
        val destination = SuggestionV4()
        val startDate = LocalDate.now()
        val endDate = startDate.plusDays(2)

        val paramsBuilder = PackageSearchParams.Builder(26, 369)
                .origin(origin)
                .destination(destination)
                .startDate(startDate)
                .adults(1)
                .children(listOf(1,2,3))
                .endDate(endDate) as PackageSearchParams.Builder

        return paramsBuilder.build()
    }
}