package com.expedia.vm.test.robolectric

import android.text.SpannableStringBuilder
import android.text.Spanned
import com.expedia.bookings.data.BillingInfo
import com.expedia.bookings.data.Location
import com.expedia.bookings.data.Money
import com.expedia.bookings.data.PaymentType
import com.expedia.bookings.data.Traveler
import com.expedia.bookings.data.TripDetails
import com.expedia.bookings.data.flights.FlightCheckoutParams
import com.expedia.bookings.data.flights.FlightCheckoutResponse
import com.expedia.bookings.data.flights.FlightCreateTripResponse
import com.expedia.bookings.data.flights.ValidFormOfPayment
import com.expedia.bookings.enums.PassengerCategory
import com.expedia.bookings.services.FlightServices
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.vm.FlightCheckoutViewModel
import org.joda.time.LocalDate
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.robolectric.RuntimeEnvironment
import rx.observers.TestSubscriber
import rx.subjects.PublishSubject
import kotlin.test.assertEquals

@RunWith(RobolectricRunner::class)
class FlightCheckoutViewModelTest {

    private val context = RuntimeEnvironment.application

    lateinit private var sut: FlightCheckoutViewModel
    lateinit private var mockFlightServices: FlightServices
    lateinit private var params: FlightCheckoutParams
    lateinit private var newTripResponse: FlightCreateTripResponse
    private val selectedCardTypeSubject = PublishSubject.create<PaymentType>()


    private fun setupSystemUnderTest() {
        sut = FlightCheckoutViewModel(context, mockFlightServices, selectedCardTypeSubject)
    }

    @Test
    fun legalTextObservable() {
        createMockFlightServices()
        givenGoodTripResponse()
        setupSystemUnderTest()

        val legalTextTestSubscriber = TestSubscriber<SpannableStringBuilder>()
        sut.legalText.subscribe(legalTextTestSubscriber)

        sut.tripResponseObservable.onNext(newTripResponse)

        legalTextTestSubscriber.assertValueCount(1)
        assertEquals("By completing this booking I agree that I have read and accept the Rules and Restrictions, the Terms and Conditions, the Privacy Policy and Fare Information.", legalTextTestSubscriber.onNextEvents[0].toString())
    }

    @Test
    fun slideToPurchaseTotalLabel() {
        givenGoodCheckoutParams()
        createMockFlightServices()
        givenGoodTripResponse()
        setupSystemUnderTest()

        val sliderPurchaseTotalTextTestSubscriber = TestSubscriber<CharSequence>()
        sut.sliderPurchaseTotalText.subscribe(sliderPurchaseTotalTextTestSubscriber)

        sut.tripResponseObservable.onNext(newTripResponse)

        sliderPurchaseTotalTextTestSubscriber.assertValueCount(1)
        assertEquals("Your card will be charged $44.50", sliderPurchaseTotalTextTestSubscriber.onNextEvents[0].toString())
    }

    @Test
    fun flightCheckoutParamsBuiltOnNewCreateTripResponse() {
        givenGoodCheckoutParams()
        createMockFlightServices()
        givenGoodTripResponse()
        setupSystemUnderTest()

        val legalTextTestSubscriber = TestSubscriber<SpannableStringBuilder>()
        val sliderPurchaseTotalTextTestSubscriber = TestSubscriber<CharSequence>()

        sut.legalText.subscribe(legalTextTestSubscriber)
        sut.sliderPurchaseTotalText.subscribe(sliderPurchaseTotalTextTestSubscriber)

        sut.tripResponseObservable.onNext(newTripResponse)

        // populate builder with required data not set on receipt of a createTrip response (just for this unit test)
        sut.builder.billingInfo(makeBillingInfo()).travelers(listOf(Traveler())).cvv("")

        val flightCheckoutParams = sut.builder.build()
        assertEquals(newTripResponse.tealeafTransactionId, flightCheckoutParams.tealeafTransactionId)
        assertEquals(newTripResponse.totalPrice.currencyCode, flightCheckoutParams.expectedFareCurrencyCode)
        assertEquals(newTripResponse.totalPrice.amount.toString(), flightCheckoutParams.expectedTotalFare)
        assertEquals(newTripResponse.tripId, flightCheckoutParams.tripId)
    }

