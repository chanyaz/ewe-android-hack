package com.expedia.bookings.test.robolectric

import android.view.View
import android.widget.FrameLayout
import com.expedia.bookings.data.Money
import com.expedia.bookings.data.abacus.AbacusUtils
import com.expedia.bookings.data.flights.Airline
import com.expedia.bookings.data.flights.FlightLeg
import com.expedia.bookings.data.flights.FlightServiceClassType
import com.expedia.bookings.data.packages.PackageOfferModel
import com.expedia.bookings.data.pos.PointOfSale
import com.expedia.bookings.data.pos.PointOfSaleId
import com.expedia.bookings.test.MultiBrand
import com.expedia.bookings.test.PointOfSaleTestConfiguration
import com.expedia.bookings.test.RunForBrands
import com.expedia.bookings.utils.AbacusTestUtils
import com.expedia.bookings.widget.flights.FlightListAdapter
import com.expedia.bookings.widget.shared.AbstractFlightListAdapter
import com.expedia.ui.FlightActivity
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import rx.subjects.BehaviorSubject
import rx.subjects.PublishSubject
import java.math.BigDecimal
import java.util.ArrayList
import kotlin.properties.Delegates
import kotlin.test.assertEquals

@RunWith(RobolectricRunner::class)
class FlightListAdapterTest {

    val activity = Robolectric.buildActivity(FlightActivity::class.java).create().get()
    lateinit var sut: FlightListAdapter
    lateinit var flightSelectedSubject: PublishSubject<FlightLeg>
    lateinit var isRoundTripSubject: BehaviorSubject<Boolean>
    lateinit var flightCabinClassSubject: BehaviorSubject<String>
    var isOutboundSearch: Boolean by Delegates.notNull<Boolean>()
    lateinit var isNonStopSubject: BehaviorSubject<Boolean>
    lateinit var isRefundableSubject: BehaviorSubject<Boolean>
    lateinit var flightLeg: FlightLeg

    @Before
    fun setup() {
        flightSelectedSubject = PublishSubject.create<FlightLeg>()
        isRoundTripSubject = BehaviorSubject.create<Boolean>()
        isRoundTripSubject.onNext(false)
        isNonStopSubject = BehaviorSubject.create(false)
        isRefundableSubject = BehaviorSubject.create(false)
        flightCabinClassSubject = BehaviorSubject.create()
        flightCabinClassSubject.onNext(FlightServiceClassType.CabinCode.COACH.name)
        isOutboundSearch = false
    }

    fun createSystemUnderTest() {
        sut = FlightListAdapter(activity, flightSelectedSubject, isRoundTripSubject, isOutboundSearch, flightCabinClassSubject, isNonStopSubject, isRefundableSubject)
    }

    @Test
    fun allFlightsHeaderNotShownForFlightsLOB() {
        sut = FlightListAdapter(activity, flightSelectedSubject, isRoundTripSubject, isOutboundSearch, flightCabinClassSubject, isNonStopSubject, isRefundableSubject)
        sut.setNewFlights(emptyList())

        val itemViewType = sut.getItemViewType(1)
        assertEquals(AbstractFlightListAdapter.ViewTypes.FLIGHT_CELL_VIEW.ordinal, itemViewType)
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun flightResultsHeaderRoundTripForDeltaPricing() {
        RoboTestHelper.bucketTests(AbacusUtils.EBAndroidAppFlightsDeltaPricing)
        createSystemUnderTest()
        givenRoundTripFlight()
        isOutboundSearch = false
        val headerViewHolder = createHeaderViewHolder()
        sut.onBindViewHolder(headerViewHolder, 0)
        assertEquals("Additional price per person for inbound flight", headerViewHolder.priceHeader.text)
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun flightResultsHeaderRoundTripForDeltaPricingInAUPOS() {
        RoboTestHelper.bucketTests(AbacusUtils.EBAndroidAppFlightsDeltaPricing)
        val initialPOSID = PointOfSale.getPointOfSale().pointOfSaleId
        setPointOfSale(PointOfSaleId.AUSTRALIA)
        createSystemUnderTest()
        givenRoundTripFlight()
        isOutboundSearch = false
        val headerViewHolder = createHeaderViewHolder()
        sut.onBindViewHolder(headerViewHolder, 0)
        assertEquals("Additional price per person for inbound flight", headerViewHolder.priceHeader.text)
        setPointOfSale(initialPOSID)
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA, MultiBrand.ORBITZ))
    fun flightResultsHeaderRoundTrip() {
        createSystemUnderTest()
        givenRoundTripFlight()
        val headerViewHolder = createHeaderViewHolder()
        sut.onBindViewHolder(headerViewHolder, 0)
        assertEquals("Prices roundtrip, per person.", headerViewHolder.priceHeader.text)
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA, MultiBrand.ORBITZ))
    fun flightResultsHeaderOneWay() {
        createSystemUnderTest()
        givenOneWayFlight()
        val headerViewHolder = createHeaderViewHolder()
        sut.onBindViewHolder(headerViewHolder, 0)
        assertEquals("Prices one-way, per person.", headerViewHolder.priceHeader.text)
    }

    @Test
    fun flightResultsHeaderOneWayMinPrice() {
        configurePointOfSale()
        createSystemUnderTest()
        givenOneWayFlight()
        val headerViewHolder = createHeaderViewHolder()
        sut.onBindViewHolder(headerViewHolder, 0)
        assertEquals("Prices one-way, per person, from", headerViewHolder.priceHeader.text)
    }

