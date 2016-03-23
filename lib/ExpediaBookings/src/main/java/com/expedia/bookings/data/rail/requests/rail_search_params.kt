package com.expedia.bookings.data.rail.requests;

import com.expedia.bookings.data.BaseSearchParams
import com.expedia.bookings.data.SuggestionV4
import org.joda.time.LocalDate

class RailSearchRequest(val origin: SuggestionV4, val destination: SuggestionV4, val departDate: LocalDate, val returnDate: LocalDate?, adults: Int, children: List<Int>)  : BaseSearchParams(adults, children){

    class Builder() : BaseSearchParams.Builder(500) {

        override fun build(): RailSearchRequest {
            if (areRequiredParamsFilled()) {
                return RailSearchRequest(departure!!, arrival!!, checkIn!!, checkOut, adults, children)
            } else {
                throw IllegalArgumentException();
            }
        }

        override fun areRequiredParamsFilled(): Boolean {
            return hasOriginAndDestination() && hasStartDate()
        }

        fun hasStartDate(): Boolean {
            return checkIn != null
        }

        fun hasOriginAndDestination(): Boolean {
            return departure != null && arrival != null
        }
    }
}

