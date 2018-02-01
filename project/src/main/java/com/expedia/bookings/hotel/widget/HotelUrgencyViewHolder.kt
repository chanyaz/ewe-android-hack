package com.expedia.bookings.hotel.widget

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.expedia.bookings.R
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.widget.TextView

class HotelUrgencyViewHolder(root: View) : RecyclerView.ViewHolder(root) {
    private val urgencyTitle: TextView by bindView(R.id.urgency_title)

    fun bind(titleText: String) {
        urgencyTitle.text = titleText
    }

    companion object {
        fun create(parent: ViewGroup): HotelUrgencyViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.hotel_results_urgency_cell, parent, false)
            return HotelUrgencyViewHolder(view)
        }
    }
}
