package com.expedia.bookings.data.packages

import com.expedia.bookings.data.multiitem.BundleSearchResponse
import org.joda.time.LocalDate

class MultiItemCreateTripParams(val flightPIID: String,
                                val hotelID: String,
                                val inventoryType: String,
                                val ratePlanCode: String,
                                val roomTypeCode: String,
                                val totalPrice: PackageOfferModel.PackagePrice,
                                val startDate: String,
                                val endDate: LocalDate,
                                val adults: Int,
                                val childAges: String?,
                                val infantsInSeats: Boolean?) {
    companion object {
        fun fromPackageSearchParamsAndLatestPackageResponse(searchParams: PackageSearchParams, packageResponse: BundleSearchResponse): MultiItemCreateTripParams {
            return MultiItemCreateTripParams(
                    searchParams.latestSelectedOfferInfo.flightPIID ?: throw IllegalArgumentException(),
                    searchParams.latestSelectedOfferInfo.hotelId ?: throw IllegalArgumentException(),
                    searchParams.latestSelectedOfferInfo.inventoryType ?: throw IllegalArgumentException(),
                    packageResponse.getRatePlanCode() ?: throw IllegalArgumentException(),
                    packageResponse.getRoomTypeCode() ?: throw IllegalArgumentException(),
                    searchParams.latestSelectedOfferInfo.productOfferPrice ?: throw IllegalArgumentException(),
                    packageResponse.getHotelCheckInDate(),
                    searchParams.endDate ?: throw IllegalArgumentException(),
                    searchParams.adults,
                    searchParams.childAges,
                    searchParams.infantsInSeats)
        }
    }
}
