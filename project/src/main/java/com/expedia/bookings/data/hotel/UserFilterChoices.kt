package com.expedia.bookings.data.hotel

import com.expedia.bookings.data.hotels.HotelSearchResponse
import com.expedia.bookings.featureconfig.ProductFlavorFeatureConfiguration
import java.util.ArrayList
import java.util.HashSet

data class UserFilterChoices(var userSort: Sort = ProductFlavorFeatureConfiguration.getInstance().defaultSort,
                             var isVipOnlyAccess: Boolean = false,
                             var hotelStarRating: StarRatings = StarRatings(),
                             var name: String = "",
                             var minPrice: Int = 0,
                             var maxPrice: Int = 0,
                             var amenity: HashSet<Int> = HashSet<Int>(),
                             var neighborhoods: HashSet<HotelSearchResponse.Neighborhood> = HashSet<HotelSearchResponse.Neighborhood>()) {

    fun filterCount(): Int {
        var count = 0
        count += hotelStarRating.getStarRatingParamsAsList().size
        if (isVipOnlyAccess) count++
        if (name.isNotEmpty()) count++
        if (neighborhoods.isNotEmpty()) count += neighborhoods.size
        if (amenity.isNotEmpty()) count += amenity.size
        if (minPrice != 0 || maxPrice != 0) count++
        return count
    }

    fun hasPriceRange(): Boolean {
        return minPrice > 0 || maxPrice > 0
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
    }
}