package com.expedia.bookings.data.flights

import com.expedia.bookings.data.SuggestionV4
import org.joda.time.Days
import org.joda.time.LocalDate
import java.util.HashMap

data class FlightSearchParams(val departureAirport: SuggestionV4, val arrivalAirport: SuggestionV4?, val departureDate: LocalDate, val returnDate: LocalDate?, val adults: Int, val children: List<Int>, val infantSeatingInLap: Boolean) {

    //TODO: Test this
    class Builder(val maxStay: Int) {
        private var departureAirport: SuggestionV4? = null
        private var arrivalAirport: SuggestionV4? = null
        private var departureDate: LocalDate? = null
        private var returnDate: LocalDate? = null
        private var adults = 1
        private var children: List<Int> = emptyList()
        private var infantSeatingInLap: Boolean = false

        fun departureAirport(departureAirport: SuggestionV4?): FlightSearchParams.Builder {
            this.departureAirport = departureAirport
            return this
        }

        fun arrivalAirport(arrivalAirport: SuggestionV4?): FlightSearchParams.Builder {
            this.arrivalAirport = arrivalAirport
            return this
        }

        fun departureDate(departureDate: LocalDate?): FlightSearchParams.Builder {
            this.departureDate = departureDate
            return this
        }

        fun returnDate(returnDate: LocalDate?): FlightSearchParams.Builder {
            this.returnDate = returnDate
            return this
        }

        fun adults(adults: Int): FlightSearchParams.Builder {
            this.adults = adults
            return this
        }

        fun children(children: List<Int>): FlightSearchParams.Builder {
            this.children = children
            return this
        }

        fun infantSeatingInLap(infantSeatingInLap: Boolean): FlightSearchParams.Builder {
            this.infantSeatingInLap = infantSeatingInLap
            return this
        }

        fun build(): FlightSearchParams {
            val departureAirport = departureAirport ?: throw IllegalArgumentException()
            val departureDate = departureDate ?: throw IllegalArgumentException()
            return FlightSearchParams(departureAirport, arrivalAirport, departureDate, returnDate, adults, children, infantSeatingInLap)
        }

        fun areRequiredParamsFilled(): Boolean {
            return hasDeparture() && hasStart()
        }

        fun hasStart(): Boolean {
            return departureDate != null
        }

        fun hasDeparture(): Boolean {
            return departureAirport?.hierarchyInfo?.airport?.airportCode != null
        }

        fun hasReturn(): Boolean {
            return arrivalAirport?.hierarchyInfo?.airport?.airportCode != null
        }

        fun hasValidDates(): Boolean {
            return Days.daysBetween(departureDate, returnDate).days <= maxStay
        }
    }

    fun guests() : Int {
        return children.size + adults
    }

    fun toQueryMap(): Map<String, Any?> {
        val params = HashMap<String, Any?>()
        params.put("departureAirport", departureAirport.hierarchyInfo?.airport?.airportCode)
        params.put("arrivalAirport", arrivalAirport?.hierarchyInfo?.airport?.airportCode)
        params.put("departureDate", departureDate.toString())
        params.put("returnDate", returnDate?.toString())
        params.put("numberOfAdultTravelers", adults)
        params.put("infantSeatingInLap", infantSeatingInLap)
        if (children.isNotEmpty()) {
            params.put("childTravelerAge", children.joinToString(","))
        }

        return params
    }
}