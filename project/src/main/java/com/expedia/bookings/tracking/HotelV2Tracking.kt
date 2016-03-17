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

class HotelV2Tracking {

    fun trackHotelV2SearchBox() {
        OmnitureTracking.trackHotelV2SearchBox()
    }

    fun trackRecentSearchClick() {
        OmnitureTracking.trackRecentSearchClick()
    }

    fun trackTravelerPickerClick(text: String) {
        OmnitureTracking.trackTravelerPickerClick(text)
    }

    fun trackGeoSuggestionClick() {
        OmnitureTracking.trackGeoSuggestionClick()
    }

    fun trackHotelsV2Search(searchParams: HotelSearchParams, searchResponse: HotelSearchResponse) {
        OmnitureTracking.internalTrackHotelsV2Search(searchParams, searchResponse)
        LeanPlumUtils.trackHotelV2Search(searchParams, searchResponse)
        TuneUtils.trackHotelV2SearchResults(searchParams, searchResponse)
        FacebookEvents().trackHotelV2Search(searchParams, searchResponse)
    }

    fun trackHotelsV2NoResult() {
        OmnitureTracking.trackHotelV2NoResult()
    }

    fun trackHotelV2SponsoredListingClick() {
        OmnitureTracking.trackHotelV2SponsoredListingClick()
    }

    fun trackHotelV2Filter() {
        OmnitureTracking.trackHotelV2Filter()
    }

    fun trackHotelV2SortBy(type: String) {
        OmnitureTracking.trackHotelV2SortBy(type)
    }

    fun trackHotelV2SortPriceSlider() {
        OmnitureTracking.trackHotelV2PriceSlider()
    }

    fun trackLinkHotelV2RefineRating(rating: String) {
        OmnitureTracking.trackLinkHotelV2FilterRating(rating + "Star")
    }

    fun trackLinkHotelV2FilterVip(isChecked: Boolean) {
        val state = if (isChecked) "On" else "Off"
        OmnitureTracking.trackLinkHotelV2FilterVip(state)
    }

    fun trackLinkHotelV2FilterNeighbourhood() {
        OmnitureTracking.trackLinkHotelV2FilterNeighbourhood()
    }

    fun trackLinkHotelV2ClearFilter() {
        OmnitureTracking.trackLinkHotelV2ClearFilter()
    }

    fun trackLinkHotelV2FilterByName() {
        OmnitureTracking.trackLinkHotelV2FilterByName()
    }

    fun trackHotelV2SearchMap() {
        OmnitureTracking.trackHotelV2SearchMap()
    }

    fun trackHotelV2MapToList() {
        OmnitureTracking.trackHotelV2MapToList()
    }

    fun trackHotelV2MapTapPin() {
        OmnitureTracking.trackHotelV2MapTapPin()
    }

    fun trackHotelV2CarouselClick() {
        OmnitureTracking.trackHotelV2CarouselClick()
    }

    fun trackHotelsV2SearchAreaClick() {
        OmnitureTracking.trackHotelV2AreaSearchClick()
    }

    fun trackHotelV2CarouselScroll() {
        OmnitureTracking.trackHotelV2CarouselScroll()
    }

    fun trackPageLoadHotelV2SoldOut() {
        OmnitureTracking.trackPageLoadHotelV2SoldOut()
    }

    fun trackPageLoadHotelV2Infosite(hotelOffersResponse: HotelOffersResponse, searchParams: HotelSearchParams, isETPEligible: Boolean, isCurrentLocationSearch: Boolean, isHotelSoldOut: Boolean, isRoomSoldOut: Boolean) {
        OmnitureTracking.trackPageLoadHotelV2Infosite(hotelOffersResponse, isETPEligible, isCurrentLocationSearch, isHotelSoldOut, isRoomSoldOut)
        TuneUtils.trackHotelV2InfoSite(hotelOffersResponse)
        FacebookEvents().trackHotelV2InfoSite(searchParams, hotelOffersResponse)
    }

    fun trackPayNowContainerClick() {
        trackLinkHotelV2EtpClick("PayNow")
    }

    fun trackPayLaterContainerClick() {
        trackLinkHotelV2EtpClick("PayLater")
    }

    private fun trackLinkHotelV2EtpClick(payType: String) {
        OmnitureTracking.trackLinkHotelV2EtpClick(payType)
    }

    fun trackLinkHotelV2AirAttachEligible(hotelRoomResponse: HotelOffersResponse.HotelRoomResponse, hotelId: String) {
        OmnitureTracking.trackLinkHotelV2AirAttachEligible(hotelRoomResponse, hotelId)
    }

    fun trackLinkHotelV2RoomBookClick(hotelRoomResponse: HotelOffersResponse.HotelRoomResponse, hasETP: Boolean) {
        OmnitureTracking.trackHotelV2RoomBookClick(hotelRoomResponse, hasETP)
    }

    fun trackLinkHotelV2ViewRoomClick() {
        OmnitureTracking.trackLinkHotelV2ViewRoomClick()
    }

    fun trackLinkHotelV2RoomInfoClick() {
        OmnitureTracking.trackLinkHotelV2RoomInfoClick()
    }

    fun trackHotelV2DetailMapView() {
        OmnitureTracking.trackHotelV2DetailMapView()
    }

    fun trackLinkHotelV2DetailBookPhoneClick() {
        OmnitureTracking.trackLinkHotelV2DetailBookPhoneClick()
    }

    fun trackLinkHotelV2DetailSelectRoom() {
        OmnitureTracking.trackLinkHotelV2DetailSelectRoom()
    }

