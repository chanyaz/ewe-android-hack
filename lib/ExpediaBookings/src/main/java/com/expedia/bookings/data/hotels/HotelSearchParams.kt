package com.expedia.bookings.data.hotels

import com.expedia.bookings.data.packages.PackageSearchParams
import org.joda.time.Days
import org.joda.time.LocalDate
import kotlin.collections.emptyList
import kotlin.collections.size

public data class HotelSearchParams(val suggestion: SuggestionV4, val checkIn: LocalDate, val checkOut: LocalDate, val adults: Int, val children: List<Int>) {

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

    class Builder(val maxStay: Int) {
        private var suggestion: SuggestionV4? = null
        private var checkIn: LocalDate? = null
        private var checkOut: LocalDate? = null
        private var adults: Int = 1
        private var children: List<Int> = emptyList()

        fun suggestion(city: SuggestionV4?): Builder {
            this.suggestion = city
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
            val location = suggestion ?: throw IllegalArgumentException()
            if (suggestion?.gaiaId == null && suggestion?.coordinates == null) throw IllegalArgumentException()
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
            return suggestion != null
        }

        public fun hasValidDates(): Boolean {
            return Days.daysBetween(checkIn, checkOut).days <= maxStay
        }

    }

    public fun guests() : Int {
        return children.size + adults
    }

}

public fun convertPackageToSearchParams(packageParams: PackageSearchParams, maxStay: Int): HotelSearchParams {
    val builder = HotelSearchParams.Builder(maxStay).suggestion(packageParams.destination).checkIn(packageParams.checkIn).checkOut(packageParams.checkOut)
    return builder.build()
}
