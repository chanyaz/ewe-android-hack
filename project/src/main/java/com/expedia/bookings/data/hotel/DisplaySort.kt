package com.expedia.bookings.data.hotel

import android.support.annotation.StringRes
import com.expedia.bookings.R
import com.expedia.bookings.data.hotels.HotelSearchParams

enum class DisplaySort(@StringRes val resId: Int) {
    RECOMMENDED(R.string.recommended),
    PRICE(R.string.price),
    DEALS(R.string.sort_description_deals),
    PACKAGE_DISCOUNT(R.string.sort_description_package_discount),
    RATING(R.string.rating),
    DISTANCE(R.string.distance);

    fun toServerSort(): HotelSearchParams.SortType {
        when (this) {
            RECOMMENDED -> return HotelSearchParams.SortType.EXPERT_PICKS
            PRICE -> return HotelSearchParams.SortType.PRICE
            DEALS -> return HotelSearchParams.SortType.MOBILE_DEALS
            RATING -> return HotelSearchParams.SortType.REVIEWS
            DISTANCE -> return HotelSearchParams.SortType.DISTANCE
            else -> return HotelSearchParams.SortType.EXPERT_PICKS
        }
    }

    companion object {
        @JvmStatic
        fun fromServerSort(sortType: HotelSearchParams.SortType): DisplaySort {
            when (sortType) {
                HotelSearchParams.SortType.EXPERT_PICKS -> return RECOMMENDED
                HotelSearchParams.SortType.PRICE -> return PRICE
                HotelSearchParams.SortType.MOBILE_DEALS -> return DEALS
                HotelSearchParams.SortType.REVIEWS -> return RATING
                HotelSearchParams.SortType.DISTANCE -> return DISTANCE
                else -> return RECOMMENDED
            }
        }

        @JvmStatic
        fun getDefaultSort() = DisplaySort.RECOMMENDED
    }
}