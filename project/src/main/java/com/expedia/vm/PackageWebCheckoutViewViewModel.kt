package com.expedia.vm

import android.content.Context
import com.expedia.bookings.data.pos.PointOfSale
import com.expedia.util.notNullAndObservable
import com.expedia.util.safeSubscribe
import com.expedia.vm.packages.PackageCreateTripViewModel

class PackageWebCheckoutViewViewModel(var context: Context) : WebCheckoutViewViewModel(context) {

    var packageCreateTripViewModel by notNullAndObservable<PackageCreateTripViewModel> {
        it.multiItemResponseSubject.safeSubscribe { multiItemResponse ->
            webViewURLObservable.onNext("https://www.${ PointOfSale.getPointOfSale().url}/MultiItemCheckout?tripid=${multiItemResponse.tripId}")
        }
    }

    override fun doCreateTrip() {
        showLoadingObservable.onNext(Unit)
        packageCreateTripViewModel.performMultiItemCreateTripSubject.onNext(Unit)
    }
}