    @Test
    fun zeroCardFees() {
        givenGoodCheckoutParams()
        createMockFlightServices()
        setupSystemUnderTest()

        val showCardFeesTestSubscriber = TestSubscriber<Boolean>()
        val cardFeeTextSubscriber = TestSubscriber<Spanned>()
        val cardFeeWarningTextSubscriber = TestSubscriber<String>()
        val cardFeeForSelectedSubscriber = TestSubscriber<ValidFormOfPayment>()

        sut.showCardFees.subscribe(showCardFeesTestSubscriber)
        sut.cardFeeTextSubject.subscribe(cardFeeTextSubscriber)
        sut.cardFeeWarningTextSubject.subscribe(cardFeeWarningTextSubscriber)
        sut.cardFeeForSelectedCard.subscribe(cardFeeForSelectedSubscriber)

        selectedCardTypeSubject.onNext(PaymentType.CARD_AMERICAN_EXPRESS)
        sut.validFormsOfPaymentSubject.onNext(listOf(createPaymentWithZeroFees()))

        showCardFeesTestSubscriber.assertValue(false)
        cardFeeTextSubscriber.assertNoValues()
        cardFeeWarningTextSubscriber.assertNoValues()
        cardFeeForSelectedSubscriber.assertNoValues()
    }

    @Test
    fun haveCardFee() {
        givenGoodCheckoutParams()
        createMockFlightServices()
        setupSystemUnderTest()

        val formOfPaymentWithFee = createPaymentWithCardFee()

        val showCardFeesTestSubscriber = TestSubscriber<Boolean>()
        val cardFeeTextSubscriber = TestSubscriber<Spanned>()
        val cardFeeWarningTextSubscriber = TestSubscriber<String>()
        val cardFeeForSelectedSubscriber = TestSubscriber<ValidFormOfPayment>()

        sut.showCardFees.subscribe(showCardFeesTestSubscriber)
        sut.cardFeeTextSubject.subscribe(cardFeeTextSubscriber)
        sut.cardFeeWarningTextSubject.subscribe(cardFeeWarningTextSubscriber)
        sut.cardFeeForSelectedCard.subscribe(cardFeeForSelectedSubscriber)

        selectedCardTypeSubject.onNext(PaymentType.CARD_AMERICAN_EXPRESS)
        sut.validFormsOfPaymentSubject.onNext(listOf(formOfPaymentWithFee))

        showCardFeesTestSubscriber.assertValues(false, true)

        cardFeeTextSubscriber.assertValueCount(1)
        assertEquals("Airline processing fee for this card: $2.50", cardFeeTextSubscriber.onNextEvents[0].toString())

        cardFeeWarningTextSubscriber.assertValueCount(1)
        cardFeeWarningTextSubscriber.assertValue("The airline charges a processing fee of $2.50 for using this card (cost included in the trip total).")

        cardFeeForSelectedSubscriber.assertValueCount(1)
        cardFeeForSelectedSubscriber.assertValue(formOfPaymentWithFee)
    }

    @Test
    fun flightServicesCheckoutCalledWithGoodParams() {
        givenGoodCheckoutParams()
        createMockFlightServices()
        setupSystemUnderTest()

        sut.checkoutParams.onNext(params)

        Mockito.verify(mockFlightServices).checkout(params.toQueryMap())
    }

    private fun givenGoodTripResponse() {
        val tripId = "1234"
        val tealeafTransactionId = "tealeaf-1234"
        val totalPrice = "42.00"
        val currencyCode = "USD"

        newTripResponse = FlightCreateTripResponse()
        newTripResponse.tripId = tripId
        newTripResponse.newTrip = TripDetails("", "", tripId)
        newTripResponse.totalPrice = Money(totalPrice, currencyCode)
        newTripResponse.selectedCardFees = Money("2.50", currencyCode)
        newTripResponse.tealeafTransactionId = tealeafTransactionId
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

    private fun makeBillingInfo(): BillingInfo {
        val billingInfo = BillingInfo()
        billingInfo.expirationDate = LocalDate()
        billingInfo.location = Location()
        billingInfo.email = "test@expedia.com"
        return billingInfo
    }

    private fun givenGoodCheckoutParams() {
        val billingInfo = makeBillingInfo()
        val traveler = Traveler()
        traveler.middleName = ""
        traveler.birthDate = LocalDate()
        traveler.setPassengerCategory(PassengerCategory.ADULT)
        traveler.redressNumber = ""
        params = FlightCheckoutParams.Builder()
                        .tealeafTransactionId("")
                        .travelers(listOf(traveler))
                        .billingInfo(billingInfo)
                        .expectedFareCurrencyCode("USD")
                        .expectedTotalFare("$42")
                        .tripId("1234")
                        .cvv("123")
                        .build() as FlightCheckoutParams
    }

    private fun createMockFlightServices() {
        givenGoodCheckoutParams()

        mockFlightServices = Mockito.mock(FlightServices::class.java)
        val checkoutResponseObservable = PublishSubject.create<FlightCheckoutResponse>()
        Mockito.`when`(mockFlightServices.checkout(params.toQueryMap())).thenReturn(checkoutResponseObservable)
    }
}
