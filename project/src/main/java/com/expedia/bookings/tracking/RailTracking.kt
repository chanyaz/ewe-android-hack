package com.expedia.bookings.tracking

import com.expedia.bookings.data.PaymentType
import com.expedia.bookings.data.rail.requests.RailSearchRequest
import com.expedia.bookings.data.rail.responses.RailCheckoutResponse
import com.expedia.bookings.data.rail.responses.RailCreateTripResponse
import com.expedia.bookings.data.rail.responses.RailLeg
import com.expedia.bookings.utils.CarnivalUtils

class RailTracking {

    fun trackRailSearchInit() {
        OmnitureTracking.trackRailSearchInit()
    }

    fun trackRailSearchTravelerPickerChooser(actionLabel: String) {
        OmnitureTracking.trackRailSearchTravelerPickerChooser(actionLabel)
    }

    fun trackRailCardPicker(actionLabel: String) {
        OmnitureTracking.trackRailCardPicker(actionLabel)
    }

    fun trackRailRoundTripJourneyDetailsAndFareOptions() {
        OmnitureTracking.trackRailRoundTripJourneyDetailsAndFareOptions()
    }

    fun trackRailRoundTripInDetails() {
        OmnitureTracking.trackRailRoundTripInDetails()
    }

    fun trackRailOneWayTripDetails() {
        OmnitureTracking.trackRailOneWayTripDetails()
    }

    fun trackRailAmenities() {
        OmnitureTracking.trackRailAmenities()
    }

    fun trackRailFares() {
        OmnitureTracking.trackRailFares()
    }

    fun trackRailOneWaySearch(outboundLeg: RailLeg, railSearchRequest: RailSearchRequest) {
        OmnitureTracking.trackRailOneWaySearch(outboundLeg, railSearchRequest)
    }

    fun trackRailRoundTripOutbound(outboundLeg: RailLeg, railSearchRequest: RailSearchRequest) {
        OmnitureTracking.trackRailRoundTripOutbound(outboundLeg, railSearchRequest)
    }

    fun trackRailRoundTripInbound() {
        OmnitureTracking.trackRailRoundTripInbound()
    }

    fun trackRailConfirmation(checkoutResponse: RailCheckoutResponse) {
        OmnitureTracking.trackAppRailsCheckoutConfirmation(checkoutResponse)
        CarnivalUtils.getInstance().trackRailConfirmation(checkoutResponse)
    }

    fun trackRailDetails(railCreateTripResponse: RailCreateTripResponse) {
        OmnitureTracking.trackRailDetails(railCreateTripResponse)
    }

    fun trackRailDetailsTotalCostToolTip() {
        OmnitureTracking.trackRailDetailsTotalCostToolTip()
    }

    fun trackRailTripOverviewDetailsExpand() {
        OmnitureTracking.trackRailTripOverviewDetailsExpand()
    }

    fun trackRailCardsApiNoResponseError() {
        trackError("Rail Cards API No Response Error")
    }

    fun trackSearchApiNoResponseError() {
        trackError("Rail Search API No Response Error")
    }

    fun trackCreateTripApiNoResponseError() {
        trackError("Rail Create Trip API No Response Error")
    }

    fun trackCardFeeApiNoResponseError() {
        trackError("Rail Card Fee API No Response Error")
    }

    fun trackCheckoutApiNoResponseError() {
        trackError("Rail Checkout API No Response Error")
    }

    fun trackRailSearchNoResults() {
        trackError("Rail Search No Results")
    }

    fun trackSearchUnknownError() {
        trackError("Rail Search Unknown Error")
    }

    fun trackCreateTripUnknownError() {
        trackError("Rail Create Trip Unknown Error")
    }

    fun trackCardFeeUnknownError() {
        trackError("Rail Card Fee Unknown Error")
    }

    fun trackCheckoutUnknownError() {
        trackCheckoutError("Rail Checkout Unknown Error")
    }

    fun trackCheckoutInvalidInputError() {
        trackCheckoutError("Rail Checkout Invalid Input Error")
    }

    fun trackPriceChange(priceDiff: Int) {
        OmnitureTracking.trackRailCheckoutPriceChange(priceDiff)
    }

    private fun trackError(errorType: String) {
        OmnitureTracking.trackRailError(errorType)
    }

    private fun trackCheckoutError(errorType: String) {
        OmnitureTracking.trackRailCheckoutError(errorType)
    }

    fun trackRailCheckoutInfo(railCreateTripResponse: RailCreateTripResponse) {
        OmnitureTracking.trackRailCheckoutInfo(railCreateTripResponse)
    }

    fun trackRailCheckoutTotalCostToolTip() {
        OmnitureTracking.trackRailCheckoutTotalCostToolTip()
    }

    fun trackRailEditTravelerInfo() {
        OmnitureTracking.trackRailEditTravelerInfo()
    }

    fun trackRailEditPaymentInfo() {
        OmnitureTracking.trackRailEditPaymentInfo()
    }

    fun trackRailCheckoutSlideToPurchase(paymentType: PaymentType) {
        OmnitureTracking.trackRailCheckoutSlideToPurchase(paymentType)
    }
}
