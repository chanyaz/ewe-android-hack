package com.expedia.bookings.tracking

import com.expedia.bookings.data.ApiError
import com.expedia.bookings.data.FlightFilter
import com.expedia.bookings.data.PaymentType
import com.expedia.bookings.data.flights.FlightLeg
import com.expedia.bookings.data.multiitem.BundleSearchResponse
import com.expedia.bookings.data.packages.PackageCheckoutResponse
import com.expedia.bookings.data.packages.PackageSearchParams
import com.expedia.bookings.marketing.carnival.CarnivalUtils
import com.expedia.bookings.packages.tracking.PackagesOmnitureTracking
import com.expedia.bookings.tracking.hotel.PageUsableData
import com.expedia.bookings.utils.TuneUtils
import com.expedia.vm.BaseFlightFilterViewModel
import com.expedia.vm.BaseTotalPriceWidgetViewModel

class PackagesTracking {

    fun trackDestinationSearchInit(pageUsableData: PageUsableData) {
        PackagesOmnitureTracking.trackPackagesDestinationSearchInit(pageUsableData)
    }

    fun trackHotelSearchResultLoad(response: BundleSearchResponse, pageUsableData: PageUsableData) {
        PackagesOmnitureTracking.trackPackagesHSRLoad(response, pageUsableData)
        TuneUtils.trackPackageHotelSearchResults(response)
    }

    fun trackHotelFilterSearchLoad(response: BundleSearchResponse, pageUsableData: PageUsableData) {
        PackagesOmnitureTracking.trackPackageFilteredHSRLoad(response, pageUsableData)
    }

    fun trackHotelMapLoad() {
        PackagesOmnitureTracking.trackPackagesHSRMapInit()
    }

    fun trackHotelMapToList() {
        PackagesOmnitureTracking.trackPackagesHotelMapToList()
    }

    fun trackHotelMapPinTap() {
        PackagesOmnitureTracking.trackPackagesHotelMapPinTap()
    }

    fun trackHotelMapCarouselPropertyClick() {
        PackagesOmnitureTracking.trackPackagesHotelMapCarouselPropertyClick()
    }

    fun trackHotelMapSearchThisAreaClick() {
        PackagesOmnitureTracking.trackPackagesHotelMapSearchThisAreaClick()
    }

    fun trackHotelFilterLoad() {
        PackagesOmnitureTracking.trackPackagesHotelFilterPageLoad()
    }

    fun trackHotelSortBy(type: String) {
        PackagesOmnitureTracking.trackPackagesHotelSortBy(type)
    }

    fun trackHotelFilterPriceSlider() {
        PackagesOmnitureTracking.trackPackagesHotelFilterPriceSlider()
    }

    fun trackHotelFilterVIP(isChecked: Boolean) {
        val state = if (isChecked) "On" else "Off"
        PackagesOmnitureTracking.trackPackagesHotelFilterVIP(state)
    }

    fun trackHotelFilterNeighbourhood() {
        PackagesOmnitureTracking.trackPackagesHotelFilterNeighborhood()
    }

    fun trackHotelFilterByName() {
        PackagesOmnitureTracking.trackPackagesHotelFilterByName()
    }

    fun trackHotelClearFilter() {
        PackagesOmnitureTracking.trackPackagesHotelClearFilter()
    }

    fun trackHotelFilterApplied() {
        PackagesOmnitureTracking.trackPackagesHotelFilterApplied()
    }

    fun trackHotelRefineRating(rating: String) {
        PackagesOmnitureTracking.trackPackagesHotelFilterRating(rating + "Star")
    }

    fun trackCheckoutSelectPaymentClick() {
        PackagesOmnitureTracking.trackPackagesPaymentSelect()
    }

    fun trackCheckoutPaymentSelectStoredCard() {
        PackagesOmnitureTracking.trackPackagesPaymentStoredCCSelect()
    }

    fun trackCheckoutPaymentConfirmation(response: PackageCheckoutResponse, hotelSupplierType: String, pageUsableData: PageUsableData, packageParams: PackageSearchParams) {
        PackagesOmnitureTracking.trackPackagesConfirmation(response, hotelSupplierType, pageUsableData)
        CarnivalUtils.getInstance().trackPackagesConfirmation(packageParams)
        TuneUtils.trackPackageConfirmation(response, packageParams)
    }

    fun trackFlightRoundTripLoad(isOutBound: Boolean, packageParams: PackageSearchParams, pageUsableData: PageUsableData) {
        if (isOutBound) {
            PackagesOmnitureTracking.trackPackagesFlightRoundTripOutLoad(pageUsableData)
            TuneUtils.trackPackageOutBoundResults(packageParams)
        } else {
            PackagesOmnitureTracking.trackPackagesFlightRoundTripInLoad(pageUsableData)
            TuneUtils.trackPackageInBoundResults(packageParams)
        }
    }

    fun trackFlightRoundTripDetailsLoad(isOutBound: Boolean, pageUsableData: PageUsableData, flight: FlightLeg) {
        if (isOutBound)
            PackagesOmnitureTracking.trackPackagesFlightRoundTripOutDetailsLoad(pageUsableData, flight)
        else PackagesOmnitureTracking.trackPackagesFlightRoundTripInDetailsLoad(pageUsableData, flight)
    }

