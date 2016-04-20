package com.expedia.bookings.widget.hotel

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import com.expedia.bookings.R
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.abacus.AbacusUtils
import com.expedia.bookings.data.hotels.Hotel
import com.expedia.bookings.widget.BaseHotelListAdapter
import com.expedia.vm.hotel.HotelViewModel
import rx.subjects.PublishSubject

class HotelListAdapter(hotelSelectedSubject: PublishSubject<Hotel>, headerSubject: PublishSubject<Unit>) :
        BaseHotelListAdapter(hotelSelectedSubject, headerSubject) {

    override fun getHotelCellViewModel(context: Context, hotel: Hotel) : HotelViewModel {
        return HotelViewModel(context, hotel)
    }

    override fun getHotelCellHolder(parent: ViewGroup): HotelCellViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.new_hotel_cell, parent, false)
        return HotelCellViewHolder(view as ViewGroup, parent.width)
    }

    override fun isBucketedForResultMap(): Boolean {
        return Db.getAbacusResponse().isUserBucketedForTest(AbacusUtils.EBAndroidAppHotelResultMapTest)
    }
}