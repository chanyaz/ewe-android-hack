package com.expedia.bookings.itin.cars.details

import android.arch.lifecycle.LifecycleOwner
import com.expedia.bookings.R
import com.expedia.bookings.extensions.LiveDataObserver
import com.expedia.bookings.itin.cars.ItinCarRepoInterface
import com.expedia.bookings.itin.common.ItinMapWidgetViewModel
import com.expedia.bookings.itin.scopes.HasCarRepo
import com.expedia.bookings.itin.scopes.HasLifecycleOwner
import com.expedia.bookings.itin.scopes.HasPhoneHandler
import com.expedia.bookings.itin.scopes.HasStringProvider
import com.expedia.bookings.itin.scopes.HasToaster
import com.expedia.bookings.itin.scopes.HasTripsTracking
import com.expedia.bookings.itin.tripstore.data.CarLocation
import com.expedia.bookings.itin.tripstore.data.ItinCar
import com.expedia.bookings.itin.tripstore.extensions.buildFullAddress
import com.expedia.bookings.itin.tripstore.extensions.buildSecondaryAddress
import com.expedia.bookings.itin.tripstore.extensions.isDropOffSame
import com.expedia.bookings.itin.utils.IPhoneHandler
import com.expedia.bookings.itin.utils.IToaster
import com.expedia.bookings.itin.utils.StringSource
import com.expedia.bookings.tracking.ITripsTracking
import com.google.android.gms.maps.model.LatLng
import io.reactivex.subjects.PublishSubject

abstract class CarItinMapWidgetViewModel<S>(val scope: S) : ItinMapWidgetViewModel<ItinCar>() where S : HasCarRepo, S : HasLifecycleOwner, S : HasTripsTracking, S : HasToaster, S : HasStringProvider, S : HasPhoneHandler {
    override val itinObserver: LiveDataObserver<ItinCar> = LiveDataObserver {
        it?.let { itinCar ->
            getLocation(itinCar)?.let { location ->
                location.addressLine1?.let {
                    addressLineFirstSubject.onNext(location.addressLine1)
                }
                addressLineSecondSubject.onNext(location.buildSecondaryAddress())

                if (location.latitude != null && location.longitude != null) {
                    latLongSubject.onNext(LatLng(location.latitude, location.longitude))
                }
                directionButtonClickSubject.subscribe {
                    //TODO("add expanded map view and omniture")
                }
                mapClickSubject.subscribe {
                    //TODO("add expanded map view and omniture")
                }
                addressClickSubject.subscribe {
                    scope.toaster.toastAndCopy(location.buildFullAddress())
                }
                addressContainerContentDescription.onNext(scope.strings.fetchWithPhrase(R.string.itin_car_call_button_content_description_TEMPLATE, mapOf("address" to location.buildFullAddress())))
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
        scope.itinCarRepo.liveDataCar.observe(scope.lifecycleOwner, itinObserver)
    }

    abstract fun getLocation(itinCar: ItinCar): CarLocation?
}

data class CarItinMapWidgetViewModelScope(override val strings: StringSource,
                                          override val tripsTracking: ITripsTracking,
                                          override val lifecycleOwner: LifecycleOwner,
                                          override val itinCarRepo: ItinCarRepoInterface,
                                          override val toaster: IToaster,
                                          override val phoneHandler: IPhoneHandler) : HasCarRepo, HasLifecycleOwner, HasTripsTracking, HasToaster, HasStringProvider, HasPhoneHandler

class CarItinPickupMapWidgetViewModel(ViewModelScope: CarItinMapWidgetViewModelScope) : CarItinMapWidgetViewModel<CarItinMapWidgetViewModelScope>(ViewModelScope) {
    override fun getLocation(itinCar: ItinCar): CarLocation? {
        return itinCar.pickupLocation
    }
}

class CarItinDropOffMapWidgetViewModel(ViewModelScope: CarItinMapWidgetViewModelScope) : CarItinMapWidgetViewModel<CarItinMapWidgetViewModelScope>(ViewModelScope) {
    val showVisibilitySubject: PublishSubject<Unit> = PublishSubject.create()
    override fun getLocation(itinCar: ItinCar): CarLocation? {
        return if (!itinCar.isDropOffSame()) {
            showVisibilitySubject.onNext(Unit)
            itinCar.dropOffLocation
        } else {
            null
        }
    }
}
