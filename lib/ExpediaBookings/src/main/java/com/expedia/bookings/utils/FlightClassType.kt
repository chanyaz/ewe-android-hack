package com.expedia.bookings.utils

class FlightClassType {

    enum class CabinCode(val cabinClassCode: String) {
        COACH("3"),
        PREMIUM_COACH("5"),
        BUSINESS("2"),
        FIRST("1"),
    }

    fun getCabinClass(seatClass: String): String {
        when (seatClass) {
            "coach" -> return CabinCode.COACH.cabinClassCode
            "premium coach" -> return CabinCode.PREMIUM_COACH.cabinClassCode
            "business" -> return CabinCode.BUSINESS.cabinClassCode
            "first" -> return CabinCode.FIRST.cabinClassCode
            else -> throw RuntimeException("Ran into unknown cabin code: " + seatClass)
        }
    }
}
