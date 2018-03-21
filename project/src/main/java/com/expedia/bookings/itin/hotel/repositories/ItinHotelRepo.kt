package com.expedia.bookings.itin.hotel.repositories

import android.arch.lifecycle.MutableLiveData
import com.expedia.bookings.data.trips.ItinCardData
import com.expedia.bookings.itin.tripstore.data.Itin
import com.expedia.bookings.itin.tripstore.data.ItinHotel
import com.expedia.bookings.itin.tripstore.extensions.firstHotel
import com.expedia.bookings.itin.tripstore.utils.IJsonToItinUtil
import io.reactivex.Observable
import io.reactivex.observers.DisposableObserver

class ItinHotelRepo(private val itinId: String, private val jsonUtil: IJsonToItinUtil, observable: Observable<MutableList<ItinCardData>>) : ItinHotelRepoInterface {

    override val liveDataHotel: MutableLiveData<ItinHotel> = MutableLiveData()
    val liveDataItin: MutableLiveData<Itin> = MutableLiveData()
    val liveDataInvalidItin: MutableLiveData<Unit> = MutableLiveData()

    val syncObserver = object : DisposableObserver<MutableList<ItinCardData>>() {
        override fun onComplete() {
        }

        override fun onNext(t: MutableList<ItinCardData>) {
            val hotel = fetchHotel()
            val itin = fetchItin()
            if (hotel != null && itin != null) {
                liveDataHotel.postValue(hotel)
                liveDataItin.postValue(itin)
            } else {
                liveDataInvalidItin.postValue(Unit)
            }
        }

        override fun onError(e: Throwable) {
        }
    }

    private fun fetchItin(): Itin? {
        return jsonUtil.getItin(itinId)
    }

    private fun fetchHotel(): ItinHotel? = jsonUtil.getItin(itinId)?.firstHotel()

    init {
        liveDataHotel.value = fetchHotel()
        liveDataItin.value = fetchItin()
        observable.subscribe(syncObserver)
    }

    fun dispose() {
        syncObserver.dispose()
    }
}
