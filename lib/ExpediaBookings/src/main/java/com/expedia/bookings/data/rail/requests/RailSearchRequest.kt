package com.expedia.bookings.data.rail.requests;

import com.expedia.bookings.data.BaseSearchParams
import com.expedia.bookings.data.SuggestionV4
import org.joda.time.LocalDate

class RailSearchRequest(origin: SuggestionV4, destination: SuggestionV4, val departDate: LocalDate, val returnDate: LocalDate?, adults: Int, children: List<Int>)  : BaseSearchParams(origin, destination, adults, children, departDate, returnDate){

    enum class SearchType {
        ONE_WAY,
        ROUND_TRIP,
        OPEN_RETURN
    }

    class Builder() : BaseSearchParams.Builder(330, 500) {
        private var returnDate: LocalDate? = null
        private var searchType = SearchType.ONE_WAY

        override fun build(): RailSearchRequest {
            if (areRequiredParamsFilled()) {
                return RailSearchRequest(originLocation!!, destinationLocation!!, startDate!!, endDate, adults, children)
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

        override fun hasValidDateDuration(): Boolean {
            return hasStartDate() && hasEndDate()
        }

        // TODO - implement this once we know where to get rail origin/destination from
        override fun isOriginSameAsDestination(): Boolean {
            return false
        }
    }
}