    fun trackHotelDetailLoad(hotelId: String, pageUsableData: PageUsableData) {
        PackagesOmnitureTracking.trackPackagesHotelInfoLoad(hotelId, pageUsableData)
    }

    fun trackHotelDetailBookPhoneClick() {
        PackagesOmnitureTracking.trackPackagesHotelInfoActionBookPhone()
    }

    fun trackHotelDetailSelectRoomClick(stickyButton: Boolean) {
        PackagesOmnitureTracking.trackPackagesHotelInfoActionSelectRoom(stickyButton)
    }

    fun trackHotelDetailGalleryClick() {
        PackagesOmnitureTracking.trackPackageHotelDetailGalleryClick()
    }

    fun trackHotelDetailRoomGalleryClick() {
        PackagesOmnitureTracking.trackPackageHotelDetailRoomGalleryClick()
    }

    fun trackHotelReviewPageLoad() {
        PackagesOmnitureTracking.trackPackagesHotelReviewPageLoad()
    }

    fun trackHotelReviewCategoryChange(tabSelected: Int) {
        val category = ( when (tabSelected) {
            0 -> "Recent"
            1 -> "Favorable"
            2 -> "Critical"
            else -> "N/A"
        })
        PackagesOmnitureTracking.trackPackagesHotelReviewCategoryChange(category)
    }

    fun trackHotelResortFeeInfoClick() {
        PackagesOmnitureTracking.trackPackagesHotelResortFeeInfo()
    }

    fun trackHotelRenovationInfoClick() {
        PackagesOmnitureTracking.trackPackagesHotelRenovationInfo()
    }

    fun trackViewBundlePageLoad(isFirstBundleLaunch: Boolean = false) {
        PackagesOmnitureTracking.trackPackagesViewBundleLoad(isFirstBundleLaunch)
    }

    fun trackBundleWidgetTap() {
        PackagesOmnitureTracking.trackPackagesBundleWidgetTap()
    }

    fun trackBundleOverviewPageLoad(packageTotal: Double?, pageUsableData: PageUsableData) {
        PackagesOmnitureTracking.trackPackagesBundlePageLoad(packageTotal, pageUsableData)
    }

    fun trackBundleOverviewHotelExpandClick(isExpanding: Boolean) {
        PackagesOmnitureTracking.trackPackagesBundleProductExpandClick("Hotel", isExpanding)
    }

    fun trackBundleOverviewFlightExpandClick(isExpanding: Boolean) {
        PackagesOmnitureTracking.trackPackagesBundleProductExpandClick("Flight", isExpanding)
    }

    fun trackSearchTravelerPickerChooserClick(text: String) {
        PackagesOmnitureTracking.trackPackagesSearchTravelerPickerChooser(text)
    }

    fun trackFlightBaggageFeeClick() {
        PackagesOmnitureTracking.trackPackagesFlightBaggageFeeClick()
    }

    fun trackFlightSortFilterLoad() {
        PackagesOmnitureTracking.trackPackagesFlightSortFilterLoad()
    }

    fun trackFlightSortBy(sortBy: FlightFilter.Sort) {
        val sortedBy = ( when (sortBy) {
            FlightFilter.Sort.PRICE -> "Price"
            FlightFilter.Sort.ARRIVAL -> "Arrival"
            FlightFilter.Sort.DEPARTURE -> "Departure"
            FlightFilter.Sort.DURATION -> "Duration"
        })
        PackagesOmnitureTracking.trackPackagesFlightSortBy(sortedBy)
    }

    fun trackFlightFilterStops(stops: BaseFlightFilterViewModel.Stops) {
        val processedStops = ( when (stops) {
            BaseFlightFilterViewModel.Stops.NONSTOP -> "No Stops"
            BaseFlightFilterViewModel.Stops.ONE_STOP -> "1 Stop"
            BaseFlightFilterViewModel.Stops.TWO_PLUS_STOPS -> "2 Stops"
        })
        PackagesOmnitureTracking.trackPackagesFlightFilterStops(processedStops)
    }

    fun trackFlightFilterAirlines(selectedAirlineTag: String) {
        PackagesOmnitureTracking.trackPackagesFlightFilterAirlines(selectedAirlineTag)
    }

    fun trackFlightFilterArrivalDeparture(isDeparture: Boolean) {
        PackagesOmnitureTracking.trackPackagesFlightFilterArrivalDeparture(isDeparture)
    }

    fun trackFlightFilterDuration() {
        PackagesOmnitureTracking.trackPackagesFlightFilterDuration()
    }

    fun trackHotelRoomBookClick() {
        PackagesOmnitureTracking.trackPackagesHotelRoomBookClick()
    }

    fun trackHotelViewBookClick() {
        PackagesOmnitureTracking.trackPackagesHotelViewBookClick()
    }

    fun trackHotelRoomMoreInfoClick() {
        PackagesOmnitureTracking.trackPackagesHotelRoomInfoClick()
    }

