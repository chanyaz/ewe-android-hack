package com.expedia.bookings.itin.lx

import com.expedia.bookings.extensions.LiveDataObserver
import com.expedia.bookings.itin.common.NewItinToolbarViewModel
import com.expedia.bookings.itin.scopes.HasLifecycleOwner
import com.expedia.bookings.itin.scopes.HasLxRepo
import com.expedia.bookings.itin.scopes.HasStringProvider
import com.expedia.bookings.itin.tripstore.data.Itin
import com.expedia.bookings.itin.tripstore.data.ItinLx

class LxItinToolbarViewModel<S>(val scope: S) : NewItinToolbarViewModel where S : HasLifecycleOwner, S : HasStringProvider, S :  HasLxRepo {

//    var lxObserver: LiveDataObserver<ItinLx>
    var itinObserver: LiveDataObserver<Itin>

    init {
        itinObserver = LiveDataObserver<Itin> { itin ->
            val stringProvider = scope.strings
            val title = itin?.title
            if (!title.isNullOrEmpty()) {
                toolbarTitleSubject.onNext(title!!)
            }


        }
        scope.itinLxRepo.liveDataItin.observe(scope.lifecycleOwner, itinObserver)
    }

}
