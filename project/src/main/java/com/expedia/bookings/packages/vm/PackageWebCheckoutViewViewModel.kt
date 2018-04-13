package com.expedia.bookings.packages.vm

import android.content.Context
import com.expedia.bookings.extensions.safeSubscribe
import com.expedia.bookings.server.EndpointProvider
import com.expedia.util.notNullAndObservable
import com.expedia.vm.WebCheckoutViewViewModel
import javax.inject.Inject

class PackageWebCheckoutViewViewModel @Inject constructor(val context: Context, val endpointProvider: EndpointProvider) : WebCheckoutViewViewModel(context) {

    var packageCreateTripViewModel by notNullAndObservable<PackageCreateTripViewModel> {
        it.multiItemResponseSubject.safeSubscribe { multiItemResponse ->
            webViewURLObservable.onNext(endpointProvider.getE3EndpointUrlWithPath("MultiItemCheckout?tripid=${multiItemResponse.tripId}"))
        }
    }

    override fun doCreateTrip() {
        showLoadingObservable.onNext(Unit)
        packageCreateTripViewModel.performMultiItemCreateTripSubject.onNext(Unit)
    }
}
