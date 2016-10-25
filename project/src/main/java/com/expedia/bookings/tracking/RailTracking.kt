package com.expedia.bookings.tracking

/**
 * Created by prassingh on 10/26/16.
 */
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
}