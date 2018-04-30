package com.expedia.bookings.test.robolectric

import android.content.Context
import android.view.View
import android.widget.FrameLayout
import com.expedia.bookings.R
import com.expedia.bookings.data.Money
import com.expedia.bookings.data.abacus.AbacusUtils
import com.expedia.bookings.data.flights.Airline
import com.expedia.bookings.data.flights.FlightLeg
import com.expedia.bookings.data.flights.FlightServiceClassType
import com.expedia.bookings.data.flights.FlightTripDetails
import com.expedia.bookings.data.packages.PackageOfferModel
import com.expedia.bookings.data.payment.LoyaltyEarnInfo
import com.expedia.bookings.data.payment.LoyaltyInformation
import com.expedia.bookings.data.payment.PointsEarnInfo
import com.expedia.bookings.data.payment.PriceEarnInfo
import com.expedia.bookings.data.pos.PointOfSale
import com.expedia.bookings.test.MultiBrand
import com.expedia.bookings.test.PointOfSaleTestConfiguration
import com.expedia.bookings.test.RunForBrands
import com.expedia.bookings.utils.AbacusTestUtils
import com.expedia.bookings.widget.TextView
import com.expedia.bookings.widget.flights.FlightListAdapter
import com.expedia.bookings.widget.packages.FlightAirlineWidget
import com.expedia.bookings.widget.shared.AbstractFlightListAdapter
import com.expedia.ui.FlightActivity
import com.expedia.vm.AbstractFlightViewModel
import com.expedia.vm.flights.FlightViewModel
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RuntimeEnvironment
import java.math.BigDecimal
import java.util.ArrayList
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@RunWith(RobolectricRunner::class)
class AbstractFlightListAdapterTest {

    val activity: FlightActivity = Robolectric.buildActivity(FlightActivity::class.java).create().get()
    lateinit var sut: AbstractFlightListAdapter
    private lateinit var flightSelectedSubject: PublishSubject<FlightLeg>
    private lateinit var isRoundTripSubject: BehaviorSubject<Boolean>
    private lateinit var flightCabinClassSubject: BehaviorSubject<String>
    private lateinit var isNonStopSubject: BehaviorSubject<Boolean>
    private lateinit var isRefundableSubject: BehaviorSubject<Boolean>
    lateinit var flightLeg: FlightLeg

    @Before
    fun setup() {
        flightSelectedSubject = PublishSubject.create<FlightLeg>()
        isRoundTripSubject = BehaviorSubject.create()
        isNonStopSubject = BehaviorSubject.createDefault(false)
        isRefundableSubject = BehaviorSubject.createDefault(false)
        flightCabinClassSubject = BehaviorSubject.create()
        PointOfSaleTestConfiguration.configurePointOfSale(RuntimeEnvironment.application, "MockSharedData/pos_with_flight_earn_messaging_disabled.json", false)
    }

    private fun createTestFlightListAdapter() {
        isRoundTripSubject.onNext(false)
        flightCabinClassSubject.onNext(FlightServiceClassType.CabinCode.COACH.name)
        sut = TestFlightListAdapter(activity, flightSelectedSubject, isRoundTripSubject)
    }

    private fun activatePackageBannerWidget() {
        AbacusTestUtils.bucketTests(AbacusUtils.EBAndroidAppFlightsCrossSellPackageOnFSR)
        PointOfSaleTestConfiguration.configurePointOfSale(activity, "MockSharedData/pos_test_config.json")
        isRoundTripSubject.onNext(true)
        flightCabinClassSubject.onNext(FlightServiceClassType.CabinCode.COACH.name)
        sut = FlightListAdapter(activity, flightSelectedSubject, isRoundTripSubject, true, flightCabinClassSubject, isNonStopSubject, isRefundableSubject)
        sut.adjustPosition()
        createFlightLegWithThreeAirlines()
        sut.setNewFlights(listOf(flightLeg))
    }

    private fun preProcessForPackageBannerWidget() {
        AbacusTestUtils.bucketTests(AbacusUtils.EBAndroidAppFlightsCrossSellPackageOnFSR)
        PointOfSaleTestConfiguration.configurePointOfSale(activity, "MockSharedData/pos_test_config.json")
        isRoundTripSubject.onNext(true)
    }

