package com.expedia.bookings.itin.repositories

import android.arch.lifecycle.MutableLiveData
import com.expedia.bookings.itin.tripstore.data.ItinHotel

interface ItinHotelRepoInterface {
    val liveDataHotel: MutableLiveData<ItinHotel>
}
