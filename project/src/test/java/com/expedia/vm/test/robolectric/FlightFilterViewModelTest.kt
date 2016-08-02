package com.expedia.vm.test.robolectric

import android.content.Context
import com.expedia.bookings.data.FlightFilter
import com.expedia.bookings.data.LineOfBusiness
import com.expedia.bookings.data.Money
import com.expedia.bookings.data.flights.FlightLeg
import com.expedia.bookings.data.packages.PackageOfferModel
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.vm.BaseFlightFilterViewModel
import org.joda.time.DateTime
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RuntimeEnvironment
import java.util.ArrayList
import kotlin.properties.Delegates
import kotlin.test.assertTrue

@RunWith(RobolectricRunner::class)
class FlightFilterViewModelTest {
    var vm: BaseFlightFilterViewModel by Delegates.notNull()

    @Before
    fun before() {
        vm = BaseFlightFilterViewModel(getContext(), LineOfBusiness.FLIGHTS_V2)
    }

    private fun getContext(): Context {
        return RuntimeEnvironment.application
    }


    @Test
    fun sortByPrice() {
        vm.filteredList = getFlightList()
        vm.sortObserver.onNext(FlightFilter.Sort.PRICE)

        for (i in 1..vm.filteredList.size - 1) {
            val current = vm.filteredList.elementAt(i).packageOfferModel.price.packageTotalPrice.amount.toInt()
            val previous = vm.filteredList.elementAt(i - 1).packageOfferModel.price.packageTotalPrice.amount.toInt()
            assertTrue(current >= previous, "Expected $current >= $previous")
        }
    }

    @Test
    fun sortByDepartureTime() {
        vm.filteredList = getFlightList()
        vm.sortObserver.onNext(FlightFilter.Sort.DEPARTURE)

        for (i in 1..vm.filteredList.size - 1) {
            val current = DateTime.parse(vm.filteredList.elementAt(i).departureDateTimeISO)
            val previous = DateTime.parse(vm.filteredList.elementAt(i - 1).departureDateTimeISO)
            assertTrue(previous.isBefore(current), "Expected $current >= $previous")
        }
    }

    @Test
    fun sortByArrivalTime() {
        vm.filteredList = getFlightList()
        vm.sortObserver.onNext(FlightFilter.Sort.ARRIVAL)

        for (i in 1..vm.filteredList.size - 1) {
            val current = DateTime.parse(vm.filteredList.elementAt(i).arrivalDateTimeISO)
            val previous = DateTime.parse(vm.filteredList.elementAt(i - 1).arrivalDateTimeISO)
            assertTrue(previous.isBefore(current), "Expected $current >= $previous")
        }
    }

    private fun getFlightList(): List<FlightLeg> {
        val list = ArrayList<FlightLeg>()
        val flightLeg1 = FlightLeg()
        flightLeg1.elapsedDays = 1
        flightLeg1.durationHour = 19
        flightLeg1.durationMinute = 10
        flightLeg1.flightSegments = ArrayList<FlightLeg.FlightSegment>()
        flightLeg1.departureDateTimeISO = "2016-09-07T20:20:00.000-05:00"
        flightLeg1.arrivalDateTimeISO = "2016-09-08T19:20:00.000+01:00"
        flightLeg1.stopCount = 1
        flightLeg1.packageOfferModel = PackageOfferModel()
        flightLeg1.packageOfferModel.price = PackageOfferModel.PackagePrice()
        flightLeg1.packageOfferModel.price.packageTotalPrice = Money("200", "USD")

        val flightLeg2 = FlightLeg()
        flightLeg2.durationHour = 19
        flightLeg2.durationMinute = 0
        flightLeg2.departureDateTimeISO = "2016-09-07T01:20:00.000-05:00"
        flightLeg2.arrivalDateTimeISO = "2016-09-07T20:20:00.000+01:00"
        flightLeg2.stopCount = 1
        flightLeg2.packageOfferModel = PackageOfferModel()
        flightLeg2.packageOfferModel.price = PackageOfferModel.PackagePrice()
        flightLeg2.packageOfferModel.price.packageTotalPrice = Money("300", "USD")

        val flightLeg3 = FlightLeg()
        flightLeg3.durationHour = 18
        flightLeg3.durationMinute = 0
        flightLeg3.departureDateTimeISO = "2016-09-07T21:20:00.000-05:00"
        flightLeg3.arrivalDateTimeISO = "2016-09-08T08:20:00.000+01:00"
        flightLeg3.stopCount = 1
        flightLeg3.packageOfferModel = PackageOfferModel()
        flightLeg3.packageOfferModel.price = PackageOfferModel.PackagePrice()
        flightLeg3.packageOfferModel.price.packageTotalPrice = Money("220", "USD")

        list.add(flightLeg1)
        list.add(flightLeg2)
        list.add(flightLeg3)
        return list
    }
}

