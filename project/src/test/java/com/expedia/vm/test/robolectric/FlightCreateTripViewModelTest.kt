package com.expedia.vm.test.robolectric

import android.support.v4.app.FragmentActivity
import com.expedia.bookings.data.ApiError
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.LineOfBusiness
import com.expedia.bookings.data.TripResponse
import com.expedia.bookings.data.abacus.AbacusUtils
import com.expedia.bookings.data.flights.FlightCreateTripParams
import com.expedia.bookings.data.flights.ValidFormOfPayment
import com.expedia.bookings.interceptors.MockInterceptor
import com.expedia.bookings.services.FlightServices
import com.expedia.bookings.services.TestObserver
import com.expedia.bookings.test.robolectric.RoboTestHelper
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.utils.Ui
import com.expedia.vm.flights.FlightCreateTripViewModel
import com.mobiata.mocke3.ExpediaDispatcher
import com.mobiata.mocke3.FileSystemOpener
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import okhttp3.logging.HttpLoggingInterceptor
import okhttp3.mockwebserver.MockWebServer
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import java.io.File
import java.io.IOException
import java.util.concurrent.TimeUnit
import kotlin.properties.Delegates
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

@RunWith(RobolectricRunner::class)
class FlightCreateTripViewModelTest {

    private var activity: FragmentActivity by Delegates.notNull()

    var server: MockWebServer = MockWebServer()
        @Rule get

    private lateinit var sut: FlightCreateTripViewModel
    private lateinit var flightServices: FlightServices
    private lateinit var selectedCardFeeSubject: PublishSubject<ValidFormOfPayment?>
    private lateinit var params: FlightCreateTripParams
    private lateinit var builder: FlightCreateTripParams.Builder

    @Before
    fun setup() {
        activity = Robolectric.buildActivity(FragmentActivity::class.java).create().visible().get()
        Db.getTripBucket().clear(LineOfBusiness.FLIGHTS_V2)
        selectedCardFeeSubject = PublishSubject.create()
        createMockFlightServices()
        Ui.getApplication(activity).defaultFlightComponents()
        sut = FlightCreateTripViewModel(activity)
        sut.flightServices = flightServices
        builder = FlightCreateTripParams.Builder()
    }

    @Test
    fun createTripRequestFired() {
        givenGoodCreateTripParams()
        assertNull(Db.getTripBucket().flightV2)

        val tripResponseSubscriber = TestObserver<TripResponse>()
        val showCreateTripDialogSubscriber = TestObserver<Boolean>()
        sut.createTripResponseObservable.map { it.value }.subscribe(tripResponseSubscriber)
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

        val tripResponseSubscriber = TestObserver<TripResponse>()
        val showCreateTripDialogSubscriber = TestObserver<Boolean>()
        val errorSubscriber = TestObserver<ApiError>()
        sut.createTripResponseObservable.map { it.value }.subscribe(tripResponseSubscriber)
        sut.showCreateTripDialogObservable.subscribe(showCreateTripDialogSubscriber)
        sut.createTripErrorObservable.subscribe(errorSubscriber)

        sut.tripParams.onNext(params)
        sut.performCreateTrip.onNext(Unit)

        errorSubscriber.awaitTerminalEvent(200, TimeUnit.MILLISECONDS)
        tripResponseSubscriber.assertValueCount(0)
        errorSubscriber.assertValueCount(1)
        assertEquals(ApiError.Code.INVALID_INPUT, errorSubscriber.values()[0].getErrorCode())
        showCreateTripDialogSubscriber.assertValues(true, false)
    }

    @Test
    fun networkErrorDialogCancel() {
        val noInternetTestSubscriber = TestObserver<Unit>()
        givenGoodCreateTripParams()

        sut.showNoInternetRetryDialog.subscribe(noInternetTestSubscriber)
        givenCreateTripCallWithIOException()

        noInternetTestSubscriber.assertValueCount(1)
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
        val testSubscriber = TestObserver<Boolean>()
        givenGoodCreateTripParams()

        sut.showCreateTripDialogObservable.subscribe(testSubscriber)
        sut.tripParams.onNext(params)
        sut.performCreateTrip.onNext(Unit)
        testSubscriber.assertValueCount(2)

        RoboTestHelper.bucketTests(AbacusUtils.EBAndroidAppFlightRateDetailsFromCache)
        sut.performCreateTrip.onNext(Unit)
        testSubscriber.assertValueCount(2)
    }

    private fun givenCreateTripCallWithIOException() {
        sut.makeCreateTripResponseObserver().onError(IOException())
    }

    private fun givenCreateTripResponseError() {
        val productKey = "INVALID_INPUT"
        params = builder.productKey(productKey).build()
    }

    private fun givenGoodCreateTripParams() {
        val productKey = "happy_round_trip"
        params = builder.productKey(productKey).build()
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
                listOf(interceptor), Schedulers.trampoline(), Schedulers.trampoline())
    }
}
