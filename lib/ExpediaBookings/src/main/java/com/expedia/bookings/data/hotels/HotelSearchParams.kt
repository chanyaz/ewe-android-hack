package com.expedia.bookings.data.hotels

import com.expedia.bookings.data.BaseSearchParams
import com.expedia.bookings.data.SuggestionV4
import com.expedia.bookings.data.packages.PackageSearchParams
import org.joda.time.LocalDate
import java.util.HashMap

open class HotelSearchParams(val suggestion: SuggestionV4,
                             val checkIn: LocalDate, val checkOut: LocalDate,
                             adults: Int, children: List<Int>,
                             var shopWithPoints: Boolean, var sortType: String? = null,
                             var mctc: Int? = null) : BaseSearchParams(suggestion, null, adults, children, checkIn, checkOut) {
    var forPackage = false
    var filterOptions: HotelFilterOptions? = null
    var enableSponsoredListings = true
    var updateSearchDestination = false
    var isDatelessSearch = false

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

        val sort: SortType? = getSortTypeFromString(sortType)

        if (sort != null) {
            return sort
        } else if (isCurrentLocationSearch()) {
            return SortType.DISTANCE
        }

        return SortType.EXPERT_PICKS
    }

    fun equalForPrefetch(other: HotelSearchParams?): Boolean {
        return other != null && suggestion.equals(other.suggestion)
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

    class Builder(maxStay: Int, maxStartRange: Int) : BaseSearchParams.Builder(maxStay, maxStartRange) {
        private var isPackage: Boolean = false
        private var shopWithPoints: Boolean = false
        private var priceRange: PriceRange? = null
        private var hotelName: String? = null
        private var starRatings: List<Int> = emptyList()
        private var neighborhoodRegion: Neighborhood? = null
        private var guestRatings: List<Int> = emptyList()
        private var vipOnly: Boolean = false
        private var userSort: SortType? = null
        private var amenities: HashSet<Int> = HashSet()
        private var isDatelessSearch: Boolean = false

        override fun destination(city: SuggestionV4?): Builder {
            this.destinationLocation = city?.copy()
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

        fun guestRatings(guestRatings: List<Int>): Builder {
            this.guestRatings = guestRatings
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

        fun neighborhood(neighborhoodRegion: Neighborhood): Builder {
            this.neighborhoodRegion = neighborhoodRegion
            return this
        }

        fun userSort(userSort: SortType): Builder {
            this.userSort = userSort
            return this
        }

        fun clearUserSort() {
            this.userSort = null
        }

        fun amenities(amenities: HashSet<Int>): Builder {
            this.amenities = amenities
            return this
        }

        fun from(params: HotelSearchParams): Builder {
            destination(params.suggestion)
            forPackage(params.forPackage)
            shopWithPoints(params.shopWithPoints)
            params.filterOptions?.let { filterOptions ->
                filterOptions.filterHotelName?.let { hotelName(it) }
                filterOptions.filterStarRatings.let { starRatings(it) }
                filterOptions.filterGuestRatings.let { guestRatings(it) }
                filterOptions.filterPrice?.let { priceRange(it) }
                vipOnly(filterOptions.filterVipOnly)
                filterOptions.filterByNeighborhood?.let { neighborhood(it) }
                filterOptions.userSort?.let { userSort(it) }
                filterOptions.amenities.let { amenities(it) }
            }
            adults(params.adults)
            children(params.children)
            startDate(params.checkIn)
            endDate(params.checkOut)
            return this
        }

        override fun build(): HotelSearchParams {
            val location = destinationLocation ?: throw IllegalArgumentException()
            if (destinationLocation?.gaiaId == null && destinationLocation?.coordinates == null) throw IllegalArgumentException()
            val checkInDate = startDate ?: throw IllegalArgumentException()
            val checkOutDate = endDate ?: throw IllegalArgumentException()
            val params = HotelSearchParams(location, checkInDate, checkOutDate, adults, children, shopWithPoints)
            params.forPackage = isPackage
            params.filterOptions = buildFilterOptions()
            params.isDatelessSearch = isDatelessSearch
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
            filterOptions.filterGuestRatings = guestRatings
            filterOptions.filterPrice = priceRange
            filterOptions.filterVipOnly = vipOnly
            filterOptions.filterByNeighborhood = neighborhoodRegion
            filterOptions.userSort = userSort
            filterOptions.amenities = amenities
            return filterOptions
        }
    }

    class HotelFilterOptions {
        var filterHotelName: String? = null
        var filterStarRatings: List<Int> = emptyList()
        var filterGuestRatings: List<Int> = emptyList()
        var filterPrice: PriceRange? = null
        var filterVipOnly: Boolean = false
        var filterByNeighborhood: Neighborhood? = null
        var userSort: SortType? = null
        var amenities: HashSet<Int> = HashSet()

        fun getFiltersQueryMap(): Map<String, String> {
            val params = HashMap<String, String>()
            if (!filterHotelName.isNullOrEmpty()) {
                params.put("filterHotelName", filterHotelName!!)
            }

            if (filterStarRatings.isNotEmpty()) {
                params.put("filterStarRatings", filterStarRatings.joinToString(","))
            }

            if (filterGuestRatings.isNotEmpty()) {
                params.put("guestRatingFilterItems", filterGuestRatings.joinToString(","))
            }

            if (filterPrice != null && filterPrice!!.isValid()) {
                params.put("filterPrice", filterPrice!!.getPriceBuckets())
            }

            if (filterVipOnly) {
                params.put("vipOnly", filterVipOnly.toString())
            }

            if (!amenities.isEmpty()) {
                params.put("filterAmenities", amenities.joinToString(","))
            }

            return params
        }

        fun isEmpty(): Boolean {
            return filterHotelName.isNullOrEmpty()
                    && filterStarRatings.isEmpty()
                    && filterGuestRatings.isEmpty()
                    && (filterPrice == null || !filterPrice!!.isValid())
                    && !filterVipOnly
                    && userSort == null
                    && amenities.isEmpty()
        }

        fun isNotEmpty(): Boolean {
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
