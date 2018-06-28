package com.expedia.bookings.itin.cruise.details

import com.expedia.bookings.R
import com.expedia.bookings.extensions.LiveDataObserver
import com.expedia.bookings.itin.common.ItinTimingsWidgetViewModel
import com.expedia.bookings.itin.scopes.HasItinRepo
import com.expedia.bookings.itin.scopes.HasLifecycleOwner
import com.expedia.bookings.itin.scopes.HasStringProvider
import com.expedia.bookings.itin.tripstore.data.Itin
import com.expedia.bookings.itin.tripstore.extensions.firstCruise

class CruiseItinTimingsWidgetViewModel<S>(val scope: S) : ItinTimingsWidgetViewModel() where S : HasItinRepo, S : HasLifecycleOwner, S : HasStringProvider {

    override val itinObserver: LiveDataObserver<Itin> = LiveDataObserver {
        it?.firstCruise()?.let { itinCruise ->
            startTitleSubject.onNext(scope.strings.fetch(R.string.itin_cruise_embarkation_title))
            itinCruise.startTime?.localizedShortDate?.let { date ->
                startDateSubject.onNext(date)
            }
            itinCruise.startTime?.localizedShortTime?.let { time ->
                startTimeSubject.onNext(time)
            }

            endTitleSubject.onNext(scope.strings.fetch(R.string.itin_cruise_disembarkation_title))
            itinCruise.endTime?.localizedShortDate?.let { date ->
                endDateSubject.onNext(date)
            }
            itinCruise.endTime?.localizedShortTime?.let { time ->
                endTimeSubject.onNext(time)
            }
        }
    }

    init {
        scope.itinRepo.liveDataItin.observe(scope.lifecycleOwner, itinObserver)
    }
}
