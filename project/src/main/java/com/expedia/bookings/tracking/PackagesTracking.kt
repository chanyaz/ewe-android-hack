package com.expedia.bookings.tracking

import com.expedia.bookings.data.packages.PackageCreateTripResponse

class PackagesTracking {

    fun trackCheckoutStart(packageDetails:PackageCreateTripResponse.PackageDetails) {
        OmnitureTracking.trackPackagesCheckoutStart(packageDetails)
    }

}