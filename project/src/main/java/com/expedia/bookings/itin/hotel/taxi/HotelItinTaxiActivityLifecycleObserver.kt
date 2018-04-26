package com.expedia.bookings.itin.hotel.taxi

import android.arch.lifecycle.DefaultLifecycleObserver
import com.expedia.bookings.data.trips.ItineraryManager
import com.expedia.bookings.extensions.LiveDataObserver
import com.expedia.bookings.itin.hotel.repositories.ItinHotelRepo
import com.expedia.bookings.itin.hotel.repositories.ItinHotelRepoInterface
import com.expedia.bookings.itin.scopes.HasItinId
import com.expedia.bookings.itin.scopes.HasJsonUtil
import com.expedia.bookings.itin.scopes.HasLifecycleOwner
import io.reactivex.subjects.PublishSubject

class HotelItinTaxiActivityLifecycleObserver<out S>(val scope: S) : DefaultLifecycleObserver where S : HasItinId, S : HasJsonUtil, S : HasLifecycleOwner  {

    var repo: ItinHotelRepoInterface = ItinHotelRepo(scope.id, scope.jsonUtil, ItineraryManager.getInstance().syncFinishObservable)
    val invalidSubject: PublishSubject<Unit> = PublishSubject.create()

    init {
        repo.liveDataInvalidItin.observe(scope.lifecycleOwner, LiveDataObserver {
            invalidSubject.onNext(Unit)
        })
    }
}