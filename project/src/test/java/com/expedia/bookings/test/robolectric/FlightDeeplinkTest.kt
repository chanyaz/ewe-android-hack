package com.expedia.bookings.test.robolectric

import android.app.Activity
import android.text.Html
import com.expedia.bookings.data.FlightSearchParams
import com.expedia.bookings.data.Location
import com.expedia.bookings.data.TravelerParams
import com.expedia.bookings.interceptors.MockInterceptor
import com.expedia.bookings.services.FlightServices
import com.expedia.bookings.utils.Ui
import com.expedia.ui.FlightActivity
import com.expedia.vm.FlightSearchViewModel
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import okhttp3.mockwebserver.MockWebServer
import org.joda.time.LocalDate
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import rx.observers.TestSubscriber
import rx.schedulers.Schedulers
import kotlin.properties.Delegates.notNull
import kotlin.test.assertTrue

@RunWith(RobolectricRunner::class)
class FlightDeeplinkTest {

    var activity: Activity by notNull()
    var flightSearchViewModel: FlightSearchViewModel by notNull()

    @Before
    fun setup() {
        val server = MockWebServer()
        val interceptor = MockInterceptor()
        val logger = HttpLoggingInterceptor()
        logger.level = HttpLoggingInterceptor.Level.BODY
        activity = Robolectric.buildActivity(Activity::class.java).create().get()
        Ui.getApplication(activity).defaultTravelerComponent()
        val flightServices = FlightServices("http://localhost:" + server.getPort(),
                OkHttpClient.Builder().addInterceptor(logger).build(),
                interceptor, Schedulers.immediate(), Schedulers.immediate())
        flightSearchViewModel = FlightSearchViewModel(activity, flightServices)
    }

    @Test
    fun testIncompleteParams() {
        val testTransitionSubscriber = TestSubscriber<FlightActivity.Screen>()
        flightSearchViewModel.deeplinkDefaultTransitionObservable.subscribe(testTransitionSubscriber)
        flightSearchViewModel.deeplinkFlightSearchParamsObserver.onNext(getFlightSearchParams(false))
        testTransitionSubscriber.assertReceivedOnNext(listOf(FlightActivity.Screen.SEARCH))
    }

    @Test
    fun testCompleteParams() {
        val testTransitionSubscriber = TestSubscriber<FlightActivity.Screen>()
        val testDateSubscriber = TestSubscriber<Pair<LocalDate?, LocalDate?>>()
        val testOriginSubscriber = TestSubscriber<String>()
        val testDestinationSubscriber = TestSubscriber<String>()
        val testTravelersSubscriber = TestSubscriber<TravelerParams>()

        flightSearchViewModel.datesObservable.subscribe(testDateSubscriber)
        flightSearchViewModel.formattedOriginObservable.subscribe(testOriginSubscriber)
        flightSearchViewModel.formattedDestinationObservable.subscribe(testDestinationSubscriber)
        flightSearchViewModel.deeplinkDefaultTransitionObservable.subscribe(testTransitionSubscriber)
        flightSearchViewModel.travelersObservable.subscribe(testTravelersSubscriber)

        val flightSearchParams = getFlightSearchParams(true)
        flightSearchViewModel.deeplinkFlightSearchParamsObserver.onNext(flightSearchParams)

        testTransitionSubscriber.assertReceivedOnNext(listOf(FlightActivity.Screen.RESULTS))
        assertTrue(flightSearchViewModel.getParamsBuilder().areRequiredParamsFilled())

        testDateSubscriber.assertValue(Pair(flightSearchParams.departureDate, flightSearchParams.returnDate))
        testOriginSubscriber.assertValue(Html.fromHtml(flightSearchParams.departureLocation.destinationId).toString())
        testDestinationSubscriber.assertValue(Html.fromHtml(flightSearchParams.arrivalLocation.destinationId).toString())
        testTravelersSubscriber.assertValue(TravelerParams(flightSearchParams.numAdults, emptyList()))
    }

    private fun getFlightSearchParams(complete: Boolean): FlightSearchParams {
        val flightSearchParams = FlightSearchParams()
        val location = Location()
        location.destinationId = "SFO"
        flightSearchParams.arrivalLocation = location
        val location1 = Location()
        location1.destinationId = "LAS"
        if (complete) {
            flightSearchParams.departureLocation = location1
            flightSearchParams.departureDate = LocalDate.now()
            flightSearchParams.returnDate = LocalDate.now().plusDays(1)
            flightSearchParams.numAdults = 3
        }
        return flightSearchParams
    }

}

