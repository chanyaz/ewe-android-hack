package com.expedia.bookings.utils

import com.expedia.bookings.data.packages.PackageSearchParams
import com.expedia.bookings.services.LocalDateTypeAdapter
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonSyntaxException
import org.joda.time.LocalDate

class PackagesDataUtil {

    companion object {

        fun getPackageSearchParamsFromJSON(packageSearchParamsJSON: String?): PackageSearchParams? {
            val gson = generateGson()
            if (Strings.isNotEmpty(packageSearchParamsJSON) ) {
                try {
                    return gson.fromJson(packageSearchParamsJSON, PackageSearchParams::class.java)
                } catch (jse: JsonSyntaxException) {
                    throw UnsupportedOperationException()
                }
            }
            return null
        }

        @JvmStatic
        fun generateGson(): Gson {
            val PATTERN = "yyyy-MM-dd"
            return GsonBuilder().registerTypeAdapter(LocalDate::class.java, LocalDateTypeAdapter(PATTERN)).create()
        }
    }
}
