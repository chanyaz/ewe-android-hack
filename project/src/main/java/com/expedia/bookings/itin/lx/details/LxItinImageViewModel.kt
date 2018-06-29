package com.expedia.bookings.itin.lx.details

import com.expedia.bookings.extensions.LiveDataObserver
import com.expedia.bookings.itin.common.ItinImageViewModel
import com.expedia.bookings.itin.scopes.HasItinRepo
import com.expedia.bookings.itin.scopes.HasLifecycleOwner
import com.expedia.bookings.itin.tripstore.data.Itin
import com.expedia.bookings.itin.tripstore.extensions.firstLx

class LxItinImageViewModel<S>(val scope: S) : ItinImageViewModel() where S : HasItinRepo, S : HasLifecycleOwner {

    override val itinObserver: LiveDataObserver<Itin> = LiveDataObserver {
        it?.firstLx()?.let { itinLx ->
            itinLx.activityTitle?.let { name ->
                nameSubject.onNext(name)
            }
            itinLx.highResImage?.url?.let { url ->
                imageUrlSubject.onNext(url)
            }
        }
    }

    init {
        scope.itinRepo.liveDataItin.observe(scope.lifecycleOwner, itinObserver)
    }
}
