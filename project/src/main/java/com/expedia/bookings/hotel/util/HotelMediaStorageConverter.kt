package com.expedia.bookings.hotel.util

import com.expedia.bookings.data.HotelMedia
import com.expedia.bookings.hotel.DEFAULT_HOTEL_GALLERY_CODE
import com.expedia.bookings.hotel.data.PersistableHotelImageInfo
import java.util.ArrayList

class HotelMediaStorageConverter {
    fun toPersistable(hotelMedia: HotelMedia, roomCode: String) : PersistableHotelImageInfo {
        return PersistableHotelImageInfo(hotelMedia.originalUrl, hotelMedia.mDescription, roomCode,
                isRoomImage = roomCode != DEFAULT_HOTEL_GALLERY_CODE)    }

    fun toHotelMediaList(persistableList: List<PersistableHotelImageInfo>) : ArrayList<HotelMedia> {
        return persistableList.mapTo(ArrayList()) { persistableInfo ->
            HotelMedia(persistableInfo.url, persistableInfo.displayText)
        }
    }

    fun toPersistableList(mediaList: List<HotelMedia>, roomCode: String) : ArrayList<PersistableHotelImageInfo> {
        return mediaList.mapTo(ArrayList()) { hotelMedia ->
            toPersistable(hotelMedia, roomCode)
        }
    }
}