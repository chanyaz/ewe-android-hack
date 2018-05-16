package com.expedia.bookings.widget.hotel

import android.view.LayoutInflater
import android.view.ViewGroup
import com.expedia.bookings.R
import com.expedia.bookings.data.hotels.shortlist.HotelShortlistItem
import com.expedia.bookings.utils.Images
import com.expedia.bookings.widget.shared.AbstractHotelCellViewHolder

class HotelFavoritesItemViewHolder(root: ViewGroup) : AbstractHotelCellViewHolder(root) {

    fun bind(item: HotelShortlistItem) {
        hotelNameStarAmenityDistance.hotelNameTextView.text = item.name
        item.media?.let { media -> loadHotelImage(Images.getMediaHost() + media) }
    }

    companion object {
        fun create(parent: ViewGroup): HotelFavoritesItemViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.hotel_cell, parent, false)
            return HotelFavoritesItemViewHolder(view as ViewGroup)
        }
    }
}
