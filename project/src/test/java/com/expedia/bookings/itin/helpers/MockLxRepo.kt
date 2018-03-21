package com.expedia.bookings.itin.helpers

import android.arch.lifecycle.MutableLiveData
import com.expedia.bookings.itin.lx.ItinLxRepoInterface
import com.expedia.bookings.itin.tripstore.data.Itin
import com.expedia.bookings.itin.tripstore.data.ItinLx

class MockLxRepo : ItinLxRepoInterface {
    override val liveDataLx: MutableLiveData<ItinLx> = MutableLiveData()
    override val liveDataItin: MutableLiveData<Itin> = MutableLiveData()
}
