package com.expedia.bookings.test

import com.expedia.bookings.data.Money
import com.expedia.bookings.data.flights.FlightLeg
import com.expedia.bookings.data.packages.PackageOfferModel
import com.expedia.bookings.data.pos.PointOfSale
import com.expedia.bookings.data.pos.PointOfSaleId
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.utils.Ui
import com.expedia.vm.AbstractFlightOverviewViewModel
import com.expedia.vm.packages.FlightOverviewViewModel
import com.mobiata.android.util.SettingUtils
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RuntimeEnvironment
import rx.observers.TestSubscriber
import kotlin.test.assertEquals

@RunWith(RobolectricRunner::class)
class AbstractFlightOverviewViewModelTest {
    lateinit private var sut: AbstractFlightOverviewViewModel
    lateinit private var flightLeg: FlightLeg
    private val context = RuntimeEnvironment.application

    @Before
    fun setup() {
        SettingUtils.save(context, "point_of_sale_key", PointOfSaleId.UNITED_KINGDOM.id.toString())
        PointOfSale.onPointOfSaleChanged(context)
        Ui.getApplication(context).defaultTravelerComponent()
        sut = FlightOverviewViewModel(context)
    }

    @Test
    fun testObFeesLink() {
        val testSubscriber = TestSubscriber<String>()
        sut.obFeeDetailsUrlObservable.subscribe(testSubscriber)
        setupFlightLeg()
        addObFees()
        sut.selectedFlightLegSubject.onNext(flightLeg)
        testSubscriber.assertValue("https://www.expedia.co.uk/p/regulatory/obfees")
        setupFlightLeg()
        sut.selectedFlightLegSubject.onNext(flightLeg)
        assertEquals(false, flightLeg.airlineMessageModel.hasAirlineWithCCfee)
        testSubscriber.assertValues("https://www.expedia.co.uk/p/regulatory/obfees", "")
    }

    @Test
    fun testObFeesReset() {
        val obFeeTestSubscriber = TestSubscriber<String>()
        sut.chargesObFeesTextSubject.subscribe(obFeeTestSubscriber)
        setupFlightLeg()
        addObFees()
        sut.selectedFlightLegSubject.onNext(flightLeg)
        assertEquals(true, flightLeg.airlineMessageModel.hasAirlineWithCCfee)
        setupFlightLeg()
        sut.selectedFlightLegSubject.onNext(flightLeg)
        assertEquals(false, flightLeg.airlineMessageModel.hasAirlineWithCCfee)
        obFeeTestSubscriber.assertValues("Payment fees may apply", "")
    }

    private fun setupFlightLeg() {
        flightLeg = FlightLeg()
        flightLeg.packageOfferModel = PackageOfferModel()
        flightLeg.packageOfferModel.urgencyMessage = PackageOfferModel.UrgencyMessage()
        flightLeg.packageOfferModel.price = PackageOfferModel.PackagePrice()
        flightLeg.packageOfferModel.price.differentialPriceFormatted = "$646.00"
        flightLeg.packageOfferModel.price.pricePerPersonFormatted = "$646.00"
        flightLeg.packageOfferModel.price.averageTotalPricePerTicket = Money()
        flightLeg.airlineMessageModel = FlightLeg.AirlineMessageModel()
        flightLeg.airlineMessageModel.hasAirlineWithCCfee = false
        flightLeg.airlineMessageModel.airlineFeeLink = "p/regulatory/obfees"

    }

    private fun addObFees(){
        flightLeg.airlineMessageModel.hasAirlineWithCCfee = true
    }
}
