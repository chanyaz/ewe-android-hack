package com.expedia.bookings.data.flights

import com.expedia.bookings.data.SuggestionV4
import org.joda.time.LocalDate

class FlightMultiDestinationSearchParam(
        val arrivalAirport: SuggestionV4,
        val departureAirport: SuggestionV4,
        val departureDate: LocalDate
) {
    class Builder {
        private var arrivalAirport: SuggestionV4? = null
        private var departureAirport: SuggestionV4? = null
        private var departureDate: LocalDate? = null

        fun arrivalAirport(city: SuggestionV4?): Builder {
            this.arrivalAirport = city
            return this
        }

        fun departureAirport(city: SuggestionV4?): Builder {
            this.departureAirport = city
            return this
        }

        fun departureDate(departureDate: LocalDate): Builder {
            this.departureDate = departureDate
            return this
        }

        fun hasArrivalAirport(): Boolean {
            return arrivalAirport != null
        }

        fun hasDepartureAirport(): Boolean {
            return departureAirport != null
        }

        fun hasDepartureDate(): Boolean {
            return departureDate != null
        }

        fun build(): FlightMultiDestinationSearchParam {
            val arrivalAirport = this.arrivalAirport ?: throw IllegalArgumentException()
            val departureAirport = this.departureAirport ?: throw IllegalArgumentException()
            val departureDate = this.departureDate ?: throw IllegalArgumentException()
            return FlightMultiDestinationSearchParam(arrivalAirport, departureAirport, departureDate)
        }
    }

    fun multiDestinationSearchParamMap(): Map<String, Any?> {
        val paramsMap = HashMap<String, Any?>()
        paramsMap.put("arrivalAirport", arrivalAirport.hierarchyInfo?.airport?.airportCode)
        paramsMap.put("departureAirport", departureAirport.hierarchyInfo?.airport?.airportCode)
        paramsMap.put("departureDate", departureDate.toString())
        return paramsMap
    }
}
