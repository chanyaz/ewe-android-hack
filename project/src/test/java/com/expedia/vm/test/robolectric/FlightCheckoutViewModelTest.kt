package com.expedia.vm.test.robolectric

import android.app.Activity
import android.content.Context
import android.text.SpannableStringBuilder
import android.text.Spanned
import com.expedia.bookings.R
import com.expedia.bookings.data.ApiError
import com.expedia.bookings.data.BaseApiResponse
import com.expedia.bookings.data.BillingInfo
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.Location
import com.expedia.bookings.data.Money
import com.expedia.bookings.data.Traveler
import com.expedia.bookings.data.TripBucketItemFlightV2
import com.expedia.bookings.data.TripDetails
import com.expedia.bookings.data.TripResponse
import com.expedia.bookings.data.flights.FlightCheckoutParams
import com.expedia.bookings.data.flights.FlightCreateTripResponse
import com.expedia.bookings.data.flights.FlightTripDetails
import com.expedia.bookings.data.pos.PointOfSale
import com.expedia.bookings.data.pos.PointOfSaleId
import com.expedia.bookings.enums.PassengerCategory
import com.expedia.bookings.interceptors.MockInterceptor
import com.expedia.bookings.services.CardFeeService
import com.expedia.bookings.services.FlightServices
import com.expedia.bookings.test.PointOfSaleTestConfiguration
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.utils.Ui
import com.expedia.vm.FlightCheckoutViewModel
import com.mobiata.android.util.SettingUtils
import com.mobiata.mocke3.ExpediaDispatcher
import com.mobiata.mocke3.FileSystemOpener
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import okhttp3.mockwebserver.MockWebServer
import org.joda.time.LocalDate
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RuntimeEnvironment
import rx.Scheduler
import rx.android.schedulers.AndroidSchedulers
import rx.observers.TestSubscriber
import rx.schedulers.Schedulers
import java.io.File
import java.io.IOException
import java.util.concurrent.TimeUnit
import kotlin.test.assertEquals

@RunWith(RobolectricRunner::class)
class FlightCheckoutViewModelTest {

    private var server: MockWebServer = MockWebServer()
        @Rule get

    private val context = RuntimeEnvironment.application

    lateinit private var sut: FlightCheckoutViewModel
    lateinit private var flightServices: FlightServices
    lateinit private var cardFeeService: CardFeeService
    lateinit private var params: FlightCheckoutParams
    lateinit private var newTripResponse: FlightCreateTripResponse

    @Test
    fun debitCardNotAccepted() {
        PointOfSaleTestConfiguration.configurePointOfSale(RuntimeEnvironment.application, "MockSharedData/pos_with_no_debit_cards_permitted.json")
        setupSystemUnderTest()

        val debitCardNotAcceptedSubscriber = TestSubscriber<Boolean>()
        sut.showDebitCardsNotAcceptedSubject.subscribe(debitCardNotAcceptedSubscriber)

        debitCardNotAcceptedSubscriber.assertValue(true)
    }

    @Test
    fun debitCardAccepted() {
        PointOfSaleTestConfiguration.configurePointOfSale(RuntimeEnvironment.application, "MockSharedData/pos_test_config.json")
        setupSystemUnderTest()

        val debitCardNotAcceptedSubscriber = TestSubscriber<Boolean>()
        sut.showDebitCardsNotAcceptedSubject.subscribe(debitCardNotAcceptedSubscriber)

        debitCardNotAcceptedSubscriber.assertValue(false)
    }

    @Test
    fun legalTextObservable() {
        givenGoodTripResponse()
        setupSystemUnderTest()

        val legalTextTestSubscriber = TestSubscriber<SpannableStringBuilder>()
        sut.legalText.subscribe(legalTextTestSubscriber)

        sut.createTripResponseObservable.onNext(newTripResponse)

        legalTextTestSubscriber.assertValueCount(1)
        assertEquals("By completing this booking I agree that I have read and accept the Rules and Restrictions, the Terms and Conditions, the Privacy Policy and Fare Information.", legalTextTestSubscriber.onNextEvents[0].toString())
    }

