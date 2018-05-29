package com.expedia.bookings.data.flights

import com.expedia.bookings.data.AbstractFlightSearchParams
import com.expedia.bookings.data.SuggestionV4
import com.expedia.bookings.utils.Constants
import com.expedia.bookings.utils.Strings
import org.joda.time.Days
import org.joda.time.LocalDate

class FlightSearchParams(val departureAirport: SuggestionV4, val arrivalAirport: SuggestionV4, val departureDate: LocalDate, val returnDate: LocalDate?, adults: Int,
                         children: List<Int>, infantSeatingInLap: Boolean, val flightCabinClass: String?, val legNo: Int?, val selectedOutboundLegId: String?,
                         val showRefundableFlight: Boolean?, val nonStopFlight: Boolean?, val featureOverride: String?, val maxOfferCount: Int?,
                         val trips: List<FlightMultiDestinationSearchParam>?, val searchType: String?) :
        AbstractFlightSearchParams(departureAirport, arrivalAirport, adults, children, departureDate, returnDate, infantSeatingInLap) {

    enum class SearchType {
        ONE_WAY,
        RETURN,
        MULTI_DEST
    }

    class Builder(maxStay: Int, maxRange: Int) : AbstractFlightSearchParams.Builder(maxStay, maxRange) {
        private var isRoundTrip = true
        private var flightCabinClass: String? = null
        private var legNo: Int? = null
        private var selectedOutboundLegId: String? = null
        //TODO default these values to false when showAdavanceSearch ABTest passes
        private var showRefundableFlight: Boolean? = null
        private var showNonStopFlight: Boolean? = null
        private var featureOverride: String? = null

        private var maxOfferCount: Int = Constants.DEFAULT_MAX_OFFER_COUNT
        private var trips: List<FlightMultiDestinationSearchParam> = emptyList()
        private val flightMultiDestSearchParamsBuilder = FlightMultiDestinationSearchParam.Builder()
        private var searchType: SearchType = SearchType.RETURN

        override fun build(): FlightSearchParams {
            val departureAirport = originLocation ?: throw IllegalArgumentException()
            val arrivalAirport = destinationLocation ?: throw IllegalArgumentException()
            val departureDate = startDate ?: throw IllegalArgumentException()
            var searchLegNo: Int? = null
            //As Byot is eligible only for round trips
            if (endDate != null && legNo != null) {
                if ((legNo == 0 && Strings.isEmpty(selectedOutboundLegId)) || (legNo == 1 && Strings.isNotEmpty(selectedOutboundLegId))) {
                    searchLegNo = legNo
                    maxOfferCount = Constants.BYOT_MAX_OFFER_COUNT
                } else {
                    throw IllegalArgumentException("In BYOT if you are searching for outbound then legNo should be 0 and selectedOutboundLegId should be empty " +
                            "or if for inbound then legNo should be 1 and selectedOutboundLegId should be non-empty ")
                }
            }

            trips = createTripList(departureAirport, arrivalAirport, departureDate)
            searchType = getSearchType()

            return FlightSearchParams(departureAirport, arrivalAirport, departureDate, endDate, adults, children, infantSeatingInLap, flightCabinClass,
                    searchLegNo, selectedOutboundLegId, showRefundableFlight, showNonStopFlight, featureOverride, maxOfferCount, trips, searchType.name)
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

        fun isCabinClassChanged(cabinClass: String?): Boolean {
            return !this.flightCabinClass.equals(cabinClass)
        }

        fun hasOriginChanged(originLocationNew: SuggestionV4?): Boolean {
            return !(hasOriginLocation() && this.originLocation?.hierarchyInfo?.airport?.airportCode
                    .equals(originLocationNew?.hierarchyInfo?.airport?.airportCode))
        }

        fun hasDestinationChanged(destinationLocationNew: SuggestionV4?): Boolean {
            return !(hasDestinationLocation() && this.destinationLocation?.hierarchyInfo?.airport?.airportCode
                    .equals(destinationLocationNew?.hierarchyInfo?.airport?.airportCode))
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

        fun createTripList(departureAirport: SuggestionV4?, arrivalAirport: SuggestionV4?, departureDate: LocalDate): List<FlightMultiDestinationSearchParam> {
            val trips = ArrayList<FlightMultiDestinationSearchParam>()
            val flightMultiDestSearchParams = flightMultiDestSearchParamsBuilder
                    .departureAirport(departureAirport)
                    .arrivalAirport(arrivalAirport)
                    .departureDate(departureDate)
                    .build()

            trips.add(flightMultiDestSearchParams)
            return trips
        }

        fun getSearchType(): SearchType {
            if (trips.size > 1) {
                this.searchType = SearchType.MULTI_DEST
            } else if (trips.size == 1 && endDate != null) {
                this.searchType = SearchType.RETURN
            } else if (trips.size == 1 && endDate == null) {
                this.searchType = SearchType.ONE_WAY
            }
            return this.searchType
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

    fun toQueryMapForKong(): Map<String, Any?> {
        val params = HashMap<String, Any?>()

        if (returnDate != null) {
            params.put("returnDate", returnDate.toString())
        }
        params.put("cabinClassPreference", flightCabinClass)
        params.put("showRefundableFlight", showRefundableFlight)
        params.put("nonStopFlight", nonStopFlight)
        params.put("featureOverride", featureOverride)
        params.put("ul", legNo)
        params.put("fl0", selectedOutboundLegId)
        if (children.isNotEmpty()) {
            params.put("childTravelerAge", children)
        }
        params.put("numberOfAdultTravelers", adults)
        params.put("infantSeatingInLap", infantSeatingInLap)
        params.put("lccAndMerchantFareCheckoutAllowed", true)
        params.put("maxOfferCount", maxOfferCount)
        params.put("trips", createTripsMap())
        params.put("tripType", searchType)

        return params
    }

    private fun createTripsMap(): List<Map<String, Any?>> {
        val tripsMapList = ArrayList<Map<String, Any?>>()
        if (trips != null && !trips.isEmpty()) {
            for (trip in trips) {
                tripsMapList.add(trip.multiDestinationSearchParamMap())
            }
        }
        return tripsMapList
    }
}
