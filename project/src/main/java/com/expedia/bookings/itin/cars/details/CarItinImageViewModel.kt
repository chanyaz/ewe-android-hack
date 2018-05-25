package com.expedia.bookings.itin.cars.details

import com.expedia.bookings.extensions.LiveDataObserver
import com.expedia.bookings.itin.common.ItinImageViewModel
import com.expedia.bookings.itin.scopes.HasCarRepo
import com.expedia.bookings.itin.scopes.HasLifecycleOwner
import com.expedia.bookings.itin.tripstore.data.ItinCar

class CarItinImageViewModel<S>(val scope: S) : ItinImageViewModel() where S : HasCarRepo, S : HasLifecycleOwner {
    val itinCarObserver: LiveDataObserver<ItinCar>
    init {
        itinCarObserver = LiveDataObserver {
            it?.let { itinCar ->
                itinCar.carVendor?.longName?.let { name ->
                    nameSubject.onNext(name)
                }

                itinCar.carCategoryImageURL?.let { url ->
                    imageUrlSubject.onNext(url)
                }
            }
        }
        scope.itinCarRepo.liveDataCar.observe(scope.lifecycleOwner, itinCarObserver)
    }
}
