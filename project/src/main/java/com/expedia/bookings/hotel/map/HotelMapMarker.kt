package com.expedia.bookings.hotel.map

import android.content.Context
import com.expedia.bookings.data.hotels.Hotel
import com.expedia.bookings.data.hotels.HotelRate
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.clustering.ClusterItem

class HotelMapMarker(val context: Context, val pos: LatLng, val hotel: Hotel,
                     val hotelIconGenerator: HotelMarkerIconGenerator) : ClusterItem {

    var isSelected = false
    var isClustered = false

    val price: HotelRate? by lazy {
        if (hotel.isSoldOut) null else hotel.lowRateInfo
    }

    var wasSelected = isSelected
    var wasSoldOut = hotel.isSoldOut

    var cacheIcon: BitmapDescriptor? = null

    fun getHotelMarkerIcon(): BitmapDescriptor? {
        if (wasSelected == isSelected && wasSoldOut == hotel.isSoldOut) {
            if (cacheIcon == null) {
                cacheIcon = hotelIconGenerator.createHotelMarkerIcon(context, hotel, isSelected)
            }
        } else {
            cacheIcon = hotelIconGenerator.createHotelMarkerIcon(context, hotel, isSelected)
            wasSelected = isSelected
            wasSoldOut = hotel.isSoldOut
        }
        return cacheIcon
    }

    override fun getPosition(): LatLng? {
        return pos
    }
}
