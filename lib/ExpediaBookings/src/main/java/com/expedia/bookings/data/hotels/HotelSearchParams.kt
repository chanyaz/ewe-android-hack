package com.expedia.bookings.data.hotels

import com.expedia.bookings.data.cars.Suggestion
import org.joda.time.LocalDate

public class HotelSearchParams(val city: SuggestionV4, val checkIn: LocalDate, val checkOut: LocalDate, val adults: Int, val children: List<Int>) {

    public fun getGuestString() : String {
        val sb = StringBuilder {
            append(adults)
            for (i in 0..children.size() - 1) {
                append(",")
                append(children.get(i))
            }
        }
        return sb.toString()
    }

    class Builder {
        private var city: SuggestionV4? = null
        private var checkIn: LocalDate? = null
        private var checkOut: LocalDate? = null
        private var adults: Int = 1
        private var children: List<Int> = emptyList()

        fun city(city: SuggestionV4): Builder {
            this.city = city
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

        fun build(): HotelSearchParams {
            val location = city ?: throw IllegalArgumentException()
            val checkInDate = checkIn ?: throw IllegalArgumentException()
            val checkOutDate = checkOut ?: throw IllegalArgumentException()
            return HotelSearchParams(location, checkInDate, checkOutDate, adults, children)
        }

        public fun areRequiredParamsFilled(): Boolean {
            return hasOrigin() && hasStartAndEndDates()
        }

        public fun hasStartAndEndDates(): Boolean {
            return checkIn != null && checkOut != null
        }

        public fun hasOrigin(): Boolean {
            return city != null
        }
    }
}
