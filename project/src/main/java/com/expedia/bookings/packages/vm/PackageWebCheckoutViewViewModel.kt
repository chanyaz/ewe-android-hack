package com.expedia.bookings.packages.vm

import android.content.Context
import com.expedia.bookings.extensions.withLatestFrom
import com.expedia.bookings.server.EndpointProvider
import com.expedia.util.notNullAndObservable
import com.expedia.vm.WebCheckoutViewViewModel
import javax.inject.Inject

class PackageWebCheckoutViewViewModel @Inject constructor(val context: Context, val endpointProvider: EndpointProvider) : WebCheckoutViewViewModel(context) {

    var packageCreateTripViewModel by notNullAndObservable<PackageCreateTripViewModel> {

        showWebViewObservable.filter { it }.withLatestFrom(it.multiItemResponseSubject, { _, multiItemResponse ->
            object {
                val multiItemResponse = multiItemResponse
            }
        }).subscribe {
            webViewURLObservable.onNext(endpointProvider.getE3EndpointUrlWithPath("MultiItemCheckout?tripid=${it.multiItemResponse.tripId}"))
        }
    }

    override fun doCreateTrip() {
        showLoadingObservable.onNext(Unit)
        packageCreateTripViewModel.performMultiItemCreateTripSubject.onNext(Unit)
    }
}
