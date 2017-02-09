package com.expedia.bookings.presenter.flight

import com.expedia.bookings.interceptors.MockInterceptor
import com.expedia.bookings.services.ItinTripServices
import com.expedia.bookings.test.MultiBrand
import com.expedia.bookings.test.RunForBrands
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.utils.Ui
import com.expedia.vm.itin.AddGuestItinViewModel
import com.mobiata.mocke3.ExpediaDispatcher
import com.mobiata.mocke3.FileSystemOpener
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import okhttp3.mockwebserver.MockWebServer
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RuntimeEnvironment
import rx.observers.TestSubscriber
import rx.schedulers.Schedulers
import java.io.File
import java.util.concurrent.TimeUnit
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@RunWith(RobolectricRunner::class)
class AddGuestItinAPIErrorTest {

    private var server: MockWebServer = MockWebServer()
        @Rule get

    private val context = RuntimeEnvironment.application

    lateinit private var sut: AddGuestItinViewModel
    lateinit private var tripServices: ItinTripServices

    @Before
    fun before() {
        val root = File("../lib/mocked/templates").canonicalPath
        val opener = FileSystemOpener(root)
        server.setDispatcher(ExpediaDispatcher(opener))

        val logger = HttpLoggingInterceptor()
        logger.level = HttpLoggingInterceptor.Level.BODY

        val interceptor = MockInterceptor()
        tripServices = ItinTripServices("http://localhost:" + server.port,
                OkHttpClient.Builder().addInterceptor(logger).build(),
                interceptor, Schedulers.immediate(), Schedulers.immediate())

        Ui.getApplication(context).defaultTripComponents()
        sut = AddGuestItinViewModel(context)
        sut.tripServices = tripServices
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun notAuthenticatedGuestItinError() {
        val showErrorMessageSubscriber = TestSubscriber<String>()
        val showSearchDialogSubscriber = TestSubscriber<Boolean>()

        sut.showSearchDialogObservable.subscribe(showSearchDialogSubscriber)
        sut.showErrorMessageObservable.subscribe(showErrorMessageSubscriber)

        sut.performGuestTripSearch.onNext(Pair("trip_error@mobiata.com", "error_trip_response"))
        assertTrue(showSearchDialogSubscriber.onNextEvents[0]) // true -> show progress bar
        assertFalse(showSearchDialogSubscriber.onNextEvents[1]) // false -> after search returns hide progress bar

        showErrorMessageSubscriber.awaitTerminalEvent(1000, TimeUnit.MILLISECONDS)
        showErrorMessageSubscriber.assertValue("This is not a guest itinerary. Please sign into the Expedia account associated with this itinerary.")
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun badGuestItinRequestError() {
        val showErrorMessageSubscriber = TestSubscriber<String>()
        val showSearchDialogSubscriber = TestSubscriber<Boolean>()

        sut.showSearchDialogObservable.subscribe(showSearchDialogSubscriber)
        sut.showErrorMessageObservable.subscribe(showErrorMessageSubscriber)

        sut.performGuestTripSearch.onNext(Pair("trip_error@mobiata.com", "error_bad_request_trip_response"))
        assertTrue(showSearchDialogSubscriber.onNextEvents[0]) // true -> show progress bar
        assertFalse(showSearchDialogSubscriber.onNextEvents[1]) // false -> after search returns hide progress bar

        showErrorMessageSubscriber.awaitTerminalEvent(1000, TimeUnit.MILLISECONDS)
        showErrorMessageSubscriber.assertValue("Unable to find itinerary. Please confirm on the Account screen that the Country setting matches the website address for your booking.")
    }

}