    private fun postProcessForPackageBannerWidget() {
        sut.adjustPosition()
        createFlightLegWithThreeAirlines()
        sut.setNewFlights(listOf(flightLeg))
    }

    @Test
    fun testPackageBannerWidgetVisibilityForOneway() {
        preProcessForPackageBannerWidget()
        isRoundTripSubject.onNext(false)
        sut = FlightListAdapter(activity, flightSelectedSubject, isRoundTripSubject, true, flightCabinClassSubject, isNonStopSubject, isRefundableSubject)
        postProcessForPackageBannerWidget()
        assertEquals(sut.getItemViewType(0), AbstractFlightListAdapter.ViewTypes.PRICING_STRUCTURE_HEADER_VIEW.ordinal)
    }

    @Test
    fun testPackageBannerWidgetVisibilityWithoutFlagInPOS() {
        preProcessForPackageBannerWidget()
        PointOfSaleTestConfiguration.configurePointOfSale(activity, "MockSharedData/pos_locale_test_config.json")
        sut = FlightListAdapter(activity, flightSelectedSubject, isRoundTripSubject, true, flightCabinClassSubject, isNonStopSubject, isRefundableSubject)
        postProcessForPackageBannerWidget()
        assertEquals(sut.getItemViewType(0), AbstractFlightListAdapter.ViewTypes.PRICING_STRUCTURE_HEADER_VIEW.ordinal)
    }

    @Test
    fun testPackageBannerWidgetVisibilityForFirstClassCabinPreference() {
        preProcessForPackageBannerWidget()
        flightCabinClassSubject.onNext(FlightServiceClassType.CabinCode.FIRST.name)
        sut = FlightListAdapter(activity, flightSelectedSubject, isRoundTripSubject, true, flightCabinClassSubject, isNonStopSubject, isRefundableSubject)
        postProcessForPackageBannerWidget()
        assertEquals(sut.getItemViewType(0), AbstractFlightListAdapter.ViewTypes.PRICING_STRUCTURE_HEADER_VIEW.ordinal)
    }

    @Test
    fun testPackageBannerWidgetVisibility() {
        activatePackageBannerWidget()

        assertEquals(sut.getItemViewType(1), AbstractFlightListAdapter.ViewTypes.PACKAGE_BANNER_VIEW.ordinal)

        val packageBannerHeaderViewHolder = sut.onCreateViewHolder(FrameLayout(activity), AbstractFlightListAdapter.ViewTypes.PACKAGE_BANNER_VIEW.ordinal)
                as AbstractFlightListAdapter.PackageBannerHeaderViewHolder

        assertEquals(View.VISIBLE, packageBannerHeaderViewHolder.packageBannerWidget.visibility)

        val packageBannerTitle = packageBannerHeaderViewHolder.packageBannerWidget.findViewById<TextView>(R.id.package_flight_banner_title)
        assertEquals("Hotel + Flight", packageBannerTitle.text)

        val packageBannerDescription = packageBannerHeaderViewHolder.packageBannerWidget.findViewById<TextView>(R.id.package_flight_banner_description)
        assertEquals("Save when you book your flights and hotels together", packageBannerDescription.text)
    }

    @Test
    fun testMoreThanThreeCarriersResultsInMultipleCarriersAirlineView() {
        createTestFlightListAdapter()
        createFlightLegWithFourAirlines()

        val flightViewModel = sut.makeFlightViewModel(activity, flightLeg)
        val flightViewHolder = createFlightViewHolder()
        flightViewHolder.bind(flightViewModel)

        assert(flightViewHolder.flightCell.flightAirlineWidget.childCount == 1)

        val airlineView = flightViewHolder.flightCell.flightAirlineWidget.getChildAt(0) as FlightAirlineWidget.AirlineView
        assertEquals(airlineView.airlineName.text, "Multiple Carriers")
    }

