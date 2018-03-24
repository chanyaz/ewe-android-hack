package com.expedia.bookings.itin.tripstore.data

import com.google.gson.annotations.SerializedName

data class ItinHotel(
        val uniqueID: String?,
        val paymentModel: PaymentModel?,
        val totalPriceDetails: TotalPriceDetails?,
        val hotelPropertyInfo: HotelPropertyInfo?,
        val rooms: List<HotelRoom>?
)

enum class PaymentModel {
    @SerializedName("HOTEL_COLLECT")
    HOTEL_COLLECT,
    @SerializedName("EXPEDIA_COLLECT")
    EXPEDIA_COLLECT
}

data class HotelRoom(
        val totalPriceDetails: TotalPriceDetails?
)

data class TotalPriceDetails(
        val totalFormatted: String?,
        val priceDetailsPerDay: List<HotelRoomPriceDetails>?
)

data class HotelPropertyInfo(
        val name: String?
)

data class HotelRoomPriceDetails(val amountFormatted: String?, val localizedDay: LocalizedDay?) {
        data class LocalizedDay(val localizedFullDate: String?)
}
