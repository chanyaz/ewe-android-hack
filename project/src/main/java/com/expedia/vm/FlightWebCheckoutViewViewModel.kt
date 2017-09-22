package com.expedia.vm

import android.content.Context
import com.expedia.util.notNullAndObservable
import com.expedia.vm.flights.FlightCreateTripViewModel


class FlightWebCheckoutViewViewModel(var c: Context): WebCheckoutViewViewModel(c) {

    var createTripViewModel by notNullAndObservable<FlightCreateTripViewModel> {

    }

    init{
    }
}