    @Test
    fun testFlightEarnMessageVisibility() {
        createTestFlightListAdapter()
        val flightViewHolder = createFlightViewHolder()
        assertEquals(flightViewHolder.flightCell.flightEarnMessage.text, "")
    }

    @Test
    fun testThreeCarriersResultsInThreeAirlineViews() {
        createTestFlightListAdapter()
        createFlightLegWithThreeAirlines()

        val flightViewModel = sut.makeFlightViewModel(activity, flightLeg)
        val flightViewHolder = createFlightViewHolder()
        flightViewHolder.bind(flightViewModel)

        assertEquals(flightViewHolder.flightCell.flightAirlineWidget.childCount, 3)

        val airlineView1 = flightViewHolder.flightCell.flightAirlineWidget.getChildAt(0) as FlightAirlineWidget.AirlineView
        val airlineView2 = flightViewHolder.flightCell.flightAirlineWidget.getChildAt(1) as FlightAirlineWidget.AirlineView
        val airlineView3 = flightViewHolder.flightCell.flightAirlineWidget.getChildAt(2) as FlightAirlineWidget.AirlineView

        assertEquals(airlineView1.airlineName.text, "United")
        assertEquals(airlineView2.airlineName.text, "Delta")
        assertEquals(airlineView3.airlineName.text, "Korean Air")
    }

    @Test
    fun testSeatsLeftUrgencyMessage() {
        createTestFlightListAdapter()

        //When seatsLeftUrgencyMessage are less than 6
        createFlightLegWithUrgencyMessage(4)
        var flightViewHolder = bindFlightViewHolderAndModel()

        assertEquals(flightViewHolder.flightCell.flightMessageContainer.visibility, View.VISIBLE)
        assertEquals(flightViewHolder.flightCell.urgencyMessageTextView.text, "4 left at this price")

        //When seatsLeftUrgencyMessage are more than 6
        createFlightLegWithUrgencyMessage(8)
        flightViewHolder = bindFlightViewHolderAndModel()
        assertEquals(flightViewHolder.flightCell.flightMessageContainer.visibility, View.GONE)
    }

    @Test
    fun testFlightCabinCodeVisibilityWhenBucketedForABTest() {
        createTestFlightListAdapter()
        createFlightClass(true)

        var flightViewHolder = bindFlightViewHolderAndModel()
        assertEquals(flightViewHolder.flightCell.flightCabinCodeTextView.visibility, View.VISIBLE)
        assertEquals(flightViewHolder.flightCell.flightCabinCodeTextView.text, "Premium Economy")

        // Does not have Flight Class
        createFlightClass(false)
        flightViewHolder = bindFlightViewHolderAndModel()
        assertEquals(flightViewHolder.flightCell.flightCabinCodeTextView.visibility, View.GONE)
    }

    @Test
    fun testEarnMessagingForDifferentPos() {
        createTestFlightListAdapter()
        createFlightLegWithThreeAirlines()

        //If pos not supports earn messaging then earn message is not visible
        var pos = PointOfSale.getPointOfSale()
        assertFalse(pos.isEarnMessageEnabledForFlights)
        var flightViewHolder = bindFlightViewHolderAndModel()
        assertEquals(flightViewHolder.flightCell.flightEarnMessage.visibility, View.GONE)

        PointOfSaleTestConfiguration.configurePointOfSale(RuntimeEnvironment.application, "MockSharedData/pos_with_flight_earn_messaging_enabled.json", false)
        pos = PointOfSale.getPointOfSale()
        assertTrue(pos.isEarnMessageEnabledForFlights)

        //If it is a round trip with pos supporting earn messaging, then earn message is visible below round trip text view
        flightViewHolder = bindFlightViewHolderAndModel()
        assertEquals(flightViewHolder.flightCell.flightEarnMessage.visibility, View.VISIBLE)
        assertEquals(flightViewHolder.flightCell.flightEarnMessage.text, "Earn 100 points")
    }

