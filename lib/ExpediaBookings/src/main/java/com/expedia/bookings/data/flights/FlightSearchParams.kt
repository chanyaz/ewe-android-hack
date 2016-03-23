package com.expedia.bookings.data.flights

import com.expedia.bookings.data.BaseSearchParams
import com.expedia.bookings.data.SuggestionV4
import org.joda.time.Days
import org.joda.time.LocalDate
import java.util.HashMap

class FlightSearchParams(val departureAirport: SuggestionV4, val arrivalAirport: SuggestionV4?, val departureDate: LocalDate, val returnDate: LocalDate?, adults: Int, children: List<Int>, val infantSeatingInLap: Boolean) : BaseSearchParams(adults, children) {

    class Builder(maxStay: Int) : BaseSearchParams.Builder(maxStay) {

        override fun build(): FlightSearchParams {
            val departureAirport = departure ?: throw IllegalArgumentException()
            val departureDate = checkIn ?: throw IllegalArgumentException()
            return FlightSearchParams(departureAirport, arrival, departureDate, checkOut, adults, children, infantSeatingInLap)
        }

        override fun areRequiredParamsFilled(): Boolean {
            return hasDeparture() && hasStart()
        }

        override fun hasValidDates(): Boolean {
            return (hasStart() && !hasEnd()) || ((hasStart() && hasEnd() && Days.daysBetween(checkIn, checkOut).days <= maxStay))
        }
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
            params.put("childTravelerAge", getChildrenString())
        }

        return params
    }
}