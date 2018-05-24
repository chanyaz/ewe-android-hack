package com.expedia.bookings.itin.common

import android.arch.lifecycle.MutableLiveData
import com.expedia.bookings.itin.tripstore.data.Itin
import io.reactivex.subjects.PublishSubject

interface ItinRepoInterface {
    val liveDataItin: MutableLiveData<Itin>
    val invalidDataSubject: PublishSubject<Unit>
    fun dispose()
}
