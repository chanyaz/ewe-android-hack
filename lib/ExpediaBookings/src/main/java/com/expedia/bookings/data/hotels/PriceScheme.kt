package com.expedia.bookings.data.hotels

enum class PriceType {
    TOTAL,
    PER_NIGHT
}

data class PriceScheme(val priceType: PriceType, val taxIncluded: Boolean, val feeIncluded: Boolean) {
    fun convertToUserPriceType(): HotelRate.UserPriceType {
        return when (priceType) {
            PriceType.TOTAL ->
                HotelRate.UserPriceType.RATE_FOR_WHOLE_STAY_WITH_TAXES
            PriceType.PER_NIGHT ->
                HotelRate.UserPriceType.PER_NIGHT_RATE_NO_TAXES
        }
    }

    fun convertToUserPriceTypeString(): String {
        return when (priceType) {
            PriceType.TOTAL ->
                "RateForWholeStayWithTaxes"
            PriceType.PER_NIGHT ->
                "PerNightRateNoTaxes"
        }
    }
}
