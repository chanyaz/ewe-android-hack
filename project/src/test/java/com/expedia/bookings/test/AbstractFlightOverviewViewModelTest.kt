package com.expedia.bookings.test

import com.expedia.bookings.data.Money
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
        flightLeg.airlineMessageModel.airlineFeeLink = "p/regulatory/obfees"
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
            sut = com.expedia.vm.packages.FlightOverviewViewModel(context)
        } else {
            sut = com.expedia.vm.flights.FlightOverviewViewModel(context)
        }
    }
}
