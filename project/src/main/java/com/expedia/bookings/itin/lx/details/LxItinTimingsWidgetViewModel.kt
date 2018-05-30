package com.expedia.bookings.itin.lx.details

import com.expedia.bookings.R
import com.expedia.bookings.extensions.LiveDataObserver
import com.expedia.bookings.itin.common.ItinTimingsWidgetViewModel
import com.expedia.bookings.itin.scopes.HasLifecycleOwner
import com.expedia.bookings.itin.scopes.HasLxRepo
import com.expedia.bookings.itin.scopes.HasStringProvider
import com.expedia.bookings.itin.tripstore.data.ItinLx

class LxItinTimingsWidgetViewModel<S>(scope: S) : ItinTimingsWidgetViewModel<ItinLx>() where S : HasLxRepo, S : HasLifecycleOwner, S : HasStringProvider {
    override val itinObserver = LiveDataObserver<ItinLx> {
        it?.let { itin ->
            startTitleSubject.onNext(scope.strings.fetch(R.string.itin_active))
            itin.startTime?.localizedFullDate?.let { startDate ->
                startDateSubject.onNext(startDate)
            }
            itin.startTime?.localizedShortTime?.let { startTime ->
                startTimeSubject.onNext(startTime)
            }
            endTitleSubject.onNext(scope.strings.fetch(R.string.itin_expires))
            itin.endTime?.localizedFullDate?.let { endDate ->
                endDateSubject.onNext(endDate)
            }
            itin.endTime?.localizedShortTime?.let { endTime ->
                endTimeSubject.onNext(endTime)
            }
        }
    }

    init {
        scope.itinLxRepo.liveDataLx.observe(scope.lifecycleOwner, itinObserver)
    }
}
