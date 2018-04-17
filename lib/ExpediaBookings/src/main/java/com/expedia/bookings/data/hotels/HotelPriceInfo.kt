package com.expedia.bookings.data.hotels

data class HotelPriceInfo(
        val displayPrice: Float?,
        val strikeThroughPrice: Float?,
        val discountPercentage: Float?,
        val currencySymbol: String?,
        val currencyCode: String?,
        val pricingScheme: PriceScheme?) {

    fun convertToHotelRate(): HotelRate {
        val hotelRate = HotelRate()

        displayPrice?.let { displayPrice ->
            hotelRate.averageRate = displayPrice
            hotelRate.total = displayPrice
            hotelRate.priceToShowUsers = displayPrice
            hotelRate.totalPriceWithMandatoryFees = displayPrice
        }

        strikeThroughPrice?.let { strikeThroughPrice ->
            hotelRate.strikethroughPriceToShowUsers = strikeThroughPrice
        }

        discountPercentage?.let { discountPercentage ->
            hotelRate.discountPercent = discountPercentage
        }

        currencySymbol?.let { currencySymbol ->
            hotelRate.currencySymbol = currencySymbol
        }

        currencyCode?.let { currencyCode ->
            hotelRate.currencyCode = currencyCode
        }

        pricingScheme?.let { pricingScheme ->
            hotelRate.userPriceType = pricingScheme.convertToUserPriceTypeString()
            hotelRate.checkoutPriceType = pricingScheme.convertToUserPriceTypeString()
            hotelRate.resortFeeInclusion = pricingScheme.feeIncluded
        }

        return hotelRate
    }
}
