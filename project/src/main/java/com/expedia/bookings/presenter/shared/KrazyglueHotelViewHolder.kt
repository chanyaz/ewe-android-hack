package com.expedia.bookings.presenter.shared

import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.TextView
import com.expedia.bookings.R
import com.expedia.bookings.data.hotels.Hotel
import com.expedia.bookings.utils.bindView


class KrazyglueHotelViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    val hotelNameTextView: TextView by bindView(R.id.hotel_name_text_view)

    fun bindData(hotel: Hotel) {
        hotelNameTextView.text = hotel.localizedName
    }

}