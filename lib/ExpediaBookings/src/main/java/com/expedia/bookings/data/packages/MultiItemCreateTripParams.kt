package com.expedia.bookings.data.packages;

import org.joda.time.LocalDate

class MultiItemCreateTripParams(val flightPIID: String, val hotelID: String, val inventoryType: String,
                                val ratePlanCode: String, val roomTypeCode: String, val totalPrice: PackageOfferModel.PackagePrice,
                                val startDate: LocalDate, val endDate: LocalDate, val adults: Int) {
    companion object {
        fun fromPackageSearchParams(
                searchParams: PackageSearchParams): MultiItemCreateTripParams {
            return MultiItemCreateTripParams(searchParams.latestSelectedFlightPIID!!, searchParams.hotelId!!, searchParams.inventoryType!!, searchParams.ratePlanCode!!, searchParams.roomTypeCode!!, searchParams.latestSelectedProductOfferPrice!!, searchParams.startDate, searchParams.endDate!!, searchParams.adults)
        }
    }

}
