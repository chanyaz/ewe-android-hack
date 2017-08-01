package com.expedia.vm.test.robolectric

import com.expedia.bookings.R
import com.expedia.bookings.data.ApiError
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.LineOfBusiness
import com.expedia.bookings.data.TripResponse
import com.expedia.bookings.data.abacus.AbacusUtils
import com.expedia.bookings.data.flights.FlightCreateTripParams
import com.expedia.bookings.data.flights.ValidFormOfPayment
import com.expedia.bookings.interceptors.MockInterceptor
import com.expedia.bookings.services.FlightServices
import com.expedia.bookings.test.robolectric.RoboTestHelper
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.utils.Ui
import com.expedia.vm.flights.FlightCreateTripViewModel
import com.mobiata.android.util.SettingUtils
import com.mobiata.mocke3.ExpediaDispatcher
import com.mobiata.mocke3.FileSystemOpener
import okhttp3.logging.HttpLoggingInterceptor
import okhttp3.mockwebserver.MockWebServer
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RuntimeEnvironment
import com.expedia.bookings.services.TestObserver
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
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
    lateinit private var builder: FlightCreateTripParams.Builder

    @Before
    fun setup() {
        Db.getTripBucket().clear(LineOfBusiness.FLIGHTS_V2)
        selectedCardFeeSubject = PublishSubject.create()
        createMockFlightServices()
        Ui.getApplication(context).defaultFlightComponents()
        sut = FlightCreateTripViewModel(context)
        sut.flightServices = flightServices
        builder = FlightCreateTripParams.Builder()
    }

    @Test
    fun createTripRequestFired() {
        givenGoodCreateTripParams()
        assertNull(Db.getTripBucket().flightV2)

<<<<<<< HEAD
        val tripResponseSubscriber = TestSubscriber<TripResponse>()
        val showCreateTripDialogSubscriber = TestSubscriber<Boolean>()
        sut.createTripResponseObservable.map { it.value }.subscribe(tripResponseSubscriber)
=======
        val tripResponseSubscriber = TestObserver<TripResponse>()
        val showCreateTripDialogSubscriber = TestObserver<Boolean>()
        sut.createTripResponseObservable.subscribe(tripResponseSubscriber)
>>>>>>> 7df61dae81... WIP
        sut.showCreateTripDialogObservable.subscribe(showCreateTripDialogSubscriber)

        sut.tripParams.onNext(params)
        sut.performCreateTrip.onNext(Unit)

        tripResponseSubscriber.awaitTerminalEvent(200, TimeUnit.MILLISECONDS)
        tripResponseSubscriber.assertValueCount(1)
        assertNotNull(Db.getTripBucket().flightV2)
        showCreateTripDialogSubscriber.assertValues(true, false)
    }

    @Test
    fun createTripError() {
        givenCreateTripResponseError()

<<<<<<< HEAD
        val tripResponseSubscriber = TestSubscriber<TripResponse>()
        val showCreateTripDialogSubscriber = TestSubscriber<Boolean>()
        val errorSubscriber = TestSubscriber<ApiError>()
        sut.createTripResponseObservable.map { it.value }.subscribe(tripResponseSubscriber)
=======
        val tripResponseSubscriber = TestObserver<TripResponse>()
        val showCreateTripDialogSubscriber = TestObserver<Boolean>()
        val errorSubscriber = TestObserver<ApiError>()
        sut.createTripResponseObservable.subscribe(tripResponseSubscriber)
>>>>>>> 7df61dae81... WIP
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
        val noInternetTestObserver = TestObserver<Unit>()
        givenGoodCreateTripParams()

        sut.showNoInternetRetryDialog.subscribe(noInternetTestObserver)
        givenCreateTripCallWithIOException()

        noInternetTestObserver.assertValueCount(1)
    }

    @Test
    fun networkErrorDialogRetry() {
        val testSubscriber = TestObserver<Unit>()
        givenGoodCreateTripParams()

        sut.showNoInternetRetryDialog.subscribe(testSubscriber)
        givenCreateTripCallWithIOException()

        testSubscriber.assertValueCount(1)
    }

    @Test
    fun createTripDialogVisibility() {
        val testSubscriber = TestSubscriber<Boolean>()
        givenGoodCreateTripParams()

        sut.showCreateTripDialogObservable.subscribe(testSubscriber)
        sut.tripParams.onNext(params)
        sut.performCreateTrip.onNext(Unit)
        testSubscriber.assertValueCount(2)

        SettingUtils.save(context, R.string.preference_flight_rate_detail_from_cache, true)
        RoboTestHelper.bucketTests(AbacusUtils.EBAndroidAppFlightRateDetailsFromCache)
        sut.performCreateTrip.onNext(Unit)
        testSubscriber.assertValueCount(2)
    }

    private fun givenCreateTripCallWithIOException() {
        sut.makeCreateTripResponseObserver().onError(IOException())
    }

    private fun givenCreateTripResponseError() {
        val productKey = "INVALID_INPUT"
        params =  builder.productKey(productKey).build()
    }

    private fun givenGoodCreateTripParams() {
        val productKey = "happy_round_trip"
        params =  builder.productKey(productKey).build()
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
                interceptor, Schedulers.trampoline(), Schedulers.trampoline())
    }

}
