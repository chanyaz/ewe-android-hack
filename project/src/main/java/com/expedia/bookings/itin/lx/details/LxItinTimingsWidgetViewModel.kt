package com.expedia.bookings.itin.lx.details

import com.expedia.bookings.R
import com.expedia.bookings.extensions.LiveDataObserver
import com.expedia.bookings.itin.common.ItinTimingsWidgetViewModel
import com.expedia.bookings.itin.scopes.HasItinRepo
import com.expedia.bookings.itin.scopes.HasLifecycleOwner
import com.expedia.bookings.itin.scopes.HasStringProvider
import com.expedia.bookings.itin.tripstore.data.Itin
import com.expedia.bookings.itin.tripstore.extensions.firstLx

class LxItinTimingsWidgetViewModel<S>(scope: S) : ItinTimingsWidgetViewModel() where S : HasItinRepo, S : HasLifecycleOwner, S : HasStringProvider {
    override val itinObserver = LiveDataObserver<Itin> {
        it?.firstLx()?.let { itinLx ->
            startTitleSubject.onNext(scope.strings.fetch(R.string.itin_active))
            itinLx.startTime?.localizedFullDate?.let { startDate ->
                startDateSubject.onNext(startDate)
            }
            itinLx.startTime?.localizedShortTime?.let { startTime ->
                startTimeSubject.onNext(startTime)
            }
            endTitleSubject.onNext(scope.strings.fetch(R.string.itin_expires))
            itinLx.endTime?.localizedFullDate?.let { endDate ->
                endDateSubject.onNext(endDate)
            }
            itinLx.endTime?.localizedShortTime?.let { endTime ->
                endTimeSubject.onNext(endTime)
            }
        }
    }

    init {
        scope.itinRepo.liveDataItin.observe(scope.lifecycleOwner, itinObserver)
    }
}