    @Test
    fun slideToPurchaseTotalLabel() {
        givenGoodCheckoutParams()
        setupCardFeeService()
        givenGoodTripResponse()
        setupSystemUnderTest()

        val sliderPurchaseTotalTextTestSubscriber = TestSubscriber<CharSequence>()
        sut.sliderPurchaseTotalText.subscribe(sliderPurchaseTotalTextTestSubscriber)

        sut.createTripResponseObservable.onNext(newTripResponse)
        sut.paymentViewModel.cardBIN.onNext("654321")

        sliderPurchaseTotalTextTestSubscriber.assertValueCount(2)
        assertEquals("Your card will be charged $42.00", sliderPurchaseTotalTextTestSubscriber.onNextEvents[0].toString())
        assertEquals("Your card will be charged $44.50", sliderPurchaseTotalTextTestSubscriber.onNextEvents[1].toString())

        givenAirlineChargesFees()

        sut.createTripResponseObservable.onNext(newTripResponse)
        sliderPurchaseTotalTextTestSubscriber.assertValueCount(3)
        assertEquals("Your card will be charged $44.50 (plus airline fee)", sliderPurchaseTotalTextTestSubscriber.onNextEvents[2].toString())
    }

    @Test
    fun guestCardPaymentFees() {
        givenGoodCheckoutParams()
        givenGoodTripResponse()
        setupSystemUnderTest()

        val selectedCardFeeSubscriber = TestSubscriber<Money>()
        sut.selectedCardFeeObservable.subscribe(selectedCardFeeSubscriber)

        sut.createTripResponseObservable.onNext(newTripResponse)
        sut.paymentViewModel.cardBIN.onNext("654321")

        selectedCardFeeSubscriber.assertValueCount(1)
        assertEquals(2.5, selectedCardFeeSubscriber.onNextEvents[0].amount.toDouble())
        assertEquals("USD", selectedCardFeeSubscriber.onNextEvents[0].currencyCode)
    }

    @Test
    fun flightCheckoutParamsBuiltOnNewCreateTripResponse() {
        givenGoodCheckoutParams()
        givenGoodTripResponse()
        setupSystemUnderTest()

        val legalTextTestSubscriber = TestSubscriber<SpannableStringBuilder>()
        val sliderPurchaseTotalTextTestSubscriber = TestSubscriber<CharSequence>()

        sut.legalText.subscribe(legalTextTestSubscriber)
        sut.sliderPurchaseTotalText.subscribe(sliderPurchaseTotalTextTestSubscriber)

        sut.createTripResponseObservable.onNext(newTripResponse)

        // populate builder with required data not set on receipt of a createTrip response (just for this unit test)
        sut.builder.billingInfo(makeBillingInfo()).travelers(listOf(Traveler())).cvv("")

        val flightCheckoutParams = sut.builder.build()
        assertEquals(newTripResponse.tealeafTransactionId, flightCheckoutParams.tealeafTransactionId)
        val offerTotalPrice = newTripResponse.details.offer.totalPrice
        assertEquals(offerTotalPrice.currencyCode, flightCheckoutParams.expectedFareCurrencyCode)
        assertEquals(offerTotalPrice.amount.toString(), flightCheckoutParams.expectedTotalFare)
        assertEquals(newTripResponse.tripId, flightCheckoutParams.tripId)
    }

    @Test
    fun selectedFlightHasFeesShowCardFeeWarnings() {
        setupSystemUnderTest()

        val cardFeeWarningTestSubscriber = TestSubscriber<Spanned>()
        sut.cardFeeWarningTextSubject.subscribe(cardFeeWarningTestSubscriber)

        givenObFeeUrl()
        givenAirlineChargesFees()

        cardFeeWarningTestSubscriber.assertValueCount(2)
        assertEquals("", cardFeeWarningTestSubscriber.onNextEvents[0].toString())
        assertEquals("An airline fee, based on card type, is added upon payment. Such fee is added to the total upon payment.",
                cardFeeWarningTestSubscriber.onNextEvents[1].toString())

        setPOS(PointOfSaleId.FRANCE)

        givenAirlineChargesFees()
        cardFeeWarningTestSubscriber.assertValueCount(3)
        assertEquals("An airline fee, based on card type, may be added upon payment. Such fee is added to the total upon payment.",
                cardFeeWarningTestSubscriber.onNextEvents[2].toString())
    }

    private fun setPOS(pos: PointOfSaleId) {
        SettingUtils.save(context, R.string.PointOfSaleKey, pos.id.toString())
        PointOfSale.onPointOfSaleChanged(context)
    }

