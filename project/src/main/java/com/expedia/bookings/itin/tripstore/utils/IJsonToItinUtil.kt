package com.expedia.bookings.itin.tripstore.utils

import com.expedia.bookings.itin.tripstore.data.Itin

interface IJsonToItinUtil {
    fun getItin(itinId: String?): Itin?
    fun getItinList(): List<Itin>
}
