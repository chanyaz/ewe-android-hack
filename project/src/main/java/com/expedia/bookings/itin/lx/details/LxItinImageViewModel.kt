package com.expedia.bookings.itin.lx.details

import com.expedia.bookings.extensions.LiveDataObserver
import com.expedia.bookings.itin.common.ItinImageViewModel
import com.expedia.bookings.itin.scopes.HasLifecycleOwner
import com.expedia.bookings.itin.scopes.HasLxRepo
import com.expedia.bookings.itin.tripstore.data.ItinLx

class LxItinImageViewModel<S>(val scope: S) : ItinImageViewModel() where S : HasLxRepo, S : HasLifecycleOwner {
    val itinLxObserver: LiveDataObserver<ItinLx>

    init {
        itinLxObserver = LiveDataObserver {
            it?.let { itinLx ->
                itinLx.activityTitle?.let { name ->
                    nameSubject.onNext(name)
                }

                itinLx.highResImage?.url?.let { url ->
                    imageUrlSubject.onNext(url)
                }
            }
        }
        scope.itinLxRepo.liveDataLx.observe(scope.lifecycleOwner, itinLxObserver)
    }
}
