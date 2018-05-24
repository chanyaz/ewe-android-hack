package com.expedia.bookings.itin.common

import android.arch.lifecycle.MutableLiveData
import com.expedia.bookings.data.trips.ItinCardData
import com.expedia.bookings.itin.tripstore.data.Itin
import com.expedia.bookings.itin.tripstore.utils.IJsonToItinUtil
import io.reactivex.Observable
import io.reactivex.observers.DisposableObserver
import io.reactivex.subjects.PublishSubject

class ItinRepo(private val itinId: String, private val jsonUtil: IJsonToItinUtil, observable: Observable<MutableList<ItinCardData>>) : ItinRepoInterface {
    override val liveDataItin: MutableLiveData<Itin> = MutableLiveData()
    override val invalidDataSubject: PublishSubject<Unit> = PublishSubject.create()

    val syncObserver = object : DisposableObserver<MutableList<ItinCardData>>() {
        override fun onComplete() {
        }

        override fun onNext(t: MutableList<ItinCardData>) {
            val itin = fetchItin()
            if (itin != null) {
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

    init {
        if (fetchItin() != null) {
            liveDataItin.value = fetchItin()
            observable.subscribe(syncObserver)
        } else {
            invalidDataSubject.onNext(Unit)
        }
    }

    override fun dispose() {
        syncObserver.dispose()
    }
}
