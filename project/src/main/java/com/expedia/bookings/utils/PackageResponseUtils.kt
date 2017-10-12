package com.expedia.bookings.utils

import android.content.Context
import com.expedia.bookings.data.hotels.HotelOffersResponse
import com.expedia.bookings.data.multiitem.BundleSearchResponse
import com.expedia.bookings.data.multiitem.MultiItemApiSearchResponse
import com.expedia.bookings.data.packages.PackageSearchResponse
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.mobiata.android.Log
import com.mobiata.android.util.IoUtils
import java.io.IOException

object PackageResponseUtils {

    val RECENT_PACKAGE_HOTELS_FILE = "package-hotel-response.dat"
    val RECENT_PACKAGE_OUTBOUND_FLIGHT_FILE = "package-outbound_flight.dat"
    val RECENT_PACKAGE_INBOUND_FLIGHT_FILE = "package-inbound_flight.dat"
    val RECENT_PACKAGE_HOTEL_OFFER_FILE = "hotel_offer.dat"


    fun savePackageResponse(context: Context, response: BundleSearchResponse, file: String, saveSuccess: ((Unit) -> Unit)? = null) {
        Thread(Runnable {
            val type = if(response is MultiItemApiSearchResponse) object: TypeToken<MultiItemApiSearchResponse>() {}.type else object: TypeToken<PackageSearchResponse>() {}.type
            val responseJson = Gson().toJson(response, type)
            try {
                IoUtils.writeStringToFile(file, responseJson, context)
                saveSuccess?.invoke(Unit)
            } catch (e: IOException) {
                Log.e("Save History Error: ", e)
            }
        }).start()
    }

    fun saveHotelOfferResponse(context: Context, hotelOffer: HotelOffersResponse, file: String, saveSuccess: ((Unit) -> Unit)? = null) {
        Thread(Runnable {
            val type = object : TypeToken<HotelOffersResponse>() {}.type
            val responseJson = Gson().toJson(hotelOffer, type)
            try {
                IoUtils.writeStringToFile(file, responseJson, context)
                saveSuccess?.invoke(Unit)
            } catch (e: IOException) {
                Log.e("Save History Error: ", e)
            }
        }).start()
    }

    fun loadPackageResponse(context: Context, file: String, isMidApiEnabled: Boolean): BundleSearchResponse? {
        try {
            val str = IoUtils.readStringFromFile(file, context)
            if(isMidApiEnabled) {
                val type = object : TypeToken<MultiItemApiSearchResponse>() {}.type
                return Gson().fromJson<MultiItemApiSearchResponse>(str, type)
            } else {
                val type = object : TypeToken<PackageSearchResponse>() {}.type
                return Gson().fromJson<PackageSearchResponse>(str, type)
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return null
    }

    fun loadHotelOfferResponse(context: Context, file: String): HotelOffersResponse? {
        var recentResponse: HotelOffersResponse? = null
        try {
            val str = IoUtils.readStringFromFile(file, context)
            val type = object : TypeToken<HotelOffersResponse>() {}.type
            recentResponse = Gson().fromJson<HotelOffersResponse>(str, type)
        } catch (e: IOException) {
            e.printStackTrace()
        }

        return recentResponse
    }
}
