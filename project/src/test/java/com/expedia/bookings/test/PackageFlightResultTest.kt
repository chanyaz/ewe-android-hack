package com.expedia.bookings.test

import com.expedia.bookings.data.Db
import com.expedia.bookings.data.LineOfBusiness
import com.expedia.bookings.data.Money
import com.expedia.bookings.data.flights.Airline
import com.expedia.bookings.data.flights.FlightLeg
import com.expedia.bookings.data.packages.PackageOfferModel
import com.expedia.bookings.data.packages.PackageSearchResponse
import com.expedia.bookings.services.TestObserver
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.test.robolectric.shadows.ShadowDateFormat
import com.expedia.vm.FlightResultsViewModel
import com.expedia.vm.packages.PackageFlightViewModel
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import java.util.ArrayList
import kotlin.properties.Delegates
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@RunWith(RobolectricRunner::class)
@Config(shadows = arrayOf(ShadowDateFormat::class))
class PackageFlightResultTest {
    var vm: PackageFlightViewModel by Delegates.notNull()
    var flight: FlightLeg by Delegates.notNull()
    val context = RuntimeEnvironment.application

    @Before
    fun before(){
        flight = makeFlight()
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA, MultiBrand.ORBITZ, MultiBrand.CHEAPTICKETS, MultiBrand.TRAVELOCITY, MultiBrand.AIRASIAGO,
            MultiBrand.VOYAGES, MultiBrand.WOTIF, MultiBrand.LASTMINUTE, MultiBrand.EBOOKERS))
    fun testFlightTime(){
        vm = PackageFlightViewModel(context, flight)
        assertEquals(vm.flightTime, "1:10 am - 12:20 pm +1d")
        flight.elapsedDays = 0
        vm = PackageFlightViewModel(context, flight)
        assertEquals(vm.flightTime, "1:10 am - 12:20 pm")
    }

    @Test
    fun testDeltaPrice() {
        vm = PackageFlightViewModel(context, flight)
        assertEquals(vm.price(), "+$11")
        flight.packageOfferModel.price.deltaPositive = false
        flight.packageOfferModel.price.differentialPriceFormatted = "-$11"
        vm = PackageFlightViewModel(context, flight)
        assertEquals(vm.price(), "-$11")
    }

    @Test
    fun testAirlines() {
        vm = PackageFlightViewModel(context, flight)
        assertEquals(vm.airline[0].airlineName, "United" )
        assertEquals(vm.airline[1].airlineName, "Delta" )
        assertEquals(vm.airline.size, 2)

        flight.airlines[1].airlineName = "United"
        vm = PackageFlightViewModel(context, flight)
        assertEquals(vm.airline[0].airlineName, "United" )
        assertEquals(vm.airline.size, 1)

        val airline3 = Airline("Delta", null)
        flight.airlines.add(airline3)
        vm = PackageFlightViewModel(context, flight)
        assertEquals(vm.airline[0].airlineName, "United" )
        assertEquals(vm.airline[1].airlineName, "United" )
        assertEquals(vm.airline[2].airlineName, "Delta" )
        assertEquals(vm.airline.size, 3)
    }

    @Test
    fun testFlightDuration(){
        vm = PackageFlightViewModel(context, flight)
        assertEquals(vm.duration, "19h 10m (1 Stop)")
        flight.stopCount = 0
        vm = PackageFlightViewModel(context, flight)
        assertEquals(vm.duration, "19h 10m (Nonstop)")
        flight.durationHour = 0
        vm = PackageFlightViewModel(context, flight)
        assertEquals(vm.duration, "10m (Nonstop)")
        flight.durationHour = 25
        vm = PackageFlightViewModel(context, flight)
        assertEquals(vm.duration, "25h 10m (Nonstop)")
    }

    @Test
    fun testFlightSortByPrice() {
        val searchResponse = PackageSearchResponse()
        searchResponse.packageResult = PackageSearchResponse.PackageResult()
        searchResponse.packageResult.flightsPackage = PackageSearchResponse.FlightPackage()
        searchResponse.packageResult.flightsPackage.flights = makeFlightList()
        Db.setPackageResponse(searchResponse)

        val resultsVM = FlightResultsViewModel(context, LineOfBusiness.PACKAGES)
        val testSubscriber = TestObserver<List<FlightLeg>>()
        resultsVM.flightResultsObservable.subscribe(testSubscriber)
        resultsVM.flightResultsObservable.onNext(searchResponse.packageResult.flightsPackage.flights)
        val testSubscriberResult = testSubscriber.values()[0]

        for (i in 2..testSubscriberResult.size - 1) {
            val current = testSubscriberResult[i].packageOfferModel.price.packageTotalPrice.amount
            val previous = testSubscriberResult[i - 1].packageOfferModel.price.packageTotalPrice.amount
            assertTrue(current.compareTo(previous) > 0, "Expected $current >= $previous")
        }
    }

    fun makeFlight(): FlightLeg {
        val flight = FlightLeg()
        flight.elapsedDays = 1
        flight.durationHour = 19
        flight.durationMinute = 10
        flight.departureTimeShort = "1:10AM"
        flight.arrivalTimeShort = "12:20PM"
        flight.departureDateTimeISO = "2016-07-10T01:10:00.000-05:00"
        flight.arrivalDateTimeISO = "2016-07-10T12:20:00.000-07:00"
        flight.stopCount = 1
        flight.packageOfferModel = PackageOfferModel()
        flight.packageOfferModel.price = PackageOfferModel.PackagePrice()
        flight.packageOfferModel.price.packageTotalPrice = Money("111", "USD")
        flight.packageOfferModel.price.deltaPositive = true
        flight.packageOfferModel.price.differentialPriceFormatted = "$11"
        val airlines = ArrayList<Airline>()
        val airline1 = Airline("United", null)
        val airline2 = Airline("Delta", null)
        airlines.add(airline1)
        airlines.add(airline2)
        flight.airlines = airlines
        return flight
    }

    fun makeFlightList(): List<FlightLeg> {
        val flightList = ArrayList<FlightLeg>()
        val flight1 = makeFlight()
        val flight2 = makeFlight()
        flight2.packageOfferModel.price.packageTotalPrice = Money("100", "USD")
        flight.packageOfferModel.price.deltaPositive = true
        flight.packageOfferModel.price.differentialPriceFormatted = "$0"
        flightList.add(flight1)
        flightList.add(flight2)
        return flightList
    }
}
