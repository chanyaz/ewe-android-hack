package com.expedia.bookings.data.hotel

import android.support.annotation.StringRes
import com.expedia.bookings.R
import com.expedia.bookings.data.BaseHotelFilterOptions

enum class DisplaySort(@StringRes val resId: Int) {
    RECOMMENDED(R.string.recommended),
    PRICE(R.string.price),
    DEALS(R.string.sort_description_deals),
    PACKAGE_DISCOUNT(R.string.sort_description_package_discount),
    RATING(R.string.rating),
    DISTANCE(R.string.distance);

    fun toServerSort(): BaseHotelFilterOptions.SortType {
        when (this) {
            RECOMMENDED -> return BaseHotelFilterOptions.SortType.EXPERT_PICKS
            PRICE -> return BaseHotelFilterOptions.SortType.PRICE
            DEALS -> return BaseHotelFilterOptions.SortType.MOBILE_DEALS
            RATING -> return BaseHotelFilterOptions.SortType.REVIEWS
            DISTANCE -> return BaseHotelFilterOptions.SortType.DISTANCE
            PACKAGE_DISCOUNT -> return BaseHotelFilterOptions.SortType.MOBILE_DEALS
            else -> return BaseHotelFilterOptions.SortType.EXPERT_PICKS
        }
    }

    companion object {
        @JvmStatic
        fun fromServerSort(sortType: BaseHotelFilterOptions.SortType): DisplaySort {
            when (sortType) {
                BaseHotelFilterOptions.SortType.EXPERT_PICKS -> return RECOMMENDED
                BaseHotelFilterOptions.SortType.PRICE -> return PRICE
                BaseHotelFilterOptions.SortType.MOBILE_DEALS -> return DEALS
                BaseHotelFilterOptions.SortType.REVIEWS -> return RATING
                BaseHotelFilterOptions.SortType.DISTANCE -> return DISTANCE
                else -> return RECOMMENDED
            }
        }

        @JvmStatic
        fun getDefaultSort() = DisplaySort.RECOMMENDED
    }
}
