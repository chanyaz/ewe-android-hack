package com.expedia.bookings.hotel.data

sealed class HotelAdapterItem(val key: Int) {
    class TransparentMapView() : HotelAdapterItem(ItemKey.TRANSPARENT_MAPVIEW)
    class Header() : HotelAdapterItem(ItemKey.HEADER)
    class Hotel(val hotel: com.expedia.bookings.data.hotels.Hotel) : HotelAdapterItem(ItemKey.HOTEL)
    class Urgency(val compressionMessage: String) : HotelAdapterItem(ItemKey.URGENCY)
    class Spacer() : HotelAdapterItem(ItemKey.SPACER)
    class Loading() : HotelAdapterItem(ItemKey.LOADING)
    class Earn2x() : HotelAdapterItem(ItemKey.EARN_2X)

    companion object ItemKey {
        val TRANSPARENT_MAPVIEW = 0
        val HEADER = 1
        val HOTEL = 2
        val URGENCY = 3
        val SPACER = 4
        val LOADING = 5
        val EARN_2X = 6
    }
}
