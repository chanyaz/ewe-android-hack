package com.expedia.vm.test.robolectric

import android.app.Activity
import android.content.Context
import android.content.DialogInterface
import android.text.SpannableStringBuilder
import android.text.Spanned
import com.expedia.bookings.R
import com.expedia.bookings.data.BillingInfo
import com.expedia.bookings.data.Location
import com.expedia.bookings.data.Money
import com.expedia.bookings.data.Traveler
import com.expedia.bookings.data.TripDetails
import com.expedia.bookings.data.flights.FlightCheckoutParams
import com.expedia.bookings.data.flights.FlightCheckoutResponse
import com.expedia.bookings.data.flights.FlightCreateTripResponse
import com.expedia.bookings.enums.PassengerCategory
import com.expedia.bookings.interceptors.MockInterceptor
import com.expedia.bookings.services.CardFeeService
import com.expedia.bookings.services.FlightServices
import com.expedia.bookings.test.PointOfSaleTestConfiguration
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.utils.Ui
import com.expedia.vm.FlightCheckoutViewModel
import com.mobiata.mocke3.ExpediaDispatcher
import com.mobiata.mocke3.FileSystemOpener
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import okhttp3.mockwebserver.MockWebServer
import org.joda.time.LocalDate
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.robolectric.Robolectric
import org.robolectric.RuntimeEnvironment
import org.robolectric.Shadows
import org.robolectric.shadows.ShadowAlertDialog
import rx.Scheduler
import rx.android.schedulers.AndroidSchedulers
import rx.observers.TestSubscriber
import rx.schedulers.Schedulers
import rx.subjects.BehaviorSubject
import rx.subjects.PublishSubject
import java.io.File
import java.io.IOException
import kotlin.test.assertEquals

@RunWith(RobolectricRunner::class)
class FlightCheckoutViewModelTest {

    val server = MockWebServer()

    private val context = RuntimeEnvironment.application

    lateinit private var sut: FlightCheckoutViewModel
    lateinit private var mockFlightServices: FlightServices
    lateinit private var cardFeeService: CardFeeService
    lateinit private var params: FlightCheckoutParams
    lateinit private var newTripResponse: FlightCreateTripResponse

    @Test
    fun debitCardNotAccepted() {
        PointOfSaleTestConfiguration.configurePointOfSale(RuntimeEnvironment.application, "MockSharedData/pos_with_no_debit_cards_permitted.json")
        createMockFlightServices()
        setupSystemUnderTest()

        val debitCardNotAcceptedSubscriber = TestSubscriber<Boolean>()
        sut.showDebitCardsNotAcceptedSubject.subscribe(debitCardNotAcceptedSubscriber)

        debitCardNotAcceptedSubscriber.assertValue(true)
    }

    @Test
    fun debitCardAccepted() {
        PointOfSaleTestConfiguration.configurePointOfSale(RuntimeEnvironment.application, "MockSharedData/pos_test_config.json")
        createMockFlightServices()
        setupSystemUnderTest()

        val debitCardNotAcceptedSubscriber = TestSubscriber<Boolean>()
        sut.showDebitCardsNotAcceptedSubject.subscribe(debitCardNotAcceptedSubscriber)

        debitCardNotAcceptedSubscriber.assertValue(false)
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
        setupCardFeeService()
        createMockFlightServices()
        givenGoodTripResponse()
        setupSystemUnderTest()

        val sliderPurchaseTotalTextTestSubscriber = TestSubscriber<CharSequence>()
        sut.sliderPurchaseTotalText.subscribe(sliderPurchaseTotalTextTestSubscriber)

        sut.tripResponseObservable.onNext(newTripResponse)
        sut.paymentViewModel.cardBIN.onNext("654321")

        sliderPurchaseTotalTextTestSubscriber.assertValueCount(2)
        assertEquals("Your card will be charged $42.00", sliderPurchaseTotalTextTestSubscriber.onNextEvents[0].toString())
        assertEquals("Your card will be charged $44.50", sliderPurchaseTotalTextTestSubscriber.onNextEvents[1].toString())
    }

