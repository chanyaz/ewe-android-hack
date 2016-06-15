package com.expedia.bookings.data

import org.joda.time.LocalDate

abstract class AbstractFlightSearchParams(adults: Int, children: List<Int>, startDate: LocalDate, endDate: LocalDate?, val infantSeatingInLap: Boolean) : BaseSearchParams(adults, children, startDate, endDate) {

    abstract class Builder(maxStay: Int, maxRange: Int): BaseSearchParams.Builder(maxStay, maxRange) {
        protected var infantSeatingInLap = false

        fun infantSeatingInLap(infantSeatingInLap: Boolean): BaseSearchParams.Builder {
            this.infantSeatingInLap = infantSeatingInLap
            return this
        }
    }
}
