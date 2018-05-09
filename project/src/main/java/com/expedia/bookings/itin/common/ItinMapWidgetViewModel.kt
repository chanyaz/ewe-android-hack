package com.expedia.bookings.itin.common

import com.google.android.gms.maps.model.LatLng
import io.reactivex.subjects.PublishSubject

abstract class ItinMapWidgetViewModel {
    val addressLineFirstSubject: PublishSubject<String> = PublishSubject.create()
    val addressLineSecondSubject: PublishSubject<String> = PublishSubject.create()
    val directionsButtonVisibilitySubject: PublishSubject<Boolean> = PublishSubject.create()
    val directionButtonClickSubject: PublishSubject<Unit> = PublishSubject.create()
    val mapClickSubject: PublishSubject<Unit> = PublishSubject.create()
    val latLongSubject: PublishSubject<LatLng> = PublishSubject.create()
}
