package com.expedia.bookings.utils

import android.content.Context
import com.expedia.bookings.data.flights.FlightSearchParams
import com.expedia.bookings.data.packages.PackageSearchParams
import com.expedia.bookings.data.packages.PackageSearchParamsForSaving
import com.mobiata.android.Log
import com.mobiata.android.util.IoUtils
import java.io.IOException

object SearchParamsHistoryUtil {

    private val FLIGHT_SERCH_PARAMS_HISTORY_FILE = "flight-search-params-history.dat"
    private val PACKAGE_SERCH_PARAMS_HISTORY_FILE = "package-search-params-history.dat"

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

    fun savePackageParams(context: Context, params: PackageSearchParams, saveSuccess: ((Unit) -> Unit)? = null) {
        Thread {
            try {
                val paramsToSave = PackageSearchParamsForSaving(params.origin, params.destination, params.startDate,
                        params.endDate!!, params.adults, params.children, params.infantSeatingInLap)
                val paramsJson = PackagesDataUtil.generateGson().toJson(paramsToSave)
                IoUtils.writeStringToFile(PACKAGE_SERCH_PARAMS_HISTORY_FILE, paramsJson, context)
                saveSuccess?.invoke(Unit)
            } catch (e: IOException) {
                Log.e("Save Package search params Error: ", e)
            }
        }.start()
    }

    fun loadPreviousFlightSearchParams(context: Context, loadSuccess: ((FlightSearchParams) -> Unit)? = null, loadFailed: (() -> Unit)? = null) {
        Thread {
            try {
                val str = IoUtils.readStringFromFile(FLIGHT_SERCH_PARAMS_HISTORY_FILE, context)
                val params = FlightsV2DataUtil.generateGson().fromJson(str, FlightSearchParams::class.java)
                loadSuccess?.invoke(params)
            } catch (e: IOException) {
                Log.e("Error reading flight search params history", e)
                loadFailed?.invoke()
            }
        }.start()
    }

    fun loadPreviousPackageSearchParams(context: Context, loadSuccess: ((PackageSearchParams) -> Unit)? = null) {
        Thread {
            try {
                val str = IoUtils.readStringFromFile(PACKAGE_SERCH_PARAMS_HISTORY_FILE, context)
                val paramsSaved = PackagesDataUtil.generateGson().fromJson(str, PackageSearchParamsForSaving::class.java)
                val params = PackageSearchParams(
                        origin = paramsSaved.origin,
                        destination = paramsSaved.destination,
                        startDate = paramsSaved.startDate,
                        endDate = paramsSaved.endDate,
                        adults = paramsSaved.adults,
                        children = paramsSaved.children,
                        infantSeatingInLap = paramsSaved.infantSeatingInLap)
                loadSuccess?.invoke(params)
            } catch (e: IOException) {
                Log.e("Error reading package search params history", e)
            }
        }.start()
    }

    @JvmStatic fun deleteCachedSearchParams(context: Context) {
        val filesToDelete = arrayOf(FLIGHT_SERCH_PARAMS_HISTORY_FILE, PACKAGE_SERCH_PARAMS_HISTORY_FILE)
        for (fileName in filesToDelete) {
            val file = context.getFileStreamPath(fileName)
            val fileExists = file.exists()
            val isDeleted = file.delete()
            if (fileExists && !isDeleted) {
                Log.e("Unable to delete flight search params history in " + FLIGHT_SERCH_PARAMS_HISTORY_FILE)
            }
        }
    }
}
