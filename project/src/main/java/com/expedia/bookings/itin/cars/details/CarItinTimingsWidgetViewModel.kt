package com.expedia.bookings.itin.cars.details

import com.expedia.bookings.R
import com.expedia.bookings.extensions.LiveDataObserver
import com.expedia.bookings.itin.common.ItinTimingsWidgetViewModel
import com.expedia.bookings.itin.scopes.HasItinRepo
import com.expedia.bookings.itin.scopes.HasLifecycleOwner
import com.expedia.bookings.itin.scopes.HasStringProvider
import com.expedia.bookings.itin.tripstore.data.Itin
import com.expedia.bookings.itin.tripstore.extensions.firstCar

class CarItinTimingsWidgetViewModel<S>(scope: S) : ItinTimingsWidgetViewModel() where S : HasItinRepo, S : HasLifecycleOwner, S : HasStringProvider {
    override val itinObserver: LiveDataObserver<Itin> = LiveDataObserver {
        it?.firstCar()?.let { itinCar ->
            startTitleSubject.onNext(scope.strings.fetch(R.string.itin_pick_up))
            itinCar.pickupTime?.localizedFullDate?.let { startDate ->
                startDateSubject.onNext(startDate)
            }
            itinCar.pickupTime?.localizedShortTime?.let { startTime ->
                startTimeSubject.onNext(startTime)
            }
            endTitleSubject.onNext(scope.strings.fetch(R.string.itin_drop_off))
            itinCar.dropOffTime?.localizedFullDate?.let { endDate ->
                endDateSubject.onNext(endDate)
            }
            itinCar.dropOffTime?.localizedShortTime?.let { endTime ->
                endTimeSubject.onNext(endTime)
            }
        }
    }

    init {
        scope.itinRepo.liveDataItin.observe(scope.lifecycleOwner, itinObserver)
    }
}
