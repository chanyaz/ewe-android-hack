package com.expedia.bookings.test

import com.expedia.bookings.data.Money
import com.expedia.bookings.data.abacus.AbacusUtils
import com.expedia.bookings.data.flights.FlightLeg
import com.expedia.bookings.data.packages.PackageOfferModel
import com.expedia.bookings.data.payment.LoyaltyEarnInfo
import com.expedia.bookings.data.payment.LoyaltyInformation
import com.expedia.bookings.data.payment.PointsEarnInfo
import com.expedia.bookings.data.pos.PointOfSale
import com.expedia.bookings.data.pos.PointOfSaleId
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.utils.Ui
import com.expedia.vm.AbstractFlightOverviewViewModel
import com.mobiata.android.util.SettingUtils
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RuntimeEnvironment
import com.expedia.bookings.services.TestObserver
import com.expedia.bookings.utils.AbacusTestUtils
import com.expedia.vm.flights.FlightOverviewViewModel
import com.expedia.bookings.packages.vm.PackageFlightOverviewViewModel
import kotlin.test.assertEquals
import kotlin.test.assertFalse

@RunWith(RobolectricRunner::class)
class AbstractFlightOverviewViewModelTest {
    private lateinit var sut: AbstractFlightOverviewViewModel
    private lateinit var flightLeg: FlightLeg
    private val context = RuntimeEnvironment.application

    @Before
    fun setup() {
        SettingUtils.save(context, "point_of_sale_key", PointOfSaleId.UNITED_KINGDOM.id.toString())
        PointOfSale.onPointOfSaleChanged(context)
        Ui.getApplication(context).defaultTravelerComponent()
    }

