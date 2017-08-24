package com.expedia.bookings.data.hotels

import com.expedia.bookings.data.BaseSearchParams
import com.expedia.bookings.data.SuggestionV4
import com.expedia.bookings.data.packages.PackageSearchParams
import org.joda.time.LocalDate
import java.util.HashMap

open class HotelSearchParams(val suggestion: SuggestionV4,
                             val checkIn: LocalDate, val checkOut: LocalDate,
                             adults: Int, children: List<Int>,
                             var shopWithPoints: Boolean, val filterUnavailable: Boolean,
                             var sortType: String? = null, var mctc: Int? = null) : BaseSearchParams(suggestion, null, adults, children, checkIn, checkOut) {
    var forPackage = false
    var filterOptions: HotelFilterOptions? = null
    var enableSponsoredListings = true
    var forcePinnedSearch = false

    fun isCurrentLocationSearch(): Boolean {
        return suggestion.isCurrentLocationSearch
    }

    fun clearPinnedHotelId() {
        suggestion.hotelId = null
    }

    fun isPinnedSearch(): Boolean {
        return suggestion.isPinnedHotelSearch
    }

    /**
     *  use user sort if set
     *  else use sort type if set
     *  else if current location search - set to distance
     *  otherwise default to expert picks
     */
    fun getSortOrder(): SortType {
        if (filterOptions?.userSort != null) {
            return filterOptions?.userSort!!
        }

        var sort: SortType? = getSortTypeFromString(sortType)

        if (sort != null) {
            return sort
        } else if (isCurrentLocationSearch()) {
            return SortType.DISTANCE
        }

        return SortType.EXPERT_PICKS
    }

    fun equalForPrefetch(other: HotelSearchParams?) : Boolean {
        return other!= null && suggestion.equals(other.suggestion)
                && checkIn == other.checkIn && checkOut == other.checkOut
                && adults == other.adults && children.size == other.children.size
                && shopWithPoints == other.shopWithPoints
                && filterOptions?.let { options -> options.isEmpty() } ?: true
                && other.filterOptions?.let { options -> options.isEmpty() } ?: true
    }

    private fun getSortTypeFromString(sortString: String?): SortType? {
        if (sortString != null) {
            when (sortString.toLowerCase()) {
                "discounts" -> return SortType.MOBILE_DEALS
                "deals" -> return SortType.MOBILE_DEALS
                "price" -> return SortType.PRICE
                "rating" -> return SortType.REVIEWS
                "guestrating" -> return SortType.REVIEWS
                else -> {
                    return null
                }
            }
        }
        return null
    }

    class Builder(maxStay: Int, maxRange: Int, val filterUnavailable: Boolean = true) : BaseSearchParams.Builder(maxStay, maxRange) {
        private var isPackage: Boolean = false
        private var shopWithPoints: Boolean = false
        private var priceRange: PriceRange? = null
        private var hotelName: String? = null
        private var starRatings: List<Int> = emptyList()
        private var neighborhoodRegionId: String? = null
        private var vipOnly: Boolean = false
        private var userSort: SortType? = null

        override fun destination(city: SuggestionV4?): Builder {
            this.destinationLocation = city?.copy() ?: null
            return this
        }

        fun forPackage(pkg: Boolean): Builder {
            this.isPackage = pkg
            return this
        }

        fun shopWithPoints(shopWithPoints: Boolean): Builder {
            this.shopWithPoints = shopWithPoints
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

        fun priceRange(priceRange: PriceRange?): Builder {
            this.priceRange = priceRange
            return this
        }

        fun vipOnly(vipOnly: Boolean): Builder {
            this.vipOnly = vipOnly
            return this
        }

        fun neighborhood(neighborhoodRegionId: String): Builder {
            this.neighborhoodRegionId = neighborhoodRegionId
            return this
        }

        fun userSort(userSort: SortType): Builder {
            this.userSort = userSort
            return this
        }

        fun clearUserSort() {
            this.userSort = null
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

        private fun buildFilterOptions(): HotelFilterOptions {
            val filterOptions = HotelFilterOptions()
            filterOptions.filterHotelName = hotelName
            filterOptions.filterStarRatings = starRatings
            filterOptions.filterPrice = priceRange
            filterOptions.filterVipOnly = vipOnly
            filterOptions.filterByNeighborhoodId = neighborhoodRegionId
            filterOptions.userSort = userSort
            return filterOptions
        }
    }

    class HotelFilterOptions {
        var filterHotelName: String? = null
        var filterStarRatings: List<Int> = emptyList()
        var filterPrice: PriceRange? = null
        var filterVipOnly: Boolean = false
        var filterByNeighborhoodId: String? = null
        var userSort: SortType? = null

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

            if (filterVipOnly) {
                params.put("vipOnly", filterVipOnly.toString())
            }

            return params
        }

        fun isEmpty() : Boolean {
            return filterHotelName.isNullOrEmpty()
                    && filterStarRatings.isEmpty()
                    && (filterPrice == null || !filterPrice!!.isValid())
                    && !filterVipOnly
                    && userSort == null
        }

        fun isNotEmpty() :Boolean {
            return !isEmpty()
      }
    }

    enum class SortType(val sortName: String) {
        EXPERT_PICKS("ExpertPicks"),
        STARS("StarRatingDesc"),
        PRICE("PriceAsc"),
        REVIEWS("Reviews"),
        DISTANCE("Distance"),
        MOBILE_DEALS("Deals")
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