    @Test
    @RunForBrands(brands = [(MultiBrand.EXPEDIA), (MultiBrand.ORBITZ), (MultiBrand.CHEAPTICKETS), (MultiBrand.TRAVELOCITY)])
    fun testEarnMessageForMoney() {
        createTestFlightListAdapter()
        createFlightLegWithThreeAirlines(true, "50")

        PointOfSaleTestConfiguration.configurePointOfSale(RuntimeEnvironment.application, "MockSharedData/pos_with_flight_earn_messaging_enabled.json", false)
        val pos = PointOfSale.getPointOfSale()
        assertTrue(pos.isEarnMessageEnabledForFlights)

        //If it is a one way trip with pos supporting earn messaging, then earn message is visible in place of round trip text view
        isRoundTripSubject.onNext(false)
        val flightViewHolder = bindFlightViewHolderAndModel()
        assertEquals(flightViewHolder.flightCell.flightEarnMessage.visibility, View.VISIBLE)
        // This will be tested for MB against different currency eg Orbucks
        assertEquals(flightViewHolder.flightCell.flightEarnMessage.text, "Earn $50")
    }

    @Test
    fun testAirlineWidgetTextWithEarnMessagingAndRoundTripWithControlledTest() {
        createTestFlightListAdapter()

        PointOfSaleTestConfiguration.configurePointOfSale(RuntimeEnvironment.application, "MockSharedData/pos_with_flight_earn_messaging_enabled.json", false)
        //If it is a round trip with pos supporting earn messaging with one airline, then airline Text View will not show Multiple Carriers for more than 2 airlines
        isRoundTripSubject.onNext(true)
        createFlightLegWithOneAirline()
        val flightViewHolder = bindFlightViewHolderAndModel()
        val airlineView = flightViewHolder.flightCell.flightAirlineWidget.getChildAt(0) as FlightAirlineWidget.AirlineView
        assert(flightViewHolder.flightCell.flightAirlineWidget.childCount == 1)
        assertEquals(airlineView.airlineName.text, "United")
    }

    private fun bindFlightViewHolderAndModel(): AbstractFlightListAdapter.FlightViewHolder {
        val flightViewModel = sut.makeFlightViewModel(activity, flightLeg)
        val flightViewHolder = createFlightViewHolder()
        flightViewHolder.bind(flightViewModel)
        return flightViewHolder
    }

    private fun createFlightLeg(isCurrencyTypeMoney: Boolean, price: String) {
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
        val earnInfo = PointsEarnInfo(100, 100, 100)

        val loyaltyInfo = if (!isCurrencyTypeMoney) LoyaltyInformation(null, LoyaltyEarnInfo(earnInfo, null), false) else LoyaltyInformation(null, LoyaltyEarnInfo(null, PriceEarnInfo(Money(price, "USD"), Money("0", "USD"), Money(price, "USD"))), true)

        flightLeg.packageOfferModel.loyaltyInfo = loyaltyInfo
    }

    private fun createFlightLeg() {
        createFlightLeg(false, "")
    }

    private fun createFlightLegWithUrgencyMessage(seatsLeft: Int) {
        createFlightLegWithThreeAirlines()
        val urgencyMessage = PackageOfferModel.UrgencyMessage()
        urgencyMessage.ticketsLeft = seatsLeft
        flightLeg.packageOfferModel.urgencyMessage = urgencyMessage
    }

    private fun createFlightLegWithFourAirlines() {
        createFlightLeg()

        val airlines = ArrayList<Airline>()
        val airline1 = Airline("United", null)
        val airline2 = Airline("Delta", null)
        val airline3 = Airline("Korean Air", null)
        val airline4 = Airline("Asiana", null)

        val segments = ArrayList<FlightLeg.FlightSegment>()
        val airlineSegment1 = createFlightSegment("U", "America", "America")
        val airlineSegment2 = createFlightSegment("D", "America", "Korea")
        val airlineSegment3 = createFlightSegment("KA", "Korea", "China")
        val airlineSegment4 = createFlightSegment("A", "China", "Malaysia")

        segments.add(airlineSegment1)
        segments.add(airlineSegment2)
        segments.add(airlineSegment3)
        segments.add(airlineSegment4)

        airlines.add(airline1)
        airlines.add(airline2)
        airlines.add(airline3)
        airlines.add(airline4)

        flightLeg.airlines = airlines
        flightLeg.flightSegments = segments
    }

