package com.expedia.bookings.data.hotel

import com.expedia.bookings.data.hotels.HotelSearchParams
import com.expedia.bookings.data.hotels.Neighborhood
import java.util.ArrayList
import java.util.HashSet

data class UserFilterChoices(var userSort: DisplaySort = DisplaySort.getDefaultSort(),
                             var isVipOnlyAccess: Boolean = false,
                             var hotelStarRating: StarRatings = StarRatings(),
                             var hotelGuestRating: GuestRatings = GuestRatings(),
                             var name: String = "",
                             var minPrice: Int = 0,
                             var maxPrice: Int = 0,
                             var amenities: HashSet<Int> = HashSet(),
                             var neighborhoods: HashSet<Neighborhood> = HashSet<Neighborhood>()) {

    fun filterCount(): Int {
        var count = 0
        count += hotelStarRating.getStarRatingParamsAsList().size
        if (hotelGuestRating.getGuestRatingParamAsList().isNotEmpty()) count++
        if (isVipOnlyAccess) count++
        if (name.isNotEmpty()) count++
        if (neighborhoods.isNotEmpty()) count += neighborhoods.size
        if (amenities.isNotEmpty()) count += amenities.size
        if (minPrice != 0 || maxPrice != 0) count++
        return count
    }

    fun hasPriceRange(): Boolean {
        return minPrice > 0 || maxPrice > 0
    }

    //Kotlin's default implementation doesn't do a deep copy correctly - passes the same ref to star ratings :-(
    fun copy(): UserFilterChoices {
        val filterChoices = UserFilterChoices()
        filterChoices.userSort = userSort
        filterChoices.isVipOnlyAccess = isVipOnlyAccess
        filterChoices.hotelStarRating = hotelStarRating.copy()
        filterChoices.hotelGuestRating = hotelGuestRating.copy()
        filterChoices.name = name
        filterChoices.minPrice = minPrice
        filterChoices.maxPrice = maxPrice
        filterChoices.amenities = HashSet<Int>(amenities)
        filterChoices.neighborhoods = HashSet<Neighborhood>(neighborhoods)
        return filterChoices
    }

    companion object {
        @JvmStatic
        fun fromHotelFilterOptions(searchOptions: HotelSearchParams.HotelFilterOptions): UserFilterChoices {
            //advanced search options support only hotel name, vip, star ratings and sort for now
            val filterChoices = UserFilterChoices()
            filterChoices.name = searchOptions.filterHotelName ?: ""
            filterChoices.isVipOnlyAccess = searchOptions.filterVipOnly

            if (searchOptions.userSort != null) {
                filterChoices.userSort = DisplaySort.fromServerSort(searchOptions.userSort!!)
            }

            if (searchOptions.filterStarRatings.isNotEmpty()) {
                filterChoices.hotelStarRating = StarRatings.fromParamList(searchOptions.filterStarRatings)
            }

            filterChoices.minPrice = searchOptions.filterPrice?.minPrice ?: 0
            filterChoices.maxPrice = searchOptions.filterPrice?.maxPrice ?: 0

            if (searchOptions.filterGuestRatings.isNotEmpty()) {
                filterChoices.hotelGuestRating = GuestRatings.fromParamList(searchOptions.filterGuestRatings)
            }

            filterChoices.amenities = searchOptions.amenities
            searchOptions.filterByNeighborhood?.let { neighborhood ->
                filterChoices.neighborhoods.add(neighborhood)
            }

            return filterChoices
        }
    }

    data class StarRatings(var one: Boolean = false, var two: Boolean = false, var three: Boolean = false, var four: Boolean = false, var five: Boolean = false) {
        fun getStarRatingParamsAsList(): List<Int> {
            val ratings = ArrayList<Int>()
            if (one) ratings.add(10)
            if (two) ratings.add(20)
            if (three) ratings.add(30)
            if (four) ratings.add(40)
            if (five) ratings.add(50)

            return ratings
        }

        companion object {
            @JvmStatic
            fun fromParamList(ratingList: List<Int>): StarRatings {
                val ratings = StarRatings()
                if (ratingList.contains(10)) ratings.one = true
                if (ratingList.contains(20)) ratings.two = true
                if (ratingList.contains(30)) ratings.three = true
                if (ratingList.contains(40)) ratings.four = true
                if (ratingList.contains(50)) ratings.five = true
                return ratings
            }
        }
    }

    data class GuestRatings(var three: Boolean = false, var four: Boolean = false, var five: Boolean = false) {
        fun getGuestRatingParamAsList(): List<Int> {
            val guestRatings = ArrayList<Int>()
            when {
                three -> {
                    guestRatings.add(3)
                    guestRatings.add(4)
                    guestRatings.add(5)
                }
                four -> {
                    guestRatings.add(4)
                    guestRatings.add(5)
                }
                five -> guestRatings.add(5)
            }
            return guestRatings
        }

        companion object {
            @JvmStatic
            fun fromParamList(ratingList: List<Int>): GuestRatings {
                val guestRatings = GuestRatings()
                if (ratingList.contains(3)) {
                    guestRatings.three = true
                } else if (ratingList.contains(4)) {
                    guestRatings.four = true
                } else if (ratingList.contains(5)) {
                    guestRatings.five = true
                }
                return guestRatings
            }
        }
    }
}
