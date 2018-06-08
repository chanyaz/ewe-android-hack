package com.expedia.bookings.itin.cars.details

import com.expedia.bookings.R
import com.expedia.bookings.extensions.LiveDataObserver
import com.expedia.bookings.itin.common.ItinExpandedMapActivity
import com.expedia.bookings.itin.common.ItinMapWidgetViewModel
import com.expedia.bookings.itin.common.TripProducts
import com.expedia.bookings.itin.scopes.HasActivityLauncher
import com.expedia.bookings.itin.scopes.HasCarRepo
import com.expedia.bookings.itin.scopes.HasLifecycleOwner
import com.expedia.bookings.itin.scopes.HasPhoneHandler
import com.expedia.bookings.itin.scopes.HasStringProvider
import com.expedia.bookings.itin.scopes.HasToaster
import com.expedia.bookings.itin.scopes.HasTripsTracking
import com.expedia.bookings.itin.tripstore.data.CarLocation
import com.expedia.bookings.itin.tripstore.data.Itin
import com.expedia.bookings.itin.tripstore.data.ItinCar
import com.expedia.bookings.itin.tripstore.extensions.buildFullAddress
import com.expedia.bookings.itin.tripstore.extensions.buildSecondaryAddress
import com.expedia.bookings.itin.utils.AnimationDirection
import com.google.android.gms.maps.model.LatLng

abstract class CarItinMapWidgetViewModel<S>(val scope: S) : ItinMapWidgetViewModel<ItinCar>() where S : HasCarRepo, S : HasLifecycleOwner, S : HasTripsTracking, S : HasToaster, S : HasStringProvider, S : HasPhoneHandler, S : HasActivityLauncher {
    val itinObserver = LiveDataObserver<Itin> {
        it?.tripId?.let { id ->
            directionButtonClickSubject.subscribe {
                scope.tripsTracking.trackItinCarDetailsDirections()
                scope.activityLauncher.launchActivity(ItinExpandedMapActivity, id, AnimationDirection.SLIDE_UP, TripProducts.CAR.name)
            }
            mapClickSubject.subscribe {
                scope.tripsTracking.trackItinCarDetailsMap()
                scope.activityLauncher.launchActivity(ItinExpandedMapActivity, id, AnimationDirection.SLIDE_UP, TripProducts.CAR.name)
            }
        }
    }

    override val itinLOBObserver: LiveDataObserver<ItinCar> = LiveDataObserver {
        it?.let { itinCar ->
            getLocation(itinCar)?.let { location ->
                location.addressLine1?.let {
                    addressLineFirstSubject.onNext(location.addressLine1)
                }
                addressLineSecondSubject.onNext(location.buildSecondaryAddress())

                if (location.latitude != null && location.longitude != null) {
                    latLongSubject.onNext(LatLng(location.latitude, location.longitude))
                }

                addressClickSubject.subscribe {
                    scope.toaster.toastAndCopy(location.buildFullAddress())
                }
                addressContainerContentDescription.onNext(scope.strings.fetchWithPhrase(R.string.itin_car_address_copy_content_description_TEMPLATE, mapOf("address" to location.buildFullAddress())))
                itinCar.carVendor?.localPhoneNumber?.let { number ->
                    phoneNumberTextSubject.onNext(number)
                    itinCar.carVendor.longName?.let { vendorName ->
                        val contDesc = scope.strings.fetchWithPhrase(R.string.itin_car_call_button_content_description_TEMPLATE, mapOf("phonenumber" to number, "vendor" to vendorName))
                        phoneNumberContDescriptionSubject.onNext(contDesc)
                        phoneNumberClickSubject.subscribe {
                            scope.phoneHandler.handle(number)
                        }
                    }
                }
            }
        }
    }

    init {
        scope.itinCarRepo.liveDataCar.observe(scope.lifecycleOwner, itinLOBObserver)
        scope.itinCarRepo.liveDataItin.observe(scope.lifecycleOwner, itinObserver)
    }

    abstract fun getLocation(itinCar: ItinCar): CarLocation?
}
