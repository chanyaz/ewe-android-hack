package com.expedia.bookings.tracking

import com.expedia.bookings.data.FlightFilter
import com.expedia.bookings.data.PaymentType
import com.expedia.bookings.data.flights.FlightSearchParams
import com.expedia.vm.BaseFlightFilterViewModel
import com.expedia.vm.InsuranceViewModel

object FlightsV2Tracking {
    fun trackSearchPageLoad() {
        OmnitureTracking.trackPageLoadFlightSearchV2()
    }

    fun trackTravelerPickerClick(actionLabel: String) {
        OmnitureTracking.trackFlightTravelerPickerClick(actionLabel)
    }

    fun trackResultOutBoundFlights(flightSearchParams: FlightSearchParams) {
        OmnitureTracking.trackResultOutBoundFlights(flightSearchParams)
    }

    fun trackFlightOverview(isOutboundFlight: Boolean) {
        OmnitureTracking.trackFlightOverview(isOutboundFlight)
    }

    fun trackResultInBoundFlights() {
        OmnitureTracking.trackResultInBoundFlights()
    }

    fun trackFlightBaggageFeeClick() {
        OmnitureTracking.trackFlightBaggageFeesClick()
    }

    fun trackPaymentFeesClick() {
        OmnitureTracking.trackFlightPaymentFeesClick()
    }

    fun trackSortFilterClick() {
        OmnitureTracking.trackSortFilterClick()
    }

    fun trackFlightSortBy(sortBy: FlightFilter.Sort) {
        val sortedBy = ( when (sortBy) {
            FlightFilter.Sort.PRICE -> "Price"
            FlightFilter.Sort.ARRIVAL -> "Arrival"
            FlightFilter.Sort.DEPARTURE -> "Departure"
            FlightFilter.Sort.DURATION -> "Duration"
        })
        OmnitureTracking.trackFlightSortBy(sortedBy)
    }

    fun trackFlightFilterStops(stops: BaseFlightFilterViewModel.Stops) {
        val processedStops = ( when (stops) {
            BaseFlightFilterViewModel.Stops.NONSTOP -> "No Stops"
            BaseFlightFilterViewModel.Stops.ONE_STOP -> "1 Stop"
            BaseFlightFilterViewModel.Stops.TWO_PLUS_STOPS -> "2 Stops"
        })
        OmnitureTracking.trackFlightFilterStops(processedStops)
    }

    fun trackFlightFilterAirlines() {
        OmnitureTracking.trackFlightFilterAirlines()
    }

    fun trackShowFlightOverView(flightSearchParams: FlightSearchParams) {
        OmnitureTracking.trackShowFlightOverView(flightSearchParams)
    }

    fun trackOverviewFlightExpandClick() {
        OmnitureTracking.trackOverviewFlightExpandClick()
    }

    fun trackFlightCostBreakdownClick() {
        OmnitureTracking.trackFlightCostBreakdownClick()
    }

    fun trackCheckoutInfoPageLoad() {
        OmnitureTracking.trackFlightCheckoutInfoPageLoad()
    }

    fun trackInsuranceUpdated(insuranceAction: InsuranceViewModel.InsuranceAction) {
        val action = if (insuranceAction === InsuranceViewModel.InsuranceAction.ADD)
            InsuranceViewModel.InsuranceAction.ADD.toString()
        else
            InsuranceViewModel.InsuranceAction.REMOVE.toString()

        OmnitureTracking.trackFlightInsuranceAdd(action)
    }

    fun trackInsuranceBenefitsClick() {
        OmnitureTracking.trackFlightInsuranceBenefitsClick()
    }

    fun trackInsuranceError(message: String?) {
        OmnitureTracking.trackFlightInsuranceError(message)
    }

    fun trackInsuranceTermsClick() {
        OmnitureTracking.trackFlightInsuranceTermsClick()
    }

    fun trackFlightPriceChange(pricePercentageChange: Int) {
        OmnitureTracking.trackFlightPriceChange(pricePercentageChange)
    }

    @JvmStatic fun trackCheckoutSelectTraveler() {
        OmnitureTracking.trackFlightCheckoutSelectTraveler()
    }

    fun trackCheckoutEditTraveler() {
        OmnitureTracking.trackFlightCheckoutEditTraveler()
    }

    fun trackPaymentStoredCCSelect() {
        OmnitureTracking.trackPaymentStoredCCSelect()
    }

    fun trackShowPaymentEdit() {
        OmnitureTracking.trackShowPaymentEdit()
    }

    fun trackCheckoutSelectPaymentClick() {
        OmnitureTracking.trackPaymentSelect()
    }

    fun trackSlideToPurchase(cardType: PaymentType) {
        val cardName = cardType.omnitureTrackingCode
        OmnitureTracking.trackSlideToPurchase(cardName)
    }

    fun trackCheckoutPaymentCID() {
        OmnitureTracking.trackFlightCheckoutPaymentCID()
    }

    fun trackCheckoutConfirmationPageLoad() {
        OmnitureTracking.trackFlightCheckoutConfirmationPageLoad()
    }

    fun trackFlightNoResult() {
        trackFlightError("Flight No Result")
    }

    fun trackFlightSearchUnknownError() {
        trackFlightError("Flight Search Unknown Error")
    }

    fun trackFlightCreateUnknownError() {
        trackFlightError("Flight Create Trip Unknown Error")
    }

    fun trackFlightCreateSessionTimeOutError() {
        trackFlightError("Flight Create Trip Session Timeout Error")
    }

    fun trackFlightCreateProductNotFoundError() {
        trackFlightError("Flight Create Trip Product Not Found Error")
    }

    fun trackFlightSoldOutError() {
        trackFlightError("Flight Sold Out Error")
    }

    private fun trackFlightError(errorType: String) {
        OmnitureTracking.trackFlightError(errorType)
    }

    fun trackFlightCheckoutUnknownError() {
        trackFlightCheckoutError("Flight Checkout Unknown Error")
    }

    fun trackFlightCheckoutPaymentError() {
        trackFlightCheckoutError("Flight Checkout Payment Error")
    }

    fun trackFlightCheckoutSessionTimeOutError() {
        trackFlightCheckoutError("Flight Checkout Session Timeout Error")
    }

    fun trackFlightTripBookedError() {
        trackFlightCheckoutError("Flight Trip Already Booked Error")
    }

    private fun trackFlightCheckoutError(errorType: String) {
        OmnitureTracking.trackFlightCheckoutError(errorType)
    }

}
