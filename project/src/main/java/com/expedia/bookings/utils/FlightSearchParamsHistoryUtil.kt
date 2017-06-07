package com.expedia.bookings.utils

import android.content.Context
import com.expedia.bookings.data.flights.FlightSearchParams
import com.mobiata.android.Log
import com.mobiata.android.util.IoUtils
import java.io.IOException

object FlightSearchParamsHistoryUtil {

    private val FLIGHT_SERCH_PARAMS_HISTORY_FILE = "flight-search-params-history.dat"

    fun saveFlightParams(context: Context, params: FlightSearchParams, saveSuccess: ((Unit) -> Unit)? = null) {
        Thread {
            try {
                val paramsJson = FlightsV2DataUtil.generateGson().toJson(params)
                IoUtils.writeStringToFile(FLIGHT_SERCH_PARAMS_HISTORY_FILE, paramsJson, context)
                saveSuccess?.invoke(Unit)
            } catch (e: IOException) {
                Log.e("Save Flight search params Error: ", e)
            }
        }.start()
    }

    fun loadPreviousFlightSearchParams(context: Context, loadSuccess: ((FlightSearchParams) -> Unit)? = null) {
        Thread {
            try {
                val str = IoUtils.readStringFromFile(FLIGHT_SERCH_PARAMS_HISTORY_FILE, context)
                val params = FlightsV2DataUtil.generateGson().fromJson(str, FlightSearchParams::class.java)
                loadSuccess?.invoke(params)
            } catch (e: IOException) {
                Log.e("Error reading flight search params history", e)
            }
        }.start()
    }

    @JvmStatic fun deleteCachedFlightSearchParams(context: Context) {
        val file = context.getFileStreamPath(FLIGHT_SERCH_PARAMS_HISTORY_FILE)
        val fileExists = file.exists()
        val isDeleted = file.delete()
        if (fileExists && !isDeleted) {
            Log.e("Unable to delete flight search params history in " + FLIGHT_SERCH_PARAMS_HISTORY_FILE)
        }
    }
}