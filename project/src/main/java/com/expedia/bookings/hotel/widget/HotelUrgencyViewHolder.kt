package com.expedia.bookings.hotel.widget

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.expedia.bookings.R

class HotelUrgencyViewHolder(root: View) : SlimCardViewHolder(root) {
    fun bind(titleText: String) {
        title.text = titleText
    }

    companion object {
        fun create(parent: ViewGroup): SlimCardViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.hotel_slim_card, parent, false)
            return HotelUrgencyViewHolder(view)
        }
    }
}
