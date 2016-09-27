package com.expedia.bookings.presenter.shared

import android.view.View
import com.expedia.bookings.data.Money
import com.expedia.bookings.data.flights.FlightLeg
import com.expedia.bookings.data.packages.PackageOfferModel
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.vm.flights.FlightOverviewViewModel
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RuntimeEnvironment
import rx.observers.TestSubscriber
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
        createSelectedFlightLeg()
        val testSubscriber = TestSubscriber<Unit>()
        sut.showPaymentFeesObservable.subscribe(testSubscriber)

        sut.paymentFeesMayApplyTextView.performClick()

        testSubscriber.assertValueCount(1)
    }

    @Test
    fun showBaggageFees() {
        val expectedUrl = "https://www.expedia.com/" + BAGGAGE_FEES_URL_PATH
        createSelectedFlightLeg()
        val testSubscriber = TestSubscriber<String>()
        sut.baggageFeeShowSubject.subscribe(testSubscriber)

        sut.showBaggageFeesButton.performClick()

        testSubscriber.assertValueCount(1)
        testSubscriber.assertValue(expectedUrl)
    }

    @Test
    fun selectFlightButton() {
        createSelectedFlightLeg()
        val testSubscriber = TestSubscriber<FlightLeg>()
        sut.vm.selectedFlightClickedSubject.subscribe(testSubscriber)

        sut.vm.selectFlightClickObserver.onNext(Unit)

        testSubscriber.assertValueCount(1)
        testSubscriber.assertValue(flightLeg)
    }

    @Test
    fun showDontShowBundlePrice() {
        sut.vm.showBundlePriceSubject.onNext(true)
        assertEquals(View.VISIBLE, sut.bundlePriceTextView.visibility)
        assertEquals(View.VISIBLE, sut.bundlePriceLabelTextView.visibility)

        sut.vm.showBundlePriceSubject.onNext(false)
        assertEquals(View.GONE, sut.bundlePriceTextView.visibility)
        assertEquals(View.GONE, sut.bundlePriceLabelTextView.visibility)
    }

    private fun createSelectedFlightLeg() {
        flightLeg = FlightLeg()
        flightLeg.flightSegments = emptyList()
        flightLeg.packageOfferModel = PackageOfferModel()
        flightLeg.packageOfferModel.price = PackageOfferModel.PackagePrice()
        flightLeg.packageOfferModel.price.pricePerPersonFormatted = "$42"
        flightLeg.packageOfferModel.price.averageTotalPricePerTicket = Money()
        flightLeg.packageOfferModel.price.averageTotalPricePerTicket.formattedPrice = "$42.00"
        flightLeg.packageOfferModel.price.averageTotalPricePerTicket = Money("42.00", "USD")
        flightLeg.baggageFeesUrl = BAGGAGE_FEES_URL_PATH
        sut.vm.selectedFlightLegSubject.onNext(flightLeg)
    }
}
