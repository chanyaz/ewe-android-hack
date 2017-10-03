package com.expedia.bookings.data.packages

import com.expedia.bookings.data.AbstractFlightSearchParams
import com.expedia.bookings.data.Money
import com.expedia.bookings.data.SuggestionV4
import com.expedia.bookings.data.flights.FlightLeg
import com.expedia.bookings.utils.Constants
import org.joda.time.Days
import org.joda.time.LocalDate
import java.util.HashMap
import kotlin.properties.Delegates

open class PackageSearchParams(origin: SuggestionV4?, destination: SuggestionV4?, startDate: LocalDate, endDate: LocalDate?, adults: Int, children: List<Int>, infantSeatingInLap: Boolean) : AbstractFlightSearchParams(origin, destination, adults, children, startDate, endDate, infantSeatingInLap) {

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
    var flightLegList: List<FlightLeg>? = null

    //MID variables
    var hotelId: String? = null
    var ratePlanCode: String? = null
    var roomTypeCode: String? = null
    var latestSelectedProductTotalPrice: Money? = null

    val originId: String?
        get() {
            return  origin?.hierarchyInfo?.airport?.multicity ?: origin?.gaiaId ?: origin?.hierarchyInfo?.airport?.regionId
        }

    val destinationId: String?
        get() {
            //Send gaiaId as the region id for destination to get the correct hotels
            //Destination on pkgs can be a non-airport too For e.g. Zion national park,UT
            //and Send airport region id for all POI suggestions types
            if (destination?.type == "POI" ) {
                return destination.hierarchyInfo?.airport?.multicity
            } else {
                return destination?.gaiaId ?: destination?.hierarchyInfo?.airport?.regionId
            }
        }

    val childAges: String?
        get() {
            if (children.isEmpty()) {
                return null
            }
            return children.joinToString(separator = ",")
        }

    val infantsInSeats: Boolean?
        get() {
            if (!children.any { it < 2 }) {
                return null
            }
            return !infantSeatingInLap
        }

    class Builder(maxStay: Int, maxRange: Int) : AbstractFlightSearchParams.Builder(maxStay, maxRange) {

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

        override fun hasValidDateDuration(): Boolean {
            return Days.daysBetween(startDate, endDate).days <= maxStay
        }

        override fun isOriginSameAsDestination(): Boolean {
            var departureCity = "";
            var arrivalCity = "";
            if (originLocation?.hierarchyInfo?.airport?.multicity != null) {
                departureCity = originLocation?.hierarchyInfo?.airport?.multicity ?: ""
                arrivalCity = destinationLocation?.hierarchyInfo?.airport?.multicity ?: ""
            }
            else {
                departureCity = originLocation?.hierarchyInfo?.airport?.regionId ?: ""
                arrivalCity = destinationLocation?.hierarchyInfo?.airport?.regionId ?: ""
            }
            return departureCity.equals(arrivalCity)
        }
    }

    fun isOutboundSearch(isMidApiEnabled: Boolean): Boolean {
        return (isMidApiEnabled || packagePIID != null) && selectedLegId == null
    }

    fun isChangePackageSearch(): Boolean {
        return pageType == Constants.PACKAGE_CHANGE_HOTEL || pageType == Constants.PACKAGE_CHANGE_FLIGHT
    }

    fun toQueryMap(): Map<String, Any?> {
        val params = HashMap<String, Any?>()
        if (pageType != null) params.put("pageType", pageType)
        // TODO Xselling packages: In Flights module we have gaiaId so we have to set originId with gaiaId
        params.put("originId", originId)
        params.put("destinationId", destinationId)
        params.put("ftla", origin?.hierarchyInfo?.airport?.airportCode)
        params.put("ttla", destination?.hierarchyInfo?.airport?.airportCode)
        params.put("fromDate", startDate.toString())
        params.put("toDate", endDate.toString())
        params.put("numberOfRooms", numberOfRooms)
        params.put("adultsPerRoom[1]", adults)
        if (children.size > 0) {
            params.put("childrenPerRoom[1]", children.size)
            makeChildrenAgesParams(params, "childAges[1]", children, 1)
        }
        if (searchProduct != null) params.put("searchProduct", searchProduct)
        if (packagePIID != null) params.put("packagePIID", packagePIID)
        if (selectedLegId != null) params.put("selectedLegId", selectedLegId)
        params.put("packageTripType", Constants.PACKAGE_TRIP_TYPE)
        if (isOutboundSearch(false) || isChangePackageSearch()) {
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

    private fun makeChildrenAgesParams(params: HashMap<String, Any?>, keyString: String, valueList: List<Int>, startIndex: Int) {
        for (i in startIndex..valueList.size) {
            val key = StringBuilder(keyString)
            key.append("[").append(i).append("]")
            val childAge = valueList[i-1]
            params.put(key.toString(), childAge)
            if (childAge < 2) {
                params.put("infantsInSeats", if (infantSeatingInLap) 0 else 1)
            }
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
