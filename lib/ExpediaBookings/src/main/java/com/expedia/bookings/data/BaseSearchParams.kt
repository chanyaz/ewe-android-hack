package com.expedia.bookings.data

import org.joda.time.Days
import org.joda.time.LocalDate

open class BaseSearchParams(val origin: SuggestionV4?, val destination: SuggestionV4?, val adults: Int, val children: List<Int>, val startDate: LocalDate, val endDate: LocalDate?) {

    open val guests = children.size + adults
    open val guestString = listOf(adults).plus(children).joinToString(",")
    val childrenString = children.joinToString(",")

    abstract class Builder(var maxStay: Int, val maxStartRange: Int) {
        protected var originLocation: SuggestionV4? = null
        protected var destinationLocation: SuggestionV4? = null
        protected var startDate: LocalDate? = null
        protected var endDate: LocalDate? = null
        protected var adults: Int = 1
        protected var children: List<Int> = emptyList()

        abstract fun isOriginSameAsDestination(): Boolean

        fun origin(city: SuggestionV4?): Builder {
            this.originLocation = city
            return this
        }

        open fun destination(city: SuggestionV4?): Builder {
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

        /*fun adults(adultsList: List<Int>): Builder {
            this.adultsList = adultsList
            return this
        }

        fun childrenList(childrenList: List<List<Int>>): Builder {
            this.childrenList = childrenList
            return this
        }*/

        abstract fun build(): BaseSearchParams

        abstract fun areRequiredParamsFilled(): Boolean

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

        open fun hasValidDateDuration(): Boolean {
            return Days.daysBetween(startDate, endDate).days <= maxStay
        }

        open fun isWithinDateRange(): Boolean {
            // end date can be max start range + 1
            return Days.daysBetween(LocalDate.now(), endDate).days <= maxStartRange + 1
        }
    }
}
