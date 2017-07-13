package com.expedia.bookings.widget.packages

import android.view.LayoutInflater
import android.view.ViewGroup
import com.expedia.bookings.R
import com.expedia.bookings.data.hotels.Hotel
import com.expedia.bookings.widget.BaseHotelListAdapter
import rx.subjects.PublishSubject

class PackageHotelListAdapter(hotelSelectedSubject: PublishSubject<Hotel>, headerSubject: PublishSubject<Unit>, pricingHeaderSelectedSubject: PublishSubject<Unit>) :
        BaseHotelListAdapter(hotelSelectedSubject, headerSubject, pricingHeaderSelectedSubject) {

    override fun getHotelCellHolder(parent: ViewGroup): PackageHotelCellViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.package_hotel_cell, parent, false)
        return PackageHotelCellViewHolder(view as ViewGroup, parent.width)
    }
}