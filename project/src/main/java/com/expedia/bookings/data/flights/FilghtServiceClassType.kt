package com.expedia.bookings.data.flights

import android.content.Context
import android.support.annotation.StringRes
import com.expedia.bookings.R
import com.expedia.bookings.utils.Strings
import com.squareup.phrase.Phrase

object FlightServiceClassType {
    enum class CabinCode constructor(@StringRes val resId: Int, val trackCode: String) {
        COACH(R.string.cabin_code_coach, "E"),
        PREMIUM_COACH(R.string.cabin_code_premium_coach, "P"),
        BUSINESS(R.string.cabin_code_business, "B"),
        FIRST(R.string.cabin_code_first, "F"),
        MIXED(R.string.cabin_code_mixed, "M")
    }

    fun getCabinCodeResourceId(seatClass: String): Int {
        when (seatClass) {
            "coach" -> return CabinCode.COACH.resId
            "premium coach" -> return CabinCode.PREMIUM_COACH.resId
            "business" -> return CabinCode.BUSINESS.resId
            "first" -> return CabinCode.FIRST.resId
            "mixed" -> return CabinCode.MIXED.resId
            else -> throw RuntimeException("Ran into unknown cabin code: " + seatClass)
        }
    }

    @JvmStatic fun getSeatClassAndBookingCodeText(context: Context, seatClass: String , bookingCode: String): String {
        if (Strings.isNotEmpty(seatClass) && Strings.isNotEmpty(bookingCode)) {
            return Phrase.from(context.resources.getString(R.string.flight_seatclass_booking_code_TEMPLATE))
                    .put("seat_class", context.resources.getString(getCabinCodeResourceId(seatClass)))
                    .put("booking_code", bookingCode)
                    .format().toString()
        } else {
            return ""
        }
    }

    @JvmStatic fun getCabinClassTrackCode(mCabinClassPreference: String): String {
        when (mCabinClassPreference) {
            FlightServiceClassType.CabinCode.COACH.name -> return FlightServiceClassType.CabinCode.COACH.trackCode
            FlightServiceClassType.CabinCode.PREMIUM_COACH.name -> return FlightServiceClassType.CabinCode.PREMIUM_COACH.trackCode
            FlightServiceClassType.CabinCode.BUSINESS.name -> return FlightServiceClassType.CabinCode.BUSINESS.trackCode
            FlightServiceClassType.CabinCode.FIRST.name -> return FlightServiceClassType.CabinCode.FIRST.trackCode
            else -> throw RuntimeException("Ran into unknown cabin code: " + mCabinClassPreference)
        }
    }

    @JvmStatic fun getMIDCabinClassRequestName(cabinCode: CabinCode): String {
        return when (cabinCode) {
            CabinCode.COACH -> "coach"
            CabinCode.PREMIUM_COACH -> "premium coach"
            CabinCode.BUSINESS -> "business"
            CabinCode.FIRST -> "first"
            else -> ""
        }
    }

    @JvmStatic fun getCabinCodeFromMIDParam(cabinCodeParam: String): CabinCode {
        return when (cabinCodeParam) {
            "coach" -> CabinCode.COACH
            "premium coach" -> CabinCode.PREMIUM_COACH
            "business" -> CabinCode.BUSINESS
            "first" -> CabinCode.FIRST
            else -> CabinCode.COACH
        }
    }

    @JvmStatic fun getCabinCodeForRichContent(cabinCodeParam: String): String {
        return when (cabinCodeParam) {
            "coach" -> "ECONOMY"
            "premium coach" -> "PREMIUM_COACH"
            "business" -> "BUSINESS"
            "first" -> "FIRST"
            else -> "ECONOMY"
        }
    }
}
