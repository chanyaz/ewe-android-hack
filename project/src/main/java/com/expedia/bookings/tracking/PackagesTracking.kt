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

    fun trackFlightRoundTripLoad(isOutBound: Boolean) {
        if (isOutBound)
            OmnitureTracking.trackPackagesFlightRoundTripOutLoad()
        else
            OmnitureTracking.trackPackagesFlightRoundTripInLoad()
    }

    fun trackFlightRoundTripDetailsLoad(isOutBound: Boolean) {
        if (isOutBound)
            OmnitureTracking.trackPackagesFlightRoundTripOutDetailsLoad()
        else
            OmnitureTracking.trackPackagesFlightRoundTripInDetailsLoad()
    }

    fun trackHotelDetailBookPhoneClick() {
        OmnitureTracking.trackPackagesHotelInfoActionBookPhone()
    }

    fun trackHotelDetailSelectRoomClick(stickyButton: Boolean) {
        OmnitureTracking.trackPackagesHotelInfoActionSelectRoom(stickyButton)
    }

    fun trackHotelReviewPageLoad() {
        OmnitureTracking.trackPackagesHotelReviewPageLoad()
    }

    fun trackHotelReviewCategoryChange(tabSelected: Int) {
        val category = ( when (tabSelected) {
            0 -> "Recent"
            1 -> "Favorable"
            2 -> "Critical"
            else -> "N/A"
        })
        OmnitureTracking.trackPackagesHotelReviewCategoryChange(category)
    }

    fun trackHotelResortFeeInfoClick() {
        OmnitureTracking.trackPackagesHotelResortFeeInfo()
    }

    fun trackHotelRenovationInfoClick() {
        OmnitureTracking.trackPackagesHotelRenovationInfo()
    }
}