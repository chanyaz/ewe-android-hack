package com.expedia.bookings.widget.packages

import android.view.View
import com.expedia.bookings.data.Money
import com.expedia.bookings.data.abacus.AbacusUtils
import com.expedia.bookings.data.flights.Airline
import com.expedia.bookings.data.flights.FlightLeg
import com.expedia.bookings.data.flights.RichContent
import com.expedia.bookings.data.packages.PackageOfferModel
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.utils.AbacusTestUtils
import com.expedia.bookings.utils.RichContentUtils
import com.expedia.vm.flights.FlightViewModel
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RuntimeEnvironment
import java.math.BigDecimal
import java.util.ArrayList
import kotlin.test.assertEquals

@RunWith(RobolectricRunner::class)
class FlightCellWidgetTest {

    private var context = RuntimeEnvironment.application
    private lateinit var sut: FlightCellWidget
    private lateinit var flightLeg: FlightLeg
    val BAGGAGE_FEES_URL_PATH = "BaggageFees"

    @Before
    fun setup() {
        sut = FlightCellWidget(context)
        createFlightLeg()
    }

    @Test
    fun testFlightLegRichContentForCompleteFeatureVariant() {
        AbacusTestUtils.bucketTestAndEnableRemoteFeature(context, AbacusUtils.EBAndroidAppFlightsRichContent)
        AbacusTestUtils.bucketTestWithVariant(AbacusUtils.EBAndroidAppFlightsRichContent, 3)
        sut.bind(FlightViewModel(context, flightLeg))

        assertEquals(View.VISIBLE, sut.richContentDividerView.visibility)
        assertEquals(View.VISIBLE, sut.richContentWifiView.visibility)
        assertEquals(View.GONE, sut.richContentEntertainmentView.visibility)
        assertEquals(View.VISIBLE, sut.richContentPowerView.visibility)
        assertEquals(View.VISIBLE, sut.routeScoreTextView.visibility)
        assertEquals("7.9/10 - Very Good!", sut.routeScoreTextView.text)
    }

    @Test
    fun testFlightLegRichContentForShowAmenitiesVariant() {
        AbacusTestUtils.bucketTestAndEnableRemoteFeature(context, AbacusUtils.EBAndroidAppFlightsRichContent)
        AbacusTestUtils.bucketTestWithVariant(AbacusUtils.EBAndroidAppFlightsRichContent, 1)
        sut.bind(FlightViewModel(context, flightLeg))

        assertEquals(View.VISIBLE, sut.richContentDividerView.visibility)
        assertEquals(View.VISIBLE, sut.richContentWifiView.visibility)
        assertEquals(View.GONE, sut.richContentEntertainmentView.visibility)
        assertEquals(View.VISIBLE, sut.richContentPowerView.visibility)
        assertEquals(View.VISIBLE, sut.routeScoreTextView.visibility)
        assertEquals("", sut.routeScoreTextView.text)
    }

    @Test
    fun testFlightLegRichContentForShowRouteScoreVariant() {
        AbacusTestUtils.bucketTestAndEnableRemoteFeature(context, AbacusUtils.EBAndroidAppFlightsRichContent)
        AbacusTestUtils.bucketTestWithVariant(AbacusUtils.EBAndroidAppFlightsRichContent, 2)
        sut.bind(FlightViewModel(context, flightLeg))

        assertEquals(View.GONE, sut.richContentDividerView.visibility)
        assertEquals(View.GONE, sut.richContentWifiView.visibility)
        assertEquals(View.GONE, sut.richContentEntertainmentView.visibility)
        assertEquals(View.GONE, sut.richContentPowerView.visibility)
        assertEquals(View.VISIBLE, sut.routeScoreTextView.visibility)
        assertEquals("7.9/10 - Very Good!", sut.routeScoreTextView.text)
    }

    private fun createFlightLeg() {
        flightLeg = FlightLeg()
        flightLeg.carrierName = "American Airlines"
        flightLeg.carrierLogoUrl = "https/AmericanAirline"
        flightLeg.elapsedDays = 1
        flightLeg.durationHour = 19
        flightLeg.durationMinute = 10
        flightLeg.flightSegments = ArrayList<FlightLeg.FlightSegment>()
        flightLeg.departureDateTimeISO = "2016-09-07T20:20:00.000-05:00"
        flightLeg.arrivalDateTimeISO = "2016-09-08T19:20:00.000+01:00"
        flightLeg.stopCount = 1
        flightLeg.packageOfferModel = PackageOfferModel()
        flightLeg.packageOfferModel.price = PackageOfferModel.PackagePrice()
        flightLeg.packageOfferModel.price.packageTotalPrice = Money("200", "USD")
        flightLeg.packageOfferModel.price.averageTotalPricePerTicket = Money("200", "USD")
        flightLeg.packageOfferModel.price.averageTotalPricePerTicket.roundedAmount = BigDecimal(200)
        flightLeg.baggageFeesUrl = BAGGAGE_FEES_URL_PATH
        flightLeg.richContent = getRichContent()

        val airlines = ArrayList<Airline>()
        val airline1 = Airline("United", null)
        val airline2 = Airline("Delta", null)
        airlines.add(airline1)
        airlines.add(airline2)
        flightLeg.airlines = airlines
    }

    private fun getRichContent(): RichContent {
        val richContent = RichContent()
        richContent.legId = ""
        richContent.score = 7.9F
        richContent.legAmenities = getRichContentAmenities()
        richContent.scoreExpression = RichContentUtils.ScoreExpression.VERY_GOOD.name
        richContent.segmentAmenitiesList = listOf(getRichContentAmenities())
        return richContent
    }

    private fun getRichContentAmenities(): RichContent.RichContentAmenity {
        val amenities = RichContent.RichContentAmenity()
        amenities.wifi = true
        amenities.entertainment = false
        amenities.power = true
        return amenities
    }
}
