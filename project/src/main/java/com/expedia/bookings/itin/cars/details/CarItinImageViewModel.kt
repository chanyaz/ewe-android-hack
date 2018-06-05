package com.expedia.bookings.itin.cars.details

import com.expedia.bookings.extensions.LiveDataObserver
import com.expedia.bookings.itin.common.ItinImageViewModel
import com.expedia.bookings.itin.scopes.HasCarRepo
import com.expedia.bookings.itin.scopes.HasLifecycleOwner
import com.expedia.bookings.itin.tripstore.data.ItinCar

class CarItinImageViewModel<S>(val scope: S) : ItinImageViewModel<ItinCar>() where S : HasCarRepo, S : HasLifecycleOwner {
    override val itinLOBObserver: LiveDataObserver<ItinCar> = LiveDataObserver {
        it?.let { itinCar ->
            itinCar.carVendor?.longName?.let { name ->
                nameSubject.onNext(name)
            }

            itinCar.carCategoryImageURL?.let { url ->
                imageUrlSubject.onNext(url)
            }
        }
    }
    init {
        scope.itinCarRepo.liveDataCar.observe(scope.lifecycleOwner, itinLOBObserver)
    }
}
