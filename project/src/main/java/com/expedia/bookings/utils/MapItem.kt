package com.expedia.bookings.utils

import android.content.Context
import com.expedia.bookings.data.hotels.Hotel
import com.expedia.bookings.data.hotels.HotelRate
import com.expedia.bookings.extension.isShowAirAttached
import com.expedia.bookings.widget.createHotelMarkerIcon
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.clustering.ClusterItem
import com.google.maps.android.ui.IconGenerator

class MapItem(val context: Context, val pos: LatLng, val title: String, val icon: BitmapDescriptor, val iconGenerator: IconGenerator, val price: HotelRate, val hotel: Hotel) : ClusterItem {

    var isSelected = false
    var isClustered = false
    val soldOutIcon: BitmapDescriptor by lazy {
        createHotelMarkerIcon(context, iconGenerator, hotel, true, hotel.lowRateInfo.isShowAirAttached(), true)
    }

    val selectedIcon: BitmapDescriptor by lazy {
        createHotelMarkerIcon(context, iconGenerator, hotel, true, hotel.lowRateInfo.isShowAirAttached(), hotel.isSoldOut)
    }

    override fun getPosition(): LatLng? {
        return pos
    }
}