    @Test
    fun selectedFlightHasNoFeesDontShowCardFeeWarnings() {
        setupSystemUnderTest()

        val cardFeeWarningTestSubscriber = TestSubscriber<Spanned>()
        sut.cardFeeWarningTextSubject.subscribe(cardFeeWarningTestSubscriber)

        sut.obFeeDetailsUrlSubject.onNext("")
        sut.selectedFlightChargesFees.onNext("")

        cardFeeWarningTestSubscriber.assertValueCount(2)
        assertEquals("", cardFeeWarningTestSubscriber.onNextEvents[0].toString())
        assertEquals("", cardFeeWarningTestSubscriber.onNextEvents[1].toString())
    }

    @Test
    fun zeroCardFeesAirlineMayChargeFees() {
        givenGoodCheckoutParams()
        givenGoodTripResponse()
        setupCardFeeService()
        setupSystemUnderTest()

        givenAirlineChargesFees()
        givenObFeeUrl()

        val cardFeeTextSubscriber = TestSubscriber<Spanned>()
        val cardFeeWarningTextSubscriber = TestSubscriber<Spanned>()
        val hasCardFeeTestSubscriber = TestSubscriber<Boolean>()

        sut.cardFeeTextSubject.subscribe(cardFeeTextSubscriber)
        sut.cardFeeWarningTextSubject.subscribe(cardFeeWarningTextSubscriber)
        sut.paymentTypeSelectedHasCardFee.subscribe(hasCardFeeTestSubscriber)

        sut.createTripResponseObservable.onNext(newTripResponse)
        sut.paymentViewModel.resetCardFees.onNext(Unit)

        hasCardFeeTestSubscriber.assertValue(false)
        cardFeeTextSubscriber.assertValueCount(1)
        cardFeeWarningTextSubscriber.assertValueCount(1)
        assertEquals("", cardFeeTextSubscriber.onNextEvents[0].toString())
        assertEquals("An airline fee, based on card type, is added upon payment. Such fee is added to the total upon payment.",
                cardFeeWarningTextSubscriber.onNextEvents[0].toString())
    }

    @Test
    fun haveCardFee() {
        givenGoodCheckoutParams()
        givenGoodTripResponse()
        setupCardFeeService()
        setupSystemUnderTest()

        val cardFeeTextSubscriber = TestSubscriber<Spanned>()
        val cardFeeWarningTextSubscriber = TestSubscriber<Spanned>()
        val hasCardFeeTestSubscriber = TestSubscriber<Boolean>()

        sut.cardFeeTextSubject.subscribeOn(AndroidSchedulers.mainThread()).subscribe(cardFeeTextSubscriber)
        sut.cardFeeWarningTextSubject.subscribeOn(AndroidSchedulers.mainThread()).subscribe(cardFeeWarningTextSubscriber)
        sut.paymentTypeSelectedHasCardFee.subscribeOn(AndroidSchedulers.mainThread()).subscribe(hasCardFeeTestSubscriber)

        sut.createTripResponseObservable.onNext(newTripResponse)
        sut.paymentViewModel.cardBIN.onNext("654321")

        cardFeeTextSubscriber.assertValueCount(1)
        assertEquals("Airline processing fee for this card: $2.50", cardFeeTextSubscriber.onNextEvents[0].toString())

        cardFeeWarningTextSubscriber.assertValueCount(1)
        assertEquals("The airline charges a processing fee of $2.50 for using this card (cost included in the trip total).",
                cardFeeWarningTextSubscriber.onNextEvents[0].toString())

        hasCardFeeTestSubscriber.assertValue(true)
    }

    @Test
    fun cardFeeClearedOnCardChange() {
        givenGoodCheckoutParams()
        givenGoodTripResponse()
        setupCardFeeService()
        setupSystemUnderTest()

        val cardFeeTextSubscriber = TestSubscriber<Spanned>()
        val cardFeeWarningTextSubscriber = TestSubscriber<Spanned>()
        val hasCardFeeTestSubscriber = TestSubscriber<Boolean>()

        sut.cardFeeTextSubject.subscribe(cardFeeTextSubscriber)
        sut.cardFeeWarningTextSubject.subscribe(cardFeeWarningTextSubscriber)
        sut.paymentTypeSelectedHasCardFee.subscribe(hasCardFeeTestSubscriber)

        sut.createTripResponseObservable.onNext(newTripResponse)
        sut.paymentViewModel.cardBIN.onNext("654321")
        sut.paymentViewModel.resetCardFees.onNext(Unit)

        cardFeeTextSubscriber.assertValueCount(2)
        assertEquals("Airline processing fee for this card: $2.50", cardFeeTextSubscriber.onNextEvents[0].toString())
        assertEquals("", cardFeeTextSubscriber.onNextEvents[1].toString())

        cardFeeWarningTextSubscriber.assertValueCount(2)
        assertEquals("The airline charges a processing fee of $2.50 for using this card (cost included in the trip total).",
                cardFeeWarningTextSubscriber.onNextEvents[0].toString())
        assertEquals("", cardFeeWarningTextSubscriber.onNextEvents[1].toString())

        hasCardFeeTestSubscriber.assertValues(true, false)
    }

