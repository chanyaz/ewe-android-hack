package com.expedia.bookings.itin.tripstore.utils

import com.expedia.bookings.itin.tripstore.data.Itin
import com.expedia.bookings.itin.tripstore.data.ItinDetailsResponse
import com.google.gson.GsonBuilder
import com.mobiata.android.Log
import javax.inject.Inject

class JsonToItinUtil @Inject constructor(private val fileUtils: ITripsJsonFileUtils) : IJsonToItinUtil {

    private val LOGGING_TAG = "JsonToItinUtil"

    override fun getItin(itinId: String?): Itin? {
        val itinJson = fileUtils.readTripFromFile(itinId)
        return parseItin(itinJson)
    }

    private fun parseItin(itinJson: String?): Itin? {
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

    override fun getItinList(): List<Itin> {
        val retList = mutableListOf<Itin>()
        val jsonList = fileUtils.readTripsFromFile()
        jsonList.forEach { json ->
            parseItin(json)?.let { nonNullItin ->
                retList.add(nonNullItin)
            }
        }
        return retList
    }
}
