package com.expedia.bookings.tracking

import com.expedia.bookings.data.rail.requests.RailSearchRequest
import com.expedia.bookings.data.rail.responses.RailCheckoutResponse
import com.expedia.bookings.data.rail.responses.RailCreateTripResponse
import com.expedia.bookings.data.rail.responses.RailLeg
import com.expedia.bookings.data.rail.responses.RailSearchResponse

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
    }
}