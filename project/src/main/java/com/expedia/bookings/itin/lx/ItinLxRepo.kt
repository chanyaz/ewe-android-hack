package com.expedia.bookings.itin.lx

import android.arch.lifecycle.MutableLiveData
import com.expedia.bookings.data.trips.ItinCardData
import com.expedia.bookings.itin.tripstore.data.Itin
import com.expedia.bookings.itin.tripstore.data.ItinLx
import com.expedia.bookings.itin.tripstore.extensions.firstLx
import com.expedia.bookings.itin.tripstore.utils.IJsonToItinUtil
import io.reactivex.Observable
import io.reactivex.observers.DisposableObserver


class ItinLxRepo(private val itinId: String, private val jsonUtil: IJsonToItinUtil, observable: Observable<MutableList<ItinCardData>>) : ItinLxRepoInterface {

    override val liveDataLx: MutableLiveData<ItinLx> = MutableLiveData()
    val liveDataItin: MutableLiveData<Itin> = MutableLiveData()
    val liveDataInvalidItin: MutableLiveData<Unit> = MutableLiveData()

    val syncObserver = object : DisposableObserver<MutableList<ItinCardData>>() {
        override fun onComplete() {
        }

        override fun onNext(t: MutableList<ItinCardData>) {
            val lx = fetchLx()
            val itin = fetchItin()
            if (lx != null && itin != null) {
                liveDataLx.postValue(lx)
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

    private fun fetchLx(): ItinLx? = jsonUtil.getItin(itinId)?.firstLx()

    init {
        liveDataLx.value = fetchLx()
        liveDataItin.value = fetchItin()
        observable.subscribe(syncObserver)
    }

    fun dispose() {
        syncObserver.dispose()
    }

}
