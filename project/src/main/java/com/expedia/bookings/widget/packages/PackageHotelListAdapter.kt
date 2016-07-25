package com.expedia.bookings.widget.packages

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import com.expedia.bookings.R
import com.expedia.bookings.data.hotels.Hotel
import com.expedia.bookings.widget.BaseHotelListAdapter
import com.expedia.bookings.widget.hotel.HotelCellViewHolder
import com.expedia.vm.hotel.HotelViewModel
import com.expedia.vm.packages.PackageHotelViewModel
import rx.subjects.PublishSubject

class PackageHotelListAdapter(hotelSelectedSubject: PublishSubject<Hotel>, headerSubject: PublishSubject<Unit>) :
        BaseHotelListAdapter(hotelSelectedSubject, headerSubject) {

    override fun getHotelCellHolder(parent: ViewGroup): HotelCellViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.package_hotel_cell, parent, false)
        return PackageHotelCellViewHolder(view as ViewGroup, parent.width)
    }

    override fun getHotelCellViewModel(context: Context, hotel: Hotel): HotelViewModel {
        return PackageHotelViewModel(context, hotel)
    }
}