    private fun createFlightLegWithThreeAirlines() {
        createFlightLegWithThreeAirlines(false, "")
    }

    private fun createFlightLegWithThreeAirlines(isCurrencyTypeMoney: Boolean, price: String) {
        if (!isCurrencyTypeMoney) createFlightLeg() else createFlightLeg(true, price)

        val airlines = ArrayList<Airline>()
        val airline1 = Airline("United", null)
        val airline2 = Airline("Delta", null)
        val airline3 = Airline("Korean Air", null)

        val segments = ArrayList<FlightLeg.FlightSegment>()
        val airlineSegment1 = createFlightSegment("U", "America", "America")
        val airlineSegment2 = createFlightSegment("D", "America", "Korea")
        val airlineSegment3 = createFlightSegment("KA", "Korea", "China")

        segments.add(airlineSegment1)
        segments.add(airlineSegment2)
        segments.add(airlineSegment3)

        airlines.add(airline1)
        airlines.add(airline2)
        airlines.add(airline3)

        flightLeg.airlines = airlines
        flightLeg.flightSegments = segments
    }

    private fun createFlightLegWithOneAirline() {
        createFlightLeg()
        val airlines = ArrayList<Airline>()
        val airline1 = Airline("United", null)
        val segments = ArrayList<FlightLeg.FlightSegment>()
        val airlineSegment1 = createFlightSegment("U", "America", "America")
        segments.add(airlineSegment1)
        airlines.add(airline1)
        flightLeg.airlines = airlines
        flightLeg.flightSegments = segments
    }

    private fun createFlightSegment(airlineCode: String, departureAirportCode: String, arrivalAirportCode: String): FlightLeg.FlightSegment {
        val airlineSegment = FlightLeg.FlightSegment()
        airlineSegment.airlineCode = airlineCode
        airlineSegment.departureAirportCode = departureAirportCode
        airlineSegment.arrivalAirportCode = arrivalAirportCode
        airlineSegment.durationHours = 2
        airlineSegment.durationMinutes = 2
        airlineSegment.layoverDurationHours = 0
        airlineSegment.layoverDurationMinutes = 0
        return airlineSegment
    }

    private fun createFlightClass(hasFlightClass: Boolean) {
        createFlightLegWithThreeAirlines()
        val seatClassAndBookingCodeList = arrayListOf<FlightTripDetails.SeatClassAndBookingCode>()
        if (hasFlightClass) {
            val seatClassAndBookingCode = FlightTripDetails.SeatClassAndBookingCode()
            seatClassAndBookingCode.seatClass = "premium coach"
            seatClassAndBookingCodeList.add(seatClassAndBookingCode)
        }
        flightLeg.seatClassAndBookingCodeList = seatClassAndBookingCodeList
    }

    private fun createFlightViewHolder(): AbstractFlightListAdapter.FlightViewHolder {
        return sut.onCreateViewHolder(FrameLayout(activity), AbstractFlightListAdapter.ViewTypes.FLIGHT_CELL_VIEW.ordinal) as AbstractFlightListAdapter.FlightViewHolder
    }

    private class TestFlightListAdapter(context: Context, flightSelectedSubject: PublishSubject<FlightLeg>, isRoundTripSearchSubject: BehaviorSubject<Boolean>) :
            AbstractFlightListAdapter(context, flightSelectedSubject, isRoundTripSearchSubject) {
        override fun getPriceDescriptorMessageIdForFSR(): Int? = null

        override fun isShowOnlyNonStopSearch(): Boolean = false

        override fun isShowOnlyRefundableSearch(): Boolean = false

        override fun showAllFlightsHeader(): Boolean = false

        override fun adjustPosition(): Int = 1

        override fun makeFlightViewModel(context: Context, flightLeg: FlightLeg): AbstractFlightViewModel {
            return FlightViewModel(context, flightLeg)
        }

        override fun showAdvanceSearchFilterHeader(): Boolean = true

        override fun getRoundTripStringResourceId(): Int = R.string.prices_roundtrip_label
    }
}
