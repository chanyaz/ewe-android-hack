package com.expedia.bookings.tracking

import com.expedia.bookings.data.packages.PackageCreateTripResponse
import com.expedia.bookings.data.packages.PackageSearchResponse

class PackagesTracking {

    fun trackCheckoutStart(packageDetails:PackageCreateTripResponse.PackageDetails) {
        OmnitureTracking.trackPackagesCheckoutStart(packageDetails)
    }

    fun trackDestinationSearchInit() {
        OmnitureTracking.trackPackagesDestinationSearchInit()
    }

    fun trackHotelSearchResultLoad(response:PackageSearchResponse) {
        OmnitureTracking.trackPackagesHSRLoad(response)
    }

    fun trackHotelMapLoad() {
        OmnitureTracking.trackPackagesHSRMapInit()
    }

    fun trackHotelMapToList() {
        OmnitureTracking.trackPackagesHotelMapToList()
    }

    fun trackHotelMapPinTap() {
        OmnitureTracking.trackPackagesHotelMapPinTap()
    }

    fun trackHotelMapCarouselPropertyClick() {
        OmnitureTracking.trackPackagesHotelMapCarouselPropertyClick()
    }

    fun trackHotelMapCarouselScroll() {
        OmnitureTracking.trackPackagesHotelMapCarouselScroll()
    }

    fun trackHotelMapSearchThisAreaClick() {
        OmnitureTracking.trackPackagesHotelMapSearchThisAreaClick()
    }

    fun trackCheckoutSelectPaymentClick() {
        OmnitureTracking.trackPackagesPaymentSelect()
    }

    fun trackCheckoutPaymentSelectStoredCard() {
        OmnitureTracking.trackPackagesPaymentStoredCCSelect()
    }

    fun trackCheckoutAddPaymentType() {
        OmnitureTracking.trackPackagesPaymentEdit()
    }

    fun trackCheckoutPaymentConfirmation() {
        OmnitureTracking.trackPackagesConfirmation()
    }
}