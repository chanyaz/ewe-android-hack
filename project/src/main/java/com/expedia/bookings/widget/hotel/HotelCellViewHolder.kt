package com.expedia.bookings.widget.hotel

import android.content.Context
import android.view.ViewGroup
import com.expedia.bookings.data.hotels.Hotel
import com.expedia.bookings.widget.shared.AbstractHotelCellViewHolder
import com.expedia.vm.hotel.HotelViewModel


class HotelCellViewHolder(root: ViewGroup, width: Int) : AbstractHotelCellViewHolder(root, width) {

    init {
        bindViewModel()
    }

    override fun bindHotelData(hotel: Hotel) {
        super.bindHotelData(hotel)
    }

    override fun createHotelViewModel(context: Context): HotelViewModel {
        return HotelViewModel(context)
    }
}