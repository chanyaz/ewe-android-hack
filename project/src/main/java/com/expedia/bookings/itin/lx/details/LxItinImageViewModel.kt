package com.expedia.bookings.itin.lx.details

import com.expedia.bookings.extensions.LiveDataObserver
import com.expedia.bookings.itin.common.ItinImageViewModel
import com.expedia.bookings.itin.scopes.HasLifecycleOwner
import com.expedia.bookings.itin.scopes.HasLxRepo
import com.expedia.bookings.itin.tripstore.data.ItinLx

class LxItinImageViewModel<S>(val scope: S) : ItinImageViewModel<ItinLx>() where S : HasLxRepo, S : HasLifecycleOwner {
    override val itinLOBObserver: LiveDataObserver<ItinLx> = LiveDataObserver {
        it?.let { itinLx ->
            itinLx.activityTitle?.let { name ->
                nameSubject.onNext(name)
            }

            itinLx.highResImage?.url?.let { url ->
                imageUrlSubject.onNext(url)
            }
        }
    }

    init {
        scope.itinLxRepo.liveDataLx.observe(scope.lifecycleOwner, itinLOBObserver)
    }
}