    @Test
    fun unknownError() {
        setupSystemUnderTest()
        givenUnknownErrorCheckoutParams()

        val testSubscriber = TestSubscriber<ApiError>()
        sut.checkoutErrorObservable.subscribe(testSubscriber)
        sut.checkoutParams.onNext(params)

        testSubscriber.awaitTerminalEvent(200, TimeUnit.MILLISECONDS)
        testSubscriber.assertValueCount(1)
        assertEquals(ApiError.Code.UNKNOWN_ERROR, testSubscriber.onNextEvents[0].errorCode)
    }

    @Test
    fun happyPath() {
        setupSystemUnderTest()
        givenGoodCheckoutParams()
        val createTripResponse = FlightCreateTripResponse()
        Db.getTripBucket().add(TripBucketItemFlightV2(createTripResponse))

        val testSubscriber = TestSubscriber<Pair<BaseApiResponse, String>>()
        sut.bookingSuccessResponse.subscribe(testSubscriber)
        sut.checkoutParams.onNext(params)

        testSubscriber.awaitTerminalEvent(200, TimeUnit.MILLISECONDS)
        testSubscriber.assertValueCount(1)
    }

    @Test
    fun priceChange() {
        setupSystemUnderTest()
        givenCheckoutPriceChangeParams()

        val testSubscriber = TestSubscriber<TripResponse>()
        sut.priceChangeObservable.subscribe(testSubscriber)
        sut.checkoutParams.onNext(params)

        testSubscriber.awaitTerminalEvent(200, TimeUnit.MILLISECONDS)
        testSubscriber.assertValueCount(1)
    }

    @Test
    fun networkErrorDialogCancel() {
        setupSystemUnderTest()

        val testSubscriber = TestSubscriber<Unit>()
        sut.showNoInternetRetryDialog.subscribe(testSubscriber)
        givenIOExceptionOnCheckoutRequest()

        testSubscriber.assertValueCount(1)
    }

    @Test
    fun networkErrorDialogRetry() {
        setupSystemUnderTest()

        val testSubscriber = TestSubscriber<Unit>()
        sut.showNoInternetRetryDialog.subscribe(testSubscriber)
        givenIOExceptionOnCheckoutRequest()

        testSubscriber.assertValueCount(1)
    }

    private fun givenIOExceptionOnCheckoutRequest() {
        sut.makeCheckoutResponseObserver().onError(IOException())
    }

    private fun givenGoodTripResponse() {
        val tripId = "1234"
        val tealeafTransactionId = "tealeaf-1234"
        val currencyCode = "USD"
        val totalPrice = Money("42.00", currencyCode)

        newTripResponse = FlightCreateTripResponse()
        newTripResponse.tripId = tripId
        newTripResponse.newTrip = TripDetails("", "", tripId)

        val details = FlightTripDetails()
        details.offer = FlightTripDetails.FlightOffer()
        details.offer.totalPrice = totalPrice
        newTripResponse.details = details
        newTripResponse.tealeafTransactionId = tealeafTransactionId
    }

    private fun makeBillingInfo(): BillingInfo {
        val billingInfo = BillingInfo()
        billingInfo.expirationDate = LocalDate()
        billingInfo.location = Location()
        billingInfo.email = "qa-ehcc@mobiata.com"
        billingInfo.firstName = "JexperCC"
        billingInfo.lastName = "MobiataTestaverde"
        billingInfo.nameOnCard = billingInfo.firstName + " " + billingInfo.lastName
        billingInfo.setNumberAndDetectType("4111111111111111")
        billingInfo.securityCode = "111"
        billingInfo.telephone = "4155555555"
        billingInfo.telephoneCountryCode = "1"
        billingInfo.expirationDate = LocalDate.now()

        val location = Location()
        location.streetAddress = arrayListOf("123 street")
        location.city = "city"
        location.stateCode = "CA"
        location.countryCode = "US"
        location.postalCode = "12334"
        billingInfo.location = location
        return billingInfo
    }

