package com.expedia.bookings.itin.tripstore.utils

import com.expedia.bookings.data.trips.TripFolder
import com.google.gson.Gson
import com.mobiata.android.Log

class JsonToFoldersUtil(private val fileUtils: ITripsJsonFileUtils) : IJsonToFoldersUtil {
    private val LOGGING_TAG = "JsonToFoldersUtil"

    override fun getTripFoldersFromDisk(): List<TripFolder> {
        val foldersJsonList = fileUtils.readFromFileDirectory()
        return parseTripFolders(foldersJsonList)
    }

    private fun parseTripFolders(foldersJsonList: List<String>): List<TripFolder> {
        val listOfFolders = mutableListOf<TripFolder>()
        val gson = Gson()
        foldersJsonList.forEach { folderJson ->
            try {
                val folder = gson.fromJson(folderJson, TripFolder::class.java)
                listOfFolders.add(folder)
            } catch (e: Exception) {
                Log.d(LOGGING_TAG, e.cause.toString())
            }
        }
        return listOfFolders
    }
}

interface IJsonToFoldersUtil {
    fun getTripFoldersFromDisk(): List<TripFolder>
}
