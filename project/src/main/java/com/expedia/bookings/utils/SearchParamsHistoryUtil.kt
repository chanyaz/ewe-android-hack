package com.expedia.bookings.utils

import android.content.Context
import com.expedia.bookings.data.flights.FlightSearchParams
import com.expedia.bookings.data.packages.PackageSearchParams
import com.expedia.bookings.data.packages.PackageSearchParamsForSaving
import com.mobiata.android.Log
import com.mobiata.android.util.IoUtils
import java.io.IOException

object SearchParamsHistoryUtil {

    private val FLIGHT_SEARCH_PARAMS_HISTORY_FILE = "flight-search-params-history.dat"
    private val PACKAGE_SEARCH_PARAMS_HISTORY_FILE = "package-search-params-history.dat"
    private val PACKAGE_SEARCH_PARAMS_HISTORY_FILE_V2 = "package-search-params-history-v2.dat"

    fun saveFlightParams(context: Context, params: FlightSearchParams, saveSuccess: ((Unit) -> Unit)? = null) {
        Thread {
            try {
                val paramsJson = FlightsV2DataUtil.generateGson().toJson(params)
                IoUtils.writeStringToFile(FLIGHT_SEARCH_PARAMS_HISTORY_FILE, paramsJson, context)
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
                        params.endDate!!, params.adults, params.children, params.infantSeatingInLap, params.flightCabinClass)
                val paramsJson = PackagesDataUtil.generateGson().toJson(paramsToSave)
                val fileName = getPackagesSearchHistoryParamsFileName(context)
                IoUtils.writeStringToFile(fileName, paramsJson, context)
                saveSuccess?.invoke(Unit)
            } catch (e: IOException) {
                Log.e("Save Package search params Error: ", e)
            }
        }.start()
    }

    private fun getPackagesSearchHistoryParamsFileName(context: Context): String = if (isPackagesMISRealWorldGeoEnabled(context)) PACKAGE_SEARCH_PARAMS_HISTORY_FILE_V2 else PACKAGE_SEARCH_PARAMS_HISTORY_FILE

    fun loadPreviousFlightSearchParams(context: Context, loadSuccess: ((FlightSearchParams) -> Unit)? = null, loadFailed: (() -> Unit)? = null) {
        Thread {
            try {
                val str = IoUtils.readStringFromFile(FLIGHT_SEARCH_PARAMS_HISTORY_FILE, context)
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
                val fileName = getPackagesSearchHistoryParamsFileName(context)
                val str = IoUtils.readStringFromFile(fileName, context)
                val paramsSaved = PackagesDataUtil.generateGson().fromJson(str, PackageSearchParamsForSaving::class.java)
                val params = PackageSearchParams(
                        origin = paramsSaved.origin,
                        destination = paramsSaved.destination,
                        startDate = paramsSaved.startDate,
                        endDate = paramsSaved.endDate,
                        adults = paramsSaved.adults,
                        children = paramsSaved.children,
                        infantSeatingInLap = paramsSaved.infantSeatingInLap,
                        flightCabinClass = paramsSaved.flightCabinClass)
                loadSuccess?.invoke(params)
            } catch (e: IOException) {
                Log.e("Error reading package search params history", e)
            }
        }.start()
    }

    @JvmStatic fun deleteCachedSearchParams(context: Context) {
        val filesToDelete = arrayOf(FLIGHT_SEARCH_PARAMS_HISTORY_FILE, PACKAGE_SEARCH_PARAMS_HISTORY_FILE)
        for (fileName in filesToDelete) {
            val file = context.getFileStreamPath(fileName)
            val fileExists = file.exists()
            val isDeleted = file.delete()
            if (fileExists && !isDeleted) {
                Log.e("Unable to delete flight search params history in " + FLIGHT_SEARCH_PARAMS_HISTORY_FILE)
            }
        }
    }
}