    @Test @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun testObFeesLink() {
        setFlightOverviewModel(true)
        val testSubscriber = TestObserver<String>()
        sut.obFeeDetailsUrlObservable.subscribe(testSubscriber)
        setupFlightLeg()
        addObFees()
        sut.selectedFlightLegSubject.onNext(flightLeg)
        testSubscriber.assertValues("", "https://www.expedia.co.uk/p/regulatory/obfees")
        setupFlightLeg()
        sut.selectedFlightLegSubject.onNext(flightLeg)
        assertEquals(false, flightLeg.airlineMessageModel.hasAirlineWithCCfee)
        testSubscriber.assertValues("", "https://www.expedia.co.uk/p/regulatory/obfees", "")
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun testUrgencyMessageLinkWhenBucketed() {
        AbacusTestUtils.bucketTestAndEnableRemoteFeature(context, AbacusUtils.EBAndroidAppFlightsUrgencyMessaging, 1)
        setFlightOverviewModel(false)
        val urgencyMessageTestSubscriber = TestObserver<String>()
        sut.bottomUrgencyMessageSubject.subscribe(urgencyMessageTestSubscriber)
        setupFlightLeg()
        setSeatsLeftInLeg(3)
        sut.selectedFlightLegSubject.onNext(flightLeg)

        setSeatsLeftInLeg(6)
        sut.selectedFlightLegSubject.onNext(flightLeg)
        urgencyMessageTestSubscriber.assertValues("3 seats left", "")
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun testUrgencyMessageLinkWhenControlled() {
        AbacusTestUtils.unbucketTests(AbacusUtils.EBAndroidAppFlightsUrgencyMessaging)
        setFlightOverviewModel(false)
        val urgencyMessageTestSubscriber = TestObserver<String>()
        sut.bottomUrgencyMessageSubject.subscribe(urgencyMessageTestSubscriber)
        setupFlightLeg()
        setSeatsLeftInLeg(3)
        sut.selectedFlightLegSubject.onNext(flightLeg)

        setSeatsLeftInLeg(6)
        sut.selectedFlightLegSubject.onNext(flightLeg)
        urgencyMessageTestSubscriber.assertValueCount(0)
    }

    @Test @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun testObFeesReset() {
        setFlightOverviewModel(true)
        val obFeeTestSubscriber = TestObserver<String>()
        sut.chargesObFeesTextSubject.subscribe(obFeeTestSubscriber)
        setupFlightLeg()
        addObFees()
        sut.selectedFlightLegSubject.onNext(flightLeg)
        assertEquals(true, flightLeg.airlineMessageModel.hasAirlineWithCCfee)
        setupFlightLeg()
        sut.selectedFlightLegSubject.onNext(flightLeg)
        assertEquals(false, flightLeg.airlineMessageModel.hasAirlineWithCCfee)
        obFeeTestSubscriber.assertValues("", "Payment fees may apply", "")
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun testEarnMessage() {
        setFlightOverviewModel(false)
        val showEarnMessageTestSubscriber = TestObserver<Boolean>()
        val earnMessageTestSubscriber = TestObserver<String>()
        sut.showEarnMessage.subscribe(showEarnMessageTestSubscriber)
        sut.earnMessage.subscribe(earnMessageTestSubscriber)

        PointOfSaleTestConfiguration.configurePointOfSale(RuntimeEnvironment.application, "MockSharedData/pos_with_flight_earn_messaging_disabled.json", false)
        val pos = PointOfSale.getPointOfSale()
        assertFalse(pos.isEarnMessageEnabledForFlights)
        setupFlightLeg()
        //pos not supports earn messaging and flight leg does not have a loyalty info object
        sut.selectedFlightLegSubject.onNext(flightLeg)
        earnMessageTestSubscriber.assertValuesAndClear("")
        showEarnMessageTestSubscriber.assertValuesAndClear(false)

        //pos not supports earn messaging but flight leg has loyalty info object
        addLoyaltyInfo()
        sut.selectedFlightLegSubject.onNext(flightLeg)
        earnMessageTestSubscriber.assertValuesAndClear("Earn 100 points")
        showEarnMessageTestSubscriber.assertValuesAndClear(false)

        //pos supports earn messaging and flight leg has loyalty info object
        PointOfSaleTestConfiguration.configurePointOfSale(RuntimeEnvironment.application, "MockSharedData/pos_with_flight_earn_messaging_enabled.json", false)
        sut.selectedFlightLegSubject.onNext(flightLeg)
        earnMessageTestSubscriber.assertValue("Earn 100 points")
        showEarnMessageTestSubscriber.assertValue(true)
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun testUrgencyMessageVisibilityWithRouteScore() {
        val bottomUrgencyMessageTestSubscriber = TestObserver<Boolean>()
        setFlightOverviewModel(false)
        setupFlightLeg()
        setSeatsLeftInLeg(2)
        sut.selectedFlightLegSubject.onNext(flightLeg)
        sut.flightMessageContainerStream.subscribe(bottomUrgencyMessageTestSubscriber)

        sut.routeScoreStream.onNext("7.9/10 - Very Good!")
        sut.bottomUrgencyMessageSubject.onNext("2 seats left")
        bottomUrgencyMessageTestSubscriber.assertValueCount(2)
        bottomUrgencyMessageTestSubscriber.assertValuesAndClear(true, true)

        sut.routeScoreStream.onNext("7.9/10 - Very Good!")
        sut.bottomUrgencyMessageSubject.onNext("")
        bottomUrgencyMessageTestSubscriber.assertValueCount(1)
        bottomUrgencyMessageTestSubscriber.assertValuesAndClear(true)

        sut.routeScoreStream.onNext("")
        sut.bottomUrgencyMessageSubject.onNext("2 seats left")
        bottomUrgencyMessageTestSubscriber.assertValueCount(1)
        bottomUrgencyMessageTestSubscriber.assertValuesAndClear(true)

        sut.routeScoreStream.onNext("")
        sut.bottomUrgencyMessageSubject.onNext("")
        bottomUrgencyMessageTestSubscriber.assertValueCount(0)
    }

    private fun setupFlightLeg() {
        flightLeg = FlightLeg()
        flightLeg.packageOfferModel = PackageOfferModel()
        flightLeg.packageOfferModel.urgencyMessage = PackageOfferModel.UrgencyMessage()
        flightLeg.packageOfferModel.price = PackageOfferModel.PackagePrice()
        flightLeg.packageOfferModel.price.differentialPriceFormatted = "$646.00"
        flightLeg.packageOfferModel.price.pricePerPersonFormatted = "$646.00"
        flightLeg.packageOfferModel.price.averageTotalPricePerTicket = Money()
        flightLeg.packageOfferModel.price.averageTotalPricePerTicket = Money("42.00", "USD")
        flightLeg.airlineMessageModel = FlightLeg.AirlineMessageModel()
        flightLeg.airlineMessageModel.hasAirlineWithCCfee = false
        flightLeg.flightSegments = listOf(createFlightSegment("coach"))
        flightLeg.airlineMessageModel.airlineFeeLink = "p/regulatory/obfees"
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

    private fun setSeatsLeftInLeg(ticketsLeft: Int) {
        flightLeg.packageOfferModel.urgencyMessage.ticketsLeft = ticketsLeft
    }

    private fun addObFees() {
        flightLeg.airlineMessageModel.hasAirlineWithCCfee = true
    }

    private fun addLoyaltyInfo() {
        val earnInfo = PointsEarnInfo(100, 100, 100)
        val loyaltyInfo = LoyaltyInformation(null, LoyaltyEarnInfo(earnInfo, null), false)
        flightLeg.packageOfferModel.loyaltyInfo = loyaltyInfo
    }

    private fun setFlightOverviewModel(isPackages: Boolean) {
        if (isPackages) {
            sut = PackageFlightOverviewViewModel(context)
        } else {
            sut = FlightOverviewViewModel(context)
        }
    }
}
