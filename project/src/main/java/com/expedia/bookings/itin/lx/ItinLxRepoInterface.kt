package com.expedia.bookings.itin.lx

import android.arch.lifecycle.MutableLiveData
import com.expedia.bookings.itin.tripstore.data.Itin
import com.expedia.bookings.itin.tripstore.data.ItinLx
import io.reactivex.subjects.PublishSubject

interface ItinLxRepoInterface {
    val liveDataLx: MutableLiveData<ItinLx>
    val liveDataItin: MutableLiveData<Itin>
    val invalidDataSubject: PublishSubject<Unit>
    fun dispose()
}
