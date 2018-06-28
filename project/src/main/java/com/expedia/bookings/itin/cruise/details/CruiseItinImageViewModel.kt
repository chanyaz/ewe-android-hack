package com.expedia.bookings.itin.cruise.details

import com.expedia.bookings.extensions.LiveDataObserver
import com.expedia.bookings.itin.common.ItinImageViewModel
import com.expedia.bookings.itin.scopes.HasItinRepo
import com.expedia.bookings.itin.scopes.HasLifecycleOwner
import com.expedia.bookings.itin.tripstore.data.Itin
import com.expedia.bookings.itin.tripstore.extensions.firstCruise

class CruiseItinImageViewModel<S>(val scope: S) : ItinImageViewModel() where S : HasItinRepo, S : HasLifecycleOwner {

    override val itinObserver: LiveDataObserver<Itin> = LiveDataObserver {
        it?.firstCruise()?.let { itinCruise ->
            itinCruise.shipImageUrl?.let { url ->
                imageUrlSubject.onNext(url)
            }
            itinCruise.shipName?.let { name ->
                nameSubject.onNext(name)
            }
        }
    }

    init {
        scope.itinRepo.liveDataItin.observe(scope.lifecycleOwner, itinObserver)
    }
}
