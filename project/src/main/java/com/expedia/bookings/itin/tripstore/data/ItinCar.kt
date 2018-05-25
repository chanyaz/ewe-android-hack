package com.expedia.bookings.itin.tripstore.data

data class ItinCar(
        val uniqueID: String?,
        val carCategoryImageURL: String?,
        val carVendor: CarVendor?,
        val pickupTime: ItinTime?,
        val dropOffTime: ItinTime?,
        val dropOffLocation: CarLocation?,
        val pickupLocation: CarLocation?
) : ItinLOB

data class CarVendor(
        val longName: String?,
        val localPhoneNumber: String?
)

data class CarLocation(
        val cityName: String?,
        val provinceStateName: String?,
        val addressLine1: String?,
        val postalCode: String?,
        val countryCode: String?,
        val latitude: Double?,
        val longitude: Double?
)
