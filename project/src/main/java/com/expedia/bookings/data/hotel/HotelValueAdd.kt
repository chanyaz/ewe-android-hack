package com.expedia.bookings.data.hotel

import android.content.Context

class HotelValueAdd(val context: Context, val valueAddsEnum: ValueAddsEnum, val apiDescription: String) : Comparable<HotelValueAdd> {

    val iconId: Int get() = valueAddsEnum.iconId

    override fun compareTo(other: HotelValueAdd): Int {
        val preferenceCompare = valueAddsEnum.compareTo(other.valueAddsEnum)
        if (preferenceCompare == 0) {
            return apiDescription.compareTo(other.apiDescription)
        }
        return preferenceCompare
    }
}
