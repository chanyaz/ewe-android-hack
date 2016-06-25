package com.expedia.vm.test.robolectric

import com.expedia.bookings.data.TripResponse
import com.expedia.bookings.data.flights.FlightCreateTripParams
import com.expedia.bookings.data.flights.FlightCreateTripResponse
import com.expedia.bookings.data.flights.ValidFormOfPayment
import com.expedia.bookings.data.utils.getFee
import com.expedia.bookings.services.FlightServices
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.vm.flights.FlightCreateTripViewModel
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import rx.observers.TestSubscriber
import rx.subjects.PublishSubject
import kotlin.test.assertEquals

@RunWith(RobolectricRunner::class)
class FlightCreateTripViewModelTest {

    lateinit private var sut: FlightCreateTripViewModel

    lateinit private var flightServices: FlightServices
    lateinit private var selectedCardFeeSubject: PublishSubject<ValidFormOfPayment>
    lateinit private var params: FlightCreateTripParams
    lateinit private var createTripResponseObservable: PublishSubject<FlightCreateTripResponse>

    @Before
    fun setup() {
        selectedCardFeeSubject = PublishSubject.create()
        createMockFlightServices()
        sut = FlightCreateTripViewModel(flightServices, selectedCardFeeSubject)
    }

    @Test
    fun createTripRequestFired() {
        givenGoodCreateTripParams()
        expectCreateTripCall()

        val testSubscriber = TestSubscriber<TripResponse>()
        sut.tripResponseObservable.subscribe(testSubscriber)

        sut.tripParams.onNext(params)
        sut.performCreateTrip.onNext(Unit)
        val expectedFlightCreateTripResponse = FlightCreateTripResponse()
        createTripResponseObservable.onNext(expectedFlightCreateTripResponse)

        testSubscriber.assertValueCount(1)
        testSubscriber.assertValue(expectedFlightCreateTripResponse)
        Mockito.verify(flightServices).createTrip(params)
    }

    @Test
    fun cardFeesSetToTripResponse() {
        val paymentFormWithCardFee = createPaymentWithCardFee()
        val originalCreateTripResponse = goodCreateTripResponse()
        val testSubscriber = TestSubscriber<TripResponse>()
        sut.tripResponseObservable.onNext(originalCreateTripResponse)
        sut.tripResponseObservable.subscribe(testSubscriber)

        sut.selectedCardFeeSubject.onNext(paymentFormWithCardFee)

        testSubscriber.assertValueCount(2)
        val newTripResponseWithFees = testSubscriber.onNextEvents[1] as FlightCreateTripResponse
        assertEquals(paymentFormWithCardFee.getFee(), newTripResponseWithFees.selectedCardFees)
    }

    @Test
    fun zeroCardFeesIgnore() {
        val paymentFormWithCardFee = createPaymentWithZeroFees()
        val testSubscriber = TestSubscriber<TripResponse>()
        sut.tripResponseObservable.subscribe(testSubscriber)

        sut.selectedCardFeeSubject.onNext(paymentFormWithCardFee)

        testSubscriber.assertNoValues()
    }

    @Test
    fun cardFeesUnchangedIgnore() {
        val paymentFormWithCardFee = createPaymentWithCardFee()
        val originalCreateTripResponse = goodCreateTripResponse()
        val testSubscriber = TestSubscriber<TripResponse>()
        sut.tripResponseObservable.onNext(originalCreateTripResponse)
        sut.tripResponseObservable.subscribe(testSubscriber)

        // fire same selected card fee twice
        sut.selectedCardFeeSubject.onNext(paymentFormWithCardFee)
        sut.selectedCardFeeSubject.onNext(paymentFormWithCardFee)

        testSubscriber.assertValueCount(2)
        val newTripResponseWithFees = testSubscriber.onNextEvents[1] as FlightCreateTripResponse
        assertEquals(paymentFormWithCardFee.getFee(), newTripResponseWithFees.selectedCardFees)
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
        return flightCreateTripResponse
    }

    private fun givenGoodCreateTripParams() {
        val productKey = ""
        val withInsurance = false
        params = FlightCreateTripParams(productKey, withInsurance)
    }

    private fun expectCreateTripCall() {
        createTripResponseObservable = PublishSubject.create<FlightCreateTripResponse>()
        Mockito.`when`(flightServices.createTrip(params)).thenReturn(createTripResponseObservable)
    }

    private fun createMockFlightServices() {
        flightServices = Mockito.mock(FlightServices::class.java)
    }
}
