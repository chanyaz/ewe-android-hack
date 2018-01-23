package com.expedia.vm

import android.content.Context
import android.support.v4.content.ContextCompat
import android.text.SpannableString
import android.text.Spanned
import android.text.style.AbsoluteSizeSpan
import android.text.style.ForegroundColorSpan
import com.expedia.bookings.R
import com.expedia.bookings.data.hotels.HotelOffersResponse
import com.expedia.bookings.utils.Images

class HotelRoomHeaderViewModel(val context: Context, val hotelRoomResponse: HotelOffersResponse.HotelRoomResponse, val roomCount: Int) {

    val imageUrl: String? by lazy { getHotelImageUrl() }
    val roomTypeString by lazy { createRoomTypeString() }

    val bedTypeString by lazy {
        (hotelRoomResponse.bedTypes ?: emptyList()).map { bedType ->
            bedType.description
        }.joinToString(context.resources.getString(R.string.delimiter_multiple_bed))
    }

    val roomDescriptionString: String? by lazy { hotelRoomResponse.roomLongDescription }

    fun hasRoomImages(): Boolean {
        return hotelRoomResponse.roomThumbnailUrlArray != null
                && hotelRoomResponse.roomThumbnailUrlArray!!.isNotEmpty()
    }

    private fun getHotelImageUrl(): String? {
        return if (!hotelRoomResponse.roomThumbnailUrl.isNullOrBlank()) {
            Images.getMediaHost() + hotelRoomResponse.roomThumbnailUrl
        } else null
    }

    private fun createRoomTypeString(): CharSequence {
        var detailString = hotelRoomResponse.roomTypeDescriptionDetail
        val roomTypeDescription = hotelRoomResponse.roomTypeDescriptionWithoutDetail
        if (roomCount < 0 && !detailString.isNullOrBlank()) {
            detailString = "  (" + detailString + ")"

            val smallTextSize = context.resources.getDimensionPixelSize(R.dimen.type_100_text_size)
            val span = SpannableString(roomTypeDescription + detailString)

            span.setSpan(AbsoluteSizeSpan(smallTextSize), roomTypeDescription.length, roomTypeDescription.length + detailString.length, Spanned.SPAN_INCLUSIVE_INCLUSIVE)
            span.setSpan(ForegroundColorSpan(ContextCompat.getColor(context, R.color.light_text_color)), roomTypeDescription.length, roomTypeDescription.length + detailString.length, Spanned.SPAN_INCLUSIVE_INCLUSIVE)

            return span
        }

        return roomTypeDescription
    }
}
