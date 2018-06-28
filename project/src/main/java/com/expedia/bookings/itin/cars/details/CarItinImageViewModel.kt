package com.expedia.bookings.itin.cars.details

import com.expedia.bookings.extensions.LiveDataObserver
import com.expedia.bookings.itin.common.ItinImageViewModel
import com.expedia.bookings.itin.scopes.HasItinRepo
import com.expedia.bookings.itin.scopes.HasLifecycleOwner
import com.expedia.bookings.itin.tripstore.data.Itin
import com.expedia.bookings.itin.tripstore.extensions.firstCar

class CarItinImageViewModel<S>(val scope: S) : ItinImageViewModel() where S : HasItinRepo, S : HasLifecycleOwner {

    override val itinObserver: LiveDataObserver<Itin> = LiveDataObserver {
        it?.firstCar()?.let { itinCar ->
            itinCar.carVendor?.longName?.let { name ->
                nameSubject.onNext(name)
            }

            itinCar.carCategoryImageURL?.let { url ->
                imageUrlSubject.onNext(url)
            }
        }
    }

    init {
        scope.itinRepo.liveDataItin.observe(scope.lifecycleOwner, itinObserver)
    }
}
