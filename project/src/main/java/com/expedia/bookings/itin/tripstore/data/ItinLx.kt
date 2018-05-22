package com.expedia.bookings.itin.tripstore.data

data class ItinLx(
        val uniqueID: String?,
        val activityLocation: ActivityLocation?,
        val activityId: String?,
        val travelerCount: String?,
        val price: LxPrice?,
        val voucherPrintURL: String?,
        val lxVoucherPrintURL: String?,
        val vendorCustomerServiceOffices: List<VendorCustomerServiceOffice>?
)

data class ActivityLocation(
        val city: String?,
        val addressLine1: String?,
        val postalCode: String?,
        val countryCode: String?,
        val countrySubdivisionCode: String?,
        val latitude: Double?,
        val longitude: Double?
)

data class LxPrice(
        val base: String?
)

data class VendorCustomerServiceOffice(
        val phoneNumber: String?
)
