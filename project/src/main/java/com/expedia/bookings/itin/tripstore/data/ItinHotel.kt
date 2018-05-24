package com.expedia.bookings.itin.tripstore.data

import com.google.gson.annotations.SerializedName

data class ItinHotel(
        val uniqueID: String?,
        val hotelId: String?,
        val paymentModel: PaymentModel?,
        val totalPriceDetails: TotalPriceDetails?,
        val rules: Rules?,
        val hotelPropertyInfo: HotelPropertyInfo?,
        val rooms: List<HotelRoom>?,
        val numberOfNights: String?,
        val localizedHotelPropertyInfo: HotelPropertyInfo?,
        val paymentsAndCreditFees: PaymentsAndCreditFees?
)

enum class PaymentModel {
    @SerializedName("HOTEL_COLLECT")
    HOTEL_COLLECT,
    @SerializedName("EXPEDIA_COLLECT")
    EXPEDIA_COLLECT
}

data class HotelRoom(
        val totalPriceDetails: TotalPriceDetails?,
        val roomPropertyFeeFormatted: String?,
        val bookingStatus: BookingStatus?
)

enum class BookingStatus {
    @SerializedName("BOOKED")
    BOOKED,
    @SerializedName("CANCELLED")
    CANCELLED
}

data class TotalPriceDetails(
        val primaryCurrencyCode: String?,
        val totalPOSCurrencyCode: String?,
        val totalFormatted: String?,
        val priceDetailsPerDay: List<HotelRoomPriceDetails>?,
        val base: String?,
        val extraGuestChargesFormatted: String?,
        val taxesAndFeesFormatted: String?,
        val adjustmentForCouponFormatted: String?,
        val totalPOSFormatted: String?
)

data class HotelPropertyInfo(
        val name: String?,
        val address: Address,
        var localizationLanguage: String?,
        val fees: List<String>?,
        val mandatoryFees: List<String>?
)

data class HotelRoomPriceDetails(val amountFormatted: String?, val localizedDay: LocalizedDay?) {
    data class LocalizedDay(val localizedFullDate: String?)
}

data class Rules(
        val currencyDisclaimer: String?,
        val dualCurrencyText: String?,
        val occupancyPolicies: List<String>?,
        val extraGuestPolicies: List<String>,
        val taxesAndFeesInfo: String?
)

data class Address(
        val fullAddress: String?
)

data class PaymentsAndCreditFees(
        val paymentsHotelFeesAndDepositsDisclaimer: String?,
        val noFeesStaticText: String?
)
