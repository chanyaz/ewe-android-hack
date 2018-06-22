package com.expedia.bookings.itin.cars.details

import android.arch.lifecycle.Observer
import com.expedia.bookings.itin.flight.common.ItinOmnitureUtils
import com.expedia.bookings.itin.scopes.HasItinRepo
import com.expedia.bookings.itin.scopes.HasLifecycleOwner
import com.expedia.bookings.itin.scopes.HasTripsTracking
import com.expedia.bookings.itin.tripstore.data.Itin
import io.reactivex.subjects.PublishSubject

class CarItinDetailsViewModel<S>(val scope: S) where S : HasItinRepo, S : HasTripsTracking, S : HasLifecycleOwner {
    val finishSubject: PublishSubject<Unit> = PublishSubject.create()
    val invalidSubject: PublishSubject<Unit> = PublishSubject.create()

    init {
        finishSubject.subscribe {
            scope.itinRepo.dispose()
        }
        scope.itinRepo.liveDataItin.observe(scope.lifecycleOwner, object : Observer<Itin> {
            override fun onChanged(t: Itin?) {
                t?.let {
                    val omnitureValues = ItinOmnitureUtils.createOmnitureTrackingValuesNew(it, ItinOmnitureUtils.LOB.CAR)
                    scope.tripsTracking.trackItinCarDetailsPageLoad(omnitureValues)
                }
                scope.itinRepo.liveDataItin.removeObserver(this)
            }
        })
        scope.itinRepo.invalidDataSubject.subscribe {
            invalidSubject.onNext(Unit)
        }
    }
}
