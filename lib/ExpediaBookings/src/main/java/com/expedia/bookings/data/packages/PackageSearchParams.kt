package com.expedia.bookings.data.packages

import com.expedia.bookings.data.SuggestionV4
import com.expedia.bookings.utils.Constants
import org.joda.time.Days
import org.joda.time.LocalDate
import java.util.HashMap
import kotlin.properties.Delegates

data class PackageSearchParams(val origin: SuggestionV4, val destination: SuggestionV4, val checkIn: LocalDate, val checkOut: LocalDate, val adults: Int, val children: List<Int>, val infantSeatingInLap: Boolean) {

    var pageType: String? = null
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
    var selectedLegId: String? = null
    var currentFlights: Array<String?> by Delegates.notNull()
    var defaultFlights: Array<String?> by Delegates.notNull()
    var numberOfRooms: String = Constants.NUMBER_OF_ROOMS

    class Builder(val maxStay: Int) {
        private var origin: SuggestionV4? = null
        private var destination: SuggestionV4? = null
        private var checkIn: LocalDate? = null
        private var checkOut: LocalDate? = null
        private var adults: Int = 1
        private var children: List<Int> = emptyList()
        private var infantSeatingInLap: Boolean = false


        fun origin(origin: SuggestionV4?): PackageSearchParams.Builder {
            this.origin = origin
            return this
        }

        fun destination(destination: SuggestionV4?): PackageSearchParams.Builder {
            this.destination = destination
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

        fun infantSeatingInLap(infantSeatingInLap: Boolean): PackageSearchParams.Builder {
            this.infantSeatingInLap = infantSeatingInLap
            return this
        }

        fun build(): PackageSearchParams {
            val flightOrigin = origin ?: throw IllegalArgumentException()
            val flightDestination = destination ?: throw IllegalArgumentException()
            val checkInDate = checkIn ?: throw IllegalArgumentException()
            val checkOutDate = checkOut ?: throw IllegalArgumentException()
            return PackageSearchParams(flightOrigin, flightDestination, checkInDate, checkOutDate, adults, children, infantSeatingInLap)
        }

        fun areRequiredParamsFilled(): Boolean {
            return hasOriginAndDestination() && hasStartAndEndDates()
        }

        fun hasStartAndEndDates(): Boolean {
            return checkIn != null && checkOut != null
        }

        fun hasOriginAndDestination(): Boolean {
            return hasOrigin() && hasDestination()
        }

        fun hasOrigin(): Boolean {
            return origin != null
        }

        fun hasDestination(): Boolean {
            return destination != null
        }

        fun hasValidDates(): Boolean {
            return Days.daysBetween(checkIn, checkOut).days <= maxStay
        }
    }

    fun guests() : Int {
        return children.size + adults
    }

    fun isOutboundSearch() : Boolean {
        return packagePIID != null && selectedLegId == null
    }

    fun isChangePackageSearch() : Boolean {
        return pageType == Constants.PACKAGE_CHANGE_HOTEL || pageType == Constants.PACKAGE_CHANGE_FLIGHT
    }

    fun toQueryMap(): Map<String, Any?> {
        val params = HashMap<String, Any?>()
        params.put("pageType", pageType)
        params.put("originId", origin.hierarchyInfo?.airport?.multicity)
        params.put("destinationId", destination.hierarchyInfo?.airport?.multicity)
        params.put("ftla",origin.hierarchyInfo?.airport?.airportCode)
        params.put("ttla", destination.hierarchyInfo?.airport?.airportCode)
        params.put("fromDate", checkIn.toString())
        params.put("toDate", checkOut.toString())
        params.put("numberOfRooms", numberOfRooms)
        params.put("adultsPerRoom[1]", adults)
        params.put("infantSeatingInLap", infantSeatingInLap)
        if (children.size > 0) {
            params.put("childrenPerRoom[1]", children.size)
            makeChildrenAgesParams(params, "childAges[1]", children, 1)
        }
        params.put("searchProduct", searchProduct)
        params.put("packagePIID", packagePIID)
        params.put("selectedLegId", selectedLegId)
        params.put("packageTripType", Constants.PACKAGE_TRIP_TYPE)
        if (isOutboundSearch() || isChangePackageSearch()) {
            params.put("currentFlights", currentFlights.joinToString(","))
        }

        if (isChangePackageSearch()) {
            params.put("defaultFlights", defaultFlights.joinToString(","))
        }

        if (pageType == Constants.PACKAGE_CHANGE_FLIGHT) {
            params.put("action", Constants.PACKAGE_FILTER_CHANGE_FLIGHT)
        }

        return params
    }

    private fun makeChildrenAgesParams(params: HashMap<String, Any?>, keyString: String, valueList: List<Any>, startIndex: Int) {
        for (i in startIndex..valueList.size) {
            var key = StringBuilder(keyString)
            key.append("[").append(i).append("]")
            params.put(key.toString(), valueList[i-1])
        }
    }
}