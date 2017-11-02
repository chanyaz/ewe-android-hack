package com.expedia.bookings.widget.hotel

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import com.expedia.bookings.R
import com.expedia.bookings.data.hotels.Hotel
import com.expedia.bookings.widget.BaseHotelListAdapter
import io.reactivex.subjects.PublishSubject

class HotelListAdapter(hotelSelectedSubject: PublishSubject<Hotel>, headerSubject: PublishSubject<Unit>, pricingHeaderSelectedSubject: PublishSubject<Unit>) :
        BaseHotelListAdapter(hotelSelectedSubject, headerSubject, pricingHeaderSelectedSubject) {

    override fun getHotelCellHolder(parent: ViewGroup): HotelCellViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.hotel_cell, parent, false)
        val holder = HotelCellViewHolder(view as ViewGroup)

        return holder
    }

    override fun getPriceDescriptorMessageIdForHSR(context: Context): Int? {
        return null
    }

    override fun shouldShowResultDescriptionLabel(context: Context): Boolean {
        return false
    }

}
