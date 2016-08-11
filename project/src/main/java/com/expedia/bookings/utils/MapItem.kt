package com.expedia.bookings.utils

import android.content.Context
import com.expedia.bookings.data.hotels.Hotel
import com.expedia.bookings.data.hotels.HotelRate
import com.expedia.bookings.widget.createHotelMarkerIcon
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.clustering.ClusterItem
import com.google.maps.android.ui.IconGenerator

class MapItem(val context: Context, val pos: LatLng, val title: String, val icon: BitmapDescriptor, val iconGenerator: IconGenerator, val hotel: Hotel) : ClusterItem {

    var isSelected = false
    val isFavorite = false
    var isClustered = false
    val price: HotelRate? by lazy {
        if (hotel.isSoldOut) null else hotel.lowRateInfo
    }

    val soldOutIcon: BitmapDescriptor by lazy {
        createHotelMarkerIcon(context, iconGenerator, hotel, true)
    }

    val selectedIcon: BitmapDescriptor by lazy {
        createHotelMarkerIcon(context, iconGenerator, hotel, true)
    }

    override fun getPosition(): LatLng? {
        return pos
    }
}