package com.expedia.vm

import android.content.Context
import com.expedia.bookings.data.flights.FlightCreateTripResponse
import com.expedia.bookings.data.pos.PointOfSale
import com.expedia.util.notNullAndObservable
import com.expedia.vm.flights.FlightCreateTripViewModel

class FlightWebCheckoutViewViewModel(var context:Context): WebCheckoutViewViewModel(context) {

    var flightCreateTripViewModel by notNullAndObservable<FlightCreateTripViewModel> {
        it.createTripResponseObservable.subscribe { createTripResponse ->
            createTripResponse.value as FlightCreateTripResponse
            webViewURLObservable.onNext("${PointOfSale.getPointOfSale().flightsWebCheckoutUrl}?tripid=${createTripResponse.value.newTrip?.tripId}")
        }
    }

    override fun doCreateTrip() {
        showLoadingObservable.onNext(Unit)
        flightCreateTripViewModel.performCreateTrip.onNext(Unit)
    }
}