    fun trackHotelDetailMapViewClick() {
        PackagesOmnitureTracking.trackPackagesHotelMapViewClick()
    }

    fun trackHotelMapViewSelectRoomClick() {
        PackagesOmnitureTracking.trackPackagesHotelMapSelectRoomClick()
    }

    fun trackShoppingError(apiCallFailing: ApiCallFailing) {
        PackagesOmnitureTracking.trackPackagesShoppingError(apiCallFailing.getErrorStringForTracking())
    }

    fun trackCheckoutError(error: ApiError) {
        PackagesOmnitureTracking.trackPackagesCheckoutError(createCheckoutError(error))
    }

    fun trackCheckoutErrorRetry() {
        PackagesOmnitureTracking.trackPackagesCheckoutErrorRetry()
    }

    fun trackSearchValidationError(errorTag: String) {
        PackagesOmnitureTracking.trackPackagesSearchValidationError(errorTag)
    }

    fun trackCheckoutPriceChange(priceDiff: Int) {
        PackagesOmnitureTracking.trackPackagesCheckoutPriceChange(priceDiff)
    }

    fun trackCreateTripPriceChange(priceDiff: Int) {
        PackagesOmnitureTracking.trackPackagesCreateTripPriceChange(priceDiff)
    }

    fun trackCheckoutSelectTraveler() {
        PackagesOmnitureTracking.trackPackagesCheckoutSelectTraveler()
    }

    fun trackCheckoutEditTraveler() {
        PackagesOmnitureTracking.trackPackagesCheckoutEditTraveler()
    }

    fun trackCheckoutSlideToPurchase(paymentType: PaymentType, flexStatus: String) {
        val cardType = paymentType.omnitureTrackingCode
        PackagesOmnitureTracking.trackPackagesCheckoutShowSlideToPurchase(flexStatus, cardType)
    }

    fun trackMidCreateTripError(error: String) {
        PackagesOmnitureTracking.trackPackagesMIDCreateTripError(error)
    }

    fun trackCheckoutPaymentCID() {
        PackagesOmnitureTracking.trackPackagesCheckoutPaymentCID()
    }

    fun trackBundleEditClick() {
        PackagesOmnitureTracking.trackPackagesBundleEditClick()
    }

    fun trackBundleEditItemClick(itemType: String) {
        PackagesOmnitureTracking.trackPackagesBundleEditItemClick(itemType)
    }

    fun createCheckoutError(error: ApiError): String {
        val errorType = "CKO:"
        val eSource = if (!error.errorInfo?.source.isNullOrEmpty()) "${error.errorInfo?.source}:" else ":"
        val eSourceErrorId = error.errorInfo?.sourceErrorId ?: error.getErrorCode()
        return "$errorType$eSource$eSourceErrorId"
    }

    fun trackFHCTabClick() {
        PackagesOmnitureTracking.trackPackagesFHCTabClick()
    }

    fun trackDormantUserHomeRedirect() {
        PackagesOmnitureTracking.trackPackagesDormantUserHomeRedirect()
    }

    fun trackBundleOverviewTotalPriceWidgetClick(priceWidgetEvent: BaseTotalPriceWidgetViewModel.PriceWidgetEvent, shouldShowSavings: Boolean) {
        when (priceWidgetEvent) {
            BaseTotalPriceWidgetViewModel.PriceWidgetEvent.SAVINGS_STRIP_CLICK -> PackagesOmnitureTracking.trackPackagesBundleCostBreakdownSavingsStripClick()
            BaseTotalPriceWidgetViewModel.PriceWidgetEvent.SAVINGS_BUTTON_CLICK -> PackagesOmnitureTracking.trackPackagesBundleCostBreakdownSavingsButtonClick()
            BaseTotalPriceWidgetViewModel.PriceWidgetEvent.INFO_ICON_CLICK -> PackagesOmnitureTracking.trackPackagesBundleCostBreakdownInfoIconClick()
            BaseTotalPriceWidgetViewModel.PriceWidgetEvent.BUNDLE_PRICE_CLICK -> PackagesOmnitureTracking.trackPackagesBundleCostBreakdownBundlePriceClick()
            BaseTotalPriceWidgetViewModel.PriceWidgetEvent.BUNDLE_WIDGET_CLICK -> PackagesOmnitureTracking.trackPackagesBundleCostBreakdownBundleWidgetClick(shouldShowSavings)
        }
    }

    fun trackBundleOverviewCostBreakdownLoad() {
        PackagesOmnitureTracking.trackPackagesBundleOverviewCostBreakdownLoad()
    }

    fun trackPackagesScrollDepth(hasUserScrolled: Boolean, resultsShown: Int, resultsViewed: Int, resultClicked: Int = -1) {
        val depth = StringBuilder()
        depth.append("SC=")
        depth.append(if (hasUserScrolled) "y" else "n")
        depth.append("|RS=$resultsShown")
        depth.append("|RV=$resultsViewed")
        if (resultClicked != -1) {
            depth.append("|RC=$resultClicked")
        }
        PackagesOmnitureTracking.trackPackagesScrollDepth(depth.toString())
    }
}
