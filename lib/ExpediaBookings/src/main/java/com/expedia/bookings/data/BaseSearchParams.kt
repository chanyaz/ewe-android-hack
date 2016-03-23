package com.expedia.bookings.data

import org.joda.time.Days
import org.joda.time.LocalDate

open class BaseSearchParams(val adults: Int, val children: List<Int>) {

    val guestString = listOf(adults).plus(children).joinToString(",")

    abstract class Builder(val maxStay: Int) {
        protected  var departure: SuggestionV4? = null
        protected var arrival: SuggestionV4? = null
        protected var checkIn: LocalDate? = null
        protected var checkOut: LocalDate? = null
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

        fun checkIn(checkIn: LocalDate?): Builder {
            this.checkIn = checkIn
            return this
        }

        fun checkOut(checkOut: LocalDate?): Builder {
            this.checkOut = checkOut
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
            return checkIn != null && checkOut != null
        }

        fun hasStart(): Boolean {
            return checkIn != null
        }

        fun hasEnd(): Boolean {
            return checkOut != null
        }

        fun hasDepartureAndArrival(): Boolean {
            return hasDeparture() && hasArrival()
        }

        fun hasDeparture(): Boolean {
            return departure?.hierarchyInfo?.airport?.airportCode != null
        }

        fun hasArrival(): Boolean {
            return arrival?.hierarchyInfo?.airport?.airportCode != null
        }

        open fun hasValidDates(): Boolean {
            return Days.daysBetween(checkIn, checkOut).days <= maxStay
        }
    }

    fun guests() : Int {
        return children.size + adults
    }

    fun getChildrenString() : String {
        return children.joinToString(",")
    }
}

data class HotelTravelerParams(val numberOfAdults: Int, val children: List<Int>)