package com.expedia.bookings.data

import org.joda.time.LocalDate

abstract class AbstractFlightSearchParams(origin: SuggestionV4?, destination: SuggestionV4?, adults: Int, children: List<Int>, startDate: LocalDate, endDate: LocalDate?, var infantSeatingInLap: Boolean) : BaseSearchParams(origin, destination, adults, children, startDate, endDate) {

    abstract class Builder(maxStay: Int, maxStartRange: Int) : BaseSearchParams.Builder(maxStay, maxStartRange) {
        protected var infantSeatingInLap = false

        fun infantSeatingInLap(infantSeatingInLap: Boolean): BaseSearchParams.Builder {
            this.infantSeatingInLap = infantSeatingInLap
            return this
        }
    }

    fun getEndOfTripDate(): LocalDate {
        return endDate ?: startDate
    }
}
