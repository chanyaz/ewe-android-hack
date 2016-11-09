package com.expedia.bookings.widget.hotel

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.expedia.bookings.R
import com.expedia.bookings.data.HotelFavoriteHelper
import com.expedia.bookings.data.abacus.AbacusUtils
import com.expedia.bookings.data.hotels.Hotel
import com.expedia.bookings.utils.FeatureToggleUtil
import com.expedia.bookings.widget.BaseHotelListAdapter
import com.expedia.vm.hotel.HotelViewModel
import rx.subjects.PublishSubject

class HotelListAdapter(hotelSelectedSubject: PublishSubject<Hotel>, headerSubject: PublishSubject<Unit>) :
        BaseHotelListAdapter(hotelSelectedSubject, headerSubject) {

    override fun getHotelCellViewModel(context: Context, hotel: Hotel) : HotelViewModel {
        return HotelViewModel(context, hotel)
    }

    override fun getHotelCellHolder(parent: ViewGroup): HotelCellViewHolder {
        val bucketedForFavoriteTest = HotelFavoriteHelper.showHotelFavoriteTest(true)
        val view = LayoutInflater.from(parent.context).inflate(R.layout.hotel_cell, parent, false)
        val heart_cell = view.findViewById(R.id.hotel_cell_heart_container)
        heart_cell.visibility = if (bucketedForFavoriteTest) View.VISIBLE else View.GONE
        val holder = HotelCellViewHolder(view as ViewGroup, parent.width, hotelFavoriteChange)
        return holder
    }

}