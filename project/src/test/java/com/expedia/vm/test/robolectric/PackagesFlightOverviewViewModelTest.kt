package com.expedia.vm.test.robolectric

import com.expedia.bookings.data.Money
import com.expedia.bookings.data.abacus.AbacusUtils
import com.expedia.bookings.data.flights.FlightLeg
import com.expedia.bookings.data.packages.PackageOfferModel
import com.expedia.bookings.data.pos.PointOfSaleId
import com.expedia.bookings.services.TestObserver
import com.expedia.bookings.test.MultiBrand
import com.expedia.bookings.test.RunForBrands
import com.expedia.bookings.test.robolectric.RoboTestHelper
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.utils.AbacusTestUtils
import com.expedia.vm.packages.FlightOverviewViewModel
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RuntimeEnvironment
import kotlin.test.assertEquals

@RunWith(RobolectricRunner::class)
class PackagesFlightOverviewViewModelTest {

    private val context = RuntimeEnvironment.application

    private lateinit var sut: FlightOverviewViewModel
    private lateinit var flightLeg: FlightLeg

    private fun setupSystemUnderTest() {
        sut = FlightOverviewViewModel(context)
    }

    private fun setupFlightLeg(mayChargeObFees: Boolean = true) {
        flightLeg = FlightLeg()
        flightLeg.packageOfferModel = PackageOfferModel()
        flightLeg.packageOfferModel.urgencyMessage = PackageOfferModel.UrgencyMessage()
        flightLeg.packageOfferModel.price = PackageOfferModel.PackagePrice()
        flightLeg.packageOfferModel.price.differentialPriceFormatted = "$646.00"
        flightLeg.packageOfferModel.price.pricePerPersonFormatted = "$646.00"
        flightLeg.packageOfferModel.price.averageTotalPricePerTicket = Money()
        flightLeg.packageOfferModel.price.averageTotalPricePerTicket.formattedPrice = "$646.00"
        flightLeg.mayChargeObFees = mayChargeObFees
    }

    private fun setupFlightLegWithAirlineMessageModel(mayChargeObFees: Boolean = true,
                                                      hasAirlineWithCCfee: Boolean = true,
                                                      airlineFeeLink: String = "/p/regulatory/obfees") {
        setupFlightLeg(mayChargeObFees)
        val airlineMessageModel = FlightLeg.AirlineMessageModel()
        airlineMessageModel.airlineFeeLink = airlineFeeLink
        airlineMessageModel.hasAirlineWithCCfee = hasAirlineWithCCfee
        airlineMessageModel.airlineName = "United"
        flightLeg.airlineMessageModel = airlineMessageModel
    }

    @Test
    fun seatsLeftText() {
        setupSystemUnderTest()
        setupFlightLeg()

        sut.numberOfTravelers.onNext(1)
        flightLeg.packageOfferModel.urgencyMessage.ticketsLeft = 1
        sut.selectedFlightLegSubject.onNext(flightLeg)
        assertEquals("1 seat left, $646.00", sut.urgencyMessagingSubject.value)

        sut.numberOfTravelers.onNext(3)
        flightLeg.packageOfferModel.urgencyMessage.ticketsLeft = 2
        sut.selectedFlightLegSubject.onNext(flightLeg)
        assertEquals("2 seats left, $646.00 per person", sut.urgencyMessagingSubject.value)

        flightLeg.packageOfferModel.urgencyMessage.ticketsLeft = 6
        sut.selectedFlightLegSubject.onNext(flightLeg)
        assertEquals("$646.00 per person", sut.urgencyMessagingSubject.value)

        flightLeg.packageOfferModel.urgencyMessage.ticketsLeft = 0
        sut.selectedFlightLegSubject.onNext(flightLeg)
        assertEquals("$646.00 per person", sut.urgencyMessagingSubject.value)

        sut.numberOfTravelers.onNext(1)
        flightLeg.packageOfferModel.urgencyMessage.ticketsLeft = 3
        sut.selectedFlightLegSubject.onNext(flightLeg)
        assertEquals("3 seats left, $646.00", sut.urgencyMessagingSubject.value)

        sut.numberOfTravelers.onNext(3)
        flightLeg.packageOfferModel.price.deltaPositive = true
        flightLeg.packageOfferModel.urgencyMessage.ticketsLeft = 2
        sut.selectedFlightLegSubject.onNext(flightLeg)
        assertEquals("2 seats left, +$646.00 per person", sut.urgencyMessagingSubject.value)
    }

    @Test @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun testFlightHeaderUrgencyMessageWhenBucketed() {
        AbacusTestUtils.bucketTestAndEnableRemoteFeature(context, AbacusUtils.EBAndroidAppFlightsUrgencyMessaging, 1)
        setupSystemUnderTest()
        setupFlightLeg()

        sut.numberOfTravelers.onNext(1)
        flightLeg.packageOfferModel.urgencyMessage.ticketsLeft = 1
        sut.selectedFlightLegSubject.onNext(flightLeg)
        assertEquals("1 seat left, $646.00", sut.urgencyMessagingSubject.value)
    }

