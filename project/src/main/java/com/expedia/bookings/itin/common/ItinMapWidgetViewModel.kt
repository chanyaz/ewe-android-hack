package com.expedia.bookings.itin.common

import com.expedia.bookings.extensions.LiveDataObserver
import com.expedia.bookings.itin.tripstore.data.Itin
import com.google.android.gms.maps.model.LatLng
import io.reactivex.subjects.PublishSubject

abstract class ItinMapWidgetViewModel {
    val carLocationTypeHeaderSubject: PublishSubject<String> = PublishSubject.create()
    val addressLineFirstSubject: PublishSubject<String> = PublishSubject.create()
    val addressLineSecondSubject: PublishSubject<String> = PublishSubject.create()
    val directionButtonClickSubject: PublishSubject<Unit> = PublishSubject.create()
    val mapClickSubject: PublishSubject<Unit> = PublishSubject.create()
    val latLongSubject: PublishSubject<LatLng> = PublishSubject.create()
    val addressClickSubject: PublishSubject<Unit> = PublishSubject.create()
    val addressContainerContentDescription: PublishSubject<String> = PublishSubject.create()
    val phoneNumberTextSubject: PublishSubject<String> = PublishSubject.create()
    val phoneNumberContDescriptionSubject: PublishSubject<String> = PublishSubject.create()
    val phoneNumberClickSubject: PublishSubject<Unit> = PublishSubject.create()

    abstract val itinObserver: LiveDataObserver<Itin>
}
