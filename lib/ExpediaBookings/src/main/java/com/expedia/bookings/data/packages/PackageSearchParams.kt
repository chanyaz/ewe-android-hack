package com.expedia.bookings.data.packages

import com.expedia.bookings.data.BaseSearchParams
import com.expedia.bookings.data.SuggestionV4
import com.expedia.bookings.utils.Constants
import org.joda.time.Days
import org.joda.time.LocalDate
import java.util.HashMap
import kotlin.properties.Delegates

open class PackageSearchParams(val origin: SuggestionV4, val destination: SuggestionV4, val checkIn: LocalDate, val checkOut: LocalDate, adults: Int, children: List<Int>, val infantSeatingInLap: Boolean) : BaseSearchParams(adults, children) {

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

    class Builder(maxStay: Int) : BaseSearchParams.Builder(maxStay) {

        override fun build(): PackageSearchParams {
            val flightOrigin = originLocation ?: throw IllegalArgumentException()
            val flightDestination = destinationLocation ?: throw IllegalArgumentException()
            val checkInDate = startDate ?: throw IllegalArgumentException()
            val checkOutDate = endDate ?: throw IllegalArgumentException()
            return PackageSearchParams(flightOrigin, flightDestination, checkInDate, checkOutDate, adults, children, infantSeatingInLap)
        }

        override fun areRequiredParamsFilled(): Boolean {
            return hasOriginAndDestination() && hasStartAndEndDates()
        }

        override fun hasValidDates(): Boolean {
            return Days.daysBetween(startDate, endDate).days <= maxStay
        }

        override fun isOriginSameAsDestination(): Boolean {
            val departureCity = originLocation?.hierarchyInfo?.airport?.multicity ?: ""
            val arrivalCity = destinationLocation?.hierarchyInfo?.airport?.multicity ?: ""

            return departureCity.equals(arrivalCity)
        }
    }

    fun isOutboundSearch(): Boolean {
        return packagePIID != null && selectedLegId == null
    }

    fun isChangePackageSearch(): Boolean {
        return pageType == Constants.PACKAGE_CHANGE_HOTEL || pageType == Constants.PACKAGE_CHANGE_FLIGHT
    }

    fun toQueryMap(): Map<String, Any?> {
        val params = HashMap<String, Any?>()
        params.put("pageType", pageType)
        params.put("originId", origin.hierarchyInfo?.airport?.multicity)
        params.put("destinationId", destination.hierarchyInfo?.airport?.multicity)
        params.put("ftla", origin.hierarchyInfo?.airport?.airportCode)
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
            params.put(key.toString(), valueList[i - 1])
        }
    }

    fun getNumberOfSeatedChildren(): Int {
        var numberOfUnseatedTravelers = 0
        for (child in children) {
            if (child < 2 && infantSeatingInLap) {
                numberOfUnseatedTravelers++
            }
        }
        return children.size - numberOfUnseatedTravelers
    }
}