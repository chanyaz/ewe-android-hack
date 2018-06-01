package com.expedia.bookings.itin.helpers

import android.arch.lifecycle.MutableLiveData
import com.expedia.bookings.itin.lx.ItinLxRepoInterface
import com.expedia.bookings.itin.tripstore.data.Itin
import com.expedia.bookings.itin.tripstore.data.ItinLx
import io.reactivex.subjects.PublishSubject

class MockLxRepo(initialize: Boolean = true, val itin: Itin = ItinMocker.lxDetailsHappy) : ItinLxRepoInterface {
    override val invalidDataSubject: PublishSubject<Unit> = PublishSubject.create()
    var disposed = false
    override fun dispose() {
        disposed = true
    }

    override val liveDataLx: MutableLiveData<ItinLx> = MutableLiveData()
    override val liveDataItin: MutableLiveData<Itin> = MutableLiveData()
    init {
        if (initialize) {
            liveDataItin.value = itin
            liveDataLx.value = itin.activities!!.first()
        }
    }

    fun deleteUrl() {
        liveDataItin.value = ItinMocker.lxDetailsNoDetailsUrl
    }

    fun deleteID() {
        liveDataItin.value = ItinMocker.lxDetailsNoTripID
    }
}
