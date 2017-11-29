package com.expedia.vm

import android.content.Context
import com.expedia.bookings.data.pos.PointOfSale
import rx.subjects.PublishSubject

class PackageWebCheckoutViewViewModel(var context: Context): WebCheckoutViewViewModel(context) {

    val tripIdSubject = PublishSubject.create<String>()

    override fun doCreateTrip() {
    }

    init {
        tripIdSubject.subscribe { id ->
            webViewURLObservable.onNext("https://www.${ PointOfSale.getPointOfSale().url}/MultiItemCheckout?tripid=${id}")
        }
    }
}
