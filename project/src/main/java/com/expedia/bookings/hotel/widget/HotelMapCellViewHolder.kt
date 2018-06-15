package com.expedia.bookings.hotel.widget

import android.content.Context
import android.view.ViewGroup
import com.expedia.bookings.hotel.vm.HotelViewModel
import com.expedia.bookings.packages.vm.PackageHotelViewModel
import com.expedia.bookings.widget.shared.AbstractHotelResultCellViewHolder

class HotelMapCellViewHolder(root: ViewGroup, val isPackage: Boolean) : AbstractHotelResultCellViewHolder(root) {

    override fun createHotelViewModel(context: Context): HotelViewModel {
        return if (isPackage) {
            PackageHotelViewModel(context, alwaysShowEarnMessageSpace = true)
        } else {
            HotelViewModel(context, alwaysShowEarnMessageSpace = true)
        }
    }
}
