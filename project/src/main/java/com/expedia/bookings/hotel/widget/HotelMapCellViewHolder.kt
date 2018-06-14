package com.expedia.bookings.hotel.widget

import android.content.Context
import android.view.ViewGroup
import com.expedia.bookings.hotel.vm.HotelViewModel
import com.expedia.bookings.widget.shared.AbstractHotelResultCellViewHolder

class HotelMapCellViewHolder(root: ViewGroup) : AbstractHotelResultCellViewHolder(root) {

    override fun createHotelViewModel(context: Context): HotelViewModel {
        return HotelViewModel(context, alwaysShowEarnMessageSpace = true)
    }
}
