package com.expedia.bookings.tracking.hotel

import com.expedia.bookings.data.ApiError
import com.expedia.bookings.data.HotelItinDetailsResponse
import com.expedia.bookings.data.PaymentType
import com.expedia.bookings.data.hotels.HotelCreateTripResponse
import com.expedia.bookings.data.hotels.HotelOffersResponse
import com.expedia.bookings.data.hotels.HotelSearchParams
import com.expedia.bookings.data.payment.PaymentSplitsType
import com.expedia.bookings.services.HotelCheckoutResponse
import com.expedia.bookings.tracking.FacebookEvents
import com.expedia.bookings.tracking.OmnitureTracking
import com.expedia.bookings.tracking.PayWithPointsErrorTrackingEnum
import com.expedia.bookings.utils.CarnivalUtils
import com.expedia.bookings.utils.TuneUtils

open class HotelTracking {
    enum class PageName {
        SEARCH_RESULT,
        INFOSITE
    }

    companion object {
        fun trackPayWithPointsDisabled() {
            OmnitureTracking.trackPayWithPointsDisabled()
        }

        fun trackUrgencyScore(score: Int) {
            OmnitureTracking.trackUrgencyScore(score)
        }

        fun trackPayWithPointsReEnabled(percentagePaidWithPoints: Int) {
            OmnitureTracking.trackPayWithPointsReEnabled(percentagePaidWithPoints)
        }

        fun trackHotelCheckoutTraveler() {
            OmnitureTracking.trackHotelV2CheckoutTraveler()
        }

        fun trackHotelStoredCardSelect() {
            OmnitureTracking.trackHotelV2StoredCardSelect()
        }

        fun trackPriceChange(priceChange: String) {
            OmnitureTracking.trackPriceChange(priceChange)
        }

        fun trackHotelCouponSuccess(couponCode: String) {
            OmnitureTracking.trackHotelV2CouponSuccess(couponCode)
        }

        fun trackHotelCouponFail(couponCode: String, errorMessage: String) {
            OmnitureTracking.trackHotelV2CouponFail(couponCode, errorMessage)
        }

        fun trackHotelSortBy(type: String) {
            OmnitureTracking.trackHotelV2SortBy(type)
        }

        fun trackHotelSortPriceSlider() {
            OmnitureTracking.trackHotelV2PriceSlider()
        }

        fun trackLinkHotelRefineRating(rating: String) {
            OmnitureTracking.trackLinkHotelV2FilterRating(rating + "Star")
        }

        fun trackLinkHotelFilterVip(isChecked: Boolean) {
            val state = if (isChecked) "On" else "Off"
            OmnitureTracking.trackLinkHotelV2FilterVip(state)
        }

        fun trackHotelSuperSearchFilter() {
            OmnitureTracking.trackHotelV2SuperSearchFilter()
        }

        fun trackHotelSuperSearchSortBy(type: String) {
            OmnitureTracking.trackHotelV2SuperSearchSortBy(type)
        }

        fun trackLinkHotelSuperSearchStarRating(rating: String) {
            OmnitureTracking.trackLinkHotelV2SuperSearchStarRating(rating + "Star")
        }

        fun trackLinkHotelSuperSearchVip(isChecked: Boolean) {
            val state = if (isChecked) "On" else "Off"
            OmnitureTracking.trackLinkHotelV2SuperSearchVip(state)
        }

        fun trackLinkHotelSuperSearchClearFilter() {
            OmnitureTracking.trackLinkHotelV2SuperSearchClearFilter()
        }

        fun trackLinkHotelFilterNeighbourhood() {
            OmnitureTracking.trackLinkHotelV2FilterNeighbourhood()
        }

        fun trackLinkHotelClearFilter() {
            OmnitureTracking.trackLinkHotelV2ClearFilter()
        }

        fun trackLinkHotelFilterByName() {
            OmnitureTracking.trackLinkHotelV2FilterByName()
        }

        fun trackHotelReviews() {
            OmnitureTracking.trackHotelV2Reviews()
        }

        fun trackLinkHotelAirAttachEligible(hotelRoomResponse: HotelOffersResponse.HotelRoomResponse, hotelId: String) {
            OmnitureTracking.trackLinkHotelV2AirAttachEligible(hotelRoomResponse, hotelId)
        }

        fun trackLinkHotelRoomBookClick(hotelRoomResponse: HotelOffersResponse.HotelRoomResponse, hasETP: Boolean) {
            OmnitureTracking.trackHotelV2RoomBookClick(hotelRoomResponse, hasETP)
        }

        fun trackLinkHotelViewRoomClick() {
            OmnitureTracking.trackLinkHotelV2ViewRoomClick()
        }

        fun trackPayWithPointsAmountUpdateSuccess(percentagePaidWithPoints: Int) {
            OmnitureTracking.trackPayWithPointsAmountUpdateSuccess(percentagePaidWithPoints)
        }

        fun trackPayWithPointsError(error: PayWithPointsErrorTrackingEnum) {
            OmnitureTracking.trackPayWithPointsError(error.errorMessage)
        }

        fun trackSwPToggle(swpToggleState: Boolean) {
            OmnitureTracking.trackSwPToggle(swpToggleState)
        }

        fun trackHotelEtpInfo() {
            OmnitureTracking.trackHotelV2EtpInfo()
        }

        fun trackHotelResortFeeInfo() {
            OmnitureTracking.trackHotelV2ResortFeeInfo()
        }

        fun trackHotelRenovationInfo() {
            OmnitureTracking.trackHotelV2RenovationInfo()
        }

        fun trackInfositeChangeDateClick() {
            OmnitureTracking.trackHotelV2InfositeChangeDateClick()
        }

        fun trackPageLoadHotelInfosite(hotelOffersResponse: HotelOffersResponse, searchParams: HotelSearchParams,
                                       isETPEligible: Boolean, isCurrentLocationSearch: Boolean,
                                       isHotelSoldOut: Boolean, isRoomSoldOut: Boolean,
                                       pageLoadTime: PageUsableData,
                                       swpEnabled: Boolean) {
            OmnitureTracking.trackPageLoadHotelV2Infosite(hotelOffersResponse, isETPEligible, isCurrentLocationSearch, isHotelSoldOut, isRoomSoldOut, pageLoadTime, swpEnabled)
            TuneUtils.trackHotelV2InfoSite(hotelOffersResponse)
            FacebookEvents().trackHotelV2InfoSite(searchParams, hotelOffersResponse)
            CarnivalUtils.getInstance().trackHotelInfoSite(hotelOffersResponse, searchParams)
        }

        fun trackHotelNarrowPrompt(){
            OmnitureTracking.trackHotelNarrowSearchPrompt()
        }

        fun trackLinkHotelDetailBookPhoneClick() {
            OmnitureTracking.trackLinkHotelV2DetailBookPhoneClick()
        }

        fun trackHotelDetailMapView() {
            OmnitureTracking.trackHotelV2DetailMapView()
        }

        fun trackLinkHotelDetailSelectRoom() {
            OmnitureTracking.trackLinkHotelV2DetailSelectRoom()
        }

        fun trackHotelDetailGalleryClick() {
            OmnitureTracking.trackHotelDetailGalleryClick()
        }

        fun trackTravelerPickerClick(text: String) {
            OmnitureTracking.trackHotelTravelerPickerClick(text)
        }

        fun trackPayNowContainerClick() {
            trackLinkHotelEtpClick("PayNow")
        }

        fun trackPayLaterContainerClick() {
            trackLinkHotelEtpClick("PayLater")
        }

        private fun trackLinkHotelEtpClick(payType: String) {
            OmnitureTracking.trackLinkHotelV2EtpClick(payType)
        }

        fun trackHotelCheckoutPaymentCid() {
            OmnitureTracking.trackHotelV2CheckoutPaymentCid()
        }

        fun trackTripSummaryClick() {
            OmnitureTracking.trackTripSummaryClick()
        }

        fun trackLinkHotelMapSelectRoom() {
            OmnitureTracking.trackLinkHotelV2MapSelectRoom()
        }

        fun trackLinkHotelRoomInfoClick() {
            OmnitureTracking.trackLinkHotelV2RoomInfoClick()
        }

        fun trackHotelSearchBox(swpIsVisibleAndToggleIsOn: Boolean) {
            OmnitureTracking.trackHotelV2SearchBox(swpIsVisibleAndToggleIsOn)
        }

        fun trackGeoSuggestionClick() {
            OmnitureTracking.trackGeoSuggestionClick()
        }

        fun trackHotelSearch(trackingParams: HotelSearchTrackingData, searchParams: HotelSearchParams) {
            OmnitureTracking.trackHotelsV2Search(trackingParams)
            FacebookEvents().trackHotelV2Search(trackingParams)
            TuneUtils.trackHotelV2SearchResults(trackingParams)
            CarnivalUtils.getInstance().trackHotelSearch(searchParams)
        }

        fun trackPinnedSearch() {
            OmnitureTracking.trackPinnedSearch()
        }

        fun trackHotelsNoResult(errorMessage: String) {
            OmnitureTracking.trackHotelV2NoResult(errorMessage)
        }

        fun trackHotelsNoPinnedResult(errorReason: String) {
            OmnitureTracking.trackHotelV2NoPinnedResult(errorReason)
        }

        fun trackHotelFilter() {
            OmnitureTracking.trackHotelV2Filter()
        }

        fun trackHotelSearchMap(swpEnabled: Boolean) {
            OmnitureTracking.trackHotelV2SearchMap(swpEnabled)
        }

        fun trackHotelMapToList(swpEnabled: Boolean) {
            OmnitureTracking.trackHotelV2MapToList(swpEnabled)
        }

        fun trackHotelMapTapPin() {
            OmnitureTracking.trackHotelV2MapTapPin()
        }

        fun trackHotelCarouselClick() {
            OmnitureTracking.trackHotelV2CarouselClick()
        }

        fun trackHotelsSearchAreaClick() {
            OmnitureTracking.trackHotelV2AreaSearchClick()
        }

        fun trackPageLoadHotelSoldOut() {
            OmnitureTracking.trackPageLoadHotelV2SoldOut()
        }

        fun trackHotelReviewsCategories(tabSelected: Int) {
            val category = (when (tabSelected) {
                0 -> "Recent"
                1 -> "Favorable"
                2 -> "Critical"
                else -> "N/A"
            })
            OmnitureTracking.trackHotelV2ReviewsCategories(category)
        }

        fun trackPageLoadHotelCheckoutInfo(hotelCreateTripResponse: HotelCreateTripResponse, searchParams: HotelSearchParams, pageUsableData: PageUsableData) {
            OmnitureTracking.trackPageLoadHotelV2CheckoutInfo(hotelCreateTripResponse, searchParams, pageUsableData)
            val hotelProductResponse = hotelCreateTripResponse.newHotelProductResponse
            TuneUtils.trackHotelV2CheckoutStarted(hotelProductResponse)
            FacebookEvents().trackHotelV2Checkout(hotelProductResponse, searchParams)
            CarnivalUtils.getInstance().trackHotelCheckoutStart(hotelCreateTripResponse, searchParams)
        }

        fun trackHotelSlideToPurchase(paymentType: PaymentType, paymentSplitsType: PaymentSplitsType) {
            OmnitureTracking.trackHotelV2ShowSlideToPurchase(paymentType, paymentSplitsType)
        }

        fun trackHotelsCheckoutError(error: ApiError) {
            OmnitureTracking.trackHotelV2CheckoutError(createCheckoutError(error))
        }

        fun trackHotelsCheckoutErrorRetry() {
            OmnitureTracking.trackHotelV2CheckoutErrorRetry()
        }

        fun trackHotelSponsoredListingClick() {
            OmnitureTracking.trackHotelV2SponsoredListingClick()
        }

        fun trackHotelPurchaseConfirmation(hotelCheckoutResponse: HotelCheckoutResponse, percentagePaidWithPoints: Int, totalAppliedRewardCurrency: String, guestCount: Int, couponCode: String, pageUsableData: PageUsableData, hotelSearchParams: HotelSearchParams) {
            OmnitureTracking.trackHotelV2PurchaseConfirmation(hotelCheckoutResponse, percentagePaidWithPoints, totalAppliedRewardCurrency, pageUsableData)
            TuneUtils.trackHotelV2Confirmation(hotelCheckoutResponse)
            FacebookEvents().trackHotelV2Confirmation(hotelCheckoutResponse)
            CarnivalUtils.getInstance().trackHotelConfirmation(hotelCheckoutResponse, hotelSearchParams)
        }

        fun trackHotelPurchaseFromWebView(hotelItinDetailsResponse: HotelItinDetailsResponse) {
            OmnitureTracking.trackHotelV2PurchaseFromWebView(hotelItinDetailsResponse)
        }

        fun trackHotelConfirmationCalendar() {
            OmnitureTracking.trackHotelV2ConfirmationCalendar()
        }

        fun trackHotelConfirmationDirection() {
            OmnitureTracking.trackHotelV2ConfirmationDirection()
        }

        fun trackHotelCallCustomerSupport() {
            OmnitureTracking.trackHotelV2CallCustomerSupport()
        }

        fun trackHotelCrossSellCar() {
            trackHotelConfirmationCrossSell("Cars")
        }

        fun trackHotelCrossSellFlight() {
            trackHotelConfirmationCrossSell("Flights")
        }

        fun trackHotelCrossSellLX() {
            trackHotelConfirmationCrossSell("LX")
        }

        private fun trackHotelConfirmationCrossSell(businessType: String) {
            OmnitureTracking.trackHotelV2ConfirmationCrossSell(businessType)
        }

        fun trackHotelExpandCoupon() {
            OmnitureTracking.trackHotelV2ExpandCoupon()
        }

        fun trackHotelCouponRemove(couponCode: String) {
            OmnitureTracking.trackHotelV2CouponRemove(couponCode)
        }

        fun createCheckoutError(error: ApiError): String {
            val errorType = "CKO:"
            val eSource = if (!error.errorInfo?.source.isNullOrEmpty()) "${error.errorInfo?.source}:" else ":"
            val eSourceErrorId = error.errorInfo?.sourceErrorId ?: error.errorCode
            return "$errorType$eSource$eSourceErrorId"
        }
    }
}
