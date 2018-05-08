package com.expedia.bookings.itin.lx.details

import com.expedia.bookings.R
import com.expedia.bookings.extensions.LiveDataObserver
import com.expedia.bookings.itin.common.ItinMapWidgetViewModel
import com.expedia.bookings.itin.scopes.HasLifecycleOwner
import com.expedia.bookings.itin.scopes.HasLxRepo
import com.expedia.bookings.itin.tripstore.data.ItinLx
import com.expedia.bookings.itin.tripstore.extensions.buildSecondaryAddress
import com.google.android.gms.maps.model.LatLng

class LxItinMapWidgetViewModel<S>(scope: S): ItinMapWidgetViewModel() where S : HasLxRepo, S : HasLifecycleOwner {
    var itinLxObserver: LiveDataObserver<ItinLx>


    init {
        itinLxObserver = LiveDataObserver { itinLx ->
            if (itinLx != null) {
                val location = itinLx.activityLocation
                if (location?.addressLine1 != null) {
                    addressLineFirstSubject.onNext(location.addressLine1)
                }
                addressLineSecondSubject.onNext(itinLx.buildSecondaryAddress())

                if (location?.latitude != null && location.longitude != null) {
                    latLongSubject.onNext(LatLng(location.latitude, location.longitude))
                }
                directionsButtonVisibilitySubject.onNext(true)
                directionButtonClickSubject.subscribe {
                    TODO("add expanded map view and omniture")
                }
                mapClickSubject.subscribe {
                    TODO("add expanded map view and omniture")
                }
            }
        }
        scope.itinLxRepo.liveDataLx.observe(scope.lifecycleOwner, itinLxObserver)
    }
}