package com.expedia.bookings.itin.tripstore.utils

import android.content.Context
import com.expedia.bookings.itin.tripstore.data.Itin
import com.expedia.bookings.itin.tripstore.data.ItinDetailsResponse
import com.expedia.bookings.utils.Ui
import com.google.gson.GsonBuilder
import com.mobiata.android.Log

object JsonToItinUtil {

    private val LOGGING_TAG = "JsonToItinUtil"

    fun getItin(context: Context, itinId: String?): Itin? {
        val fileUtils = Ui.getApplication(context).appComponent().tripJsonFileUtils()
        val itinJson = fileUtils.readTripFromFile(itinId)
        val gson = GsonBuilder().create()
        if (!itinJson.isNullOrEmpty()) {
            try {
                val itinDetailsResponse = gson.fromJson(itinJson, ItinDetailsResponse::class.java)
                return itinDetailsResponse?.itin
            } catch (e: Exception) {
                Log.d(LOGGING_TAG, e.cause.toString())
            }
        }
        return null
    }
}
