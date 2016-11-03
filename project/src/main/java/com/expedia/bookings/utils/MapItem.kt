package com.expedia.bookings.utils

import android.content.Context
import com.expedia.bookings.data.HotelFavoriteHelper
import com.expedia.bookings.data.hotels.Hotel
import com.expedia.bookings.data.hotels.HotelRate
import com.expedia.bookings.widget.HotelMarkerIconGenerator
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.clustering.ClusterItem

class MapItem(val context: Context, val pos: LatLng, val hotel: Hotel, val hotelIconGenerator: HotelMarkerIconGenerator, isFavoritingSupported: Boolean) : ClusterItem {

    var isSelected = false
    var isClustered = false
    var isFavorite: Boolean = HotelFavoriteHelper.isHotelFavorite(context, hotel.hotelId, isFavoritingSupported)

    val price: HotelRate? by lazy {
        if (hotel.isSoldOut) null else hotel.lowRateInfo
    }

    var wasSelected = isSelected
    var wasFavorite = isFavorite
    var wasSoldOut = hotel.isSoldOut

    var cacheIcon: BitmapDescriptor? = null

    fun getHotelMarkerIcon(): BitmapDescriptor? {
        if (wasSelected == isSelected && wasFavorite == isFavorite && wasSoldOut == hotel.isSoldOut) {
            if (cacheIcon == null) {
                cacheIcon = hotelIconGenerator.createHotelMarkerIcon(context, hotel, isSelected, isFavorite)
            }
        } else {
            cacheIcon = hotelIconGenerator.createHotelMarkerIcon(context, hotel, isSelected, isFavorite)
            wasSelected = isSelected
            wasFavorite = isFavorite
            wasSoldOut = hotel.isSoldOut
        }
        return cacheIcon
    }

    override fun getPosition(): LatLng? {
        return pos
    }

}