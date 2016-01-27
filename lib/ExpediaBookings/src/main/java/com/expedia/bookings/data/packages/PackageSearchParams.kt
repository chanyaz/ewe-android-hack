package com.expedia.bookings.data.packages

import com.expedia.bookings.data.SuggestionV4
import com.expedia.bookings.utils.Constants
import org.joda.time.Days
import org.joda.time.LocalDate
import kotlin.collections.emptyList
import kotlin.text.isNullOrEmpty

public data class PackageSearchParams(val destination: SuggestionV4, val arrival: SuggestionV4, val checkIn: LocalDate, val checkOut: LocalDate, val adults: Int, val children: List<Int>) {

    var searchProduct: String? = null
    var packagePIID: String? = null
        set(value) {
            field = value
            if (!value.isNullOrEmpty()) {
                searchProduct = Constants.PRODUCT_FLIGHT
            } else {
                searchProduct = null
            }
        }
    var flightType: String? = null  //null for outbound flights & 1 for inbound flights
    var selectedLegId: String? = null

    class Builder(val maxStay: Int) {
        private var destination: SuggestionV4? = null
        private var arrival: SuggestionV4? = null
        private var checkIn: LocalDate? = null
        private var checkOut: LocalDate? = null
        private var adults: Int = 1
        private var children: List<Int> = emptyList()

        fun destination(destination: SuggestionV4?): PackageSearchParams.Builder {
            this.destination = destination
            return this
        }

        fun arrival(arrival: SuggestionV4?): PackageSearchParams.Builder {
            this.arrival = arrival
            return this
        }

        fun checkIn(checkIn: LocalDate?): PackageSearchParams.Builder {
            this.checkIn = checkIn
            return this
        }

        fun checkOut(checkOut: LocalDate?): PackageSearchParams.Builder {
            this.checkOut = checkOut
            return this
        }

        fun adults(adults: Int): PackageSearchParams.Builder {
            this.adults = adults
            return this
        }

        fun children(children: List<Int>): PackageSearchParams.Builder {
            this.children = children
            return this
        }

        fun build(): PackageSearchParams {
            val flightDestination = destination ?: throw IllegalArgumentException()
            val flightArrival = arrival ?: throw IllegalArgumentException()
            val checkInDate = checkIn ?: throw IllegalArgumentException()
            val checkOutDate = checkOut ?: throw IllegalArgumentException()
            return PackageSearchParams(flightDestination, flightArrival, checkInDate, checkOutDate, adults, children)
        }

        public fun areRequiredParamsFilled(): Boolean {
            return hasOrigin() && hasStartAndEndDates()
        }

        public fun hasStartAndEndDates(): Boolean {
            return checkIn != null && checkOut != null
        }

        public fun hasOrigin(): Boolean {
            return hasDestination() && hasArrival()
        }

        public fun hasDestination(): Boolean {
            return destination != null
        }

        public fun hasArrival(): Boolean {
            return arrival != null
        }

        public fun hasValidDates(): Boolean {
            return Days.daysBetween(checkIn, checkOut).days <= maxStay
        }
    }

    public fun guests() : Int {
        return children.size + adults
    }
}