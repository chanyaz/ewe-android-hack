package com.expedia.bookings.utils

import android.view.LayoutInflater
import android.view.ViewGroup
import com.expedia.bookings.R
import com.expedia.bookings.data.hotels.HotelSearchResponse
import com.expedia.bookings.widget.HotelAmenityFilter
import com.expedia.util.subscribeOnClick
import com.expedia.vm.hotel.BaseHotelFilterViewModel
import java.util.Comparator
import java.util.TreeMap

enum class FilterAmenity(val preference: Int, val resId: Int, val strId: Int) {
    FREE_INTERNET(1, R.drawable.ic_amenity_internet, R.string.FilterFreeInternet),
    FREE_BREAKFAST(2, R.drawable.ic_amenity_breakfast, R.string.FilterFreeBreakfast),
    FREE_PARKING(3, R.drawable.ic_amenity_parking, R.string.AmenityFreeParking),
    FREE_AIRPORT_SHUTTLE(10, R.drawable.ic_amenity_airport_shuttle, R.string.FilterFreeAirportShuttle),
    KITCHEN(15, R.drawable.ic_amenity_kitchen, R.string.AmenityKitchen);

    companion object {
        fun amenityFilterToShow(map: Map<String, HotelSearchResponse.AmenityOptions>): Map<FilterAmenity, Int> {
            val amenityMap = TreeMap<FilterAmenity, Int>(FilterAmenityComparator())

            for (filterId in map.keys) {
                val id = filterId.toInt()
                when (id) {
                    14 -> amenityMap.put(FilterAmenity.FREE_PARKING, id)
                    16 -> amenityMap.put(FilterAmenity.FREE_BREAKFAST, id)
                    19 -> amenityMap.put(FilterAmenity.FREE_INTERNET, id)
                    66 -> amenityMap.put(FilterAmenity.FREE_AIRPORT_SHUTTLE, id)
                    2158 -> amenityMap.put(FilterAmenity.KITCHEN, id)
                }
            }
            return amenityMap
        }

        fun addAmenityFilters(viewGroup: ViewGroup, amenityMaps: Map<FilterAmenity, Int>, vm: BaseHotelFilterViewModel) {
            viewGroup.removeAllViews()

            for (amenityEntry in amenityMaps.entries) {
                val amenityLayout = LayoutInflater.from(viewGroup.context).inflate(R.layout.filter_amenity_row, null) as HotelAmenityFilter
                amenityLayout.bind(amenityEntry.key, amenityEntry.value, vm)
                val columnNum = 4
                amenityLayout.layoutParams = ViewGroup.LayoutParams(viewGroup.context.resources.displayMetrics.widthPixels / columnNum, ViewGroup.LayoutParams.WRAP_CONTENT)
                viewGroup.addView(amenityLayout)
                amenityLayout.subscribeOnClick(amenityLayout.selectObserver)
            }
        }

        fun amenityIdToFilterId(amenityId: Int): Int {
            return when (amenityId) {
            //Free Breakfast
                4, 16777216, 2, 1073742786, 8192, 1073742857 -> 16
            //Free Internet
                2048, 1024, 1073742787 -> 19
            //Airport Shuttle
                32768 -> 66
            //Free parking
                128, 16384 -> 14
            //Kitchen
                134217728 -> 2158
                else -> -1
            }
        }

    }
}

class FilterAmenityComparator : Comparator<FilterAmenity> {
    override fun compare(lhs: FilterAmenity, rhs: FilterAmenity): Int {
        return lhs.preference.minus(rhs.preference)
    }
}
