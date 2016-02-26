package com.expedia.bookings.data.hotels

import com.expedia.bookings.data.SuggestionV4
import com.expedia.bookings.data.packages.PackageSearchParams
import org.joda.time.Days
import org.joda.time.LocalDate

public data class HotelSearchParams(val suggestion: SuggestionV4, val checkIn: LocalDate, val checkOut: LocalDate, val adults: Int, val children: List<Int>) {
    var forPackage = false

    public val guestString = listOf(adults).plus(children).joinToString(",")

    class Builder(val maxStay: Int) {
        private var suggestion: SuggestionV4? = null
        private var checkIn: LocalDate? = null
        private var checkOut: LocalDate? = null
        private var adults: Int = 1
        private var children: List<Int> = emptyList()
        private var isPackage: Boolean = false

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

        fun forPackage(pkg: Boolean): Builder {
            this.isPackage = pkg
            return this
        }

        fun build(): HotelSearchParams {
            val location = suggestion ?: throw IllegalArgumentException()
            if (suggestion?.gaiaId == null && suggestion?.coordinates == null) throw IllegalArgumentException()
            val checkInDate = checkIn ?: throw IllegalArgumentException()
            val checkOutDate = checkOut ?: throw IllegalArgumentException()
            var params = HotelSearchParams(location, checkInDate, checkOutDate, adults, children)
            params.forPackage = isPackage
            return params
        }

        fun areRequiredParamsFilled(): Boolean {
            return hasOrigin() && hasStartAndEndDates()
        }

        fun hasStartAndEndDates(): Boolean {
            return checkIn != null && checkOut != null
        }

        fun hasOrigin(): Boolean {
            return suggestion != null
        }

        fun hasValidDates(): Boolean {
            return Days.daysBetween(checkIn, checkOut).days <= maxStay
        }
    }

    fun guests() : Int {
        return children.size + adults
    }
}

fun convertPackageToSearchParams(packageParams: PackageSearchParams, maxStay: Int): HotelSearchParams {
    val builder = HotelSearchParams.Builder(maxStay).suggestion(packageParams.destination)
            .checkIn(packageParams.checkIn).checkOut(packageParams.checkOut).adults(packageParams.adults)
            .children(packageParams.children).forPackage(true)
    return builder.build()
}
