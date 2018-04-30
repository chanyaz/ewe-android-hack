package com.expedia.vm.hotel

import android.content.Context
import com.expedia.bookings.data.hotel.DisplaySort
import com.expedia.bookings.data.hotel.UserFilterChoices
import com.expedia.bookings.data.hotels.HotelSearchParams
import com.expedia.bookings.tracking.hotel.FilterTracker
import com.expedia.bookings.tracking.hotel.HotelFilterTracker
import io.reactivex.subjects.PublishSubject

class HotelFilterViewModel(context: Context) : BaseHotelFilterViewModel(context) {
    //out

    override fun updatePresetOptions(filterOptions: HotelSearchParams.HotelFilterOptions) {
        presetFilterOptions = false
        if (filterOptions.isNotEmpty()) {
            val filterChoices = UserFilterChoices.fromHotelFilterOptions(filterOptions)
            previousFilterChoices = filterChoices
            presetFilterOptions = true
            presetFilterOptionsUpdatedSubject.onNext(filterChoices)
        }
    }

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
