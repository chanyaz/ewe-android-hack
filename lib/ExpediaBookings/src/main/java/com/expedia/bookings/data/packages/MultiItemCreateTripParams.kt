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
            return MultiItemCreateTripParams(searchParams.latestSelectedFlightPIID!!,
                    searchParams.hotelId ?: throw IllegalArgumentException() ,
                    searchParams.inventoryType ?: throw IllegalArgumentException(),
                    searchParams.ratePlanCode ?: throw IllegalArgumentException(),
                    searchParams.roomTypeCode ?: throw IllegalArgumentException(),
                    searchParams.latestSelectedProductOfferPrice ?: throw IllegalArgumentException(),
                    searchParams.startDate,
                    searchParams.endDate ?: throw IllegalArgumentException(),
                    searchParams.adults,
                    searchParams.childAges,
                    searchParams.infantsInSeats)
        }
    }
}
