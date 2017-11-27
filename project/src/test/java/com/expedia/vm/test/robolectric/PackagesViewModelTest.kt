package com.expedia.vm.test.robolectric

import android.content.Context
import com.expedia.bookings.data.Money
import com.expedia.bookings.data.flights.Airline
import com.expedia.bookings.data.flights.FlightLeg
import com.expedia.bookings.data.packages.PackageOfferModel
import com.expedia.bookings.data.payment.LoyaltyEarnInfo
import com.expedia.bookings.data.payment.LoyaltyInformation
import com.expedia.bookings.data.payment.PointsEarnInfo
import com.expedia.bookings.data.pos.PointOfSale
import com.expedia.bookings.test.PointOfSaleTestConfiguration
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.vm.packages.PackageFlightViewModel
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RuntimeEnvironment
import java.util.ArrayList
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@RunWith(RobolectricRunner::class)
class PackagesViewModelTest {

    private lateinit var sut: PackageFlightViewModel
    lateinit var flightLeg: FlightLeg

    fun createSystemUnderTest() {
        sut = PackageFlightViewModel(getContext(), flightLeg)
    }

    fun createExpectedFlightLeg() {
        flightLeg = FlightLeg()
        flightLeg.flightSegments = ArrayList()
        flightLeg.elapsedDays = 1
        flightLeg.durationHour = 19
        flightLeg.durationMinute = 10
        flightLeg.departureTimeShort = "1:10AM"
        flightLeg.arrivalTimeShort = "12:20PM"
        flightLeg.departureDateTimeISO = "2016-07-10T01:10:00.000-05:00"
        flightLeg.arrivalDateTimeISO = "2016-07-10T12:20:00.000-07:00"
        flightLeg.stopCount = 1
        flightLeg.packageOfferModel = PackageOfferModel()
        flightLeg.packageOfferModel.price = PackageOfferModel.PackagePrice()
        flightLeg.packageOfferModel.price.packageTotalPrice = Money("111", "USD")
        flightLeg.packageOfferModel.price.deltaPositive = true
        flightLeg.packageOfferModel.price.differentialPriceFormatted = "$11"
        flightLeg.packageOfferModel.price.pricePerPersonFormatted = "200.0"
        flightLeg.packageOfferModel.price.averageTotalPricePerTicket = Money("200.0", "USD")
        flightLeg.packageOfferModel.price.pricePerPerson = Money("200.0", "USD")

        val airlines = ArrayList<Airline>()
        val airline1 = Airline("United", null)
        val airline2 = Airline("Delta", null)
        airlines.add(airline1)
        airlines.add(airline2)
        flightLeg.airlines = airlines
    }

    private fun createFlightLegWithLoyaltyInfo() {
        val earnInfo = PointsEarnInfo(100, 100, 100)
        val loyaltyInfo = LoyaltyInformation(null, LoyaltyEarnInfo(earnInfo, null), false)
        flightLeg.packageOfferModel.loyaltyInfo = loyaltyInfo
    }

    @Test
    fun testEarnMessageVisibility() {
        createExpectedFlightLeg()
        createFlightLegWithLoyaltyInfo()
        createSystemUnderTest()

        //If pos supports earnMessage
        PointOfSaleTestConfiguration.configurePointOfSale(RuntimeEnvironment.application, "MockSharedData/pos_with_flight_earn_messaging_enabled.json", false)
        var pos = PointOfSale.getPointOfSale()
        assertTrue(pos.isEarnMessageEnabledForPackages)
        assertFalse(sut.isEarnMessageVisible(""))
        assertTrue(sut.isEarnMessageVisible("Earn 103 points"))

        //If pos not supports earnMessage
        PointOfSaleTestConfiguration.configurePointOfSale(RuntimeEnvironment.application, "MockSharedData/pos_with_flight_earn_messaging_disabled.json", false)
        pos = PointOfSale.getPointOfSale()
        assertFalse(pos.isEarnMessageEnabledForPackages)
        assertFalse(sut.isEarnMessageVisible(""))
        assertFalse(sut.isEarnMessageVisible("Earn 103 points"))
    }

    private fun getContext(): Context {
        return RuntimeEnvironment.application
    }
}
