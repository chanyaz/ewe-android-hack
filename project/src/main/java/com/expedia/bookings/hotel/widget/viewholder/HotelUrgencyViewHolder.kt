package com.expedia.bookings.hotel.widget.viewholder

import android.support.v4.content.ContextCompat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.expedia.bookings.R

class HotelUrgencyViewHolder(root: View) : SlimCardViewHolder(root) {
    fun bind(titleText: String) {
        title.text = titleText
    }
    init {
        val drawable = root.context.getDrawable(R.drawable.urgency)
        icon.setImageDrawable(drawable)
        icon.setColorFilter(ContextCompat.getColor(root.context, R.color.hotel_urgency_icon_color))
        icon.visibility = View.VISIBLE
    }

    companion object {
        fun create(parent: ViewGroup): SlimCardViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.hotel_slim_card, parent, false)
            return HotelUrgencyViewHolder(view)
        }
    }
}
