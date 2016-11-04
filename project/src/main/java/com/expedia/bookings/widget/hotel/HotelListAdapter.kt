package com.expedia.bookings.widget.hotel

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.expedia.bookings.R
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

        val bucketedAndFeatureEnabledForFavoriteTest = FeatureToggleUtil.isUserBucketedAndFeatureEnabled(parent.context, AbacusUtils.EBAndroidAppHotelFavoriteTest,
                R.string.preference_enable_hotel_favorite)
        val view = LayoutInflater.from(parent.context).inflate(R.layout.hotel_cell, parent, false)
        val heart_cell = view.findViewById(R.id.hotel_cell_heart_container)
        heart_cell.visibility = if (bucketedAndFeatureEnabledForFavoriteTest) View.VISIBLE else View.GONE
        return HotelCellViewHolder(view as ViewGroup, parent.width)
    }

}