package com.expedia.bookings.tracking

import com.expedia.bookings.data.ApiError
import com.expedia.bookings.data.FlightFilter
import com.expedia.bookings.data.packages.PackageCheckoutResponse
import com.expedia.bookings.data.packages.PackageCreateTripResponse
import com.expedia.bookings.data.packages.PackageSearchResponse
import com.expedia.bookings.tracking.hotel.PageUsableData
import com.expedia.vm.BaseFlightFilterViewModel

class PackagesTracking {

    fun trackCheckoutStart(packageDetails:PackageCreateTripResponse.PackageDetails, hotelSupplierType: String) {
        OmnitureTracking.trackPackagesCheckoutStart(packageDetails, hotelSupplierType)
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

    fun trackHotelFilterLoad() {
        OmnitureTracking.trackPackagesHotelFilterPageLoad()
    }

    fun trackHotelSortBy(type: String) {
        OmnitureTracking.trackPackagesHotelSortBy(type)
    }

    fun trackHotelFilterPriceSlider() {
        OmnitureTracking.trackPackagesHotelFilterPriceSlider()
    }

    fun trackHotelFilterVIP(isChecked: Boolean) {
        val state = if (isChecked) "On" else "Off"
        OmnitureTracking.trackPackagesHotelFilterVIP(state)
    }

    fun trackHotelFilterNeighbourhood() {
        OmnitureTracking.trackPackagesHotelFilterNeighborhood()
    }

    fun trackHotelFilterByName() {
        OmnitureTracking.trackPackagesHotelFilterByName()
    }

    fun trackHotelClearFilter() {
        OmnitureTracking.trackPackagesHotelClearFilter()
    }

    fun trackHotelRefineRating(rating: String) {
        OmnitureTracking.trackPackagesHotelFilterRating(rating + "Star")
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

    fun trackCheckoutPaymentConfirmation(response: PackageCheckoutResponse, hotelSupplierType: String, pageUsableData: PageUsableData) {
        OmnitureTracking.trackPackagesConfirmation(response, hotelSupplierType, pageUsableData)
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

    fun trackHotelDetailLoad(hotelId: String) {
        OmnitureTracking.trackPackagesHotelInfoLoad(hotelId)
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

    fun trackViewBundlePageLoad() {
        OmnitureTracking.trackPackagesViewBundleLoad()
    }

    fun trackBundleOverviewPageLoad(packageDetails: PackageCreateTripResponse.PackageDetails) {
        OmnitureTracking.trackPackagesBundlePageLoad(packageDetails)
    }

    fun trackBundleOverviewHotelExpandClick() {
        OmnitureTracking.trackPackagesBundleProductExpandClick("Hotel")
    }

    fun trackBundleOverviewFlightExpandClick() {
        OmnitureTracking.trackPackagesBundleProductExpandClick("Flight")
    }

    fun trackBundleOverviewCostBreakdownClick() {
        OmnitureTracking.trackPackagesBundleCostBreakdownClick()
    }

    fun trackSearchTravelerPickerChooserClick(text: String) {
        OmnitureTracking.trackPackagesSearchTravelerPickerChooser(text)
    }

    fun trackFlightBaggageFeeClick() {
        OmnitureTracking.trackPackagesFlightBaggageFeeClick()
    }

    fun trackFlightSortFilterLoad() {
        OmnitureTracking.trackPackagesFlightSortFilterLoad()
    }

    fun trackFlightSortBy(sortBy: FlightFilter.Sort) {
        val sortedBy = ( when (sortBy) {
            FlightFilter.Sort.PRICE -> "Price"
            FlightFilter.Sort.ARRIVAL -> "Arrival"
            FlightFilter.Sort.DEPARTURE -> "Departure"
            FlightFilter.Sort.DURATION -> "Duration"
        })
        OmnitureTracking.trackPackagesFlightSortBy(sortedBy)
    }

    fun trackFlightFilterStops(stops: BaseFlightFilterViewModel.Stops) {
        val processedStops = ( when (stops) {
            BaseFlightFilterViewModel.Stops.NONSTOP -> "No Stops"
            BaseFlightFilterViewModel.Stops.ONE_STOP -> "1 Stop"
            BaseFlightFilterViewModel.Stops.TWO_PLUS_STOPS -> "2 Stops"
        })
        OmnitureTracking.trackPackagesFlightFilterStops(processedStops)
    }

    fun trackFlightFilterAirlines() {
        OmnitureTracking.trackPackagesFlightFilterAirlines()
    }

    fun trackHotelRoomBookClick() {
        OmnitureTracking.trackPackagesHotelRoomBookClick()
    }

    fun trackHotelViewBookClick() {
        OmnitureTracking.trackPackagesHotelViewBookClick()
    }

    fun trackHotelRoomMoreInfoClick() {
        OmnitureTracking.trackPackagesHotelRoomInfoClick()
    }

    fun trackHotelDetailMapViewClick() {
        OmnitureTracking.trackPackagesHotelMapViewClick()
    }

    fun trackHotelMapViewSelectRoomClick() {
        OmnitureTracking.trackPackagesHotelMapSelectRoomClick()
    }

    fun trackSearchError(errorType: String) {
        OmnitureTracking.trackPackagesSearchError(errorType)
    }

    fun trackCheckoutError(error: ApiError) {
        OmnitureTracking.trackPackagesCheckoutError(createCheckoutError(error))
    }

    fun trackCheckoutErrorRetry() {
        OmnitureTracking.trackPackagesCheckoutErrorRetry()
    }

    fun trackCheckoutPriceChange(priceDiff: Int) {
        OmnitureTracking.trackPackagesCheckoutPriceChange(priceDiff)
    }

    fun trackCreateTripPriceChange(priceDiff: Int) {
        OmnitureTracking.trackPackagesCreateTripPriceChange(priceDiff)
    }

    fun trackCheckoutSelectTraveler() {
        OmnitureTracking.trackPackagesCheckoutSelectTraveler()
    }

    fun trackCheckoutEditTraveler() {
        OmnitureTracking.trackPackagesCheckoutEditTraveler()
    }

    fun trackCheckoutSlideToPurchase(flexStatus: String) {
        OmnitureTracking.trackPackagesCheckoutSlideToPurchase(flexStatus)
    }

    fun trackCheckoutPaymentCID() {
        OmnitureTracking.trackPackagesCheckoutPaymentCID()
    }

    fun trackBundleEditClick() {
        // TODO: Can't seem to figure out how to fire this event, need to get back to this.
        OmnitureTracking.trackPackagesBundleEditClick()
    }

    fun trackBundleEditItemClick(itemType: String) {
        OmnitureTracking.trackPackagesBundleEditItemClick(itemType)
    }

    fun createCheckoutError(error: ApiError): String {
        var errorType = "CKO:"
        val eSource = if (!error.errorInfo?.source.isNullOrEmpty()) "${error.errorInfo?.source}:" else ":"
        val eSourceErrorId = error.errorInfo?.sourceErrorId ?: error.errorCode
        return "$errorType$eSource$eSourceErrorId"
    }
}