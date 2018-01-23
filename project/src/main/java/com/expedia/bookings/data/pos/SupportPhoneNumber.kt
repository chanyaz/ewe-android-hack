package com.expedia.bookings.data.pos

import org.json.JSONObject

class SupportPhoneNumber(jsonData: JSONObject?) {
    private val supportNumberPhone: String
    private val supportNumberTablet: String

    init {
        if (jsonData == null) {
            supportNumberPhone = ""
            supportNumberTablet = ""
        } else {
            val androidNumber = jsonData.optString("Android", "")
            val androidTabletNumber = jsonData.optString("AndroidTablet", "")
            val genericNumber = jsonData.optString("*", "")

            supportNumberPhone = if (androidNumber.isNotEmpty()) androidNumber else genericNumber
            supportNumberTablet = if (androidTabletNumber.isNotEmpty()) androidTabletNumber else genericNumber
        }
    }

    fun getPhoneNumberForTabletDevice(): String {
        return supportNumberTablet
    }

    fun getPhoneNumberForPhoneDevice(): String {
        return supportNumberPhone
    }

    fun getPhoneNumberForDevice(isTablet: Boolean): String {
        return if (isTablet) supportNumberTablet else supportNumberPhone
    }
}