    private fun givenGoodCheckoutParams() {
        val billingInfo = makeBillingInfo()
        val traveler = makeTraveler()
        params = FlightCheckoutParams.Builder()
                        .tealeafTransactionId("tealeafFlight:happy_roundtrip_0")
                        .travelers(listOf(traveler))
                        .billingInfo(billingInfo)
                        .expectedFareCurrencyCode("USD")
                        .expectedTotalFare("$42")
                        .tripId("happy_roundtrip_0")
                        .cvv("123")
                        .build() as FlightCheckoutParams
    }

    private fun givenCheckoutPriceChangeParams() {
        val billingInfo = makeBillingInfo()
        val traveler = makeTraveler()
        params = FlightCheckoutParams.Builder()
                .tealeafTransactionId("tealeafFlight:checkout_price_change")
                .travelers(listOf(traveler))
                .billingInfo(billingInfo)
                .expectedFareCurrencyCode("USD")
                .expectedTotalFare("$42")
                .tripId("checkout_price_change")
                .cvv("123")
                .build() as FlightCheckoutParams
    }

    private fun givenUnknownErrorCheckoutParams() {
        params = FlightCheckoutParams.Builder()
                .tealeafTransactionId("tealeafFlight:UNKNOWN_ERROR")
                .travelers(listOf(makeTraveler()))
                .billingInfo(makeBillingInfo())
                .expectedFareCurrencyCode("USD")
                .expectedTotalFare("$42")
                .tripId("UNKNOWN_ERROR")
                .cvv("123")
                .build() as FlightCheckoutParams
    }

    private fun makeTraveler(): Traveler {
        val traveler = Traveler()
        traveler.firstName = "JexperCC"
        traveler.lastName = "MobiataTestaverde"
        traveler.birthDate = LocalDate()
        traveler.email = "qa-ehcc@mobiata.com"
        traveler.phoneNumber = "4155555555"
        traveler.phoneCountryCode = "US"
        traveler.passengerCategory = PassengerCategory.ADULT
        return traveler
    }

    private fun setupFlightService() {
        val logger = HttpLoggingInterceptor()
        logger.level = HttpLoggingInterceptor.Level.BODY
        val interceptor = MockInterceptor()
        flightServices = FlightServices("http://localhost:" + server.port,
                OkHttpClient.Builder().addInterceptor(logger).build(),
                interceptor, Schedulers.immediate(), Schedulers.immediate())
    }

    private fun setupCardFeeService() {
        val logger = HttpLoggingInterceptor()
        logger.level = HttpLoggingInterceptor.Level.BODY
        val interceptor = MockInterceptor()
        cardFeeService = CardFeeService("http://localhost:" + server.port,
                OkHttpClient.Builder().addInterceptor(logger).build(),
                interceptor, Schedulers.immediate(), Schedulers.immediate())
    }

    private fun setupMockServer() {
        val root = File("../lib/mocked/templates").canonicalPath
        val opener = FileSystemOpener(root)
        server.setDispatcher(ExpediaDispatcher(opener))
    }

    private fun setupSystemUnderTest() {
        setupMockServer()
        setupFlightService()
        setupCardFeeService()
        val activity = Robolectric.buildActivity(Activity::class.java).create().get()
        activity.setTheme(R.style.FlightTheme)
        Ui.getApplication(context).defaultTravelerComponent()
        Ui.getApplication(context).defaultFlightComponents()
        sut = TestFlightCheckoutViewModelClass(context)
        sut.email = "qa-ehcc@mobiata.com"
        sut.cardFeeTripResponse.subscribe(sut.createTripResponseObservable)
        sut.flightServices = flightServices
        sut.cardFeeService = cardFeeService
    }

    private fun givenObFeeUrl() {
        sut.obFeeDetailsUrlSubject.onNext("http://url")
    }

    private fun givenAirlineChargesFees() {
        sut.selectedFlightChargesFees.onNext("Airline Fee")
    }

    class TestFlightCheckoutViewModelClass(context: Context): FlightCheckoutViewModel(context) {
        override fun getScheduler(): Scheduler {
            return Schedulers.immediate()
        }
    }
}
