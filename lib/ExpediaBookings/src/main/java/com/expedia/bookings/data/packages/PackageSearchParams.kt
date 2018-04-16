package com.expedia.bookings.data.packages

import com.expedia.bookings.data.AbstractFlightSearchParams
import com.expedia.bookings.data.BaseHotelFilterOptions
import com.expedia.bookings.data.SuggestionV4
import com.expedia.bookings.data.flights.FlightLeg
import com.expedia.bookings.data.hotels.HotelSearchParams
import com.expedia.bookings.utils.Constants
import org.joda.time.Days
import org.joda.time.LocalDate
import kotlin.properties.Delegates

open class PackageSearchParams(origin: SuggestionV4?, destination: SuggestionV4?, startDate: LocalDate, endDate: LocalDate?, adults: Int, children: List<Int>, infantSeatingInLap: Boolean, val flightCabinClass: String? = null, val multiRoomAdults: Map<Int, Int> = emptyMap(), val multiRoomChildren: Map<Int, List<Int>> = emptyMap()) : AbstractFlightSearchParams(origin, destination, adults, children, startDate, endDate, infantSeatingInLap) {

    override val guests: Int
        get() = if (isMultiRoomSearch()) multiRoomAdults.map { it.value }.sum() + multiRoomChildren.map { it.value.size }.sum() else super.guests

    var pageType: String? = null
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
            if (children.isEmpty() && multiRoomChildren.isEmpty()) {
                return null
            }
            return if (isMultiRoomSearch()) getChildrenStringForMultipleRooms() else children.joinToString(separator = ",")
        }

    private fun getChildrenStringForMultipleRooms(): String? {
        if (multiRoomChildren.isEmpty()) return null

        val childString = StringBuilder()
        for (i in 1..Constants.PACKAGE_MAX_ROOMS_ALLOWED_TO_BOOK) {
            val childrenInRoom = multiRoomChildren[i]
            if (childrenInRoom != null && childrenInRoom.isNotEmpty()) {
                childString.append(childrenInRoom.joinToString(","))
            }
            if (i != Constants.PACKAGE_MAX_ROOMS_ALLOWED_TO_BOOK) childString.append("_")
        }
        return childString.toString()
    }

    val adultsQueryParam: String
        get() {
            return if (isMultiRoomSearch()) multiRoomAdults.values.joinToString(",") else adults.toString()
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

    private fun isMultiRoomSearch(): Boolean {
        return multiRoomAdults.isNotEmpty()
    }

    class Builder(maxStay: Int, maxRange: Int) : AbstractFlightSearchParams.Builder(maxStay, maxRange) {

        private var flightCabinClass: String? = null

        private var hotelName: String? = null
        private var starRatings: List<Int> = emptyList()
        private var vipOnly: Boolean = false
        private var userSort: BaseHotelFilterOptions.SortType? = null

        var multiRoomAdults: Map<Int, Int> = emptyMap()
        var multiRoomChildren: Map<Int, List<Int>> = emptyMap()

        override fun build(): PackageSearchParams {
            val flightOrigin = originLocation ?: throw IllegalArgumentException()
            val flightDestination = destinationLocation ?: throw IllegalArgumentException()
            val checkInDate = startDate ?: throw IllegalArgumentException()
            val checkOutDate = endDate ?: throw IllegalArgumentException()

            val params = PackageSearchParams(flightOrigin, flightDestination, checkInDate, checkOutDate, adults, children, infantSeatingInLap, flightCabinClass, multiRoomAdults, multiRoomChildren)
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

        fun multiRoomAdults(multiRoomAdults: Map<Int, Int>): PackageSearchParams.Builder {
            this.multiRoomAdults = multiRoomAdults
            return this
        }

        fun multiRoomChildren(multiRoomChildren: Map<Int, List<Int>>): PackageSearchParams.Builder {
            this.multiRoomChildren = multiRoomChildren
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
