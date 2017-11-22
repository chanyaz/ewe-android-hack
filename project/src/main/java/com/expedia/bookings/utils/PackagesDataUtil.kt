package com.expedia.bookings.utils

import com.expedia.bookings.services.LocalDateTypeAdapter
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import org.joda.time.LocalDate

class PackagesDataUtil {

    companion object {

        @JvmStatic
        fun generateGson(): Gson {
            val PATTERN = "yyyy-MM-dd"
            return GsonBuilder().registerTypeAdapter(LocalDate::class.java, LocalDateTypeAdapter(PATTERN)).create()
        }
    }
}
