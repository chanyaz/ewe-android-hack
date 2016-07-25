package com.expedia.vm.test.robolectric

import android.content.Context
import com.expedia.bookings.data.SuggestionV4
import com.expedia.bookings.data.flights.FlightSearchParams
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.vm.FlightCheckoutOverviewViewModel
import org.joda.time.LocalDate
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RuntimeEnvironment
import rx.observers.TestSubscriber
import kotlin.test.assertEquals

@RunWith(RobolectricRunner::class)
class FlightCheckoutOverviewViewModelTest {

    @Test
    fun flightViewModelTest() {
        val viewmodel = FlightCheckoutOverviewViewModel(getContext())

        val origin = getFakeSuggestion("SFO")
        val destination = getFakeSuggestion("SEA")
        val params = FlightSearchParams.Builder(100, 500)
                .origin(origin)
                .destination(destination)
                .startDate(LocalDate.now().withYear(1989).withMonthOfYear(9).withDayOfMonth(6))
                .endDate(LocalDate.now().withYear(2021).withMonthOfYear(9).withDayOfMonth(6))
                .adults(1).build() as FlightSearchParams


        val titleTestSubscriber = TestSubscriber<String>(3)
        val checkInOutTestSubscriber = TestSubscriber<String>(2)
        val urlTestSubscriber = TestSubscriber<List<String>>(1)

        viewmodel.cityTitle.subscribe(titleTestSubscriber)
        viewmodel.datesTitle.subscribe(titleTestSubscriber)
        viewmodel.travelersTitle.subscribe(titleTestSubscriber)

        viewmodel.checkIn.subscribe(checkInOutTestSubscriber)
        viewmodel.checkOut.subscribe(checkInOutTestSubscriber)

        viewmodel.url.subscribe(urlTestSubscriber)

        viewmodel.params.onNext(params)

        assertEquals("San Francisco, CA", titleTestSubscriber.onNextEvents[0])
        assertEquals("Wed Sep 06, 1989 - Mon Sep 06, 2021", titleTestSubscriber.onNextEvents[1])
        assertEquals("1 Traveler", titleTestSubscriber.onNextEvents[2])
        assertEquals("1989-09-06", checkInOutTestSubscriber.onNextEvents[0])
        assertEquals("2021-09-06", checkInOutTestSubscriber.onNextEvents[1])
        assertEquals("https://media.expedia.com/mobiata/mobile/apps/ExpediaBooking/FlightDestinations/images/SEA.jpg?downsize=480px:*&crop=w:165/480xw;center,top&output-quality=60&output-format=jpeg&", urlTestSubscriber.getOnNextEvents()[0][0].toString())
        titleTestSubscriber.assertValueCount(3)
        checkInOutTestSubscriber.assertValueCount(2)
        urlTestSubscriber.assertValueCount(1)
    }

    private fun getFakeSuggestion(airportCode: String): SuggestionV4 {
        val suggestion = SuggestionV4()
        val hierarchyInfo = SuggestionV4.HierarchyInfo()
        val airport = SuggestionV4.Airport()
        airport.airportCode = airportCode
        hierarchyInfo.airport = airport
        val country = SuggestionV4.Country()
        country.name = ""
        hierarchyInfo.country = country
        suggestion.hierarchyInfo = hierarchyInfo

        val regionName = SuggestionV4.RegionNames()
        regionName.shortName = "San Francisco, CA"
        regionName.displayName = "San Francisco, CA"
        suggestion.regionNames = regionName
        return suggestion
    }

    private fun getContext(): Context {
        return RuntimeEnvironment.application
    }
}
