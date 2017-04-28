package com.expedia.vm

import android.content.Context
import com.expedia.bookings.R
import com.expedia.bookings.data.hotels.HotelOffersResponse
import com.expedia.bookings.utils.Images

class HotelRoomHeaderViewModel(val context: Context, val hotelRoomResponse: HotelOffersResponse.HotelRoomResponse) {

    val imageUrl: String?
        get() = if (!hotelRoomResponse.roomThumbnailUrl.isNullOrBlank()) {
            Images.getMediaHost() + hotelRoomResponse.roomThumbnailUrl
        } else null

    val roomTypeString: String get() = createRoomTypeString()

    val bedTypeString: String
        get() = (hotelRoomResponse.bedTypes ?: emptyList()).map {
            it.description
        }.joinToString(context.resources.getString(R.string.delimiter_multiple_bed))

    private fun createRoomTypeString(): String {
        var trimmedRoomTypeString = hotelRoomResponse.roomTypeDescription
        val dashIndex = trimmedRoomTypeString.indexOf(" - ")
        if (dashIndex != -1) {
            trimmedRoomTypeString = trimmedRoomTypeString.substring(0, dashIndex)
        }
        return trimmedRoomTypeString
    }
}
