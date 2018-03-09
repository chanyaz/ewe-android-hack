package com.expedia.bookings.mia.vm

import android.content.Context
import com.expedia.bookings.data.sos.DealsDestination
import com.expedia.bookings.enums.DiscountColors

class LastMinuteDealsCardViewModel(context: Context, leadingHotel: DealsDestination.Hotel, currency: String?) : BaseDealsCardViewModel(context, leadingHotel, currency) {

    override val numberOfTravelers = 2

    override val title =
            if (leadingHotel.hotelInfo?.localizedHotelName.isNullOrEmpty())
                leadingHotel.hotelInfo?.hotelName
            else leadingHotel.hotelInfo?.localizedHotelName

    override val subtitle = cityName

    override val hotelId = leadingHotel.hotelInfo?.hotelId

    override val prioritizedBackgroundImageUrls = listOf(getLeadingHotelBackgroundImageUrl(), getDestinationBackgroundImageUrl())

    override val discountColors = DiscountColors.LAST_MINUTE_DEALS

    private fun getLeadingHotelBackgroundImageUrl(): String? {
        val hotelImageUrl = leadingHotel.hotelInfo?.hotelImageUrl
        if (hotelImageUrl != null) {
            if (hotelImageUrl.contains("_l.jpg")) {
                return leadingHotel.hotelInfo?.hotelImageUrl?.replace("_l.jpg", "_z.jpg", true)
            } else return leadingHotel.hotelInfo?.hotelImageUrl?.replace(".jpg", "_z.jpg")
        } else return null
    }
}
