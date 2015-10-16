package com.expedia.bookings.tracking

import com.expedia.bookings.data.hotels.HotelCreateTripResponse
import com.expedia.bookings.data.hotels.HotelOffersResponse
import com.expedia.bookings.data.hotels.HotelSearchParams
import com.expedia.bookings.data.hotels.HotelSearchResponse
import com.expedia.bookings.services.HotelCheckoutResponse
import com.expedia.bookings.utils.LeanPlumUtils
import com.expedia.bookings.utils.TuneUtils

class HotelV2Tracking {

    fun trackHotelV2SearchBox() {
        OmnitureTracking.trackHotelV2SearchBox()
    }

    fun trackTravelerPickerClick(text: String) {
        OmnitureTracking.trackTravelerPickerClick(text)
    }

    fun trackHotelsV2Search(searchParams: HotelSearchParams, searchResponse: HotelSearchResponse) {
        OmnitureTracking.internalTrackHotelsV2Search(searchParams, searchResponse)
        LeanPlumUtils.trackHotelV2Search(searchParams, searchResponse)
        TuneUtils.trackHotelV2SearchResults(searchParams, searchResponse)
        FacebookEvents().trackHotelV2Search(searchParams, searchResponse)
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

    fun trackPageLoadHotelV2Infosite(hotelOffersResponse: HotelOffersResponse, searchParams: HotelSearchParams, isETPEligible: Boolean, isCurrentLocationSearch: Boolean) {
        OmnitureTracking.trackPageLoadHotelV2Infosite(hotelOffersResponse, isETPEligible, isCurrentLocationSearch)
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

    fun trackLinkHotelV2ViewRoomClick() {
        OmnitureTracking.trackLinkHotelV2ViewRoomClick()
    }

    fun trackLinkHotelV2RoomInfoClick() {
        OmnitureTracking.trackLinkHotelV2RoomInfoClick()
    }

    fun trackLinkHotelV2DetailMapClick() {
        OmnitureTracking.trackLinkHotelV2DetailMapClick()
    }

    fun trackLinkHotelV2DetailBookPhoneClick() {
        OmnitureTracking.trackLinkHotelV2DetailBookPhoneClick()
    }

    fun trackLinkHotelV2DetailSelectRoom() {
        OmnitureTracking.trackLinkHotelV2DetailSelectRoom()
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

    fun trackPageLoadHotelV2CheckoutInfo(hotelProductResponse: HotelCreateTripResponse.HotelProductResponse, searchParams: HotelSearchParams) {
        OmnitureTracking.trackPageLoadHotelV2CheckoutInfo(hotelProductResponse, searchParams)
        LeanPlumUtils.trackHotelV2CheckoutStarted(hotelProductResponse)
        TuneUtils.trackHotelV2CheckoutStarted(hotelProductResponse)
        FacebookEvents().trackHotelV2Checkout(hotelProductResponse, searchParams)
    }

    fun trackHotelV2CheckoutTraveler() {
        OmnitureTracking.trackHotelV2CheckoutTraveler()
    }

    fun trackHotelV2PaymentInfo() {
        OmnitureTracking.trackHotelV2PaymentInfo()
    }

    fun trackHotelV2PaymentEdit() {
        OmnitureTracking.trackHotelV2PaymentEdit()
    }

    fun trackHotelV2SlideToPurchase() {
        OmnitureTracking.trackHotelV2SlideToPurchase()
    }

    fun trackHotelV2CheckoutPaymentCid() {
        OmnitureTracking.trackHotelV2CheckoutPaymentCid()
    }

    fun trackHotelV2PurchaseConfirmation(hotelCheckoutResponse: HotelCheckoutResponse) {
        OmnitureTracking.trackHotelV2PurchaseConfirmation(hotelCheckoutResponse)
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

    fun trackHotelV2CrossSellCar() {
        trackHotelV2ConfirmationCrossSell("Cars")
    }

    fun trackHotelV2CrossSellFlight() {
        trackHotelV2ConfirmationCrossSell("Flights")
    }

    private fun trackHotelV2ConfirmationCrossSell(businesType: String) {
        OmnitureTracking.trackHotelV2ConfirmationCrossSell(businesType)
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

}