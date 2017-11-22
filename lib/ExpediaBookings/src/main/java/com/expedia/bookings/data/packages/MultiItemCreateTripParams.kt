package com.expedia.bookings.data.packages

class MultiItemCreateTripParams(val flightPIID: String,
                                val hotelID: String,
                                val inventoryType: String,
                                val ratePlanCode: String,
                                val roomTypeCode: String,
                                val totalPrice: PackageOfferModel.PackagePrice,
                                val startDate: String,
                                val endDate: String,
                                val adults: List<Int>,
                                val childAges: List<List<Int>>,
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
                    searchParams.latestSelectedOfferInfo.hotelCheckInDate ?: throw IllegalArgumentException(),
                    searchParams.latestSelectedOfferInfo.hotelCheckOutDate ?: throw IllegalArgumentException(),
                    searchParams.adultsList,
                    searchParams.childrenList,
                    searchParams.infantsInSeats)
        }
    }
}
