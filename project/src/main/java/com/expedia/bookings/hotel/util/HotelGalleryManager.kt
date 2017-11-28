package com.expedia.bookings.hotel.util

import com.expedia.bookings.R
import com.expedia.bookings.data.HotelMedia
import com.expedia.bookings.data.hotels.HotelOffersResponse
import com.expedia.bookings.utils.Images
import java.util.ArrayList
import com.expedia.bookings.hotel.DEFAULT_HOTEL_GALLERY_CODE
import com.expedia.bookings.utils.Images.getMediaHost

open class HotelGalleryManager() {

    private val overviewImages = ArrayList<HotelMedia>()
    private val roomImages = HashMap<String, ArrayList<HotelMedia>>()

    fun saveHotelOfferMedia(response: HotelOffersResponse) {
        val images = Images.getHotelImages(response, R.drawable.room_fallback)
        saveOverviewImages(images)
        response.hotelRoomResponse?.let { saveRoomImages(response.hotelRoomResponse) }
    }

    fun fetchMediaList(roomCode: String) : ArrayList<HotelMedia> {
        if (roomCode == DEFAULT_HOTEL_GALLERY_CODE) {
            return overviewImages
        }
        return roomImages[roomCode] ?: ArrayList<HotelMedia>()
    }

    private fun saveOverviewImages(images: List<HotelMedia>) {
        overviewImages.clear()
        overviewImages.addAll(images)
    }

    private fun saveRoomImages(rooms: List<HotelOffersResponse.HotelRoomResponse>) {
        roomImages.clear()
        for (i  in rooms.indices) {
            val room = rooms[i]
            val code = room.roomGroupingKey()
            room.roomThumbnailUrlArray?.forEach { url ->
                if (roomImages[code] == null) {
                    roomImages.put(code, ArrayList<HotelMedia>())
                }
                roomImages[code]!!.add(HotelMedia(getMediaHost() + url))
            }
        }
    }
}