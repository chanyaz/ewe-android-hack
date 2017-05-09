package com.expedia.bookings.data.hotel

class HotelValueAdd(val valueAddsEnum: ValueAddsEnum, val description: String): Comparable<HotelValueAdd>  {

    val iconId: Int get() = valueAddsEnum.iconId

    override fun compareTo(other: HotelValueAdd): Int {
        val preferenceCompare = valueAddsEnum.priority.minus(other.valueAddsEnum.priority)
        if (preferenceCompare == 0) {
            return description.compareTo(other.description)
        }
        return preferenceCompare
    }
}
