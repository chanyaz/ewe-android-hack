package com.expedia.bookings.itin.lx.details

import com.expedia.bookings.R
import com.expedia.bookings.extensions.LiveDataObserver
import com.expedia.bookings.itin.common.ItinExpandedMapActivity
import com.expedia.bookings.itin.common.ItinMapWidgetViewModel
import com.expedia.bookings.itin.common.TripProducts
import com.expedia.bookings.itin.scopes.HasActivityLauncher
import com.expedia.bookings.itin.scopes.HasLifecycleOwner
import com.expedia.bookings.itin.scopes.HasLxRepo
import com.expedia.bookings.itin.scopes.HasPhoneHandler
import com.expedia.bookings.itin.scopes.HasStringProvider
import com.expedia.bookings.itin.scopes.HasToaster
import com.expedia.bookings.itin.scopes.HasTripsTracking
import com.expedia.bookings.itin.tripstore.data.Itin
import com.expedia.bookings.itin.tripstore.data.ItinLx
import com.expedia.bookings.itin.tripstore.extensions.buildFullAddress
import com.expedia.bookings.itin.tripstore.extensions.buildSecondaryAddress
import com.expedia.bookings.itin.utils.AnimationDirection
import com.google.android.gms.maps.model.LatLng

class LxItinMapWidgetViewModel<S>(val scope: S) : ItinMapWidgetViewModel<ItinLx>() where S : HasLxRepo, S : HasLifecycleOwner, S : HasTripsTracking, S : HasToaster, S : HasStringProvider, S : HasPhoneHandler, S : HasActivityLauncher {

    val itinObserver: LiveDataObserver<Itin> = LiveDataObserver { itin ->
        itin?.tripId?.let { id ->
            directionButtonClickSubject.subscribe {
                scope.tripsTracking.trackItinLxDetailsDirections()
                scope.activityLauncher.launchActivity(ItinExpandedMapActivity, id, AnimationDirection.SLIDE_UP, TripProducts.ACTIVITY.name)
            }
            mapClickSubject.subscribe {
                scope.tripsTracking.trackItinLxDetailsMap()
                scope.activityLauncher.launchActivity(ItinExpandedMapActivity, id, AnimationDirection.SLIDE_UP, TripProducts.ACTIVITY.name)
            }
        }
    }

    override val itinLOBObserver: LiveDataObserver<ItinLx> = LiveDataObserver { itinLx ->
        if (itinLx != null) {
            val location = itinLx.activityLocation
            if (location?.addressLine1 != null) {
                addressLineFirstSubject.onNext(location.addressLine1)
            }
            addressLineSecondSubject.onNext(itinLx.buildSecondaryAddress())

            if (location?.latitude != null && location.longitude != null) {
                latLongSubject.onNext(LatLng(location.latitude, location.longitude))
            }
            addressClickSubject.subscribe {
                scope.toaster.toastAndCopy(itinLx.buildFullAddress())
            }
            addressContainerContentDescription.onNext(scope.strings.fetchWithPhrase(R.string.itin_lx_details_address_copy_content_description_TEMPLATE, mapOf("address" to itinLx.buildFullAddress())))
            val phoneNumber = itinLx.vendorCustomerServiceOffices?.first()?.phoneNumber
            phoneNumber?.let { number ->
                phoneNumberTextSubject.onNext(number)
                val contDesc = scope.strings.fetchWithPhrase(R.string.itin_activity_manage_booking_call_lx_button_content_description_TEMPLATE, mapOf("phonenumber" to number))
                phoneNumberContDescriptionSubject.onNext(contDesc)
                phoneNumberClickSubject.subscribe {
                    scope.phoneHandler.handle(number)
                }
            }
        }
    }

    init {
        scope.itinLxRepo.liveDataLx.observe(scope.lifecycleOwner, itinLOBObserver)
        scope.itinLxRepo.liveDataItin.observe(scope.lifecycleOwner, itinObserver)
    }
}
