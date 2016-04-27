package com.expedia.bookings.data

import org.joda.time.Days
import org.joda.time.LocalDate

open class BaseSearchParams(val adults: Int, val children: List<Int>) {

    val guests = children.size + adults
    val guestString = listOf(adults).plus(children).joinToString(",")
    val childrenString = children.joinToString(",")

    abstract class Builder(val maxStay: Int) {
        protected var originLocation: SuggestionV4? = null
        protected var destinationLocation: SuggestionV4? = null
        protected var startDate: LocalDate? = null
        protected var endDate: LocalDate? = null
        protected var adults: Int = 1
        protected var children: List<Int> = emptyList()
        protected var infantSeatingInLap: Boolean = false

        abstract fun isOriginSameAsDestination(): Boolean;

        fun origin(city: SuggestionV4?): Builder {
            this.originLocation = city
            return this
        }

        fun destination(city: SuggestionV4?): Builder {
            this.destinationLocation = city
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
            return hasOriginLocation() && hasStartAndEndDates()
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

        open fun hasOriginAndDestination(): Boolean {
            return hasOriginLocation() && hasDestinationLocation()
        }

        fun hasOriginLocation(): Boolean {
            return originLocation != null
        }

        fun hasDestinationLocation(): Boolean {
            return destinationLocation != null
        }

        open fun hasValidDates(): Boolean {
            return Days.daysBetween(startDate, endDate).days <= maxStay
        }
    }
}

data class TravelerParams(val numberOfAdults: Int, val childrenAges: List<Int>)