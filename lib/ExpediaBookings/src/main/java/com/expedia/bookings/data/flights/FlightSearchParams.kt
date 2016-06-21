package com.expedia.bookings.data.flights

import com.expedia.bookings.data.AbstractFlightSearchParams
import com.expedia.bookings.data.BaseSearchParams
import com.expedia.bookings.data.SuggestionV4
import org.joda.time.Days
import org.joda.time.LocalDate
import java.util.HashMap

class FlightSearchParams(val departureAirport: SuggestionV4, val arrivalAirport: SuggestionV4?, val departureDate: LocalDate, val returnDate: LocalDate?, adults: Int, children: List<Int>, infantSeatingInLap: Boolean) : AbstractFlightSearchParams(adults, children, departureDate, returnDate, infantSeatingInLap) {

    class Builder(maxStay: Int, maxRange: Int) : AbstractFlightSearchParams.Builder(maxStay, maxRange) {

        override fun build(): FlightSearchParams {
            val departureAirport = originLocation ?: throw IllegalArgumentException()
            val departureDate = startDate ?: throw IllegalArgumentException()
            return FlightSearchParams(departureAirport, destinationLocation, departureDate, endDate, adults, children, infantSeatingInLap)
        }

        override fun areRequiredParamsFilled(): Boolean {
            return hasOriginLocation() && hasStart()
        }

        override fun hasValidDateDuration(): Boolean {
            return (hasStart() && !hasEnd()) || ((hasStart() && hasEnd() && Days.daysBetween(startDate, endDate).days <= maxStay))
        }

        override fun isOriginSameAsDestination(): Boolean {
            val departureAirportCode = originLocation?.hierarchyInfo?.airport?.airportCode ?: ""
            val arrivalAirportCode = destinationLocation?.hierarchyInfo?.airport?.airportCode ?: ""

            return departureAirportCode.equals(arrivalAirportCode)
        }
    }

    fun toQueryMap(): Map<String, Any?> {
        val params = HashMap<String, Any?>()
        params.put("departureAirport", departureAirport.hierarchyInfo?.airport?.airportCode)
        params.put("arrivalAirport", arrivalAirport?.hierarchyInfo?.airport?.airportCode)
        params.put("departureDate", departureDate.toString())
        if (returnDate != null) {
            params.put("returnDate", returnDate.toString())
        }
        params.put("numberOfAdultTravelers", adults)
        params.put("infantSeatingInLap", infantSeatingInLap)

        return params
    }
}