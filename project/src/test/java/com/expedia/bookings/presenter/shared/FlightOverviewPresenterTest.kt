package com.expedia.bookings.presenter.shared

import android.view.View
import com.expedia.bookings.R
import com.expedia.bookings.data.Money
import com.expedia.bookings.data.flights.FlightLeg
import com.expedia.bookings.data.packages.PackageOfferModel
import com.expedia.bookings.test.MultiBrand
import com.expedia.bookings.test.RunForBrands
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.vm.flights.FlightOverviewViewModel
import com.mobiata.android.util.SettingUtils
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RuntimeEnvironment
import rx.observers.TestSubscriber
import java.util.ArrayList
import kotlin.test.assertEquals

@RunWith(RobolectricRunner::class)
class FlightOverviewPresenterTest {

    val context = RuntimeEnvironment.application
    val BAGGAGE_FEES_URL_PATH = "BaggageFees"

    lateinit var sut: FlightOverviewPresenter
    lateinit var flightLeg: FlightLeg

    @Before
    fun setup() {
        sut = FlightOverviewPresenter(context, null)
        sut.vm = FlightOverviewViewModel(context)
    }

    @Test
    fun showPaymentFees() {
        createSelectedFlightLeg(true)
        val testSubscriber = TestSubscriber<Boolean>()
        sut.showPaymentFeesObservable.subscribe(testSubscriber)

        sut.paymentFeesMayApplyTextView.performClick()

        testSubscriber.assertValueCount(1)
        testSubscriber.assertValue(true)
    }

    @Test
    fun showTextPaymentFees() {
        createSelectedFlightLeg(false)
        val testSubscriber = TestSubscriber<Boolean>()
        sut.showPaymentFeesObservable.subscribe(testSubscriber)

        sut.paymentFeesMayApplyTextView.performClick()

        testSubscriber.assertValueCount(1)
        testSubscriber.assertValue(false)
    }

    @Test @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun showBaggageFees() {
        val expectedUrl = "https://www.expedia.com/" + BAGGAGE_FEES_URL_PATH
        createSelectedFlightLeg(true)
        val testSubscriber = TestSubscriber<String>()
        sut.baggageFeeShowSubject.subscribe(testSubscriber)

        sut.showBaggageFeesButton.performClick()

        testSubscriber.assertValueCount(1)
        testSubscriber.assertValue(expectedUrl)
    }

    @Test
    fun selectFlightButton() {
        createSelectedFlightLeg(true)
        val testSubscriber = TestSubscriber<FlightLeg>()
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
        val testSubscriber = TestSubscriber<Boolean>()
        sut.vm.showBasicEconomyTooltip.subscribe(testSubscriber)

        createSelectedFlightLeg(true)
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
        val toolTipRulesTestSubscriber = TestSubscriber<Array<String>>()
        val toolTipTitleTestSubscriber = TestSubscriber<String>()
        sut.basicEconomyToolTipInfoView.viewmodel.basicEconomyTooltipTitle.subscribe(toolTipTitleTestSubscriber)
        sut.basicEconomyToolTipInfoView.viewmodel.basicEconomyTooltipFareRules.subscribe(toolTipRulesTestSubscriber)

        createSelectedFlightLeg(true)
        createBasicEconomyTooltipInfo()
        sut.vm.selectedFlightLegSubject.onNext(flightLeg)
        assertEquals(2, toolTipRulesTestSubscriber.onNextEvents[0].size)
        assertEquals("1 personal item only, no access to overhead bin", toolTipRulesTestSubscriber.onNextEvents[0].get(0))
        assertEquals("Seats assigned at check-in.", toolTipRulesTestSubscriber.onNextEvents[0].get(1))
        assertEquals("United Airlines Basic Economy Fare", toolTipTitleTestSubscriber.onNextEvents[0])
    }

    private fun createSelectedFlightLeg(hasObFees: Boolean) {
        flightLeg = FlightLeg()
        flightLeg.flightSegments = emptyList()
        flightLeg.packageOfferModel = PackageOfferModel()
        flightLeg.packageOfferModel.price = PackageOfferModel.PackagePrice()
        flightLeg.packageOfferModel.price.pricePerPersonFormatted = "$42"
        flightLeg.packageOfferModel.price.averageTotalPricePerTicket = Money()
        flightLeg.packageOfferModel.price.averageTotalPricePerTicket.formattedPrice = "$42.00"
        flightLeg.packageOfferModel.price.averageTotalPricePerTicket = Money("42.00", "USD")
        flightLeg.baggageFeesUrl = BAGGAGE_FEES_URL_PATH
        flightLeg.mayChargeObFees = hasObFees
        sut.vm.selectedFlightLegSubject.onNext(flightLeg)
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
}
