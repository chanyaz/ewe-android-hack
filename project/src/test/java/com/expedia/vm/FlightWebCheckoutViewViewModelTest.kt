package com.expedia.vm

import android.content.Context
import com.expedia.bookings.data.TripDetails
import com.expedia.bookings.data.flights.FlightCreateTripResponse
import com.expedia.bookings.services.TestObserver
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.utils.Ui
import com.expedia.util.Optional
import com.expedia.vm.flights.FlightCreateTripViewModel
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RuntimeEnvironment

@RunWith(RobolectricRunner::class)
class FlightWebCheckoutViewViewModelTest {
    private lateinit var context: Context
    private lateinit var viewModel: FlightWebCheckoutViewViewModel

    @Before
    fun setup() {
        context = RuntimeEnvironment.application
        viewModel = FlightWebCheckoutViewViewModel(context, Ui.getApplication(context).appComponent().endpointProvider())
        viewModel.flightCreateTripViewModel = FlightCreateTripViewModel(context)
    }

    @Test
    fun testWebViewURL() {
        val testObserver = TestObserver<String>()
        viewModel.webViewURLObservable.subscribe(testObserver)

        viewModel.flightCreateTripViewModel.createTripResponseObservable.onNext(Optional(getDummyFlightCreateTripResponse()))
        viewModel.showWebViewObservable.onNext(true)

        testObserver.assertValue("https://www.expedia.com/FlightCheckout?tripid=tripId")
    }

    @Test
    fun testNoWebViewURLWhenNotShown() {
        val testObserver = TestObserver<String>()
        viewModel.webViewURLObservable.subscribe(testObserver)

        viewModel.flightCreateTripViewModel.createTripResponseObservable.onNext(Optional(getDummyFlightCreateTripResponse()))

        testObserver.assertValueCount(0)
    }

    @Test
    fun testNoWebViewURLWhenCreateTripResponseIsNotAvailable() {
        val testObserver = TestObserver<String>()
        viewModel.webViewURLObservable.subscribe(testObserver)

        viewModel.showWebViewObservable.onNext(true)

        testObserver.assertValueCount(0)
    }

    private fun getDummyFlightCreateTripResponse(): FlightCreateTripResponse {
        val response = FlightCreateTripResponse()
        response.newTrip = TripDetails(tripId = "tripId")
        return response
    }
}