    fun trackLinkHotelV2MapSelectRoom() {
        OmnitureTracking.trackLinkHotelV2MapSelectRoom()
    }

    fun trackHotelV2Reviews() {
        OmnitureTracking.trackHotelV2Reviews()
    }

    fun trackHotelV2ReviewsCategories(tabSelected: Int) {
        val category = ( when (tabSelected) {
            0 -> "Recent"
            1 -> "Favorable"
            2 -> "Critical"
            else -> "N/A"
        })
        OmnitureTracking.trackHotelV2ReviewsCategories(category)
    }

    fun trackHotelV2EtpInfo() {
        OmnitureTracking.trackHotelV2EtpInfo()
    }

    fun trackHotelV2ResortFeeInfo() {
        OmnitureTracking.trackHotelV2ResortFeeInfo()
    }

    fun trackHotelV2RenovationInfo() {
        OmnitureTracking.trackHotelV2RenovationInfo()
    }

    fun trackPageLoadHotelV2CheckoutInfo(hotelCreateTripResponse: HotelCreateTripResponse, searchParams: HotelSearchParams) {
        OmnitureTracking.trackPageLoadHotelV2CheckoutInfo(hotelCreateTripResponse, searchParams)
        val hotelProductResponse = hotelCreateTripResponse.newHotelProductResponse
        LeanPlumUtils.trackHotelV2CheckoutStarted(hotelProductResponse)
        TuneUtils.trackHotelV2CheckoutStarted(hotelProductResponse)
        FacebookEvents().trackHotelV2Checkout(hotelProductResponse, searchParams)
    }

    fun trackTripSummaryClick() {
        OmnitureTracking.trackTripSummaryClick()
    }

    fun trackPriceChange(priceChange: String) {
        OmnitureTracking.trackPriceChange(priceChange)
    }

    fun trackHotelV2CheckoutTraveler() {
        OmnitureTracking.trackHotelV2CheckoutTraveler()
    }

    fun trackHotelV2PaymentEdit() {
        OmnitureTracking.trackHotelV2PaymentEdit()
    }

    fun trackHotelV2StoredCardSelect() {
        OmnitureTracking.trackHotelV2StoredCardSelect()
    }

    fun trackHotelV2SlideToPurchase(paymentType: PaymentType, paymentSplitsType: PaymentSplitsType) {
        OmnitureTracking.trackHotelV2SlideToPurchase(paymentType, paymentSplitsType)
    }

    fun trackHotelV2CheckoutPaymentCid() {
        OmnitureTracking.trackHotelV2CheckoutPaymentCid()
    }

    fun trackHotelsV2CardError() {
        trackHotelsV2CheckoutError("Payment Info Error")
    }

    fun trackHotelsV2TravelerError() {
        trackHotelsV2CheckoutError("Traveler Info Error")
    }

    fun trackHotelsV2ProductExpiredError() {
        trackHotelsV2CheckoutError("Product Expired")
    }

    fun trackHotelsV2TripAlreadyBookedError() {
        trackHotelsV2CheckoutError("Trip Already Booked")
    }

    fun trackHotelsV2PaymentFailedError() {
        trackHotelsV2CheckoutError("Payment Failed")
    }

    fun trackHotelsV2SessionTimeOutError() {
        trackHotelsV2CheckoutError("Session Timeout")
    }

    fun trackHotelsV2UnknownError() {
        trackHotelsV2CheckoutError("Unknown Error")
    }

    private fun trackHotelsV2CheckoutError(errorType: String) {
        OmnitureTracking.trackHotelV2CheckoutError(errorType)
    }

    fun trackHotelsV2CheckoutErrorRetry() {
        OmnitureTracking.trackHotelV2CheckoutErrorRetry()
    }

    fun trackHotelV2PurchaseConfirmation(hotelCheckoutResponse: HotelCheckoutResponse, percentagePaidWithPoints: Int, totalBurnedAmount: Int) {
        OmnitureTracking.trackHotelV2PurchaseConfirmation(hotelCheckoutResponse, percentagePaidWithPoints, totalBurnedAmount)
        LeanPlumUtils.trackHotelV2Booked(hotelCheckoutResponse)
        TuneUtils.trackHotelV2Confirmation(hotelCheckoutResponse)
        FacebookEvents().trackHotelV2Confirmation(hotelCheckoutResponse)
    }

    fun trackHotelV2ConfirmationCalendar() {
        OmnitureTracking.trackHotelV2ConfirmationCalendar()
    }

    fun trackHotelV2ConfirmationDirection() {
        OmnitureTracking.trackHotelV2ConfirmationDirection()
    }

    fun trackHotelV2CallCustomerSupport() {
        OmnitureTracking.trackHotelV2CallCustomerSupport()
    }

    fun trackHotelV2CrossSellCar() {
        trackHotelV2ConfirmationCrossSell("Cars")
    }

    fun trackHotelV2CrossSellFlight() {
        trackHotelV2ConfirmationCrossSell("Flights")
    }

    private fun trackHotelV2ConfirmationCrossSell(businessType: String) {
        OmnitureTracking.trackHotelV2ConfirmationCrossSell(businessType)
    }

    fun trackHotelV2ExpandCoupon() {
        OmnitureTracking.trackHotelV2ExpandCoupon()
    }

    fun trackHotelV2CouponSuccess(couponCode: String) {
        OmnitureTracking.trackHotelV2CouponSuccess(couponCode)
    }

    fun trackHotelV2CouponFail(couponCode: String, errorMessage: String) {
        OmnitureTracking.trackHotelV2CouponFail(couponCode, errorMessage)
    }

    fun trackHotelV2CouponRemove(couponCode: String) {
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

}