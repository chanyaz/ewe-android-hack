package com.expedia.bookings.itin.cars

import android.arch.lifecycle.MutableLiveData
import com.expedia.bookings.data.trips.ItinCardData
import com.expedia.bookings.itin.tripstore.data.Itin
import com.expedia.bookings.itin.tripstore.data.ItinCar
import com.expedia.bookings.itin.tripstore.extensions.firstCar
import com.expedia.bookings.itin.tripstore.utils.IJsonToItinUtil
import io.reactivex.Observable
import io.reactivex.observers.DisposableObserver
import io.reactivex.subjects.PublishSubject

class ItinCarRepo(private val itinId: String, private val jsonUtil: IJsonToItinUtil, observable: Observable<MutableList<ItinCardData>>) : ItinCarRepoInterface {

    override val liveDataCar: MutableLiveData<ItinCar> = MutableLiveData()
    override val liveDataItin: MutableLiveData<Itin> = MutableLiveData()
    override val invalidDataSubject: PublishSubject<Unit> = PublishSubject.create()

    val syncObserver = object : DisposableObserver<MutableList<ItinCardData>>() {
        override fun onComplete() {
        }

        override fun onNext(t: MutableList<ItinCardData>) {
            val car = fetchCar()
            val itin = fetchItin()
            if (car != null && itin != null) {
                liveDataCar.postValue(car)
                liveDataItin.postValue(itin)
            } else {
                invalidDataSubject.onNext(Unit)
            }
        }

        override fun onError(e: Throwable) {
        }
    }

    private fun fetchItin(): Itin? {
        return jsonUtil.getItin(itinId)
    }

    private fun fetchCar(): ItinCar? = jsonUtil.getItin(itinId)?.firstCar()

    init {
        liveDataCar.value = fetchCar()
        liveDataItin.value = fetchItin()
        observable.subscribe(syncObserver)
    }

    override fun dispose() {
        syncObserver.dispose()
    }
}

interface ItinCarRepoInterface {
    val liveDataCar: MutableLiveData<ItinCar>
    val liveDataItin: MutableLiveData<Itin>
    val invalidDataSubject: PublishSubject<Unit>
    fun dispose()
}
