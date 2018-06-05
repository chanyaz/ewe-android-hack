package com.expedia.bookings.tracking.flight

import com.expedia.bookings.data.ApiError
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.FlightFilter
import com.expedia.bookings.data.LineOfBusiness
import com.expedia.bookings.data.PaymentType
import com.expedia.bookings.data.flights.FlightCheckoutResponse
import com.expedia.bookings.data.flights.FlightCreateTripResponse
import com.expedia.bookings.data.flights.FlightLeg
import com.expedia.bookings.data.flights.FlightSearchParams
import com.expedia.bookings.data.flights.KrazyglueResponse
import com.expedia.bookings.marketing.carnival.CarnivalUtils
import com.expedia.bookings.presenter.flight.FlightSummaryWidget
import com.expedia.bookings.tracking.ApiCallFailing
import com.expedia.bookings.tracking.FacebookEvents
import com.expedia.bookings.tracking.OmnitureTracking
import com.expedia.bookings.tracking.hotel.PageUsableData
import com.expedia.bookings.utils.TuneUtils
import com.expedia.vm.BaseFlightFilterViewModel
import com.expedia.vm.InsuranceViewModel

object FlightsV2Tracking {
    fun trackSearchClick(flightSearchParams: FlightSearchParams, trackGreedyCallEvent: Boolean = false, isGreedyCallAborted: Boolean = true) {
        OmnitureTracking.trackFlightSearchButtonClick(trackGreedyCallEvent, isGreedyCallAborted)
        CarnivalUtils.getInstance().trackFlightSearch(flightSearchParams.destination?.regionNames?.fullName, flightSearchParams.adults, flightSearchParams.departureDate)
    }

    fun trackSRPScrollDepth(scrollDepth: Int, isOutboundFlight: Boolean, isRoundTrip: Boolean, totalCount: Int) {
        OmnitureTracking.trackFlightSRPScrollDepth(scrollDepth, isOutboundFlight, isRoundTrip, totalCount)
    }

    fun trackSearchPageLoad() {
        OmnitureTracking.trackPageLoadFlightSearchV2()
    }

    fun trackRecentSearchDisplayed(count: Int) {
        OmnitureTracking.trackFlightsRecentSearchItemsDisplayed(count)
    }

    fun trackRecentSearchFieldChange(actionLabel: String) {
        OmnitureTracking.trackFlightsRecentSearchFieldChange(actionLabel)
    }

    fun trackRecentSearchItemClicked(index: Int, count: Int) {
        OmnitureTracking.trackFlightsRecentSearchItemClicked(index, count)
    }

    fun trackTravelerPickerClick(actionLabel: String) {
        OmnitureTracking.trackFlightTravelerPickerClick(actionLabel)
    }

    fun trackAdvanceSearchFilterClick(filterLabel: String, isSelected: Boolean) {
        OmnitureTracking.trackFlightAdvanceSearchFiltersClick(filterLabel, isSelected)
    }

    fun trackResultOutBoundFlights(trackingData: FlightSearchTrackingData, isSubpub: Boolean) {
        OmnitureTracking.trackResultOutBoundFlights(trackingData , isSubpub)
        TuneUtils.trackFlightV2OutBoundResults(trackingData)
        FacebookEvents().trackFlightV2Search(trackingData)
    }

    fun trackFlightOverview(isOutboundFlight: Boolean, isRoundTrip: Boolean, flight: FlightLeg) {
        OmnitureTracking.trackFlightOverview(isOutboundFlight, isRoundTrip, flight)
    }

    fun trackResultInBoundFlights(trackingData: FlightSearchTrackingData, outboundSelectedAndTotalLegRank: Pair<Int, Int>) {
        OmnitureTracking.trackResultInBoundFlights(trackingData, outboundSelectedAndTotalLegRank)
        TuneUtils.trackFlightV2InBoundResults(trackingData)
    }

