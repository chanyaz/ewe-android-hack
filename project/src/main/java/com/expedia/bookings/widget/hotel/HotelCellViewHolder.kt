package com.expedia.bookings.widget.hotel

import android.content.Context
import android.view.ViewGroup
import com.expedia.bookings.widget.shared.AbstractHotelCellViewHolder
import com.expedia.vm.hotel.HotelViewModel


class HotelCellViewHolder(root: ViewGroup) : AbstractHotelCellViewHolder(root) {

    override fun createHotelViewModel(context: Context): HotelViewModel {
        return HotelViewModel(context)
    }
}