package com.expedia.bookings.itin.tripstore.utils

import android.content.Context
import com.expedia.bookings.itin.tripstore.data.Itin

interface IJsonToItinUtil {

    fun getItin(context: Context, itinId: String?): Itin?
}
