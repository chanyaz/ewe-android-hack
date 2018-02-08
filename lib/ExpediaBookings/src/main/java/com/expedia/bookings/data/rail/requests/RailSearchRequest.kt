package com.expedia.bookings.data.rail.requests

import com.expedia.bookings.data.BaseSearchParams
import com.expedia.bookings.data.SuggestionV4
import com.expedia.bookings.data.rail.responses.RailCard
import org.joda.time.Days
import org.joda.time.LocalDate

class RailSearchRequest(val searchType: SearchType, origin: SuggestionV4, destination: SuggestionV4, val departDate: LocalDate,
                        val returnDate: LocalDate?, val departDateTimeMillis: Int, val returnDateTimeMillis: Int?,
                        adults: Int, children: List<Int>, val youths: List<Int>, val seniors: List<Int>, val selectedRailCards: List<RailCard>) : BaseSearchParams(origin, destination, adults, children, departDate, returnDate) {
    enum class SearchType {
        ONE_WAY,
        ROUND_TRIP
    }

    override val guests = adults + children.size + youths.size + seniors.size
    override val guestString = listOf(adults).plus(children).plus(youths).plus(seniors).joinToString(",")

    fun isRoundTripSearch(): Boolean {
        return this.searchType == SearchType.ROUND_TRIP
    }

    class Builder(maxStay: Int, maxStartRange: Int) : BaseSearchParams.Builder(maxStay, maxStartRange) {
        private var searchType = SearchType.ONE_WAY
        private var departDateTimeMillis: Int? = null
        private var returnDateTimeMillis: Int? = null
        private var selectedRailCards = emptyList<RailCard>()
        private var youths: List<Int> = emptyList()
        private var seniors: List<Int> = emptyList()
        override fun build(): RailSearchRequest {
            if (areRequiredParamsFilled()) {
                return RailSearchRequest(searchType, originLocation!!, destinationLocation!!, startDate!!, endDate, departDateTimeMillis!!, returnDateTimeMillis, adults, children, youths, seniors, selectedRailCards)
            } else {
                throw IllegalArgumentException()
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
            return departDateTimeMillis != null
        }

        fun hasReturnTime(): Boolean {
            return returnDateTimeMillis != null
        }

        fun departDateTimeMillis(time: Int?): Builder {
            this.departDateTimeMillis = time
            return this
        }

        fun returnDateTimeMillis(time: Int?): Builder {
            this.returnDateTimeMillis = time
            return this
        }

        fun searchType(isRoundTrip: Boolean): Builder {
            this.searchType = if (isRoundTrip) SearchType.ROUND_TRIP else SearchType.ONE_WAY
            return this
        }

        fun fareQualifierList(selectedRailCards: List<RailCard>): Builder {
            this.selectedRailCards = selectedRailCards
            return this
        }

        override fun isOriginSameAsDestination(): Boolean {
            val departureStationCode = originLocation?.hierarchyInfo?.rails?.stationCode ?: ""
            val arrivalStationCode = destinationLocation?.hierarchyInfo?.rails?.stationCode ?: ""
            return departureStationCode.equals(arrivalStationCode)
        }

        override fun isWithinDateRange(): Boolean {
            if (searchType == SearchType.ROUND_TRIP) {
                return Days.daysBetween(LocalDate.now(), endDate).days <= maxStartRange
            } else {
                return Days.daysBetween(LocalDate.now(), startDate).days <= maxStartRange
            }
        }

        fun hasStartAndOrEndDates(): Boolean {
            if (searchType == SearchType.ROUND_TRIP) {
                return hasStartAndEndDates()
            } else {
                return hasStart()
            }
        }

        fun youths(youths: List<Int>): Builder {
            this.youths = youths
            return this
        }

        fun seniors(seniors: List<Int>): Builder {
            this.seniors = seniors
            return this
        }

        fun isRailCardsCountInvalid(): Boolean {
            return selectedRailCards.size > adults + seniors.size + youths.size + children.size
        }
    }
}
