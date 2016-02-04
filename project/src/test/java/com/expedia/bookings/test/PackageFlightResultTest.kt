package com.expedia.bookings.test

import com.expedia.bookings.data.packages.Airline
import com.expedia.bookings.data.packages.FlightLeg
import com.expedia.bookings.data.packages.PackageOfferModel
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.vm.PackageFlightViewModel
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RuntimeEnvironment
import java.util.ArrayList
import kotlin.properties.Delegates
import kotlin.test.assertEquals

@RunWith(RobolectricRunner::class)
public class PackageFlightResultTest {
    var vm: PackageFlightViewModel by Delegates.notNull()
    var flight: FlightLeg by Delegates.notNull()
    val context = RuntimeEnvironment.application

    @Before
    fun before(){
        flight = makeFlight()
    }

    @Test
    fun testFlightTime(){
        vm = PackageFlightViewModel(context, flight)
        assertEquals(vm.flightTimeObserver.value, "1:10 am - 12:20 pm +1d")
        flight.elapsedDays = 0
        vm = PackageFlightViewModel(context, flight)
        assertEquals(vm.flightTimeObserver.value, "1:10 am - 12:20 pm")
    }

    @Test
    fun testDeltaPrice() {
        vm = PackageFlightViewModel(context, flight)
        assertEquals(vm.priceObserver.value, "+$11")
        flight.packageOfferModel.price.deltaPositive = false
        flight.packageOfferModel.price.differentialPriceFormatted = "-$11"
        vm = PackageFlightViewModel(context, flight)
        assertEquals(vm.priceObserver.value, "-$11")
    }

    @Test
    fun testAirlines() {
        vm = PackageFlightViewModel(context, flight)
        assertEquals(vm.airlineObserver.value[0].airlineName, "United" )
        assertEquals(vm.airlineObserver.value[1].airlineName, "Delta" )
        assertEquals(vm.airlineObserver.value.size, 2)

        flight.airlines[1].airlineName = "United"
        vm = PackageFlightViewModel(context, flight)
        assertEquals(vm.airlineObserver.value[0].airlineName, "United" )
        assertEquals(vm.airlineObserver.value.size, 1)

        val airline3 = Airline("Delta", null)
        flight.airlines.add(airline3)
        vm = PackageFlightViewModel(context, flight)
        assertEquals(vm.airlineObserver.value[0].airlineName, "United" )
        assertEquals(vm.airlineObserver.value[1].airlineName, "United" )
        assertEquals(vm.airlineObserver.value[2].airlineName, "Delta" )
        assertEquals(vm.airlineObserver.value.size, 3)
    }

    @Test
    fun testFlightDuration(){
        vm = PackageFlightViewModel(context, flight)
        assertEquals(vm.durationObserver.value, "19h 10m (1 Stop)")
        flight.stopCount = 0
        vm = PackageFlightViewModel(context, flight)
        assertEquals(vm.durationObserver.value, "19h 10m (Nonstop)")
        flight.durationHour = 0
        vm = PackageFlightViewModel(context, flight)
        assertEquals(vm.durationObserver.value, "10m (Nonstop)")
        flight.durationHour = 25
        vm = PackageFlightViewModel(context, flight)
        assertEquals(vm.durationObserver.value, "25h 10m (Nonstop)")
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
}
