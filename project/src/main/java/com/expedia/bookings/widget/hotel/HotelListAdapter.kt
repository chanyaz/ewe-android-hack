package com.expedia.bookings.widget.hotel

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.expedia.bookings.R
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.HotelFavoriteHelper
import com.expedia.bookings.data.abacus.AbacusUtils
import com.expedia.bookings.data.hotels.Hotel
import com.expedia.bookings.widget.BaseHotelListAdapter
import rx.subjects.PublishSubject

class HotelListAdapter(hotelSelectedSubject: PublishSubject<Hotel>, headerSubject: PublishSubject<Unit>, pricingHeaderSelectedSubject: PublishSubject<Unit>) :
        BaseHotelListAdapter(hotelSelectedSubject, headerSubject, pricingHeaderSelectedSubject) {

    override fun getHotelCellHolder(parent: ViewGroup): HotelCellViewHolder {
        val bucketedForFavoriteTest = HotelFavoriteHelper.showHotelFavoriteTest(true)

        var view: View
        val variateForTest = Db.getAbacusResponse().variateForTest(AbacusUtils.EBAndroidAppHotelResultsCardReadability)
        if (variateForTest == AbacusUtils.DefaultTwoVariant.VARIANT1.ordinal) {
            view = LayoutInflater.from(parent.context).inflate(R.layout.hotel_cell_content_below_image, parent, false)
        } else if (variateForTest == AbacusUtils.DefaultTwoVariant.VARIANT2.ordinal) {
            view = LayoutInflater.from(parent.context).inflate(R.layout.hotel_cell_content_right_of_image, parent, false)
        } else {
            view = LayoutInflater.from(parent.context).inflate(R.layout.hotel_cell, parent, false)
        }

        val heart_cell = view.findViewById(R.id.hotel_cell_heart_container)
        heart_cell.visibility = if (bucketedForFavoriteTest) View.VISIBLE else View.GONE
        val holder = HotelCellViewHolder(view as ViewGroup, parent.width, hotelFavoriteChange)
        return holder
    }

}