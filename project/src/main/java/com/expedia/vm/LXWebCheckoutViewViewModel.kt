package com.expedia.vm

import android.content.Context
import com.expedia.bookings.data.pos.PointOfSale
import com.expedia.bookings.extensions.safeSubscribeOptional
import com.expedia.bookings.server.EndpointProvider
import com.expedia.vm.lx.LXCreateTripViewModel
import javax.inject.Inject

class LXWebCheckoutViewViewModel @Inject constructor(var context: Context, val endpointProvider: EndpointProvider, val lxCreateTripViewModel: LXCreateTripViewModel) : WebCheckoutViewViewModel(context) {
    init {
        val webCheckoutPath = PointOfSale.getPointOfSale().lxWebCheckoutPath
        lxCreateTripViewModel.createTripResponseObservable.safeSubscribeOptional { createTripResponse ->
            webViewURLObservable.onNext(endpointProvider.getE3EndpointUrlWithPath("$webCheckoutPath?tripid=${createTripResponse.tripId}"))
        }
    }

    override fun doCreateTrip() {
        showLoadingObservable.onNext(Unit)
        lxCreateTripViewModel.performCreateTrip.onNext(Unit)
    }
}