    fun trackFlightBaggageFeeClick() {
        OmnitureTracking.trackFlightBaggageFeesClick()
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

    fun trackFlightFilterAirlines(selectedAirlineTag: String) {
        OmnitureTracking.trackFlightFilterAirlines(selectedAirlineTag)
    }

    fun trackFlightFilterDone(flightLegs: List<FlightLeg>) {
        val flightSearchParams = Db.getFlightSearchParams()
        FacebookEvents().trackFilteredFlightV2Search(flightSearchParams, flightLegs)
    }

    fun trackShowFlightOverView(flightSearchParams: FlightSearchParams, flightCreateTripResponse: FlightCreateTripResponse,
                                overviewPageUsableData: PageUsableData, outboundSelectedAndTotalLegRank: Pair<Int, Int>?, inboundSelectedAndTotalLegRank: Pair<Int, Int>?,
                                isFareFamilyAvailable: Boolean, isFareFamilySelected: Boolean, hasSubPub: Boolean, flightSummary: FlightSummaryWidget) {
        OmnitureTracking.trackShowFlightOverView(flightSearchParams, overviewPageUsableData, outboundSelectedAndTotalLegRank, inboundSelectedAndTotalLegRank,
                isFareFamilyAvailable, isFareFamilySelected, hasSubPub)
        TuneUtils.trackFlightV2RateDetailOverview(flightSearchParams)
        FacebookEvents().trackFlightV2Detail(flightSearchParams, flightCreateTripResponse)

        CarnivalUtils.getInstance().trackFlightCheckoutStart(flightSearchParams.destination?.regionNames?.fullName,
                flightSearchParams.adults, flightSearchParams.departureDate,
                flightSummary.outboundFlightWidget.viewModel.flight.value,
                flightSummary.inboundFlightWidget.viewModel.flight.value, flightSearchParams.isRoundTrip())
    }

    fun trackFareFamilyCardViewClick(isUpgradingFlight: Boolean) {
        OmnitureTracking.trackFareFamilyCardViewClick(isUpgradingFlight)
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
        else InsuranceViewModel.InsuranceAction.REMOVE.toString()

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

    fun trackConfirmationShareItinClicked() {
        OmnitureTracking.trackFlightConfirmationShareItinClicked()
    }

    fun trackKrazyglueExposure(krazyGlueHotels: List<KrazyglueResponse.KrazyglueHotel>?) {
        OmnitureTracking.trackFlightsKrazyglueExposure(krazyGlueHotels)
    }

    fun trackKrazyglueHotelClicked(position: Int) {
        OmnitureTracking.trackFlightsKrazyglueClick(position)
    }

    fun trackKrazyglueSeeMoreClicked() {
        OmnitureTracking.trackFlightsKrazyGlueSeeMoreClick()
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

    fun trackCheckoutSelectPaymentClick() {
        OmnitureTracking.trackPaymentSelect()
    }

    fun trackShowSlideToPurchase(cardType: PaymentType, flexStatus: String) {
        val cardName = cardType.omnitureTrackingCode
        OmnitureTracking.trackFlightShowSlideToPurchase(cardName, flexStatus)
    }

    fun trackCheckoutPaymentCID() {
        OmnitureTracking.trackFlightCheckoutPaymentCID()
    }

    fun trackCheckoutConfirmationPageLoad(flightCheckoutResponse: FlightCheckoutResponse, pageUsableData: PageUsableData, flightSummary: FlightSummaryWidget) {
        val searchParams = Db.getFlightSearchParams()
        OmnitureTracking.trackFlightCheckoutConfirmationPageLoad(pageUsableData)
        TuneUtils.trackFlightV2Booked(flightCheckoutResponse, searchParams)
        CarnivalUtils.getInstance().trackFlightCheckoutConfirmation(searchParams.destination?.regionNames?.fullName, searchParams.adults, searchParams.departureDate,
                flightSummary.outboundFlightWidget.viewModel.flight.value, flightSummary.inboundFlightWidget.viewModel.flight.value, searchParams.isRoundTrip())
        FacebookEvents().trackFlightV2Confirmation(flightCheckoutResponse, searchParams)
    }

    fun trackFlightShoppingError(apiCallFailing: ApiCallFailing) {
        OmnitureTracking.trackFlightShoppingError(apiCallFailing.getErrorStringForTracking())
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

    fun trackFlightCabinClassViewDisplayed(lineOfBusiness: LineOfBusiness) {
        OmnitureTracking.trackFlightCabinClassViewDisplayed(lineOfBusiness)
    }

    fun trackFlightCabinClassSelect(lineOfBusiness: LineOfBusiness, cabinClass: String) {
        OmnitureTracking.trackFlightCabinClassSelect(lineOfBusiness, cabinClass)
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

    fun trackRouteHappyEmptyResults(isOutboundFlight: Boolean, isRoundTrip: Boolean) {
        OmnitureTracking.trackRouteHappyResultsEmptyResults(isOutboundFlight, isRoundTrip)
    }

    fun trackRouteHappyResultCountRatio(isOutboundFlight: Boolean, isRoundTrip: Boolean, routeHappyCount: Int, totalCount: Int) {
        OmnitureTracking.trackRouteHappyResultsCountRatio(isOutboundFlight, isRoundTrip, routeHappyCount, totalCount)
    }

    fun trackRouteHappyNotDisplayed(isOutboundFlight: Boolean, isRoundTrip: Boolean) {
        OmnitureTracking.trackRouteHappyNotApplicable(isOutboundFlight, isRoundTrip)
    }

    fun createCheckoutError(error: ApiError): String {
        val errorType = "CKO:"
        val eSource = if (!error.errorInfo?.source.isNullOrEmpty()) "${error.errorInfo?.source}:" else ":"
        val eSourceErrorId = error.errorInfo?.sourceErrorId ?: error.getErrorCode()
        return "$errorType$eSource$eSourceErrorId"
    }
}
