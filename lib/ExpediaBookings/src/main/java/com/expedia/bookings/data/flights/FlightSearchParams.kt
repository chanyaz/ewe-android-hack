package com.expedia.bookings.data.flights

import com.expedia.bookings.data.AbstractFlightSearchParams
import com.expedia.bookings.data.SuggestionV4
import com.expedia.bookings.utils.Constants
import com.expedia.bookings.utils.Strings
import org.joda.time.Days
import org.joda.time.LocalDate
import java.util.HashMap

class FlightSearchParams(
    val departureAirport: SuggestionV4,
    val arrivalAirport: SuggestionV4,
    val departureDate: LocalDate,
    val returnDate: LocalDate?,
    adults: Int,
    children: List<Int>,
    infantSeatingInLap: Boolean,
    val flightCabinClass: String?,
    val legNo: Int?,
    val selectedOutboundLegId: String?,
    val showRefundableFlight: Boolean?,
    val nonStopFlight: Boolean?,
    val featureOverride: String?
) :
        AbstractFlightSearchParams(departureAirport, arrivalAirport, adults, children, departureDate, returnDate, infantSeatingInLap) {

    class Builder(maxStay: Int, maxRange: Int) : AbstractFlightSearchParams.Builder(maxStay, maxRange) {
        private var isRoundTrip = true
        private var flightCabinClass: String? = null
        private var legNo: Int? = null
        private var selectedOutboundLegId: String? = null
        //TODO default these values to false when showAdavanceSearch ABTest passes
        private var showRefundableFlight: Boolean? = null
        private var showNonStopFlight: Boolean? = null
        private var featureOverride: String? = null

        override fun build(): FlightSearchParams {
            val departureAirport = originLocation ?: throw IllegalArgumentException()
            val arrivalAirport = destinationLocation ?: throw IllegalArgumentException()
            val departureDate = startDate ?: throw IllegalArgumentException()
            var searchLegNo: Int? = null
            //As Byot is eligible only for round trips
            if (endDate != null && legNo != null) {
                if ((legNo == 0 && Strings.isEmpty(selectedOutboundLegId)) || (legNo == 1 && Strings.isNotEmpty(selectedOutboundLegId))) {
                    searchLegNo = legNo
                } else {
                    throw IllegalArgumentException("In BYOT if you are searching for outbound then legNo should be 0 and selectedOutboundLegId should be empty " +
                            "or if for inbound then legNo should be 1 and selectedOutboundLegId should be non-empty ")
                }
            }
            return FlightSearchParams(departureAirport, arrivalAirport, departureDate, endDate, adults, children, infantSeatingInLap, flightCabinClass,
                    searchLegNo, selectedOutboundLegId, showRefundableFlight, showNonStopFlight, featureOverride)
        }

        override fun areRequiredParamsFilled(): Boolean {
            return hasOriginLocation() && hasDestinationLocation() && hasValidDates()
        }

        override fun hasValidDateDuration(): Boolean {
            return (hasStart() && !hasEnd()) || ((hasStart() && hasEnd() && Days.daysBetween(startDate, endDate).days <= maxStay))
        }

        override fun isOriginSameAsDestination(): Boolean {
            val departureAirportCode = originLocation?.hierarchyInfo?.airport?.airportCode ?: ""
            val arrivalAirportCode = destinationLocation?.hierarchyInfo?.airport?.airportCode ?: ""

            return departureAirportCode.equals(arrivalAirportCode)
        }

        fun hasValidDates(): Boolean {
            return if (isRoundTrip) hasStartAndEndDates() else hasStart()
        }

        fun roundTrip(isRoundTrip: Boolean): Builder {
            this.isRoundTrip = isRoundTrip
            return this
        }

        fun flightCabinClass(cabinClass: String?): Builder {
            this.flightCabinClass = cabinClass
            return this
        }

        fun legNo(legNo: Int?): Builder {
            this.legNo = legNo
            return this
        }

        fun selectedLegID(legId: String?): Builder {
            this.selectedOutboundLegId = legId
            return this
        }

        fun nonStopFlight(isApplied: Boolean?): Builder {
            this.showNonStopFlight = isApplied
            return this
        }

        fun setFeatureOverride(newFeatureOverride: String?): Builder {
            if (featureOverride.isNullOrBlank()) {
                featureOverride = newFeatureOverride
            } else {
                val builder = StringBuilder(featureOverride)
                builder.append(",")
                builder.append(newFeatureOverride)
                featureOverride = builder.toString()
            }
            return this
        }

        fun showRefundableFlight(isApplied: Boolean?): Builder {
            this.showRefundableFlight = isApplied
            return this
        }
    }

    fun buildParamsForInboundSearch(maxStay: Int, maxRange: Int, selectedOutboundLegId: String?): FlightSearchParams {
        return Builder(maxStay, maxRange).roundTrip(true).legNo(1).selectedLegID(selectedOutboundLegId).flightCabinClass(flightCabinClass).setFeatureOverride(featureOverride)
                .showRefundableFlight(showRefundableFlight).nonStopFlight(nonStopFlight)
                .infantSeatingInLap(infantSeatingInLap).origin(departureAirport)
                .destination(arrivalAirport).startDate(departureDate).endDate(returnDate)
                .adults(adults).children(children)
                .build() as FlightSearchParams
    }

    fun buildParamsForCachedSearch(maxStay: Int, maxRange: Int): FlightSearchParams {
        var overrides = if (featureOverride.isNullOrEmpty()) Constants.FEATURE_FLIGHT_CACHE else featureOverride + "," + Constants.FEATURE_FLIGHT_CACHE
        return Builder(maxStay, maxRange).roundTrip(isRoundTrip())
                .flightCabinClass(flightCabinClass)
                .setFeatureOverride(overrides)
                .origin(departureAirport)
                .destination(arrivalAirport).startDate(departureDate).endDate(returnDate)
                .adults(adults)
                .build() as FlightSearchParams
    }

    fun toQueryMap(): Map<String, Any?> {
        val params = HashMap<String, Any?>()
        params.put("departureAirport", departureAirport.hierarchyInfo?.airport?.airportCode)
        params.put("arrivalAirport", arrivalAirport.hierarchyInfo?.airport?.airportCode)
        params.put("departureDate", departureDate.toString())
        if (returnDate != null) {
            params.put("returnDate", returnDate.toString())
        }
        params.put("numberOfAdultTravelers", adults)
        params.put("infantSeatingInLap", infantSeatingInLap)

        return params
    }

    fun isRoundTrip(): Boolean {
        return returnDate != null
    }

    fun hasAdvanceSearchOption(): Boolean {
        return showRefundableFlight ?: false || nonStopFlight ?: false
    }
}
