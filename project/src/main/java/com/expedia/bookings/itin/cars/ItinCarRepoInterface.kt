package com.expedia.bookings.itin.cars

import android.arch.lifecycle.MutableLiveData
import com.expedia.bookings.itin.tripstore.data.Itin
import com.expedia.bookings.itin.tripstore.data.ItinCar
import io.reactivex.subjects.PublishSubject

interface ItinCarRepoInterface {
    val liveDataCar: MutableLiveData<ItinCar>
    val liveDataItin: MutableLiveData<Itin>
    val invalidDataSubject: PublishSubject<Unit>
    fun dispose()
}
