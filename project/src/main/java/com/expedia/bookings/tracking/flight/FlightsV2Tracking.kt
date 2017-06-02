package com.expedia.bookings.tracking.flight

import com.expedia.bookings.data.ApiError
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.FlightFilter
import com.expedia.bookings.data.PaymentType
import com.expedia.bookings.data.flights.FlightCheckoutResponse
import com.expedia.bookings.data.flights.FlightCreateTripResponse
import com.expedia.bookings.data.flights.FlightLeg
import com.expedia.bookings.data.flights.FlightSearchParams
import com.expedia.bookings.tracking.FacebookEvents
import com.expedia.bookings.tracking.OmnitureTracking
import com.expedia.bookings.tracking.hotel.PageUsableData
import com.expedia.bookings.utils.TuneUtils
import com.expedia.vm.BaseFlightFilterViewModel
import com.expedia.vm.InsuranceViewModel

object FlightsV2Tracking {
    fun trackSearchPageLoad() {
        OmnitureTracking.trackPageLoadFlightSearchV2()
    }

    fun trackTravelerPickerClick(actionLabel: String) {
        OmnitureTracking.trackFlightTravelerPickerClick(actionLabel)
    }

    fun trackResultOutBoundFlights(trackingData: FlightSearchTrackingData) {
        OmnitureTracking.trackResultOutBoundFlights(trackingData)
        TuneUtils.trackFlightV2OutBoundResults(trackingData)
        FacebookEvents().trackFlightV2Search(trackingData)
    }

    fun trackFlightOverview(isOutboundFlight: Boolean, isRoundTrip: Boolean) {
        OmnitureTracking.trackFlightOverview(isOutboundFlight, isRoundTrip)
    }

    fun trackResultInBoundFlights(trackingData: FlightSearchTrackingData) {
        OmnitureTracking.trackResultInBoundFlights(trackingData)
        TuneUtils.trackFlightV2InBoundResults(trackingData)
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

    fun trackFlightFilterDone(flightLegs: List<FlightLeg>) {
        val flightSearchParams = Db.getFlightSearchParams()
        FacebookEvents().trackFilteredFlightV2Search(flightSearchParams, flightLegs)
    }

    fun trackShowFlightOverView(flightSearchParams: FlightSearchParams, flightCreateTripResponse: FlightCreateTripResponse,
                                overviewPageUsableData: PageUsableData) {
        OmnitureTracking.trackShowFlightOverView(flightSearchParams, overviewPageUsableData)
        TuneUtils.trackFlightV2RateDetailOverview(flightSearchParams)
        FacebookEvents().trackFlightV2Detail(flightSearchParams, flightCreateTripResponse)
    }

    fun trackOverviewFlightExpandClick(isExpanding: Boolean) {
        OmnitureTracking.trackOverviewFlightExpandClick(isExpanding)
    }

    fun trackFlightCostBreakdownClick() {
        OmnitureTracking.trackFlightCostBreakdownClick()
    }

    fun trackCheckoutInfoPageLoad() {
        val tripResponse = Db.getTripBucket().flightV2.flightCreateTripResponse
        val searchParams = Db.getFlightSearchParams()
        OmnitureTracking.trackFlightCheckoutInfoPageLoad(tripResponse)
        FacebookEvents().trackFlightV2Checkout(tripResponse, searchParams)
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

    @JvmStatic fun trackCheckoutSelectTraveler() {
        OmnitureTracking.trackFlightCheckoutSelectTraveler()
    }

    fun trackCheckoutEditTraveler() {
        OmnitureTracking.trackFlightCheckoutTravelerEditInfo()
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

    fun trackSlideToPurchase(cardType: PaymentType, flexStatus: String) {
        val cardName = cardType.omnitureTrackingCode
        OmnitureTracking.trackSlideToPurchase(cardName, flexStatus)
    }

    fun trackCheckoutPaymentCID() {
        OmnitureTracking.trackFlightCheckoutPaymentCID()
    }

    fun trackCheckoutConfirmationPageLoad(flightCheckoutResponse: FlightCheckoutResponse, pageUsableData: PageUsableData) {
        val searchParams = Db.getFlightSearchParams()
        OmnitureTracking.trackFlightCheckoutConfirmationPageLoad(pageUsableData)
        TuneUtils.trackFlightV2Booked(flightCheckoutResponse, searchParams)
        FacebookEvents().trackFlightV2Confirmation(flightCheckoutResponse, searchParams)
    }

    fun trackFlightSearchAPINoResponseError() {
        trackFlightError("Flight Search API No Response Error")
    }

    fun trackFlightCreateTripNoResponseError() {
        trackFlightError("Flight Create Trip API No Response Error")
    }

    fun trackFlightCheckoutAPINoResponseError() {
        trackFlightError("Flight Checkout API No Response Error")
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

    fun trackFlightCheckoutError(error: ApiError) {
        OmnitureTracking.trackFlightCheckoutError(createCheckoutError(error))
    }

    fun trackFlightCreateTripPriceChange(diffPercentage: Int) {
        OmnitureTracking.trackFlightCreateTripPriceChange(diffPercentage)
    }

    fun trackFlightCheckoutPriceChange(diffPercentage: Int) {
        OmnitureTracking.trackFlightCheckoutPriceChange(diffPercentage)
    }

    fun trackFlightCabinClassSelect(cabinClass: String) {
        OmnitureTracking.trackFlightCabinClassSelect(cabinClass);
    }

    fun trackAirAttachShown() {
        OmnitureTracking.trackFlightConfirmationAirAttachEligible()
    }

    fun trackAirAttachClicked() {
        OmnitureTracking.trackFlightConfirmationAirAttachClick()
    }

    fun trackCrossSellPackageBannerClick() {
        OmnitureTracking.trackCrossSellPackageBannerClick()
    }

    fun createCheckoutError(error: ApiError): String {
        var errorType = "CKO:"
        val eSource = if (!error.errorInfo?.source.isNullOrEmpty()) "${error.errorInfo?.source}:" else ":"
        val eSourceErrorId = error.errorInfo?.sourceErrorId ?: error.errorCode
        return "$errorType$eSource$eSourceErrorId"
    }

    fun trackFlightsTimeToClick(timeTaken: String?) {
        OmnitureTracking.trackFlightsTimeToClick(timeTaken)
    }

    fun trackFlightsSearchFieldsChanged() {
        OmnitureTracking.trackFlightsSearchFieldsChanged()
    }
}
