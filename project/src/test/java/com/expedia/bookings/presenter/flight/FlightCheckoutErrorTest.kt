package com.expedia.bookings.presenter.flight

import android.app.Activity
import android.content.Context
import com.expedia.bookings.R
import com.expedia.bookings.data.ApiError
import com.expedia.bookings.data.BillingInfo
import com.expedia.bookings.data.Location
import com.expedia.bookings.data.Traveler
import com.expedia.bookings.data.flights.FlightCheckoutParams
import com.expedia.bookings.enums.PassengerCategory
import com.expedia.bookings.interceptors.MockInterceptor
import com.expedia.bookings.services.FlightServices
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.utils.Ui
import com.expedia.vm.FlightCheckoutViewModel
import com.mobiata.mocke3.ExpediaDispatcher
import com.mobiata.mocke3.FileSystemOpener
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import okhttp3.mockwebserver.MockWebServer
import org.joda.time.LocalDate
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RuntimeEnvironment
import rx.Scheduler
import rx.observers.TestSubscriber
import rx.schedulers.Schedulers
import java.io.File
import java.util.concurrent.TimeUnit
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

@RunWith(RobolectricRunner::class)
class FlightCheckoutErrorTest {

    var server: MockWebServer = MockWebServer()
        @Rule get

    private val context = RuntimeEnvironment.application

    lateinit private var flightCheckoutVM: FlightCheckoutViewModel
    lateinit private var flightServices: FlightServices
    lateinit private var checkoutParams: FlightCheckoutParams

    @Before
    fun before() {
        val root = File("../lib/mocked/templates").canonicalPath
        val opener = FileSystemOpener(root)
        server.setDispatcher(ExpediaDispatcher(opener))

        val logger = HttpLoggingInterceptor()
        logger.level = HttpLoggingInterceptor.Level.BODY

        val interceptor = MockInterceptor()
        flightServices = FlightServices("http://localhost:" + server.port,
                OkHttpClient.Builder().addInterceptor(logger).build(),
                interceptor, Schedulers.immediate(), Schedulers.immediate())

        val activity = Robolectric.buildActivity(Activity::class.java).create().get()
        activity.setTheme(R.style.FlightTheme)
        Ui.getApplication(context).defaultTravelerComponent()
        Ui.getApplication(context).defaultFlightComponents()
        flightCheckoutVM = TestFlightCheckoutViewModelClass(context)
        flightCheckoutVM.flightServices = flightServices
    }

    @Test
    fun notNull() {
        assertNotNull(flightCheckoutVM)
    }

    @Test
    fun testTripAlreadyBooked() {
        createCheckoutParams("tealeafFlight:trip_already_booked", "trip_already_booked", "")
        val testSubscriber = TestSubscriber<ApiError>()
        flightCheckoutVM.checkoutErrorObservable.subscribe(testSubscriber)
        flightCheckoutVM.checkoutParams.onNext(checkoutParams)

        testSubscriber.awaitTerminalEvent(200, TimeUnit.MILLISECONDS)
        testSubscriber.assertValueCount(1)
        assertEquals(ApiError.Code.TRIP_ALREADY_BOOKED, testSubscriber.onNextEvents[0].errorCode)
    }

    @Test
    fun testPaymentFailed() {
        createCheckoutParams("tealeafFlight:payment_failed", "payment_failed", "")
        val testSubscriber = TestSubscriber<ApiError>()
        flightCheckoutVM.checkoutErrorObservable.subscribe(testSubscriber)
        flightCheckoutVM.checkoutParams.onNext(checkoutParams)

        testSubscriber.awaitTerminalEvent(200, TimeUnit.MILLISECONDS)
        testSubscriber.assertValueCount(1)
        assertEquals(ApiError.Code.PAYMENT_FAILED, testSubscriber.onNextEvents[0].errorCode)
    }

    @Test
    fun testSessionTimeOut() {
        createCheckoutParams("tealeafFlight:session_timeout", "session_timeout", "")
        val testSubscriber = TestSubscriber<ApiError>()
        flightCheckoutVM.checkoutErrorObservable.subscribe(testSubscriber)
        flightCheckoutVM.checkoutParams.onNext(checkoutParams)

        testSubscriber.awaitTerminalEvent(200, TimeUnit.MILLISECONDS)
        testSubscriber.assertValueCount(1)
        assertEquals(ApiError.Code.SESSION_TIMEOUT, testSubscriber.onNextEvents[0].errorCode)
    }

    @Test
    fun testUnknownError() {
        createCheckoutParams("tealeafFlight:UNKNOWN_ERROR", "UNKNOWN_ERROR", "unknownerror")
        val testSubscriber = TestSubscriber<ApiError>()
        flightCheckoutVM.checkoutErrorObservable.subscribe(testSubscriber)
        flightCheckoutVM.checkoutParams.onNext(checkoutParams)

        testSubscriber.awaitTerminalEvent(200, TimeUnit.MILLISECONDS)
        testSubscriber.assertValueCount(1)
        assertEquals(ApiError.Code.UNKNOWN_ERROR, testSubscriber.onNextEvents[0].errorCode)
    }

    @Test
    fun testInvalidInput() {
        createCheckoutParams("tealeafFlight:invalid_input", "invalid_input", "")
        val testSubscriber = TestSubscriber<ApiError>()
        flightCheckoutVM.checkoutErrorObservable.subscribe(testSubscriber)
        flightCheckoutVM.checkoutParams.onNext(checkoutParams)

        testSubscriber.awaitTerminalEvent(200, TimeUnit.MILLISECONDS)
        testSubscriber.assertValueCount(1)
        assertEquals(ApiError.Code.INVALID_INPUT, testSubscriber.onNextEvents[0].errorCode)
    }

    private fun createCheckoutParams(transactionId: String, tripId: String, billingInfo: String) {
        checkoutParams = FlightCheckoutParams.Builder()
                .tealeafTransactionId(transactionId)
                .flightLeg(emptyList())
                .travelers(listOf(makeTraveler()))
                .billingInfo(makeBillingInfo(billingInfo))
                .expectedFareCurrencyCode("USD")
                .expectedTotalFare("$42")
                .tripId(tripId)
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

    private fun makeBillingInfo(firstName: String = "JexperCC"): BillingInfo {
        val billingInfo = BillingInfo()
        billingInfo.expirationDate = LocalDate()
        billingInfo.location = Location()
        billingInfo.email = "qa-ehcc@mobiata.com"
        billingInfo.firstName = firstName
        billingInfo.lastName = "MobiataTestaverde"
        billingInfo.nameOnCard = billingInfo.firstName + " " + billingInfo.lastName
        billingInfo.setNumberAndDetectType("4111111111111111", context)
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

    class TestFlightCheckoutViewModelClass(context: Context) : FlightCheckoutViewModel(context) {
        override fun getScheduler(): Scheduler {
            return Schedulers.immediate()
        }
    }

}