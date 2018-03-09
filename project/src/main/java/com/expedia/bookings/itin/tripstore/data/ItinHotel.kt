package com.expedia.bookings.itin.tripstore.data

import com.google.gson.annotations.SerializedName

data class ItinHotel(
        val uniqueID: String?,
        val paymentModel: PaymentModel?,
        val totalPriceDetails: TotalPriceDetails?,
        val hotelPropertyInfo: HotelPropertyInfo?
)

enum class PaymentModel {
    @SerializedName("HOTEL_COLLECT")
    HOTEL_COLLECT,
    @SerializedName("EXPEDIA_COLLECT")
    EXPEDIA_COLLECT
}

data class TotalPriceDetails(
        val totalFormatted: String?
)

data class HotelPropertyInfo(
        val name: String?
)
