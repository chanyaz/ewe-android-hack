package com.expedia.bookings.test.robolectric

import android.widget.FrameLayout
import com.expedia.bookings.data.Money
import com.expedia.bookings.data.abacus.AbacusUtils
import com.expedia.bookings.data.flights.Airline
import com.expedia.bookings.data.flights.FlightLeg
import com.expedia.bookings.data.flights.FlightServiceClassType
import com.expedia.bookings.data.packages.PackageOfferModel
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
    var isOutboundSearch: Boolean by Delegates.notNull<Boolean>()
    lateinit var flightCabinClass: String
    lateinit var flightLeg: FlightLeg

    @Before
    fun setup() {
        flightSelectedSubject = PublishSubject.create<FlightLeg>()
        isRoundTripSubject = BehaviorSubject.create<Boolean>()
        isRoundTripSubject.onNext(false)
        isOutboundSearch = false
        flightCabinClass = FlightServiceClassType.CabinCode.COACH.name
    }

    fun createSystemUnderTest() {
        sut = FlightListAdapter(activity, flightSelectedSubject, isRoundTripSubject, isOutboundSearch, flightCabinClass)
    }

    @Test
    fun allFlightsHeaderNotShownForFlightsLOB() {
        sut = FlightListAdapter(activity, flightSelectedSubject, isRoundTripSubject, isOutboundSearch, flightCabinClass)
        sut.setNewFlights(emptyList())

        val itemViewType = sut.getItemViewType(1)
        assertEquals(AbstractFlightListAdapter.ViewTypes.FLIGHT_CELL_VIEW.ordinal, itemViewType)
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA, MultiBrand.ORBITZ))
    fun flightResultsHeaderRoundTrip() {
        createSystemUnderTest()
        givenRoundTripFlight()
        val headerViewHolder = createHeaderViewHolder()
        sut.onBindViewHolder(headerViewHolder, 0)
        assertEquals("Prices roundtrip per person", headerViewHolder.title.text)
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA, MultiBrand.ORBITZ))
    fun flightResultsHeaderOneWay() {
        createSystemUnderTest()
        givenOneWayFlight()
        val headerViewHolder = createHeaderViewHolder()
        sut.onBindViewHolder(headerViewHolder, 0)
        assertEquals("Prices one-way per person", headerViewHolder.title.text)
    }

    @Test
    fun flightResultsHeaderOneWayMinPrice() {
        configurePointOfSale()
        createSystemUnderTest()
        givenOneWayFlight()
        val headerViewHolder = createHeaderViewHolder()
        sut.onBindViewHolder(headerViewHolder, 0)
        assertEquals("Prices one-way, per person, from", headerViewHolder.title.text)
    }

    @Test
    fun flightResultsHeaderReturnMinPrice() {
        configurePointOfSale()
        createSystemUnderTest()
        givenRoundTripFlight()
        val headerViewHolder = createHeaderViewHolder()
        sut.onBindViewHolder(headerViewHolder, 0)
        assertEquals("Prices roundtrip, per person, from", headerViewHolder.title.text)
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
        flightCabinClass = FlightServiceClassType.CabinCode.COACH.name
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
}
