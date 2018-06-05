package com.expedia.vm

import android.content.Context
import com.expedia.bookings.data.flights.FlightCreateTripResponse
import com.expedia.bookings.data.pos.PointOfSale
import com.expedia.bookings.extensions.safeSubscribeOptional
import com.expedia.bookings.extensions.withLatestFrom
import com.expedia.bookings.server.EndpointProvider
import com.expedia.bookings.utils.isShowFlightsNativeRateDetailsWebviewCheckoutEnabled
import com.expedia.util.notNullAndObservable
import com.expedia.vm.flights.FlightCreateTripViewModel
import javax.inject.Inject

class FlightWebCheckoutViewViewModel @Inject constructor(val context: Context, val endpointProvider: EndpointProvider) : WebCheckoutViewViewModel(context) {

    var flightCreateTripViewModel by notNullAndObservable<FlightCreateTripViewModel> {
        if (shouldShowWebCheckoutWithoutNativeRateDetails()) {
            it.createTripResponseObservable.safeSubscribeOptional { createTripResponse ->
                createTripResponse as FlightCreateTripResponse
                webViewURLObservable.onNext(endpointProvider.getE3EndpointUrlWithPath("FlightCheckout?tripid=${createTripResponse.newTrip?.tripId}"))
            }
        } else {
            showWebViewObservable
                    .filter { it }
                    .withLatestFrom(it.createTripResponseObservable, { _, createTripResponse ->
                        object {
                            val createTripResponse = createTripResponse.value as? FlightCreateTripResponse
                        }
                    })
                    .filter { it.createTripResponse != null }
                    .subscribe {
                        webViewURLObservable.onNext(endpointProvider.getE3EndpointUrlWithPath("FlightCheckout?tripid=${it.createTripResponse?.newTrip?.tripId}"))
                    }
        }
    }

    fun shouldShowWebCheckoutWithoutNativeRateDetails(): Boolean {
        return PointOfSale.getPointOfSale().shouldShowWebCheckout() && !isShowFlightsNativeRateDetailsWebviewCheckoutEnabled(context)
    }
    override fun doCreateTrip() {
        showLoadingObservable.onNext(Unit)
        flightCreateTripViewModel.performCreateTrip.onNext(Unit)
    }
}
