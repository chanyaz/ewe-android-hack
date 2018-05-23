package com.expedia.bookings.presenter.shared

import android.view.LayoutInflater
import android.view.View
import com.expedia.bookings.R
import com.expedia.bookings.data.Money
import com.expedia.bookings.data.abacus.AbacusUtils
import com.expedia.bookings.data.flights.FlightLeg
import com.expedia.bookings.data.flights.RichContent
import com.expedia.bookings.data.packages.PackageOfferModel
import com.expedia.bookings.services.TestObserver
import com.expedia.bookings.test.MultiBrand
import com.expedia.bookings.test.RunForBrands
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.utils.AbacusTestUtils
import com.expedia.bookings.utils.RichContentUtils
import com.expedia.vm.flights.FlightOverviewViewModel
import com.expedia.bookings.packages.vm.PackageFlightOverviewViewModel
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RuntimeEnvironment
import java.util.ArrayList
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

@RunWith(RobolectricRunner::class)
class FlightDetailsPresenterTest {

    val context = RuntimeEnvironment.application
    val BAGGAGE_FEES_URL_PATH = "BaggageFees"
    val FLIGHT_LEG_ID = "ab64aefca28e772ca024d4a00e6ae131"

    lateinit var sut: FlightDetailsPresenter
    lateinit var flightLeg: FlightLeg

