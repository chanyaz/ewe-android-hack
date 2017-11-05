package com.expedia.vm.test.robolectric

import android.content.Context
import com.expedia.bookings.data.SuggestionV4
import com.expedia.bookings.data.abacus.AbacusUtils
import com.expedia.bookings.data.flights.FlightSearchParams
import com.expedia.bookings.test.MultiBrand
import com.expedia.bookings.test.RunForBrands
import com.expedia.bookings.test.robolectric.RoboTestHelper
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
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun flightViewModelTest() {
        setShowMoreInfoTest()
        val viewmodel = FlightCheckoutOverviewViewModel(getContext())

        val origin = getFakeSuggestion("SFO")
        val destination = getFakeSuggestion("SEA")
        val params = FlightSearchParams.Builder(100, 500)
                .origin(origin)
                .destination(destination)
                .startDate(LocalDate.now().withYear(1989).withMonthOfYear(9).withDayOfMonth(6))
                .endDate(LocalDate.now().withYear(2021).withMonthOfYear(9).withDayOfMonth(6))
                .adults(1).build() as FlightSearchParams


        val titleTestSubscriber = TestSubscriber<String>()
        val checkInOutTestSubscriber = TestSubscriber<Pair<String, String>>()
        val urlTestSubscriber = TestSubscriber<List<String>>()
        val subTitleTestSubscriber = TestSubscriber<String>()

        viewmodel.cityTitle.subscribe(titleTestSubscriber)
        viewmodel.datesTitle.subscribe(titleTestSubscriber)
        viewmodel.datesTitleContDesc.subscribe(titleTestSubscriber)
        viewmodel.travelersTitle.subscribe(titleTestSubscriber)

        viewmodel.checkInAndCheckOutDate.subscribe(checkInOutTestSubscriber)

        viewmodel.url.subscribe(urlTestSubscriber)
        viewmodel.subTitleText.subscribe(subTitleTestSubscriber)

        viewmodel.params.onNext(params)

        assertEquals("San Francisco, CA", titleTestSubscriber.onNextEvents[0])
        assertEquals("Wed Sep 06, 1989 - Mon Sep 06, 2021", titleTestSubscriber.onNextEvents[1])
        assertEquals("Wed Sep 06, 1989 to Mon Sep 06, 2021", titleTestSubscriber.onNextEvents[2])
        assertEquals("1 traveler", titleTestSubscriber.onNextEvents[3])
        assertEquals("1989-09-06", checkInOutTestSubscriber.onNextEvents[0].first)
        assertEquals("2021-09-06", checkInOutTestSubscriber.onNextEvents[0].second)
        assertEquals("https://media.expedia.com/mobiata/mobile/apps/ExpediaBooking/FlightDestinations/images/SEA.jpg?downsize=480px:*&crop=w:165/480xw;center,top&output-quality=60&output-format=jpeg&", urlTestSubscriber.onNextEvents[0][0].toString())
        assertEquals("Wed Sep 06, 1989 - Mon Sep 06, 2021, 1 traveler", subTitleTestSubscriber.onNextEvents[0])
        titleTestSubscriber.assertValueCount(4)
        checkInOutTestSubscriber.assertValueCount(1)
        urlTestSubscriber.assertValueCount(1)
    }

    @Test
    fun testCheckInAndCheckOutDate() {
        setShowMoreInfoTest()
        val viewmodel = FlightCheckoutOverviewViewModel(getContext())

        val origin = getFakeSuggestion("SFO")
        val destination = getFakeSuggestion("SEA")
        val params = FlightSearchParams.Builder(100, 500)
                .origin(origin)
                .destination(destination)
                .startDate(LocalDate.now().withYear(1989).withMonthOfYear(9).withDayOfMonth(6))
                .endDate(LocalDate.now().withYear(2021).withMonthOfYear(9).withDayOfMonth(6))
                .adults(1).build() as FlightSearchParams

        val checkInAndCheckOutDateTestSubscriber = TestSubscriber<Pair<String, String>>()
        val checkInWithoutCheckoutDateTestSubscriber = TestSubscriber<String>()

        viewmodel.checkInAndCheckOutDate.subscribe(checkInAndCheckOutDateTestSubscriber)
        viewmodel.checkInWithoutCheckoutDate.subscribe(checkInWithoutCheckoutDateTestSubscriber)

        viewmodel.params.onNext(params)

        checkInAndCheckOutDateTestSubscriber.assertValueCount(1)
        checkInWithoutCheckoutDateTestSubscriber.assertValueCount(0)

        assertEquals("1989-09-06", checkInAndCheckOutDateTestSubscriber.onNextEvents.first().first)
        assertEquals("2021-09-06", checkInAndCheckOutDateTestSubscriber.onNextEvents.first().second)
    }

    @Test
    fun testCheckInWithoutCheckOutDate() {
        setShowMoreInfoTest()
        val viewmodel = FlightCheckoutOverviewViewModel(getContext())

        val origin = getFakeSuggestion("SFO")
        val destination = getFakeSuggestion("SEA")
        val params = FlightSearchParams.Builder(100, 500)
                .origin(origin)
                .destination(destination)
                .startDate(LocalDate.now().withYear(1989).withMonthOfYear(9).withDayOfMonth(6))
                .adults(1).build() as FlightSearchParams

        val checkInAndCheckOutDateTestSubscriber = TestSubscriber<Pair<String, String>>()
        val checkInWithoutCheckoutDateTestSubscriber = TestSubscriber<String>()

        viewmodel.checkInAndCheckOutDate.subscribe(checkInAndCheckOutDateTestSubscriber)
        viewmodel.checkInWithoutCheckoutDate.subscribe(checkInWithoutCheckoutDateTestSubscriber)

        viewmodel.params.onNext(params)

        checkInAndCheckOutDateTestSubscriber.assertValueCount(0)
        checkInWithoutCheckoutDateTestSubscriber.assertValueCount(1)

        assertEquals("1989-09-06", checkInWithoutCheckoutDateTestSubscriber.onNextEvents.first())
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
        regionName.shortName = "San Francisco, CA (SFO-San Francisco Intl.)"
        regionName.displayName = "<B>San</B> Francisco, CA (<B>SFO</B>-San Francisco Intl.)"
        regionName.fullName = "San Francisco, CA, United States (<B>SFO</B>-San Francisco Intl.)"
        suggestion.regionNames = regionName
        return suggestion
    }

    private fun getContext(): Context {
        return RuntimeEnvironment.application
    }

    private fun setShowMoreInfoTest() {
        RoboTestHelper.bucketTests(AbacusUtils.EBAndroidAppFlightsMoreInfoOnOverview)
    }
}
