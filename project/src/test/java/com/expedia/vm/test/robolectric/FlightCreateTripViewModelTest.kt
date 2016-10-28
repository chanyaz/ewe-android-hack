package com.expedia.vm.test.robolectric

import com.expedia.bookings.data.ApiError
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.LineOfBusiness
import com.expedia.bookings.data.TripResponse
import com.expedia.bookings.data.flights.FlightCreateTripParams
import com.expedia.bookings.data.flights.ValidFormOfPayment
import com.expedia.bookings.interceptors.MockInterceptor
import com.expedia.bookings.services.FlightServices
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.utils.Ui
import com.expedia.vm.flights.FlightCreateTripViewModel
import com.mobiata.mocke3.ExpediaDispatcher
import com.mobiata.mocke3.FileSystemOpener
import okhttp3.logging.HttpLoggingInterceptor
import okhttp3.mockwebserver.MockWebServer
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RuntimeEnvironment
import rx.observers.TestSubscriber
import rx.schedulers.Schedulers
import rx.subjects.PublishSubject
import java.io.File
import java.io.IOException
import java.util.concurrent.TimeUnit
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

@RunWith(RobolectricRunner::class)
class FlightCreateTripViewModelTest {

    private val context = RuntimeEnvironment.application

    var server: MockWebServer = MockWebServer()
        @Rule get

    lateinit private var sut: FlightCreateTripViewModel
    lateinit private var flightServices: FlightServices
    lateinit private var selectedCardFeeSubject: PublishSubject<ValidFormOfPayment?>
    lateinit private var params: FlightCreateTripParams

    @Before
    fun setup() {
        Db.getTripBucket().clear(LineOfBusiness.FLIGHTS_V2)
        selectedCardFeeSubject = PublishSubject.create()
        createMockFlightServices()
        Ui.getApplication(context).defaultFlightComponents()
        sut = FlightCreateTripViewModel(context)
        sut.flightServices = flightServices
    }

    @Test
    fun createTripRequestFired() {
        givenGoodCreateTripParams()
        assertNull(Db.getTripBucket().flightV2)

        val tripResponseSubscriber = TestSubscriber<TripResponse>()
        val showCreateTripDialogSubscriber = TestSubscriber<Boolean>()
        sut.createTripResponseObservable.subscribe(tripResponseSubscriber)
        sut.showCreateTripDialogObservable.subscribe(showCreateTripDialogSubscriber)

        sut.tripParams.onNext(params)
        sut.performCreateTrip.onNext(Unit)

        tripResponseSubscriber.awaitTerminalEvent(200, TimeUnit.MILLISECONDS)
        tripResponseSubscriber.assertValueCount(1)
        assertNotNull(Db.getTripBucket().flightV2)
        showCreateTripDialogSubscriber.assertValues(true, false)
    }

    @Test
    fun createTripPriceChange() {
        givenCreateTripPriceChange()

        val tripResponseSubscriber = TestSubscriber<TripResponse>()
        val showCreateTripDialogSubscriber = TestSubscriber<Boolean>()
        val priceChangeSubscriber = TestSubscriber<TripResponse>()
        sut.createTripResponseObservable.subscribe(tripResponseSubscriber)
        sut.showCreateTripDialogObservable.subscribe(showCreateTripDialogSubscriber)
        sut.priceChangeObservable.subscribe(priceChangeSubscriber)

        sut.tripParams.onNext(params)
        sut.performCreateTrip.onNext(Unit)

        tripResponseSubscriber.awaitTerminalEvent(200, TimeUnit.MILLISECONDS)
        tripResponseSubscriber.assertValueCount(1)
        priceChangeSubscriber.assertValueCount(1)
        showCreateTripDialogSubscriber.assertValues(true, false)
    }

    @Test
    fun createTripError() {
        givenCreateTripResponseError()

        val tripResponseSubscriber = TestSubscriber<TripResponse>()
        val showCreateTripDialogSubscriber = TestSubscriber<Boolean>()
        val errorSubscriber = TestSubscriber<ApiError>()
        sut.createTripResponseObservable.subscribe(tripResponseSubscriber)
        sut.showCreateTripDialogObservable.subscribe(showCreateTripDialogSubscriber)
        sut.createTripErrorObservable.subscribe(errorSubscriber)

        sut.tripParams.onNext(params)
        sut.performCreateTrip.onNext(Unit)

        errorSubscriber.awaitTerminalEvent(200, TimeUnit.MILLISECONDS)
        tripResponseSubscriber.assertValueCount(0)
        errorSubscriber.assertValueCount(1)
        assertEquals(ApiError.Code.INVALID_INPUT, errorSubscriber.onNextEvents[0].errorCode)
        showCreateTripDialogSubscriber.assertValues(true, false)
    }

    @Test
    fun networkErrorDialogCancel() {
        val noInternetTestSubscriber = TestSubscriber<Unit>()
        givenGoodCreateTripParams()

        sut.showNoInternetRetryDialog.subscribe(noInternetTestSubscriber)
        givenCreateTripCallWithIOException()

        noInternetTestSubscriber.assertValueCount(1)
    }

    @Test
    fun networkErrorDialogRetry() {
        val testSubscriber = TestSubscriber<Unit>()
        givenGoodCreateTripParams()

        sut.showNoInternetRetryDialog.subscribe(testSubscriber)
        givenCreateTripCallWithIOException()

        testSubscriber.assertValueCount(1)
    }

    private fun givenCreateTripCallWithIOException() {
        sut.makeCreateTripResponseObserver().onError(IOException())
    }

    private fun givenCreateTripResponseError() {
        val productKey = "INVALID_INPUT"
        val withInsurance = false
        params = FlightCreateTripParams(productKey, withInsurance)
    }

    private fun givenCreateTripPriceChange() {
        val productKey = "create_trip_price_change"
        val withInsurance = false
        params = FlightCreateTripParams(productKey, withInsurance)
    }

    private fun givenGoodCreateTripParams() {
        val productKey = "happy_roundtrip_0"
        val withInsurance = false
        params = FlightCreateTripParams(productKey, withInsurance)
    }

    private fun createMockFlightServices() {
        val logger = HttpLoggingInterceptor()
        val root = File("../lib/mocked/templates").canonicalPath
        val opener = FileSystemOpener(root)
        val interceptor = MockInterceptor()
        logger.level = HttpLoggingInterceptor.Level.BODY
        server.setDispatcher(ExpediaDispatcher(opener))
        flightServices = FlightServices("http://localhost:" + server.port,
                okhttp3.OkHttpClient.Builder().addInterceptor(logger).build(),
                interceptor, Schedulers.immediate(), Schedulers.immediate())
    }

}
