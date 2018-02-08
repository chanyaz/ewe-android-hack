package com.expedia.bookings.data.packages

import org.joda.time.LocalDate

class MultiItemCreateTripParams(val flightPIID: String,
                                val hotelID: String,
                                val inventoryType: String,
                                val ratePlanCode: String,
                                val roomTypeCode: String,
                                val totalPrice: PackageOfferModel.PackagePrice,
                                val startDate: LocalDate,
                                val endDate: LocalDate,
                                val adults: Int,
                                val childAges: String?,
                                val infantsInSeats: Boolean?) {
    companion object {
        fun fromPackageSearchParams(searchParams: PackageSearchParams): MultiItemCreateTripParams {
            return MultiItemCreateTripParams(
                    searchParams.latestSelectedOfferInfo.flightPIID ?: throw IllegalArgumentException(),
                    searchParams.latestSelectedOfferInfo.hotelId ?: throw IllegalArgumentException(),
                    searchParams.latestSelectedOfferInfo.inventoryType ?: throw IllegalArgumentException(),
                    searchParams.latestSelectedOfferInfo.ratePlanCode ?: throw IllegalArgumentException(),
                    searchParams.latestSelectedOfferInfo.roomTypeCode ?: throw IllegalArgumentException(),
                    searchParams.latestSelectedOfferInfo.productOfferPrice ?: throw IllegalArgumentException(),
                    searchParams.startDate,
                    searchParams.endDate ?: throw IllegalArgumentException(),
                    searchParams.adults,
                    searchParams.childAges,
                    searchParams.infantsInSeats)
        }
    }
}
