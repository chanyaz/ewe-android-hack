package com.expedia.bookings.tracking

import com.expedia.bookings.data.ApiError
import com.expedia.bookings.data.FlightFilter
import com.expedia.bookings.data.PaymentType
import com.expedia.bookings.data.multiitem.BundleSearchResponse
import com.expedia.bookings.data.packages.PackageCheckoutResponse
import com.expedia.bookings.data.packages.PackageCreateTripResponse
import com.expedia.bookings.data.packages.PackageSearchParams
import com.expedia.bookings.tracking.hotel.PageUsableData
import com.expedia.bookings.utils.CarnivalUtils
import com.expedia.bookings.utils.TuneUtils
import com.expedia.vm.BaseFlightFilterViewModel

class PackagesTracking {

    fun trackCheckoutStart(packageDetails: PackageCreateTripResponse.PackageDetails, hotelSupplierType: String) {
        OmnitureTracking.trackPackagesCheckoutStart(packageDetails, hotelSupplierType)
    }

    fun trackDestinationSearchInit(pageUsableData: PageUsableData) {
        OmnitureTracking.trackPackagesDestinationSearchInit(pageUsableData)
    }

    fun trackHotelSearchResultLoad(response: BundleSearchResponse, pageUsableData: PageUsableData) {
        OmnitureTracking.trackPackagesHSRLoad(response, pageUsableData)
        TuneUtils.trackPackageHotelSearchResults(response)
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

    fun trackCheckoutPaymentConfirmation(response: PackageCheckoutResponse, hotelSupplierType: String, pageUsableData: PageUsableData, packageParams: PackageSearchParams) {
        OmnitureTracking.trackPackagesConfirmation(response, hotelSupplierType, pageUsableData)
        CarnivalUtils.getInstance().trackPackagesConfirmation(packageParams)
        TuneUtils.trackPackageConfirmation(response, packageParams)
    }

    fun trackFlightRoundTripLoad(isOutBound: Boolean, packageParams: PackageSearchParams, pageUsableData: PageUsableData) {
        if (isOutBound) {
            OmnitureTracking.trackPackagesFlightRoundTripOutLoad(pageUsableData)
            TuneUtils.trackPackageOutBoundResults(packageParams)
        } else {
            OmnitureTracking.trackPackagesFlightRoundTripInLoad(pageUsableData)
            TuneUtils.trackPackageInBoundResults(packageParams)
        }
    }

    fun trackFlightRoundTripDetailsLoad(isOutBound: Boolean) {
        if (isOutBound)
            OmnitureTracking.trackPackagesFlightRoundTripOutDetailsLoad()
        else OmnitureTracking.trackPackagesFlightRoundTripInDetailsLoad()
    }

    fun trackHotelDetailLoad(hotelId: String, pageUsableData: PageUsableData) {
        OmnitureTracking.trackPackagesHotelInfoLoad(hotelId, pageUsableData)
    }

    fun trackHotelDetailBookPhoneClick() {
        OmnitureTracking.trackPackagesHotelInfoActionBookPhone()
    }

    fun trackHotelDetailSelectRoomClick(stickyButton: Boolean) {
        OmnitureTracking.trackPackagesHotelInfoActionSelectRoom(stickyButton)
    }

    fun trackHotelDetailGalleryClick() {
        OmnitureTracking.trackPackageHotelDetailGalleryClick()
    }

    fun trackHotelDetailRoomGalleryClick() {
        OmnitureTracking.trackPackageHotelDetailRoomGalleryClick()
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

    fun trackViewBundlePageLoad(isFirstBundleLaunch: Boolean = false) {
        OmnitureTracking.trackPackagesViewBundleLoad(isFirstBundleLaunch)
    }

    fun trackBundleWidgetTap() {
        OmnitureTracking.trackPackagesBundleWidgetTap()
    }

    fun trackBundleOverviewPageLoad(packageDetails: PackageCreateTripResponse.PackageDetails, pageUsableData: PageUsableData) {
        OmnitureTracking.trackPackagesBundlePageLoad(packageDetails, pageUsableData)
    }

    fun trackBundleOverviewHotelExpandClick(isExpanding: Boolean) {
        OmnitureTracking.trackPackagesBundleProductExpandClick("Hotel", isExpanding)
    }

    fun trackBundleOverviewFlightExpandClick(isExpanding: Boolean) {
        OmnitureTracking.trackPackagesBundleProductExpandClick("Flight", isExpanding)
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

    fun trackInfositeError(errorType: String) {
        OmnitureTracking.trackPackagesInfositeError(errorType)
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

    fun trackCheckoutSlideToPurchase(paymentType: PaymentType, flexStatus: String) {
        val cardType = paymentType.omnitureTrackingCode
        OmnitureTracking.trackPackagesCheckoutShowSlideToPurchase(flexStatus, cardType)
    }

    fun trackCheckoutPaymentCID() {
        OmnitureTracking.trackPackagesCheckoutPaymentCID()
    }

    fun trackBundleEditClick() {
        OmnitureTracking.trackPackagesBundleEditClick()
    }

    fun trackBundleEditItemClick(itemType: String) {
        OmnitureTracking.trackPackagesBundleEditItemClick(itemType)
    }

    fun createCheckoutError(error: ApiError): String {
        val errorType = "CKO:"
        val eSource = if (!error.errorInfo?.source.isNullOrEmpty()) "${error.errorInfo?.source}:" else ":"
        val eSourceErrorId = error.errorInfo?.sourceErrorId ?: error.errorCode
        return "$errorType$eSource$eSourceErrorId"
    }

    fun trackAppUpgradeClick() {
        OmnitureTracking.trackAppUpgradeClick()
    }

    fun trackForceUpgradeBanner() {
        OmnitureTracking.trackForceUpgradeBanner()
    }
}
