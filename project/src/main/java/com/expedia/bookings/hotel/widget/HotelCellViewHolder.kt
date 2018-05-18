package com.expedia.bookings.hotel.widget

import android.content.Context
import android.view.ViewGroup
import com.expedia.bookings.widget.shared.AbstractHotelResultCellViewHolder
import com.expedia.vm.hotel.HotelViewModel

class HotelCellViewHolder(root: ViewGroup) : AbstractHotelResultCellViewHolder(root) {

    override fun createHotelViewModel(context: Context): HotelViewModel {
        return HotelViewModel(context)
    }
}
