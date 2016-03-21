package com.expedia.bookings.data.rail.requests;

import com.expedia.bookings.data.SuggestionV4
import org.joda.time.LocalDate

data class RailSearchRequest(val origin: SuggestionV4, val destination: SuggestionV4, val departDate: LocalDate, val returnDate: LocalDate?, val adults: Int, val children: List<Int>) {

    class Builder() {
        private var origin: SuggestionV4? = null
        private var destination: SuggestionV4? = null
        private var departDate: LocalDate? = null
        private var returnDate: LocalDate? = null
        private var adults: Int = 1
        private var children: List<Int> = emptyList()

        fun origin(origin: SuggestionV4?): Builder {
            this.origin = origin
            return this
        }

        fun destination(destination: SuggestionV4?): Builder {
            this.destination = destination
            return this
        }

        fun departDate(departDate: LocalDate?): Builder {
            this.departDate = departDate
            return this
        }

        fun returnDate(returnDate: LocalDate?): Builder {
            this.returnDate = returnDate
            return this
        }

        fun adults(adults: Int): Builder {
            this.adults = adults
            return this
        }

        fun children(children: List<Int>): Builder {
            this.children = children
            return this
        }

        fun build(): RailSearchRequest {
            if (areRequiredParamsFilled()) {
                return RailSearchRequest(origin!!, destination!!, departDate!!, returnDate, adults, children)
            } else {
                throw IllegalArgumentException();
            }
        }

        fun areRequiredParamsFilled(): Boolean {
            return hasOriginAndDestination() && hasStartDate()
        }

        fun hasStartDate(): Boolean {
            return departDate != null
        }

        fun hasOriginAndDestination(): Boolean {
            return origin != null && destination != null
        }
    }
}

