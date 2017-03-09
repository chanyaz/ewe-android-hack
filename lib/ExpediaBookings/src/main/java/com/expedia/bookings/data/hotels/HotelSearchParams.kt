package com.expedia.bookings.data.hotels

import com.expedia.bookings.data.BaseSearchParams
import com.expedia.bookings.data.SuggestionV4
import com.expedia.bookings.data.packages.PackageSearchParams
import org.joda.time.LocalDate
import java.util.HashMap

open class HotelSearchParams(val suggestion: SuggestionV4, val checkIn: LocalDate, val checkOut: LocalDate, adults: Int, children: List<Int>, val shopWithPoints: Boolean, val filterUnavailable: Boolean, val sortType: String? = null) : BaseSearchParams(suggestion, null, adults, children, checkIn, checkOut) {
    var forPackage = false
    var filterOptions: HotelFilterOptions? = null

    fun isCurrentLocationSearch(): Boolean {
        return suggestion.isCurrentLocationSearch
    }

    class Builder(maxStay: Int, maxRange: Int, val filterUnavailable: Boolean = true) : BaseSearchParams.Builder(maxStay, maxRange) {
        private var isPackage: Boolean = false
        private var shopWithPoints: Boolean = false
        var hotelName: String? = null
        var starRatings: List<Int> = emptyList()
        var priceRange: PriceRange? = null

        fun forPackage(pkg: Boolean): Builder {
            this.isPackage = pkg
            return this
        }

        fun shopWithPoints(shopWithPoints: Boolean): Builder {
            this.shopWithPoints = shopWithPoints
            return this
        }

        override fun build(): HotelSearchParams {
            val location = destinationLocation ?: throw IllegalArgumentException()
            if (destinationLocation?.gaiaId == null && destinationLocation?.coordinates == null) throw IllegalArgumentException()
            val checkInDate = startDate ?: throw IllegalArgumentException()
            val checkOutDate = endDate ?: throw IllegalArgumentException()
            var params = HotelSearchParams(location, checkInDate, checkOutDate, adults, children, shopWithPoints, filterUnavailable)
            params.forPackage = isPackage
            params.filterOptions = buildFilterOptions()
            return params
        }

        override fun areRequiredParamsFilled(): Boolean {
            return hasDestinationLocation() && hasStartAndEndDates()
        }

        override fun hasOriginAndDestination(): Boolean {
            return hasDestinationLocation() //origin won't be set
        }

        override fun isOriginSameAsDestination(): Boolean {
            return false // not possible for hotel search
        }

        private fun buildFilterOptions() : HotelFilterOptions {
            val filterOptions = HotelFilterOptions()
            filterOptions.filterHotelName = hotelName
            filterOptions.filterStarRatings = starRatings
            filterOptions.filterPrice = priceRange
            return filterOptions
        }
    }

    class HotelFilterOptions {
        var filterHotelName: String? = null
        var filterStarRatings: List<Int> = emptyList()
        var filterPrice: PriceRange? = null

        fun getFiltersQueryMap(): Map<String, Any?> {
            val params = HashMap<String, Any?>()
            if (!filterHotelName.isNullOrEmpty()) {
                params.put("filterHotelName", filterHotelName)
            }

            if (filterStarRatings.isNotEmpty()) {
                params.put("filterStarRatings", filterStarRatings.joinToString(","))
            }

            if (filterPrice != null && filterPrice!!.isValid()) {
                params.put("filterPrice", filterPrice!!.getPriceBuckets())
            }

            return params
        }
    }

    data class PriceRange(val minPrice: Int, val maxPrice: Int) {
        fun isValid(): Boolean {
            return minPrice > 0 || maxPrice > 0
        }

        fun getPriceBuckets(): String {
            val sb = StringBuffer()
            sb.append(minPrice)
            if (maxPrice > 0) {
                sb.append(",").append(maxPrice)
            }
            return sb.toString()
        }
    }
}

fun convertPackageToSearchParams(packageParams: PackageSearchParams, maxStay: Int, maxRange: Int): HotelSearchParams {
    val builder = HotelSearchParams.Builder(maxStay, maxRange).destination(packageParams.destination)
            .startDate(packageParams.startDate).endDate(packageParams.endDate).adults(packageParams.adults)
            .children(packageParams.children) as HotelSearchParams.Builder
    return builder.forPackage(true).build()
}
