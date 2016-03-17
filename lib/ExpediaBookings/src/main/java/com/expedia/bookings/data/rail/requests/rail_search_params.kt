package com.expedia.bookings.data.rail.requests;

import com.expedia.bookings.data.BaseSearchParams
import com.expedia.bookings.data.SuggestionV4
import org.joda.time.LocalDate

class RailSearchRequest(val origin: SuggestionV4, val destination: SuggestionV4, val departDate: LocalDate, val returnDate: LocalDate?, adults: Int, children: List<Int>)  : BaseSearchParams(adults, children){

    enum class SearchType {
        ONE_WAY,
        ROUND_TRIP,
        OPEN_RETURN
    }

    class Builder() : BaseSearchParams.Builder(500) {
        private var origin: SuggestionV4? = null
        private var destination: SuggestionV4? = null
        private var departDate: LocalDate? = null
        private var returnDate: LocalDate? = null
        private var searchType = SearchType.ONE_WAY

        override fun build(): RailSearchRequest {
            if (areRequiredParamsFilled()) {
                return RailSearchRequest(departure!!, arrival!!, startDate!!, endDate, adults, children)
            } else {
                throw IllegalArgumentException();
            }
        }

        override fun areRequiredParamsFilled(): Boolean {
            return hasOriginAndDestination() && hasStartDate() && hasEndDate()
        }

        fun hasStartDate(): Boolean {
            return startDate != null
        }

        fun hasEndDate(): Boolean {
            return SearchType.ONE_WAY == searchType || returnDate != null
        }

        override fun hasValidDates(): Boolean {
            return hasStartDate() && hasEndDate()
        }

        fun hasOriginAndDestination(): Boolean {
            return departure != null && arrival != null
        }
    }
}

