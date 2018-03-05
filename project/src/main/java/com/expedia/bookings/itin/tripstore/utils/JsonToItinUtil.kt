package com.expedia.bookings.itin.tripstore.utils

import android.content.Context
import com.expedia.bookings.itin.tripstore.data.Itin
import com.expedia.bookings.itin.tripstore.data.ItinDetailsResponse
import com.expedia.bookings.utils.Ui
import com.google.gson.GsonBuilder

object JsonToItinUtil {

    fun getItin(context: Context, itinId: String?): Itin? {
        val fileUtils = Ui.getApplication(context).appComponent().tripJsonFileUtils()
        val itinJson = fileUtils.readTripFromFile(itinId)
        val gson = GsonBuilder().create()
        if (!itinJson.isNullOrEmpty()) {
            val itinDetailsResponse = gson.fromJson(itinJson, ItinDetailsResponse::class.java)
            return itinDetailsResponse?.itin
        }
        return null
    }
}
