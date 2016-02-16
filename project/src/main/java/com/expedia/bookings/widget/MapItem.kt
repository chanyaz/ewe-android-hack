package com.expedia.bookings.widget

import com.expedia.bookings.data.hotels.Hotel
import com.expedia.bookings.data.hotels.HotelRate
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.clustering.ClusterItem

class MapItem(val pos: LatLng, val title: String, val icon: BitmapDescriptor, val selectedIcon: BitmapDescriptor, val price: HotelRate, val hotel: Hotel) : ClusterItem {

    var isSelected = false
    var isClustered = false

    override fun getPosition(): LatLng? {
        return pos
    }
}