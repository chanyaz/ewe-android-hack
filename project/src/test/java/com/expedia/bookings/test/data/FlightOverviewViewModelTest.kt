package com.expedia.bookings.test.data

import com.expedia.bookings.data.Money
import com.expedia.bookings.data.flights.FlightLeg
import com.expedia.bookings.data.packages.PackageOfferModel
import com.expedia.bookings.services.TestObserver
import com.expedia.bookings.test.MultiBrand
import com.expedia.bookings.test.RunForBrands
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.utils.Ui
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RuntimeEnvironment
import java.math.BigDecimal
import kotlin.test.assertEquals
import com.expedia.vm.flights.FlightOverviewViewModel as FlightsOverviewViewModel
import com.expedia.vm.packages.FlightOverviewViewModel as PackagesOverviewViewModel

@RunWith(RobolectricRunner::class)
class FlightOverviewViewModelTest {
    private lateinit var sutFlight: FlightsOverviewViewModel
    private lateinit var sutPackages: PackagesOverviewViewModel
    private lateinit var flightLeg: FlightLeg
    private val context = RuntimeEnvironment.application

    @Before
    fun setup() {
        Ui.getApplication(context).defaultTravelerComponent()
        sutFlight = FlightsOverviewViewModel(context)
        sutPackages = PackagesOverviewViewModel(context)
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun testPricePerPersonMessagingFlights() {
        val urgencyMessagingTestSubscriber = TestObserver<String>()
        setupFlightLeg()
        sutFlight.urgencyMessagingSubject.subscribe(urgencyMessagingTestSubscriber)
        sutFlight.updateUrgencyMessage(flightLeg)
        assertEquals("$42.00 per person", urgencyMessagingTestSubscriber.values()[0])
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun testPricePerPersonMessagingPackages() {
        val urgencyMessagingTestSubscriber = TestObserver<String>()
        setupFlightLeg()
        sutPackages.urgencyMessagingSubject.subscribe(urgencyMessagingTestSubscriber)
        sutPackages.updateUrgencyMessage(flightLeg)
        assertEquals("+$77.00 per person", urgencyMessagingTestSubscriber.values()[0])
    }

    private fun setupFlightLeg() {
        flightLeg = FlightLeg()
        flightLeg.packageOfferModel = PackageOfferModel()
        flightLeg.packageOfferModel.urgencyMessage = PackageOfferModel.UrgencyMessage()
        flightLeg.packageOfferModel.price = PackageOfferModel.PackagePrice()
        flightLeg.packageOfferModel.price.differentialPriceFormatted = "$77.00"
        flightLeg.packageOfferModel.price.pricePerPersonFormatted = "$77.00"
        flightLeg.packageOfferModel.price.averageTotalPricePerTicket = Money()
        flightLeg.packageOfferModel.price.averageTotalPricePerTicket = Money("42.00", "USD")
        flightLeg.airlineMessageModel = FlightLeg.AirlineMessageModel()
        flightLeg.airlineMessageModel.hasAirlineWithCCfee = false
        flightLeg.airlineMessageModel.airlineFeeLink = "p/regulatory/obfees"
        flightLeg.packageOfferModel.price.deltaPositive = true
        flightLeg.packageOfferModel.price.deltaPrice = Money("4", "USD")
        flightLeg.packageOfferModel.price.deltaPrice.roundedAmount = BigDecimal("4")
    }
}
