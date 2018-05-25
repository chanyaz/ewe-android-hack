package com.expedia.bookings.itin.helpers

import android.arch.lifecycle.MutableLiveData
import com.expedia.bookings.itin.cars.ItinCarRepoInterface
import com.expedia.bookings.itin.tripstore.data.Itin
import com.expedia.bookings.itin.tripstore.data.ItinCar
import io.reactivex.subjects.PublishSubject

class MockCarRepo : ItinCarRepoInterface {
    override val liveDataCar: MutableLiveData<ItinCar> = MutableLiveData()
    override val liveDataItin: MutableLiveData<Itin> = MutableLiveData()
    override val invalidDataSubject: PublishSubject<Unit> = PublishSubject.create()
    var disposed = false
    override fun dispose() {
        disposed = true
    }
}
