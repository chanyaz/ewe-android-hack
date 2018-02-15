package com.expedia.bookings.extensions

import com.google.android.gms.maps.GoogleMap
import io.reactivex.Observer

fun GoogleMap.subscribeOnClick(observer: Observer<Unit>) {
    this.setOnMapClickListener {
        observer.onNext(Unit)
    }
}