    @Test
    fun flightResultsHeaderReturnMinPrice() {
        RoboTestHelper.controlTests(AbacusUtils.EBAndroidAppFlightsDeltaPricing)
        configurePointOfSale()
        createSystemUnderTest()
        givenRoundTripFlight()
        val headerViewHolder = createHeaderViewHolder()
        sut.onBindViewHolder(headerViewHolder, 0)
        assertEquals("Prices roundtrip, per person, from", headerViewHolder.priceHeader.text)
    }

    @Test
    fun flightResultsAdvanceSearchFilterHeader() {
        AbacusTestUtils.bucketTests(AbacusUtils.EBAndroidAppFlightAdvanceSearch)
        isRoundTripSubject.onNext(true)
        createSystemUnderTest()
        val headerViewHolder = createHeaderViewHolder()
        //When Non Stop and Refundable both are not chosen
        assertEquals(View.GONE, headerViewHolder.advanceSearchFilterHeader.visibility)
        //When User searches with Non Stop filter
        isNonStopSubject.onNext(true)
        sut.onBindViewHolder(headerViewHolder, 0)
        assertEquals(View.VISIBLE, headerViewHolder.advanceSearchFilterHeader.visibility)
        val shouldAdjustPricing = PointOfSale.getPointOfSale().shouldAdjustPricingMessagingForAirlinePaymentMethodFee()
        var expectedHeader: String
        if (shouldAdjustPricing) {
            expectedHeader = "Showing nonstop flights. Prices roundtrip, per person, from"
        } else {
            expectedHeader = "Showing nonstop flights. Prices roundtrip, per person."
        }
        assertEquals(expectedHeader, headerViewHolder.advanceSearchFilterHeader.text.toString())
        assertEquals(expectedHeader, headerViewHolder.advanceSearchFilterHeader.contentDescription.toString())

        //When User searches with Refundable and Non Stop filter
        if (shouldAdjustPricing) {
            expectedHeader = "Showing nonstop and refundable flights. Prices roundtrip, per person, from"
        } else {
            expectedHeader = "Showing nonstop and refundable flights. Prices roundtrip, per person."
        }
        isRefundableSubject.onNext(true)
        sut.onBindViewHolder(headerViewHolder, 0)
        assertEquals(View.VISIBLE, headerViewHolder.advanceSearchFilterHeader.visibility)
        assertEquals(expectedHeader, headerViewHolder.advanceSearchFilterHeader.text.toString())
        assertEquals(expectedHeader, headerViewHolder.advanceSearchFilterHeader.contentDescription.toString())

        //When User searches with Refundable filter
        if (shouldAdjustPricing) {
            expectedHeader = "Showing refundable flights. Prices roundtrip, per person, from"
        } else {
            expectedHeader = "Showing refundable flights. Prices roundtrip, per person."
        }
        isNonStopSubject.onNext(false)
        sut.onBindViewHolder(headerViewHolder, 0)
        assertEquals(View.VISIBLE, headerViewHolder.advanceSearchFilterHeader.visibility)
        assertEquals(expectedHeader, headerViewHolder.advanceSearchFilterHeader.text.toString())
        assertEquals(expectedHeader, headerViewHolder.advanceSearchFilterHeader.contentDescription.toString())
    }

    @Test
    fun getFlightViewModel() {
        createSystemUnderTest()
        createExpectedFlightLeg()
        val flightViewModel = sut.makeFlightViewModel(activity, flightLeg)
        assertEquals(flightLeg, flightViewModel.layover)
    }

    @Test
    fun adjustPosition() {
        createSystemUnderTest()
        assertEquals(1, sut.adjustPosition())
    }

    private fun givenOneWayFlight() {
        isRoundTripSubject.onNext(false)
    }

    private fun givenRoundTripFlight() {
        isRoundTripSubject.onNext(true)
    }

    private fun configurePointOfSale() {
        PointOfSaleTestConfiguration.configurePointOfSale(activity, "MockSharedData/pos_with_airline_payment_fees.json")
    }

    private fun createHeaderViewHolder(): AbstractFlightListAdapter.HeaderViewHolder {
        return sut.onCreateViewHolder(FrameLayout(activity), AbstractFlightListAdapter.ViewTypes.PRICING_STRUCTURE_HEADER_VIEW.ordinal) as AbstractFlightListAdapter.HeaderViewHolder
    }

    @Test
    fun testAdjustPositionShowingPackageBanner() {
        AbacusTestUtils.bucketTests(AbacusUtils.EBAndroidAppFlightsCrossSellPackageOnFSR)
        PointOfSaleTestConfiguration.configurePointOfSale(activity, "MockSharedData/pos_test_config.json")
        isRoundTripSubject.onNext(true)
        isOutboundSearch = true
        createSystemUnderTest()
        assertEquals(2, sut.adjustPosition())
    }

    private fun createExpectedFlightLeg() {
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
        flightLeg.packageOfferModel.price.averageTotalPricePerTicket = Money("200.0", "USD")
        flightLeg.packageOfferModel.price.averageTotalPricePerTicket.roundedAmount = BigDecimal(200)
        flightLeg.packageOfferModel.price.pricePerPerson = Money("200.0", "USD")

        val airlines = ArrayList<Airline>()
        val airline1 = Airline("United", null)
        val airline2 = Airline("Delta", null)
        airlines.add(airline1)
        airlines.add(airline2)
        flightLeg.airlines = airlines
    }

    private fun setPointOfSale(posId: PointOfSaleId) {
        PointOfSaleTestConfiguration.configurePOS(activity, "ExpediaSharedData/ExpediaPointOfSaleConfig.json", Integer.toString(posId.id), false)
    }
}
