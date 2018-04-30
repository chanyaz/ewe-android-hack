package com.expedia.vm

import android.content.Context
import com.expedia.bookings.data.flights.FlightCreateTripResponse
import com.expedia.bookings.extensions.safeSubscribeOptional
import com.expedia.bookings.server.EndpointProvider
import com.expedia.util.notNullAndObservable
import com.expedia.vm.flights.FlightCreateTripViewModel
import javax.inject.Inject

class FlightWebCheckoutViewViewModel @Inject constructor(val context: Context, val endpointProvider: EndpointProvider) : WebCheckoutViewViewModel(context) {

    var flightCreateTripViewModel by notNullAndObservable<FlightCreateTripViewModel> {
        it.createTripResponseObservable.safeSubscribeOptional { createTripResponse ->
            createTripResponse as FlightCreateTripResponse
            webViewURLObservable.onNext(endpointProvider.getE3EndpointUrlWithPath("//FlightCheckoutError"))
        }
    }

    override fun doCreateTrip() {
        showLoadingObservable.onNext(Unit)
        flightCreateTripViewModel.performCreateTrip.onNext(Unit)
    }
}
