package com.expedia.bookings.data.rail.requests;

import com.expedia.bookings.data.BaseSearchParams
import com.expedia.bookings.data.SuggestionV4
import org.joda.time.Days
import org.joda.time.LocalDate

class RailSearchRequest(val searchType: SearchType, origin: SuggestionV4, destination: SuggestionV4, val departDate: LocalDate,
                        val returnDate: LocalDate?, val departTime: Long, val returnTime: Long?,
                        adults: Int, children: List<Int>) : BaseSearchParams(origin, destination, adults, children, departDate, returnDate) {
    enum class SearchType {
        ONE_WAY,
        ROUND_TRIP
    }

    class Builder(maxStay: Int, maxRange: Int) : BaseSearchParams.Builder(maxStay, maxRange) {
        private var searchType = SearchType.ONE_WAY
        private var departTime: Long? = null
        private var returnTime: Long? = null

        override fun build(): RailSearchRequest {
            if (areRequiredParamsFilled()) {
                return RailSearchRequest(searchType, originLocation!!, destinationLocation!!, startDate!!, endDate, departTime!!, returnTime, adults, children)
            } else {
                throw IllegalArgumentException();
            }
        }

        override fun areRequiredParamsFilled(): Boolean {
            val requiredOneWay = hasOriginAndDestination() && hasDepartDate() && hasDepartTime()
            if (searchType == SearchType.ONE_WAY) {
                return requiredOneWay
            } else {
                return requiredOneWay && hasReturnDate() && hasReturnTime()
            }
        }

        fun hasDepartDate(): Boolean {
            return startDate != null
        }

        fun hasReturnDate(): Boolean {
            return endDate != null
        }

        fun hasDepartTime(): Boolean {
            return departTime != null
        }

        fun hasReturnTime(): Boolean {
            return returnTime != null
        }

        fun departTime(time: Long?): BaseSearchParams.Builder {
            this.departTime = time
            return this
        }

        fun returnTime(time: Long?): BaseSearchParams.Builder {
            this.returnTime = time
            return this
        }

        fun searchType(isRoundTrip: Boolean): BaseSearchParams.Builder {
            this.searchType = if (isRoundTrip) SearchType.ROUND_TRIP else SearchType.ONE_WAY
            return this
        }

        override fun isOriginSameAsDestination(): Boolean {
            val departureStationCode = originLocation?.hierarchyInfo?.rails?.stationCode ?: ""
            val arrivalStationCode = destinationLocation?.hierarchyInfo?.rails?.stationCode ?: ""
            return departureStationCode.equals(arrivalStationCode)
        }

        override fun isWithinDateRange(): Boolean {
            if (searchType == SearchType.ROUND_TRIP) {
                return Days.daysBetween(LocalDate.now(), endDate).days <= maxRange
            } else {
                return Days.daysBetween(LocalDate.now(), startDate).days <= maxRange
            }
        }

        fun hasStartAndOrEndDates(): Boolean {
            if (searchType == SearchType.ROUND_TRIP) {
                return hasStartAndEndDates()
            } else {
                return hasStart()
            }
        }
    }
}

