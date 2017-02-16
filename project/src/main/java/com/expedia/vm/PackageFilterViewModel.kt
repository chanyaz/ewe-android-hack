package com.expedia.vm

import android.content.Context
import com.expedia.bookings.data.hotel.Sort
import com.expedia.bookings.tracking.PackagesFilterTracker
import com.expedia.bookings.tracking.hotel.FilterTracker

class PackageFilterViewModel(context: Context) : HotelClientFilterViewModel(context) {

    override fun createFilterTracker(): FilterTracker {
        return PackagesFilterTracker()
    }

    override fun sortItemToRemove(): Sort {
        return Sort.DEALS
    }

    override fun showHotelFavorite(): Boolean {
        return false
    }
}