    @Before
    fun setup() {
        sut = LayoutInflater.from(context).inflate(R.layout.test_flight_overview_presenter, null) as FlightDetailsPresenter
        sut.vm = FlightOverviewViewModel(context)
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun showBaggageFees() {
        val expectedUrl = "https://www.expedia.com/" + BAGGAGE_FEES_URL_PATH

        createSelectedFlightLeg()
        val testSubscriber = TestObserver<String>()
        sut.baggageFeeShowSubject.subscribe(testSubscriber)

        sut.showBaggageFeesButton.performClick()

        testSubscriber.assertValueCount(1)
        testSubscriber.assertValue(expectedUrl)
    }

    @Test
    fun selectFlightButton() {
        createSelectedFlightLeg()
        val testSubscriber = TestObserver<FlightLeg>()
        sut.vm.selectedFlightClickedSubject.subscribe(testSubscriber)

        sut.vm.selectFlightClickObserver.onNext(Unit)

        testSubscriber.assertValueCount(1)
        testSubscriber.assertValue(flightLeg)
    }

    @Test
    fun showDontShowBundlePrice() {
        sut.vm.showBundlePriceSubject.onNext(true)
        sut.vm.showEarnMessage.onNext(false)
        assertEquals(View.VISIBLE, sut.bundlePriceTextView.visibility)
        assertEquals(View.VISIBLE, sut.bundlePriceLabelTextView.visibility)
        assertEquals(View.GONE, sut.earnMessageTextView.visibility)

        sut.vm.showBundlePriceSubject.onNext(false)
        sut.vm.showEarnMessage.onNext(true)
        assertEquals(View.VISIBLE, sut.earnMessageTextView.visibility)
        assertEquals(View.VISIBLE, sut.bundlePriceTextView.visibility)
        assertEquals(View.GONE, sut.bundlePriceLabelTextView.visibility)

        sut.vm.showBundlePriceSubject.onNext(false)
        sut.vm.showEarnMessage.onNext(false)
        assertEquals(View.GONE, sut.bundlePriceTextView.visibility)
        assertEquals(View.GONE, sut.bundlePriceLabelTextView.visibility)
        assertEquals(View.GONE, sut.earnMessageTextView.visibility)
    }

    @Test
    fun showBasicEconomyTooltip() {
        sut.vm = FlightOverviewViewModel(context)
        val testSubscriber = TestObserver<Boolean>()
        sut.vm.showBasicEconomyTooltip.subscribe(testSubscriber)

        createSelectedFlightLeg()
        testSubscriber.assertValueCount(1)
        testSubscriber.assertValue(false)
        assertEquals(View.GONE, sut.basicEconomyTooltip.visibility)

        createBasicEconomyTooltipInfo()
        sut.vm.selectedFlightLegSubject.onNext(flightLeg)

        testSubscriber.assertValueCount(2)
        testSubscriber.assertValues(false, true)
        assertEquals(View.VISIBLE, sut.basicEconomyTooltip.visibility)
    }

    @Test
    fun basicEconomyTooltipDialogTest() {
        sut.vm = FlightOverviewViewModel(context)
        val toolTipRulesTestSubscriber = TestObserver<Array<String>>()
        val toolTipTitleTestSubscriber = TestObserver<String>()
        sut.basicEconomyToolTipInfoView.viewmodel.basicEconomyTooltipTitle.subscribe(toolTipTitleTestSubscriber)
        sut.basicEconomyToolTipInfoView.viewmodel.basicEconomyTooltipFareRules.subscribe(toolTipRulesTestSubscriber)

        createSelectedFlightLeg()
        createBasicEconomyTooltipInfo()
        sut.vm.selectedFlightLegSubject.onNext(flightLeg)
        assertEquals(2, toolTipRulesTestSubscriber.values()[0].size)
        assertEquals("1 personal item only, no access to overhead bin", toolTipRulesTestSubscriber.values()[0][0])
        assertEquals("Seats assigned at check-in.", toolTipRulesTestSubscriber.values()[0][1])
        assertEquals("United Airlines Basic Economy Fare", toolTipTitleTestSubscriber.values()[0])
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun testSelectedFlightRichContent() {
        AbacusTestUtils.bucketTestAndEnableRemoteFeature(context, AbacusUtils.EBAndroidAppFlightsRichContent, 3)
        sut.vm = FlightOverviewViewModel(context)
        createSelectedFlightLeg()
        assertEquals(View.VISIBLE, sut.flightMessageContainer.visibility)
        assertEquals(View.VISIBLE, sut.routeScoreText.visibility)
        assertEquals("7.9/10 - Very Good!", sut.routeScoreText.text)
        assertTrue(sut.vm.selectedFlightLegSubject.value.flightSegments[0].flightAmenities.wifi)
        assertFalse(sut.vm.selectedFlightLegSubject.value.flightSegments[0].flightAmenities.entertainment)
        assertTrue(sut.vm.selectedFlightLegSubject.value.flightSegments[0].flightAmenities.power)
    }

    private fun createSelectedFlightLeg() {
        flightLeg = FlightLeg()
        flightLeg.legId = FLIGHT_LEG_ID
        flightLeg.carrierName = "United Airlines"
        flightLeg.richContent = getRichContentRichContent()
        flightLeg.packageOfferModel = PackageOfferModel()
        flightLeg.packageOfferModel.price = PackageOfferModel.PackagePrice()
        flightLeg.packageOfferModel.price.pricePerPersonFormatted = "$42"
        flightLeg.packageOfferModel.price.averageTotalPricePerTicket = Money()
        flightLeg.packageOfferModel.price.averageTotalPricePerTicket.formattedPrice = "$42.00"
        flightLeg.packageOfferModel.price.averageTotalPricePerTicket = Money("42.00", "USD")
        flightLeg.baggageFeesUrl = BAGGAGE_FEES_URL_PATH
        flightLeg.flightSegments = listOf(createFlightSegment("coach"))
        flightLeg.richContent = getRichContent()
        sut.vm.selectedFlightLegSubject.onNext(flightLeg)
    }

    private fun createFlightSegment(seatClass: String): FlightLeg.FlightSegment {
        val airlineSegment = FlightLeg.FlightSegment()
        airlineSegment.flightNumber = "51"
        airlineSegment.airplaneType = "Airbus A320"
        airlineSegment.carrier = "United Airlines"
        airlineSegment.operatingAirlineCode = ""
        airlineSegment.operatingAirlineName = ""
        airlineSegment.departureDateTimeISO = ""
        airlineSegment.arrivalDateTimeISO = ""
        airlineSegment.departureCity = "San Francisco"
        airlineSegment.arrivalCity = "Honolulu"
        airlineSegment.departureAirportCode = "SFO"
        airlineSegment.arrivalAirportCode = "SEA"
        airlineSegment.durationHours = 2
        airlineSegment.durationMinutes = 2
        airlineSegment.layoverDurationHours = 0
        airlineSegment.layoverDurationMinutes = 0
        airlineSegment.elapsedDays = 0
        airlineSegment.seatClass = seatClass
        airlineSegment.bookingCode = "O"
        return airlineSegment
    }

    private fun getRichContent(): RichContent {
        val richContent = RichContent()
        richContent.legId = FLIGHT_LEG_ID
        richContent.score = 7.9F
        richContent.legAmenities = getFlightAmenities()
        richContent.scoreExpression = RichContentUtils.ScoreExpression.VERY_GOOD.name
        richContent.segmentAmenitiesList = listOf(getFlightAmenities())
        return richContent
    }

    private fun getFlightAmenities(): RichContent.RichContentAmenity {
        val flightAmenities = RichContent.RichContentAmenity()
        flightAmenities.wifi = true
        flightAmenities.entertainment = false
        flightAmenities.power = true
        return flightAmenities
    }

    private fun getRichContentRichContent(): RichContent {
        val richContent = RichContent()
        richContent.legId = FLIGHT_LEG_ID
        richContent.score = 7.9F
        richContent.scoreExpression = RichContentUtils.ScoreExpression.VERY_GOOD.name
        return richContent
    }

    private fun createBasicEconomyTooltipInfo() {
        flightLeg.isBasicEconomy = true
        val fareRules = arrayOf("1 personal item only, no access to overhead bin", "Seats assigned at check-in.")
        val toolTipInfo = FlightLeg.BasicEconomyTooltipInfo()
        toolTipInfo.fareRulesTitle = "United Airlines Basic Economy Fare"
        toolTipInfo.fareRules = fareRules
        flightLeg.basicEconomyTooltipInfo = ArrayList<FlightLeg.BasicEconomyTooltipInfo>()
        flightLeg.basicEconomyTooltipInfo.add(toolTipInfo)
    }

    @Test
    fun basicEconomyTooltipDialogTestForPackages() {
        sut.vm = PackageFlightOverviewViewModel(context)
        val toolTipRulesTestSubscriber = TestObserver<Array<String>>()
        val toolTipTitleTestSubscriber = TestObserver<String>()
        sut.basicEconomyToolTipInfoView.viewmodel.basicEconomyTooltipTitle.subscribe(toolTipTitleTestSubscriber)
        sut.basicEconomyToolTipInfoView.viewmodel.basicEconomyTooltipFareRules.subscribe(toolTipRulesTestSubscriber)
        createSelectedFlightLeg()
        createBasicEconomyRulesForPackages(getFareRules())
        sut.vm.selectedFlightLegSubject.onNext(flightLeg)
        sut.vm.basicEconomyMessagingToolTipInfo.onNext(sut.vm.convertTooltipInfo(flightLeg))
        assertEquals(2, toolTipRulesTestSubscriber.values()[0].size)
        assertEquals("Seats assigned after check-in.", toolTipRulesTestSubscriber.values()[0][0])
        assertEquals("Changes and refunds are not permitted.", toolTipRulesTestSubscriber.values()[0][1])
        assertEquals("United Airlines Basic Economy Fare", toolTipTitleTestSubscriber.values()[0])
        assertNotEquals(null, sut.basicEconomyTooltip.compoundDrawables[2])
    }

    @Test
    fun basicEconomyTooltipDialogTestForPackagesWithEmptyFareRules() {
        sut.vm = PackageFlightOverviewViewModel(context)
        val toolTipRulesTestSubscriber = TestObserver<Array<String>>()
        val toolTipTitleTestSubscriber = TestObserver<String>()
        sut.basicEconomyToolTipInfoView.viewmodel.basicEconomyTooltipTitle.subscribe(toolTipTitleTestSubscriber)
        sut.basicEconomyToolTipInfoView.viewmodel.basicEconomyTooltipFareRules.subscribe(toolTipRulesTestSubscriber)
        createSelectedFlightLeg()
        createBasicEconomyRulesForPackages(getEmptyFareRules())
        sut.vm.selectedFlightLegSubject.onNext(flightLeg)
        sut.vm.basicEconomyMessagingToolTipInfo.onNext(sut.vm.convertTooltipInfo(flightLeg))
        assertEquals(0, toolTipRulesTestSubscriber.values()[0].size)
        assertEquals(null, sut.basicEconomyTooltip.compoundDrawables[2])
    }

    private fun createBasicEconomyRulesForPackages(fareRules: List<String>) {
        flightLeg.isBasicEconomy = true
        flightLeg.basicEconomyRules = fareRules
    }

    private fun getFareRules(): List<String> = listOf<String>("Seats assigned after check-in.", "Changes and refunds are not permitted.")

    private fun getEmptyFareRules(): List<String> = listOf<String>()
}
