package com.expedia.bookings.lx.vm

import android.content.Context
import com.expedia.bookings.extensions.safeSubscribeOptional
import com.expedia.bookings.server.EndpointProvider
import com.expedia.vm.WebCheckoutViewViewModel
import javax.inject.Inject

class LXWebCheckoutViewViewModel @Inject constructor(var context: Context, val endpointProvider: EndpointProvider, val lxCreateTripViewModel: LXCreateTripViewModel) : WebCheckoutViewViewModel(context) {
    init {
        lxCreateTripViewModel.createTripResponseObservable.safeSubscribeOptional { createTripResponse ->
            webViewURLObservable.onNext(endpointProvider.getE3EndpointUrlWithPath("MultiItemCheckout?tripid=${createTripResponse.tripId}"))
        }
    }

    override fun doCreateTrip() {
        showLoadingObservable.onNext(Unit)
        lxCreateTripViewModel.performCreateTrip.onNext(Unit)
    }
}
