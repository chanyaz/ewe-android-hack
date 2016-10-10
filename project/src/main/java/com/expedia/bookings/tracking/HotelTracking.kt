package com.expedia.bookings.tracking

import com.expedia.bookings.data.PaymentType
import com.expedia.bookings.data.hotels.HotelCreateTripResponse
import com.expedia.bookings.data.hotels.HotelOffersResponse
import com.expedia.bookings.data.hotels.HotelSearchParams
import com.expedia.bookings.data.hotels.HotelSearchResponse
import com.expedia.bookings.data.payment.PaymentSplitsType
import com.expedia.bookings.services.HotelCheckoutResponse
import com.expedia.bookings.utils.LeanPlumUtils
import com.expedia.bookings.utils.TuneUtils

open class HotelTracking {

    enum class PageName {
        SEARCH_RESULT,
        INFOSITE
    }

    fun trackHotelSearchBox(swpIsVisibleAndToggleIsOn: Boolean) {
        OmnitureTracking.trackHotelV2SearchBox(swpIsVisibleAndToggleIsOn)
    }

    fun trackSwPToggle(swpToggleState: Boolean){
        OmnitureTracking.trackSwPToggle(swpToggleState)
    }

    fun trackTravelerPickerClick(text: String) {
        OmnitureTracking.trackHotelTravelerPickerClick(text)
    }

    fun trackGeoSuggestionClick() {
        OmnitureTracking.trackGeoSuggestionClick()
    }

    fun trackHotelsSearch(searchParams: HotelSearchParams, searchResponse: HotelSearchResponse) {
        OmnitureTracking.internalTrackHotelsV2Search(searchParams, searchResponse)
        LeanPlumUtils.trackHotelV2Search(searchParams, searchResponse)
        TuneUtils.trackHotelV2SearchResults(searchParams, searchResponse)
        FacebookEvents().trackHotelV2Search(searchParams, searchResponse)
    }

    fun trackHotelsNoResult() {
        OmnitureTracking.trackHotelV2NoResult()
    }

    fun trackHotelSponsoredListingClick() {
        OmnitureTracking.trackHotelV2SponsoredListingClick()
    }

    fun trackHotelFilter() {
        OmnitureTracking.trackHotelV2Filter()
    }

