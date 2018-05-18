package com.expedia.bookings.hotel.widget.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import com.expedia.bookings.R
import com.expedia.bookings.data.abacus.AbacusUtils
import com.expedia.bookings.data.hotels.Hotel
import com.expedia.bookings.featureconfig.AbacusFeatureConfigManager
import com.expedia.bookings.widget.BaseHotelListAdapter
import com.expedia.bookings.hotel.widget.HotelCellViewHolder
import io.reactivex.subjects.PublishSubject

class HotelListAdapter(hotelSelectedSubject: PublishSubject<Hotel>, headerSubject: PublishSubject<Unit>, pricingHeaderSelectedSubject: PublishSubject<Unit>, canShow2xMessaging: Boolean = false) :
        BaseHotelListAdapter(hotelSelectedSubject, headerSubject, pricingHeaderSelectedSubject, canShow2xMessaging) {

    override fun getHotelCellHolder(parent: ViewGroup): HotelCellViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.hotel_cell, parent, false)
        val holder = HotelCellViewHolder(view as ViewGroup)

        return holder
    }

    override fun getPriceDescriptorMessageIdForHSR(context: Context): Int? {
        return null
    }

    override fun getHeaderTopPadding(context: Context, currentPadding: Int): Int {
        if (AbacusFeatureConfigManager.isBucketedForTest(context, AbacusUtils.HotelResultChangeDate) &&
                AbacusFeatureConfigManager.isBucketedForTest(context, AbacusUtils.HotelHideMiniMapOnResult)) {
            return context.resources.getDimensionPixelSize(R.dimen.hotel_results_change_date_height) + currentPadding
        }
        return currentPadding
    }
}
