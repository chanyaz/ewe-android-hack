package com.expedia.bookings.itin.helpers

import android.arch.lifecycle.MutableLiveData
import com.expedia.bookings.itin.common.ItinRepoInterface
import com.expedia.bookings.itin.tripstore.data.Itin
import io.reactivex.subjects.PublishSubject

class MockItinRepo() : ItinRepoInterface {
    override val invalidDataSubject: PublishSubject<Unit> = PublishSubject.create()
    override val liveDataItin: MutableLiveData<Itin> = MutableLiveData()
    var disposed = false
    override fun dispose() {
        disposed = true
    }
}