    fun trackHotelFilterFavoriteClicked(checked: Boolean) {
        val state = if(checked) "On" else "Off"
        OmnitureTracking.trackLinkHotelV2FilterFavorite(state)
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

    fun trackLinkHotelFilterNeighbourhood() {
        OmnitureTracking.trackLinkHotelV2FilterNeighbourhood()
    }

    fun trackLinkHotelClearFilter() {
        OmnitureTracking.trackLinkHotelV2ClearFilter()
    }

    fun trackLinkHotelFilterByName() {
        OmnitureTracking.trackLinkHotelV2FilterByName()
    }

    fun trackHotelSearchMap() {
        OmnitureTracking.trackHotelV2SearchMap()
    }

    fun trackHotelMapToList() {
        OmnitureTracking.trackHotelV2MapToList()
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

    fun trackHotelCarouselScroll() {
        OmnitureTracking.trackHotelV2CarouselScroll()
    }

    fun trackPageLoadHotelSoldOut() {
        OmnitureTracking.trackPageLoadHotelV2SoldOut()
    }

    fun trackPageLoadHotelInfosite(hotelOffersResponse: HotelOffersResponse, searchParams: HotelSearchParams, isETPEligible: Boolean, isCurrentLocationSearch: Boolean, isHotelSoldOut: Boolean, isRoomSoldOut: Boolean) {
        OmnitureTracking.trackPageLoadHotelV2Infosite(hotelOffersResponse, isETPEligible, isCurrentLocationSearch, isHotelSoldOut, isRoomSoldOut)
        TuneUtils.trackHotelV2InfoSite(hotelOffersResponse)
        FacebookEvents().trackHotelV2InfoSite(searchParams, hotelOffersResponse)
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

    fun trackLinkHotelAirAttachEligible(hotelRoomResponse: HotelOffersResponse.HotelRoomResponse, hotelId: String) {
        OmnitureTracking.trackLinkHotelV2AirAttachEligible(hotelRoomResponse, hotelId)
    }

    fun trackLinkHotelRoomBookClick(hotelRoomResponse: HotelOffersResponse.HotelRoomResponse, hasETP: Boolean) {
        OmnitureTracking.trackHotelV2RoomBookClick(hotelRoomResponse, hasETP)
    }

    fun trackLinkHotelViewRoomClick() {
        OmnitureTracking.trackLinkHotelV2ViewRoomClick()
    }

    fun trackLinkHotelRoomInfoClick() {
        OmnitureTracking.trackLinkHotelV2RoomInfoClick()
    }

    fun trackHotelDetailMapView() {
        OmnitureTracking.trackHotelV2DetailMapView()
    }

    fun trackLinkHotelDetailBookPhoneClick() {
        OmnitureTracking.trackLinkHotelV2DetailBookPhoneClick()
    }

    fun trackLinkHotelDetailSelectRoom() {
        OmnitureTracking.trackLinkHotelV2DetailSelectRoom()
    }

    fun trackLinkHotelMapSelectRoom() {
        OmnitureTracking.trackLinkHotelV2MapSelectRoom()
    }

    fun trackHotelReviews() {
        OmnitureTracking.trackHotelV2Reviews()
    }

    fun trackHotelReviewsCategories(tabSelected: Int) {
        val category = ( when (tabSelected) {
            0 -> "Recent"
            1 -> "Favorable"
            2 -> "Critical"
            else -> "N/A"
        })
        OmnitureTracking.trackHotelV2ReviewsCategories(category)
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

    fun trackPageLoadHotelCheckoutInfo(hotelCreateTripResponse: HotelCreateTripResponse, searchParams: HotelSearchParams) {
        OmnitureTracking.trackPageLoadHotelV2CheckoutInfo(hotelCreateTripResponse, searchParams)
        val hotelProductResponse = hotelCreateTripResponse.newHotelProductResponse
        LeanPlumUtils.trackHotelV2CheckoutStarted(hotelProductResponse, searchParams.guests)
        TuneUtils.trackHotelV2CheckoutStarted(hotelProductResponse)
        FacebookEvents().trackHotelV2Checkout(hotelProductResponse, searchParams)
    }

    fun trackTripSummaryClick() {
        OmnitureTracking.trackTripSummaryClick()
    }

    fun trackPriceChange(priceChange: String) {
        OmnitureTracking.trackPriceChange(priceChange)
    }

    fun trackHotelCheckoutTraveler() {
        OmnitureTracking.trackHotelV2CheckoutTraveler()
    }

    fun trackHotelPaymentEdit() {
        OmnitureTracking.trackHotelV2PaymentEdit()
    }

    fun trackHotelStoredCardSelect() {
        OmnitureTracking.trackHotelV2StoredCardSelect()
    }

    fun trackHotelSlideToPurchase(paymentType: PaymentType, paymentSplitsType: PaymentSplitsType) {
        OmnitureTracking.trackHotelV2SlideToPurchase(paymentType, paymentSplitsType)
    }

    fun trackHotelCheckoutPaymentCid() {
        OmnitureTracking.trackHotelV2CheckoutPaymentCid()
    }

    fun trackHotelsCardError() {
        trackHotelsCheckoutError("Payment Info Error")
    }

    fun trackHotelsTravelerError() {
        trackHotelsCheckoutError("Traveler Info Error")
    }

    fun trackHotelsProductExpiredError() {
        trackHotelsCheckoutError("Product Expired")
    }

    fun trackHotelsTripAlreadyBookedError() {
        trackHotelsCheckoutError("Trip Already Booked")
    }

    fun trackHotelsPaymentFailedError() {
        trackHotelsCheckoutError("Payment Failed")
    }

    fun trackHotelsSessionTimeOutError() {
        trackHotelsCheckoutError("Session Timeout")
    }

    fun trackHotelsUnknownError() {
        trackHotelsCheckoutError("Unknown Error")
    }

    private fun trackHotelsCheckoutError(errorType: String) {
        OmnitureTracking.trackHotelV2CheckoutError(errorType)
    }

    fun trackHotelsCheckoutErrorRetry() {
        OmnitureTracking.trackHotelV2CheckoutErrorRetry()
    }

    fun trackHotelCardIOButtonClicked() {
        OmnitureTracking.trackHotelV2CardIOButtonClicked()
    }

    fun trackHotelPurchaseConfirmation(hotelCheckoutResponse: HotelCheckoutResponse, percentagePaidWithPoints: Int, totalAppliedRewardCurrency: String, guestCount: Int, couponCode: String) {
        OmnitureTracking.trackHotelV2PurchaseConfirmation(hotelCheckoutResponse, percentagePaidWithPoints, totalAppliedRewardCurrency)
        LeanPlumUtils.trackHotelV2Booked(hotelCheckoutResponse, guestCount, couponCode)
        TuneUtils.trackHotelV2Confirmation(hotelCheckoutResponse)
        FacebookEvents().trackHotelV2Confirmation(hotelCheckoutResponse)
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

    fun trackHotelCouponSuccess(couponCode: String) {
        OmnitureTracking.trackHotelV2CouponSuccess(couponCode)
    }

    fun trackHotelCouponFail(couponCode: String, errorMessage: String) {
        OmnitureTracking.trackHotelV2CouponFail(couponCode, errorMessage)
    }

    fun trackHotelCouponRemove(couponCode: String) {
        OmnitureTracking.trackHotelV2CouponRemove(couponCode)
    }

    fun trackPayWithPointsAmountUpdateSuccess(percentagePaidWithPoints: Int) {
        OmnitureTracking.trackPayWithPointsAmountUpdateSuccess(percentagePaidWithPoints)
    }

    fun trackPayWithPointsDisabled() {
        OmnitureTracking.trackPayWithPointsDisabled()
    }

    fun trackPayWithPointsReEnabled(percentagePaidWithPoints: Int) {
        OmnitureTracking.trackPayWithPointsReEnabled(percentagePaidWithPoints)
    }

    fun trackPayWithPointsError(error: PayWithPointsErrorTrackingEnum) {
        OmnitureTracking.trackPayWithPointsError(error.errorMessage)
    }

    open fun trackHotelFavoriteClick(hotelId: String, favorite: Boolean, page: PageName) {
        OmnitureTracking.trackHotelFavoriteClick(hotelId, favorite, page)
    }
}
