package com.expedia.bookings.itin.lx.details

import com.expedia.bookings.R
import com.expedia.bookings.extensions.LiveDataObserver
import com.expedia.bookings.itin.common.ItinMapWidgetViewModel
import com.expedia.bookings.itin.scopes.HasLifecycleOwner
import com.expedia.bookings.itin.scopes.HasLxRepo
import com.expedia.bookings.itin.scopes.HasStringProvider
import com.expedia.bookings.itin.scopes.HasToaster
import com.expedia.bookings.itin.scopes.HasTripsTracking
import com.expedia.bookings.itin.tripstore.data.ItinLx
import com.expedia.bookings.itin.tripstore.extensions.buildFullAddress
import com.expedia.bookings.itin.tripstore.extensions.buildSecondaryAddress
import com.google.android.gms.maps.model.LatLng

class LxItinMapWidgetViewModel<S>(val scope: S) : ItinMapWidgetViewModel() where S : HasLxRepo, S : HasLifecycleOwner, S : HasTripsTracking, S : HasToaster, S : HasStringProvider {
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
                directionButtonClickSubject.subscribe {
                    scope.tripsTracking.trackItinLxDetailsDirections()
                    //TODO("add expanded map view and omniture")
                }
                mapClickSubject.subscribe {
                    scope.tripsTracking.trackItinLxDetailsMap()
                    //TODO("add expanded map view and omniture")
                }
                addressClickSubject.subscribe {
                    scope.toaster.toastAndCopy(itinLx.buildFullAddress())
                }
                addressContainerContentDescription.onNext(scope.strings.fetchWithPhrase(R.string.itin_lx_details_address_copy_content_description_TEMPLATE, mapOf("address" to itinLx.buildFullAddress())))
            }
        }
        scope.itinLxRepo.liveDataLx.observe(scope.lifecycleOwner, itinLxObserver)
    }
}
