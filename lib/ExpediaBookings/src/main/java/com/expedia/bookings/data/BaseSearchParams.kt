package com.expedia.bookings.data

import org.joda.time.Days
import org.joda.time.LocalDate

open class BaseSearchParams(val adults: Int, val children: List<Int>) {

    val guests = children.size + adults
    val guestString = listOf(adults).plus(children).joinToString(",")
    val childrenString = children.joinToString(",")

    abstract class Builder(val maxStay: Int) {
        protected  var departure: SuggestionV4? = null
        protected var arrival: SuggestionV4? = null
        protected var startDate: LocalDate? = null
        protected var endDate: LocalDate? = null
        protected var adults: Int = 1
        protected var children: List<Int> = emptyList()
        protected var infantSeatingInLap: Boolean = false

        fun departure(city: SuggestionV4?): Builder {
            this.departure = city
            return this
        }

        fun arrival(city: SuggestionV4?): Builder {
            this.arrival = city
            return this
        }

        fun startDate(date: LocalDate?): Builder {
            this.startDate = date
            return this
        }

        fun endDate(date: LocalDate?): Builder {
            this.endDate = date
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

        fun infantSeatingInLap(infantSeatingInLap: Boolean): Builder {
            this.infantSeatingInLap = infantSeatingInLap
            return this
        }

        abstract fun build(): BaseSearchParams

        open fun areRequiredParamsFilled(): Boolean {
            return hasDeparture() && hasStartAndEndDates()
        }

        open fun hasStartAndEndDates(): Boolean {
            return startDate != null && endDate != null
        }

        fun hasStart(): Boolean {
            return startDate != null
        }

        fun hasEnd(): Boolean {
            return endDate != null
        }

        fun hasDepartureAndArrival(): Boolean {
            return hasDeparture() && hasArrival()
        }

        fun hasDeparture(): Boolean {
            return departure != null
        }

        fun hasArrival(): Boolean {
            return arrival != null
        }

        open fun hasValidDates(): Boolean {
            return Days.daysBetween(startDate, endDate).days <= maxStay
        }
    }
}

data class TravelerParams(val numberOfAdults: Int, val childrenAges: List<Int>)