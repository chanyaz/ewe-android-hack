package com.expedia.bookings.presenter.shared

import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.TextView
import com.expedia.bookings.R
import com.expedia.bookings.data.flights.KrazyglueResponse
import com.expedia.bookings.tracking.flight.FlightsV2Tracking
import com.expedia.bookings.utils.bindView


class KrazyglueHotelViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener {

    val hotelNameTextView: TextView by bindView(R.id.hotel_name_text_view)

    fun bindData(hotel: KrazyglueResponse.KrazyglueHotel) {
        hotelNameTextView.text = hotel.hotelName
    }

    override fun onClick(p0: View?) {
//        TODO: go onto hotels activity
        FlightsV2Tracking.trackKrazyglueHotelClicked(adapterPosition + 1)
    }
}