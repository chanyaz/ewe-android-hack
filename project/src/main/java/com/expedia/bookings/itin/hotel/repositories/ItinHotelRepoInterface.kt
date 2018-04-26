package com.expedia.bookings.itin.hotel.repositories

import android.arch.lifecycle.MutableLiveData
import com.expedia.bookings.itin.tripstore.data.Itin
import com.expedia.bookings.itin.tripstore.data.ItinHotel

interface ItinHotelRepoInterface {
    val liveDataItin: MutableLiveData<Itin>
    val liveDataHotel: MutableLiveData<ItinHotel>
    val liveDataInvalidItin: MutableLiveData<Unit>
}