    @Test
    fun urgencyMessageSeatsLeftText() {
        setupSystemUnderTest()
        setupFlightLeg()

        sut.numberOfTravelers.onNext(1)
        flightLeg.packageOfferModel.urgencyMessage.ticketsLeft = 5
        sut.selectedFlightLegSubject.onNext(flightLeg)
        assertEquals("5 seats left, $646.00", sut.urgencyMessagingSubject.value)

        sut.numberOfTravelers.onNext(1)
        flightLeg.packageOfferModel.urgencyMessage.ticketsLeft = 6
        sut.selectedFlightLegSubject.onNext(flightLeg)
        assertEquals("$646.00", sut.urgencyMessagingSubject.value)
    }

    @Test
    fun totalDurationText() {
        setupSystemUnderTest()
        setupFlightLeg()
        flightLeg.durationHour = 3
        flightLeg.durationMinute = 50

        sut.selectedFlightLegSubject.onNext(flightLeg)
        assertEquals("Total Duration: 3h 50m", sut.totalDurationSubject.value.toString())
        assertEquals("Total Duration: 3 hour 50 minutes", sut.totalDurationContDescSubject.value)
    }

    @Test
    fun pricePerPersonText() {
        setupSystemUnderTest()
        setupFlightLeg()

        sut.selectedFlightLegSubject.onNext(flightLeg)
        assertEquals("$646.00/person", sut.bundlePriceSubject.value)
    }

    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    @Test
    fun testUpdateOBFeesHasAirlineFeeLink() {
        RoboTestHelper.setPOS(PointOfSaleId.AUSTRALIA)
        setupSystemUnderTest()
        setupFlightLegWithAirlineMessageModel()

        val paymentInfoTestSubscriber = TestObserver<String>()
        sut.chargesObFeesTextSubject.subscribe(paymentInfoTestSubscriber)
        val obFeeDetailsUrlTestSubscriber = TestObserver<String>()
        sut.obFeeDetailsUrlObservable.subscribe(obFeeDetailsUrlTestSubscriber)

        sut.selectedFlightLegSubject.onNext(flightLeg)
        paymentInfoTestSubscriber.assertValues("", "Payment fees may apply")

        obFeeDetailsUrlTestSubscriber.assertValues("", sut.e3EndpointUrl + "/p/regulatory/obfees")
    }

    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    @Test
    fun testUpdateOBFeesNoAirlineFeeLink() {
        RoboTestHelper.setPOS(PointOfSaleId.AUSTRALIA)
        setupSystemUnderTest()
        setupFlightLegWithAirlineMessageModel(airlineFeeLink = "")

        val paymentInfoTestSubscriber = TestObserver<String>()
        sut.airlineFeesWarningTextSubject.subscribe(paymentInfoTestSubscriber)
        val obFeeDetailsUrlTestSubscriber = TestObserver<String>()
        sut.obFeeDetailsUrlObservable.subscribe(obFeeDetailsUrlTestSubscriber)

        sut.selectedFlightLegSubject.onNext(flightLeg)

        paymentInfoTestSubscriber.assertValues("", "There may be an additional fee based on your payment method.")
        obFeeDetailsUrlTestSubscriber.assertValue("")
    }

    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    @Test
    fun testUpdateOBFeesNoShowAirlineFeeLink() {
        RoboTestHelper.setPOS(PointOfSaleId.AUSTRALIA)
        setupSystemUnderTest()
        setupFlightLegWithAirlineMessageModel(mayChargeObFees = false, hasAirlineWithCCfee = false)

        val paymentInfoTestSubscriber = TestObserver<String>()
        sut.chargesObFeesTextSubject.subscribe(paymentInfoTestSubscriber)
        val obFeeDetailsUrlTestSubscriber = TestObserver<String>()
        sut.obFeeDetailsUrlObservable.subscribe(obFeeDetailsUrlTestSubscriber)

        sut.selectedFlightLegSubject.onNext(flightLeg)

        paymentInfoTestSubscriber.assertValue("")
        obFeeDetailsUrlTestSubscriber.assertValue("")
    }

    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    @Test
    fun testUpdateOBFeesNoShowAirlineFeeLinkOnDifferentPOS() {
        RoboTestHelper.setPOS(PointOfSaleId.UNITED_STATES)
        setupSystemUnderTest()
        setupFlightLegWithAirlineMessageModel(mayChargeObFees = true, hasAirlineWithCCfee = true)

        val paymentInfoTestSubscriber = TestObserver<String>()
        sut.chargesObFeesTextSubject.subscribe(paymentInfoTestSubscriber)
        val obFeeDetailsUrlTestSubscriber = TestObserver<String>()
        sut.obFeeDetailsUrlObservable.subscribe(obFeeDetailsUrlTestSubscriber)

        sut.selectedFlightLegSubject.onNext(flightLeg)

        paymentInfoTestSubscriber.assertValue("")
        obFeeDetailsUrlTestSubscriber.assertValue("")
    }
}
