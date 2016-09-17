package com.expedia.vm.test.robolectric

import com.expedia.bookings.data.Money
import com.expedia.bookings.data.TripResponse
import com.expedia.bookings.data.flights.FlightCreateTripParams
import com.expedia.bookings.data.flights.FlightCreateTripResponse
import com.expedia.bookings.data.flights.FlightTripDetails
import com.expedia.bookings.data.flights.ValidFormOfPayment
import com.expedia.bookings.services.FlightServices
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.utils.Ui
import com.expedia.vm.flights.FlightCreateTripViewModel
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.robolectric.RuntimeEnvironment
import rx.observers.TestSubscriber
import rx.subjects.BehaviorSubject
import rx.subjects.PublishSubject
import java.io.IOException

@RunWith(RobolectricRunner::class)
class FlightCreateTripViewModelTest {

    private val context = RuntimeEnvironment.application

    lateinit private var sut: FlightCreateTripViewModel
    lateinit private var flightServices: FlightServices
    lateinit private var selectedCardFeeSubject: PublishSubject<ValidFormOfPayment?>
    lateinit private var params: FlightCreateTripParams
    lateinit private var createTripResponseObservable: BehaviorSubject<FlightCreateTripResponse>

    @Before
    fun setup() {
        selectedCardFeeSubject = PublishSubject.create()
        createMockFlightServices()
        Ui.getApplication(context).defaultFlightComponents()
        sut = FlightCreateTripViewModel(context)
        sut.flightServices = flightServices
    }

    @Test
    fun createTripRequestFired() {
        givenGoodCreateTripParams()
        expectCreateTripCall()

        val testSubscriber = TestSubscriber<TripResponse>()
        sut.tripResponseObservable.subscribe(testSubscriber)

        sut.tripParams.onNext(params)
        sut.performCreateTrip.onNext(Unit)

        testSubscriber.assertValueCount(1)
        Mockito.verify(flightServices).createTrip(params)
    }

    @Test
    fun networkErrorDialogCancel() {
        val noInternetTestSubscriber = TestSubscriber<Unit>()
        givenGoodCreateTripParams()
        givenCreateTripCallWithIOException()

        sut.showNoInternetRetryDialog.subscribe(noInternetTestSubscriber)

        sut.tripParams.onNext(params)
        sut.performCreateTrip.onNext(Unit)

        noInternetTestSubscriber.assertValueCount(1)
    }

    @Test
    fun networkErrorDialogRetry() {
        val testSubscriber = TestSubscriber<Unit>()
        givenGoodCreateTripParams()
        givenCreateTripCallWithIOException()

        sut.showNoInternetRetryDialog.subscribe(testSubscriber)
        sut.tripParams.onNext(params)
        sut.performCreateTrip.onNext(Unit)

        testSubscriber.assertValueCount(1)
    }

    private fun createPaymentWithCardFee(): ValidFormOfPayment {
        val validFormOfPayment = ValidFormOfPayment()
        validFormOfPayment.name = "AmericanExpress"
        validFormOfPayment.fee = "2.50"
        validFormOfPayment.formattedFee = "$2.50"
        validFormOfPayment.feeCurrencyCode = "USD"
        return validFormOfPayment
    }

    private fun createPaymentWithZeroFees(): ValidFormOfPayment {
        val validFormOfPayment = ValidFormOfPayment()
        validFormOfPayment.name = "AmericanExpress"
        validFormOfPayment.fee = "0"
        validFormOfPayment.feeCurrencyCode = "USD"
        return validFormOfPayment
    }

    private fun goodCreateTripResponse(): FlightCreateTripResponse {
        val flightCreateTripResponse = FlightCreateTripResponse()
        val flightTripDetails = FlightTripDetails()
        flightTripDetails.offer = FlightTripDetails.FlightOffer()
        flightTripDetails.offer.totalPrice = Money("42", "USD")
        flightCreateTripResponse.details = flightTripDetails
        return flightCreateTripResponse
    }

    private fun givenGoodCreateTripParams() {
        val productKey = ""
        val withInsurance = false
        params = FlightCreateTripParams(productKey, withInsurance)
    }

    private fun expectCreateTripCall(hasSelectedCardFees: Boolean = false, feeAmount: Int = 0) {
        // Move this into helper
        val expectedFlightCreateTripResponse = goodCreateTripResponse()
        if (hasSelectedCardFees) {
            expectedFlightCreateTripResponse.selectedCardFees = Money(feeAmount, "USD")
        }
        createTripResponseObservable = BehaviorSubject.create<FlightCreateTripResponse>()
        createTripResponseObservable.onNext(expectedFlightCreateTripResponse)
        Mockito.`when`(flightServices.createTrip(params)).thenReturn(createTripResponseObservable)
    }

    private fun givenCreateTripCallWithIOException() {
        createTripResponseObservable = BehaviorSubject.create<FlightCreateTripResponse>()
        createTripResponseObservable.onError(IOException())
        Mockito.`when`(flightServices.createTrip(params)).thenReturn(createTripResponseObservable)
    }

    private fun createMockFlightServices() {
        flightServices = Mockito.mock(FlightServices::class.java)
    }
}
