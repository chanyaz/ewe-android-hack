package com.expedia.bookings.itin.helpers

import android.arch.lifecycle.MutableLiveData
import com.expedia.bookings.itin.repositories.ItinHotelRepoInterface
import com.expedia.bookings.itin.tripstore.data.ItinHotel

class MockHotelRepo : ItinHotelRepoInterface {
    override val liveDataHotel: MutableLiveData<ItinHotel> = MutableLiveData()
}
