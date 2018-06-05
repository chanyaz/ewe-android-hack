package com.expedia.bookings.itin.cars.details

import com.expedia.bookings.R
import com.expedia.bookings.extensions.LiveDataObserver
import com.expedia.bookings.itin.common.ItinTimingsWidgetViewModel
import com.expedia.bookings.itin.scopes.HasCarRepo
import com.expedia.bookings.itin.scopes.HasLifecycleOwner
import com.expedia.bookings.itin.scopes.HasStringProvider
import com.expedia.bookings.itin.tripstore.data.ItinCar

class CarItinTimingsWidgetViewModel<S>(scope: S) : ItinTimingsWidgetViewModel<ItinCar>() where S : HasCarRepo, S : HasLifecycleOwner, S : HasStringProvider {
    override val itinObserver: LiveDataObserver<ItinCar> = LiveDataObserver {
        it?.let { itin ->
            startTitleSubject.onNext(scope.strings.fetch(R.string.itin_pick_up))
            itin.pickupTime?.localizedFullDate?.let { startDate ->
                startDateSubject.onNext(startDate)
            }
            itin.pickupTime?.localizedShortTime?.let { startTime ->
                startTimeSubject.onNext(startTime)
            }
            endTitleSubject.onNext(scope.strings.fetch(R.string.itin_drop_off))
            itin.dropOffTime?.localizedFullDate?.let { endDate ->
                endDateSubject.onNext(endDate)
            }
            itin.dropOffTime?.localizedShortTime?.let { endTime ->
                endTimeSubject.onNext(endTime)
            }
        }
    }

    init {
        scope.itinCarRepo.liveDataCar.observe(scope.lifecycleOwner, itinObserver)
    }
}
