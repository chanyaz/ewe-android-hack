package com.expedia.vm.test.robolectric

import android.content.Context
import com.expedia.bookings.R
import com.expedia.bookings.data.Money
import com.expedia.bookings.data.abacus.AbacusUtils
import com.expedia.bookings.data.flights.Airline
import com.expedia.bookings.data.flights.FlightLeg
import com.expedia.bookings.data.packages.PackageOfferModel
import com.expedia.bookings.data.pos.PointOfSale
import com.expedia.bookings.data.pos.PointOfSaleId
import com.expedia.bookings.test.MultiBrand
import com.expedia.bookings.test.RunForBrands
import com.expedia.bookings.test.robolectric.RoboTestHelper
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.utils.SpannableBuilder
import com.expedia.vm.flights.FlightViewModel
import com.mobiata.android.util.SettingUtils
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RuntimeEnvironment
import java.math.BigDecimal
import java.util.ArrayList
import kotlin.test.assertEquals

@RunWith(RobolectricRunner::class)
class FlightViewModelTest {

    lateinit private var sut: FlightViewModel
    lateinit var flightLeg: FlightLeg
    val context = RuntimeEnvironment.application

    fun createSystemUnderTest() {
        sut = FlightViewModel(getContext(), flightLeg)
    }

    fun createExpectedFlightLeg(roundUp: Boolean = false) {
        flightLeg = FlightLeg()
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
        flightLeg.packageOfferModel.price.averageTotalPricePerTicket = Money("200.4", "USD")
        flightLeg.packageOfferModel.price.averageTotalPricePerTicket.roundedAmount = if (roundUp) BigDecimal(201) else BigDecimal(200)
        flightLeg.packageOfferModel.price.pricePerPerson = Money("200.0", "USD")


        val airlines = ArrayList<Airline>()
        val airline1 = Airline("United", null)
        val airline2 = Airline("Delta", null)
        airlines.add(airline1)
        airlines.add(airline2)
        flightLeg.airlines = airlines
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA, MultiBrand.ORBITZ, MultiBrand.CHEAPTICKETS, MultiBrand.TRAVELOCITY))
    fun priceString() {
        createExpectedFlightLeg()
        createSystemUnderTest()
        assertEquals("$200", sut.price())
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun priceStringForFrance() {
        setPOS(PointOfSaleId.FRANCE)
        createExpectedFlightLeg(true)
        createSystemUnderTest()
        assertEquals("$201", sut.price())
    }

    private fun getContext(): Context {
        return RuntimeEnvironment.application
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA, MultiBrand.ORBITZ, MultiBrand.CHEAPTICKETS, MultiBrand.TRAVELOCITY))
    fun testFlightOfferContentDescription() {
        createExpectedFlightLeg()
        createSystemUnderTest()
        val expectedResult = SpannableBuilder()
        expectedResult.append("Flight time is 01:10:00 to 12:20:00 plus 1d with price $200. Flying with UnitedDelta. The flight duration is 19 hours 10 minutes with 1 stops\u0020Button")
        assertEquals(expectedResult.build(), sut.getFlightContentDesc(false))
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun testDeltaPrice() {
        RoboTestHelper.bucketTests(AbacusUtils.EBAndroidAppFlightsDeltaPricing)
        createExpectedFlightLeg(true)
        flightLeg.packageOfferModel.price.deltaPositive = true
        flightLeg.packageOfferModel.price.deltaPrice = Money("4", "USD")
        flightLeg.packageOfferModel.price.deltaPrice.roundedAmount = BigDecimal("4")
        sut = FlightViewModel(getContext(), flightLeg, false)
        assertEquals("+ $4", sut.price())

        sut = FlightViewModel(getContext(), flightLeg, true)
        assertEquals("$201", sut.price())
    }

    private fun setPOS(pos: PointOfSaleId) {
        SettingUtils.save(getContext(), R.string.PointOfSaleKey, pos.id.toString())
        PointOfSale.onPointOfSaleChanged(getContext())
    }
}
