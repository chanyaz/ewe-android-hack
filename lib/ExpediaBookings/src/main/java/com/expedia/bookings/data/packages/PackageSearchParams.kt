package com.expedia.bookings.data.packages

import com.expedia.bookings.data.AbstractFlightSearchParams
import com.expedia.bookings.data.BaseHotelFilterOptions
import com.expedia.bookings.data.SuggestionV4
import com.expedia.bookings.data.flights.FlightLeg
import com.expedia.bookings.data.hotels.HotelSearchParams
import com.expedia.bookings.utils.Constants
import org.joda.time.Days
import org.joda.time.LocalDate
import java.util.HashMap
import kotlin.properties.Delegates

open class PackageSearchParams(origin: SuggestionV4?, destination: SuggestionV4?, startDate: LocalDate, endDate: LocalDate?, adults: Int, children: List<Int>, infantSeatingInLap: Boolean, val flightCabinClass: String? = null) : AbstractFlightSearchParams(origin, destination, adults, children, startDate, endDate, infantSeatingInLap) {

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
    var filterOptions: PackageHotelFilterOptions? = null

    //MID variables
    var latestSelectedOfferInfo: PackageSelectedOfferInfo = PackageSelectedOfferInfo()

    val originId: String?
        get() {
            return origin?.gaiaId ?: origin?.hierarchyInfo?.airport?.regionId
        }

    val destinationId: String?
        get() {
            return destination?.gaiaId ?: destination?.hierarchyInfo?.airport?.regionId
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

    fun getHotelsSortOrder(): BaseHotelFilterOptions.SortType {
        if (filterOptions?.userSort != null) {
            return filterOptions!!.userSort!!
        }

        return BaseHotelFilterOptions.SortType.EXPERT_PICKS
    }

    class Builder(maxStay: Int, maxRange: Int) : AbstractFlightSearchParams.Builder(maxStay, maxRange) {

        private var flightCabinClass: String? = null
        private var hotelName: String? = null
        private var starRatings: List<Int> = emptyList()
        private var vipOnly: Boolean = false
        private var userSort: BaseHotelFilterOptions.SortType? = null

        override fun build(): PackageSearchParams {
            val flightOrigin = originLocation ?: throw IllegalArgumentException()
            val flightDestination = destinationLocation ?: throw IllegalArgumentException()
            val checkInDate = startDate ?: throw IllegalArgumentException()
            val checkOutDate = endDate ?: throw IllegalArgumentException()
            val params = PackageSearchParams(flightOrigin, flightDestination, checkInDate, checkOutDate, adults, children, infantSeatingInLap, flightCabinClass)
            params.filterOptions = buildFilterOptions()
            return params
        }

        override fun areRequiredParamsFilled(): Boolean {
            return hasOriginAndDestination() && hasStartAndEndDates()
        }

        override fun hasValidDateDuration(): Boolean {
            return Days.daysBetween(startDate, endDate).days <= maxStay
        }

        override fun isOriginSameAsDestination(): Boolean {
            val departureCity: String
            val arrivalCity: String
            if (originLocation?.hierarchyInfo?.airport?.multicity != null && destinationLocation?.hierarchyInfo?.airport?.multicity != null) {
                departureCity = originLocation?.hierarchyInfo?.airport?.multicity ?: ""
                arrivalCity = destinationLocation?.hierarchyInfo?.airport?.multicity ?: ""
            } else {
                departureCity = originLocation?.hierarchyInfo?.airport?.airportCode ?: ""
                arrivalCity = destinationLocation?.hierarchyInfo?.airport?.airportCode ?: ""
            }
            return departureCity == arrivalCity
        }

        fun flightCabinClass(cabinClass: String?): PackageSearchParams.Builder {
            this.flightCabinClass = cabinClass
            return this
        }

        fun hotelName(name: String): Builder {
            this.hotelName = name
            return this
        }

        fun starRatings(starRatings: List<Int>): Builder {
            this.starRatings = starRatings
            return this
        }

        fun vipOnly(vipOnly: Boolean): Builder {
            this.vipOnly = vipOnly
            return this
        }

        fun userSort(userSort: BaseHotelFilterOptions.SortType): Builder {
            this.userSort = userSort
            return this
        }

        private fun buildFilterOptions(): PackageHotelFilterOptions {
            var filterOptions = PackageHotelFilterOptions()
            filterOptions.filterHotelName = hotelName
            filterOptions.filterStarRatings = starRatings
            filterOptions.filterVipOnly = vipOnly
            filterOptions.userSort = userSort
            return filterOptions
        }
    }

    fun isOutboundSearch(): Boolean {
        return selectedLegId == null
    }

    fun isChangePackageSearch(): Boolean {
        return pageType == Constants.PACKAGE_CHANGE_HOTEL || pageType == Constants.PACKAGE_CHANGE_FLIGHT
    }

    fun isHotelFilterSearch(): Boolean {
        return filterOptions?.isNotEmpty() == true
    }

    fun toQueryMap(): Map<String, Any?> {
        val params = HashMap<String, Any?>()
        if (pageType != null) params.put("pageType", pageType)
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
        if (isOutboundSearch() || isChangePackageSearch()) {
            params.put("currentFlights", currentFlights.joinToString(","))
        }

        if (isChangePackageSearch()) {
            params.put("defaultFlights", defaultFlights.joinToString(","))
        }

        if (pageType == Constants.PACKAGE_CHANGE_FLIGHT) {
            params.put("action", Constants.PACKAGE_FILTER_CHANGE_FLIGHT)
        }

        if (filterOptions != null) {
            params.putAll(filterOptions!!.getFiltersQueryMap())
        }

        return params
    }

    private fun makeChildrenAgesParams(params: HashMap<String, Any?>, keyString: String, valueList: List<Int>, startIndex: Int) {
        for (i in startIndex..valueList.size) {
            val key = StringBuilder(keyString)
            key.append("[").append(i).append("]")
            val childAge = valueList[i - 1]
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

    fun convertToHotelSearchParams(maxStay: Int, maxRange: Int): HotelSearchParams {
        val builder = HotelSearchParams.Builder(maxStay, maxRange).destination(this.destination)
                .startDate(this.startDate).endDate(this.endDate).adults(this.adults)
                .children(this.children) as HotelSearchParams.Builder
        return addPackageFilterParams(builder, this.filterOptions).forPackage(true).build()
    }

    private fun addPackageFilterParams(builder: HotelSearchParams.Builder, filterOptions: BaseHotelFilterOptions?): HotelSearchParams.Builder {
        filterOptions?.takeUnless { it.isEmpty() }?.let {
            it.filterHotelName?.let { builder.hotelName(it) }
            it.userSort?.let { builder.userSort(it) }
            builder.vipOnly(it.filterVipOnly)
            builder.starRatings(it.filterStarRatings)
        }
        return builder
    }
}