    @Test
    fun storedCardPaymentFees() {
        givenGoodCheckoutParams()
        setupCardFeeService()
        createMockFlightServices()
        givenGoodTripResponse()
        setupSystemUnderTest()

        val selectedCardFeeSubscriber = TestSubscriber<Money>()
        sut.selectedCardFeeObservable.subscribe(selectedCardFeeSubscriber)

        sut.tripResponseObservable.onNext(newTripResponse)
        sut.paymentViewModel.storedPaymentInstrumentId.onNext("stored-payment-id")

        selectedCardFeeSubscriber.assertValueCount(1)
        assertEquals(2.5, selectedCardFeeSubscriber.onNextEvents[0].amount.toDouble())
        assertEquals("USD", selectedCardFeeSubscriber.onNextEvents[0].currencyCode)
    }

    @Test
    fun guestCardPaymentFees() {
        givenGoodCheckoutParams()
        setupCardFeeService()
        createMockFlightServices()
        givenGoodTripResponse()
        setupSystemUnderTest()

        val selectedCardFeeSubscriber = TestSubscriber<Money>()
        sut.selectedCardFeeObservable.subscribe(selectedCardFeeSubscriber)

        sut.tripResponseObservable.onNext(newTripResponse)
        sut.paymentViewModel.cardBIN.onNext("654321")

        selectedCardFeeSubscriber.assertValueCount(1)
        assertEquals(2.5, selectedCardFeeSubscriber.onNextEvents[0].amount.toDouble())
        assertEquals("USD", selectedCardFeeSubscriber.onNextEvents[0].currencyCode)
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
    fun selectedFlightHasFeesShowCardFeeWarnings() {
        createMockFlightServices()
        setupSystemUnderTest()

        val cardFeeWarningTestSubscriber = TestSubscriber<Spanned>()
        sut.cardFeeWarningTextSubject.subscribe(cardFeeWarningTestSubscriber)

        sut.obFeeDetailsUrlSubject.onNext("http://url")
        sut.selectedFlightChargesFees.onNext("Airline Fee")

        cardFeeWarningTestSubscriber.assertValueCount(1)
        assertEquals("An airline fee, based on card type, is added upon payment. Such fee is added to the total upon payment.",
                cardFeeWarningTestSubscriber.onNextEvents[0].toString())
    }

    @Test
    fun selectedFlightHasNoFeesDontShowCardFeeWarnings() {
        createMockFlightServices()
        setupSystemUnderTest()

        val cardFeeWarningTestSubscriber = TestSubscriber<Spanned>()
        sut.cardFeeWarningTextSubject.subscribe(cardFeeWarningTestSubscriber)

        sut.obFeeDetailsUrlSubject.onNext("")
        sut.selectedFlightChargesFees.onNext("")

        cardFeeWarningTestSubscriber.assertValueCount(1)
        cardFeeWarningTestSubscriber.assertValue(null)
    }

    @Test
    fun zeroCardFees() {
        givenGoodCheckoutParams()
        givenGoodTripResponse()
        setupCardFeeService()
        createMockFlightServices()
        setupSystemUnderTest()

        val cardFeeTextSubscriber = TestSubscriber<Spanned>()
        val cardFeeWarningTextSubscriber = TestSubscriber<Spanned>()
        val hasCardFeeTestSubscriber = TestSubscriber<Boolean>()

        sut.cardFeeTextSubject.subscribe(cardFeeTextSubscriber)
        sut.cardFeeWarningTextSubject.subscribe(cardFeeWarningTextSubscriber)
        sut.paymentTypeSelectedHasCardFee.subscribe(hasCardFeeTestSubscriber)

        sut.tripResponseObservable.onNext(newTripResponse)
        sut.paymentViewModel.cardBIN.onNext("")

        hasCardFeeTestSubscriber.assertValues(false, false, false)
        cardFeeTextSubscriber.assertValueCount(1)
        cardFeeWarningTextSubscriber.assertValueCount(1)
        assertEquals("", cardFeeTextSubscriber.onNextEvents[0].toString())
        assertEquals("", cardFeeWarningTextSubscriber.onNextEvents[0].toString())
    }

    @Test
    fun haveCardFee() {
        givenGoodCheckoutParams()
        givenGoodTripResponse()
        setupCardFeeService()
        createMockFlightServices()
        setupSystemUnderTest()

        val cardFeeTextSubscriber = TestSubscriber<Spanned>()
        val cardFeeWarningTextSubscriber = TestSubscriber<Spanned>()
        val hasCardFeeTestSubscriber = TestSubscriber<Boolean>()

        sut.cardFeeTextSubject.subscribeOn(AndroidSchedulers.mainThread()).subscribe(cardFeeTextSubscriber)
        sut.cardFeeWarningTextSubject.subscribeOn(AndroidSchedulers.mainThread()).subscribe(cardFeeWarningTextSubscriber)
        sut.paymentTypeSelectedHasCardFee.subscribeOn(AndroidSchedulers.mainThread()).subscribe(hasCardFeeTestSubscriber)

        sut.tripResponseObservable.onNext(newTripResponse)
        sut.paymentViewModel.cardBIN.onNext("654321")

        cardFeeTextSubscriber.assertValueCount(1)
        assertEquals("Airline processing fee for this card: $2.50", cardFeeTextSubscriber.onNextEvents[0].toString())

        cardFeeWarningTextSubscriber.assertValueCount(1)
        assertEquals("The airline charges a processing fee of $2.50 for using this card (cost included in the trip total).",
                cardFeeWarningTextSubscriber.onNextEvents[0].toString())

        hasCardFeeTestSubscriber.assertValues(false, false, true)
    }

    @Test
    fun cardFeeClearedOnCardChange() {
        givenGoodCheckoutParams()
        givenGoodTripResponse()
        setupCardFeeService()
        createMockFlightServices()
        setupSystemUnderTest()

        val cardFeeTextSubscriber = TestSubscriber<Spanned>()
        val cardFeeWarningTextSubscriber = TestSubscriber<Spanned>()
        val hasCardFeeTestSubscriber = TestSubscriber<Boolean>()

        sut.cardFeeTextSubject.subscribe(cardFeeTextSubscriber)
        sut.cardFeeWarningTextSubject.subscribe(cardFeeWarningTextSubscriber)
        sut.paymentTypeSelectedHasCardFee.subscribe(hasCardFeeTestSubscriber)

        sut.tripResponseObservable.onNext(newTripResponse)
        sut.paymentViewModel.cardBIN.onNext("654321")
        sut.paymentViewModel.cardBIN.onNext("")

        cardFeeTextSubscriber.assertValueCount(2)
        assertEquals("Airline processing fee for this card: $2.50", cardFeeTextSubscriber.onNextEvents[0].toString())
        assertEquals("", cardFeeTextSubscriber.onNextEvents[1].toString())

        cardFeeWarningTextSubscriber.assertValueCount(2)
        assertEquals("The airline charges a processing fee of $2.50 for using this card (cost included in the trip total).",
                cardFeeWarningTextSubscriber.onNextEvents[0].toString())
        assertEquals("", cardFeeWarningTextSubscriber.onNextEvents[1].toString())

        hasCardFeeTestSubscriber.assertValues(false, false, true, false, false)
    }

    @Test
    fun flightServicesCheckoutCalledWithGoodParams() {
        givenGoodCheckoutParams()
        createMockFlightServices()
        setupSystemUnderTest()

        sut.checkoutParams.onNext(params)

        Mockito.verify(mockFlightServices).checkout(params.toQueryMap())
    }

    @Test
    fun showPaymentFeesHiddenOnCreateTripResponse() {
        createMockFlightServices()
        givenGoodTripResponse()
        setupSystemUnderTest()

        val hasCardFeeTestSubscriber = TestSubscriber<Boolean>()
        sut.paymentTypeSelectedHasCardFee.subscribe(hasCardFeeTestSubscriber)

        sut.tripResponseObservable.onNext(newTripResponse)

        hasCardFeeTestSubscriber.assertValue(false)
    }

    @Test
    fun networkErrorDialogCancel() {
        val testSubscriber = TestSubscriber<Unit>()
        givenIOExceptionOnCheckoutRequest()
        setupSystemUnderTest()

        sut.noNetworkObservable.subscribe(testSubscriber)

        sut.checkoutParams.onNext(params)
        val noInternetDialog = ShadowAlertDialog.getLatestAlertDialog()
        val shadowAlertDialog = Shadows.shadowOf(noInternetDialog)
        val cancelBtn = noInternetDialog.getButton(DialogInterface.BUTTON_NEGATIVE)
        cancelBtn.performClick()

        assertEquals("", shadowAlertDialog.title)
        assertEquals("Your device is not connected to the internet.  Please check your connection and try again.", shadowAlertDialog.message)
        testSubscriber.assertValueCount(1)
    }

    @Test
    fun networkErrorDialogRetry() {
        givenIOExceptionOnCheckoutRequest()
        setupSystemUnderTest()

        sut.checkoutParams.onNext(params)
        val noInternetDialog = ShadowAlertDialog.getLatestAlertDialog()
        val shadowAlertDialog = Shadows.shadowOf(noInternetDialog)
        val retryBtn = noInternetDialog.getButton(DialogInterface.BUTTON_POSITIVE)
        retryBtn.performClick()
        retryBtn.performClick()

        assertEquals("", shadowAlertDialog.title)
        assertEquals("Your device is not connected to the internet.  Please check your connection and try again.", shadowAlertDialog.message)
        Mockito.verify(mockFlightServices, Mockito.times(3)).checkout(params.toQueryMap()) // 1 first attempt, 2 retries
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
        newTripResponse.tealeafTransactionId = tealeafTransactionId
    }

    private fun givenTripResponseWithFees() {
        givenGoodTripResponse()
        newTripResponse.selectedCardFees = Money("2.5", "USD")
    }

    private fun makeBillingInfo(): BillingInfo {
        val billingInfo = BillingInfo()
        billingInfo.expirationDate = LocalDate()
        billingInfo.location = Location()
        return billingInfo
    }

    private fun givenGoodCheckoutParams() {
        val billingInfo = makeBillingInfo()
        val traveler = Traveler()
        traveler.middleName = ""
        traveler.birthDate = LocalDate()
        traveler.email = ""
        traveler.passengerCategory = PassengerCategory.ADULT
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

    private fun givenIOExceptionOnCheckoutRequest() {
        givenGoodCheckoutParams()

        mockFlightServices = Mockito.mock(FlightServices::class.java)
        val observableWithIOException = BehaviorSubject.create<FlightCheckoutResponse>()
        observableWithIOException.onError(IOException())
        Mockito.`when`(mockFlightServices.checkout(params.toQueryMap()))
                .thenReturn(observableWithIOException)
    }

    private fun createMockFlightServices() {
        givenGoodCheckoutParams()

        mockFlightServices = Mockito.mock(FlightServices::class.java)
        val checkoutResponseObservable = PublishSubject.create<FlightCheckoutResponse>()
        Mockito.`when`(mockFlightServices.checkout(params.toQueryMap()))
                .thenReturn(checkoutResponseObservable)
    }

    private fun setupCardFeeService() {
        val root = File("../lib/mocked/templates").canonicalPath
        val opener = FileSystemOpener(root)
        server.setDispatcher(ExpediaDispatcher(opener))

        val logger = HttpLoggingInterceptor()
        logger.level = HttpLoggingInterceptor.Level.BODY
        val interceptor = MockInterceptor()
        cardFeeService = CardFeeService("http://localhost:" + server.port,
                OkHttpClient.Builder().addInterceptor(logger).build(),
                interceptor, Schedulers.immediate(), Schedulers.immediate())
    }

    private fun setupSystemUnderTest() {
        setupCardFeeService()
        val activity = Robolectric.buildActivity(Activity::class.java).create().get()
        activity.setTheme(R.style.FlightTheme)
        Ui.getApplication(context).defaultTravelerComponent()
        Ui.getApplication(context).defaultFlightComponents()
        sut = TestFlightCheckoutViewModelClass(context)
        sut.cardFeeTripResponse.subscribe(sut.tripResponseObservable)
        sut.flightServices = mockFlightServices
        sut.cardFeeService = cardFeeService
    }

    class TestFlightCheckoutViewModelClass(context: Context): FlightCheckoutViewModel(context) {
        override fun getScheduler(): Scheduler {
            return Schedulers.immediate()
        }
    }
}
