package com.expedia.bookings.hotel.vm

import android.content.Context
import com.expedia.bookings.data.hotel.DisplaySort
import com.expedia.bookings.tracking.hotel.FilterTracker
import com.expedia.bookings.tracking.hotel.HotelFilterTracker

class HotelFilterViewModel(context: Context) : BaseHotelFilterViewModel(context) {

    override fun getDefaultSort(): DisplaySort {
        if (isCurrentLocationSearch.value) {
            return DisplaySort.DISTANCE
        }
        return super.getDefaultSort()
    }

    override fun sortItemToRemove(): DisplaySort {
        return DisplaySort.PACKAGE_DISCOUNT
    }

    override fun createFilterTracker(): FilterTracker {
        return HotelFilterTracker()
    }

    override fun isClientSideFiltering(): Boolean {
        return false
    }
}
