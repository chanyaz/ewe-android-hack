package com.expedia.bookings.utils

import android.content.Context
import com.expedia.bookings.data.foursquare.FourSquareResponse
import com.expedia.bookings.data.multiitem.MultiItemApiSearchResponse
import com.expedia.bookings.data.packages.PackageSearchResponse
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.mobiata.android.Log
import com.mobiata.android.util.IoUtils
import java.io.IOException

/**
 * Created by nbirla on 09/03/18.
 */
object FoursquareResponseUtil {

    private val fileName = "foursquare-response.dat"

    fun saveResponse(context: Context, response: FourSquareResponse, saveSuccess: ((Unit) -> Unit)? = null) {
        Thread(Runnable {
            val type = object : TypeToken<FourSquareResponse>() {}.type
            val responseJson = Gson().toJson(response, type)
            try {
                IoUtils.writeStringToFile(fileName, responseJson, context)
                saveSuccess?.invoke(Unit)
            } catch (e: IOException) {
                Log.e("Foursquare save Error: ", e)
            }
        }).start()
    }

    fun loadResponse(context: Context): FourSquareResponse? {
        try {
            val str = IoUtils.readStringFromFile(fileName, context)
            val type = object : TypeToken<FourSquareResponse>() {}.type
            return Gson().fromJson<FourSquareResponse>(str, type)
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return null
    }

}
