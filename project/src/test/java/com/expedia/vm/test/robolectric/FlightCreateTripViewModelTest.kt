package com.expedia.vm.test.robolectric

import android.content.DialogInterface
import com.expedia.bookings.data.TripResponse
import com.expedia.bookings.data.flights.FlightCreateTripParams
import com.expedia.bookings.data.flights.FlightCreateTripResponse
import com.expedia.bookings.data.flights.FlightTripDetails
import com.expedia.bookings.data.flights.ValidFormOfPayment
import com.expedia.bookings.data.utils.getFee
import com.expedia.bookings.services.FlightServices
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.vm.flights.FlightCreateTripViewModel
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.robolectric.RuntimeEnvironment
import org.robolectric.Shadows
import org.robolectric.shadows.ShadowAlertDialog
import rx.Observable
import rx.observers.TestSubscriber
import rx.subjects.BehaviorSubject
import rx.subjects.PublishSubject
import java.io.IOException
import kotlin.test.assertEquals

@RunWith(RobolectricRunner::class)
class FlightCreateTripViewModelTest {

    private val context = RuntimeEnvironment.application

    lateinit private var sut: FlightCreateTripViewModel
    lateinit private var flightServices: FlightServices
    lateinit private var selectedCardFeeSubject: PublishSubject<ValidFormOfPayment>
    lateinit private var params: FlightCreateTripParams
    lateinit private var createTripResponseObservable: BehaviorSubject<FlightCreateTripResponse>

    @Before
    fun setup() {
        selectedCardFeeSubject = PublishSubject.create()
        createMockFlightServices()
        sut = FlightCreateTripViewModel(context, flightServices, selectedCardFeeSubject)
    }

    @Test
    fun createTripRequestFired() {
        givenGoodCreateTripParams()
        expectGoodCreateTripCall()

        val testSubscriber = TestSubscriber<TripResponse>()
        sut.tripResponseObservable.subscribe(testSubscriber)

        sut.tripParams.onNext(params)
        sut.performCreateTrip.onNext(Unit)
        val expectedFlightCreateTripResponse = goodCreateTripResponse()
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

    @Test
    fun networkErrorDialogCancel() {
        val testSubscriber = TestSubscriber<Unit>()
        givenGoodCreateTripParams()
        givenCreateTripCallWithIOException()

        sut.noNetworkObservable.subscribe(testSubscriber)

        sut.tripParams.onNext(params)
        sut.performCreateTrip.onNext(Unit)

        val latestAlertDialog = ShadowAlertDialog.getLatestAlertDialog()
        val shadowAlertDialog = Shadows.shadowOf(latestAlertDialog)
        val cancelBtn = latestAlertDialog.getButton(DialogInterface.BUTTON_NEGATIVE)
        cancelBtn.performClick()

        assertEquals("", shadowAlertDialog.title)
        assertEquals("Your device is not connected to the internet.  Please check your connection and try again.", shadowAlertDialog.message)
        testSubscriber.assertValueCount(1)
    }

    @Test
    fun networkErrorDialogRetry() {
        givenGoodCreateTripParams()
        givenCreateTripCallWithIOException()

        sut.tripParams.onNext(params)
        sut.performCreateTrip.onNext(Unit)

        val latestAlertDialog = ShadowAlertDialog.getLatestAlertDialog()
        val shadowAlertDialog = Shadows.shadowOf(latestAlertDialog)
        val retryBtn = latestAlertDialog.getButton(DialogInterface.BUTTON_POSITIVE)
        retryBtn.performClick()
        retryBtn.performClick()
        retryBtn.performClick()

        assertEquals("", shadowAlertDialog.title)
        assertEquals("Your device is not connected to the internet.  Please check your connection and try again.", shadowAlertDialog.message)
        Mockito.verify(flightServices, Mockito.times(4)).createTrip(params) // 1 original, 3 retries
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
        val field = flightCreateTripResponse.javaClass.getDeclaredField("details") // using reflection as field private
        field.isAccessible = true
        field.set(flightCreateTripResponse, FlightTripDetails())
        return flightCreateTripResponse
    }

    private fun givenGoodCreateTripParams() {
        val productKey = ""
        val withInsurance = false
        params = FlightCreateTripParams(productKey, withInsurance)
    }

    private fun expectGoodCreateTripCall() {
        createTripResponseObservable = BehaviorSubject.create<FlightCreateTripResponse>()
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
