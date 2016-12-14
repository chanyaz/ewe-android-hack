package com.expedia.bookings.data.flights

import android.content.Context
import android.support.annotation.StringRes
import com.expedia.bookings.R
import com.expedia.bookings.utils.Strings
import com.squareup.phrase.Phrase

object FlightServiceClassType {
    enum class CabinCode constructor(@StringRes val resId: Int) {
        COACH(R.string.cabin_code_coach),
        PREMIUM_COACH(R.string.cabin_code_premium_coach),
        BUSINESS(R.string.cabin_code_business),
        FIRST(R.string.cabin_code_first)
    }


    fun getCabinCode(seatClass: String): Int {
        when (seatClass) {
            "coach" -> return CabinCode.COACH.resId
            "premium coach" -> return CabinCode.PREMIUM_COACH.resId
            "business" -> return CabinCode.BUSINESS.resId
            "first" -> return CabinCode.FIRST.resId
            else -> throw RuntimeException("Ran into unknown cabin code: " + seatClass)
        }
    }

    @JvmStatic fun getSeatClassAndBookingCodeText(context: Context, segment: FlightLeg.FlightSegment): String {
        if (Strings.isNotEmpty(segment.seatClass) && Strings.isNotEmpty(segment.bookingCode)) {
            return Phrase.from(context.resources.getString(R.string.flight_seatclass_booking_code_TEMPLATE))
                    .put("seat_class", context.resources.getString(getCabinCode(segment.seatClass)))
                    .put("booking_code", segment.bookingCode)
                    .format().toString()
        } else {
            return "";
        }
    }
}

