package com.expedia.vm

import android.content.Context
import com.expedia.bookings.R
import com.expedia.bookings.data.ApiError
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.TravelerParams
import com.expedia.bookings.data.flights.FlightLeg
import com.expedia.bookings.data.flights.FlightSearchParams
import com.expedia.bookings.data.flights.FlightSearchResponse
import com.expedia.bookings.data.flights.FlightTripDetails
import com.expedia.bookings.data.packages.PackageOfferModel
import com.expedia.bookings.data.pos.PointOfSale
import com.expedia.bookings.services.FlightServices
import com.expedia.bookings.utils.DateUtils
import com.expedia.bookings.utils.FlightsV2DataUtil
import com.expedia.bookings.utils.SpannableBuilder
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.utils.validation.TravelerValidator
import com.expedia.ui.FlightActivity
import com.expedia.util.endlessObserver
import com.squareup.phrase.Phrase
import org.joda.time.LocalDate
import rx.Observer
import rx.subjects.BehaviorSubject
import rx.subjects.PublishSubject
import java.util.HashMap
import java.util.LinkedHashSet
import javax.inject.Inject

class FlightSearchViewModel(context: Context, val flightServices: FlightServices) : BaseSearchViewModel(context) {
    lateinit var travelerValidator: TravelerValidator
        @Inject set

    var flightMap: HashMap<String, LinkedHashSet<FlightLeg>> = HashMap()
    var flightOfferModels: HashMap<String, FlightTripDetails.FlightOffer> = HashMap()
    val errorObservable = PublishSubject.create<ApiError>()


    // Outputs
    val searchParamsObservable = BehaviorSubject.create<FlightSearchParams>()
    val cachedEndDateObservable = BehaviorSubject.create<LocalDate?>()
    val outboundResultsObservable = PublishSubject.create<List<FlightLeg>>()
    val inboundResultsObservable = BehaviorSubject.create<List<FlightLeg>>()
    val flightProductId = PublishSubject.create<String>()
    val confirmedOutboundFlightSelection = BehaviorSubject.create<FlightLeg>()
    val confirmedInboundFlightSelection = BehaviorSubject.create<FlightLeg>()
    val outboundSelected = BehaviorSubject.create<FlightLeg>()
    val inboundSelected = PublishSubject.create<FlightLeg>()
    val showChargesObFeesSubject = PublishSubject.create<Boolean>()
    val offerSelectedChargesObFeesSubject = BehaviorSubject.create<String>()
    val flightOfferSelected = PublishSubject.create<FlightTripDetails.FlightOffer>()
    val obFeeDetailsUrlObservable = BehaviorSubject.create<String>()
    val isRoundTripSearchObservable = BehaviorSubject.create<Boolean>(true)
    val flightParamsBuilder = FlightSearchParams.Builder(getMaxSearchDurationDays(), getMaxDateRange())
    val deeplinkDefaultTransitionObservable = PublishSubject.create<FlightActivity.Screen>()
    val airlinesChargePaymentFees = PointOfSale.getPointOfSale().doAirlinesChargeAdditionalFeeBasedOnPaymentMethod()


    val searchObserver = endlessObserver<Unit> {
        getParamsBuilder().maxStay = getMaxSearchDurationDays()
        if (getParamsBuilder().areRequiredParamsFilled()) {
            val flightSearchParams = getParamsBuilder().build()
            travelerValidator.updateForNewSearch(flightSearchParams)
            Db.setFlightSearchParams(flightSearchParams)
            searchParamsObservable.onNext(flightSearchParams)
        } else {
            if (!getParamsBuilder().hasOriginLocation()) {
                errorNoOriginObservable.onNext(Unit)
            } else if (!getParamsBuilder().hasDestinationLocation()) {
                errorNoDestinationObservable.onNext(Unit)
            } else if (!getParamsBuilder().hasValidDates()) {
                errorNoDatesObservable.onNext(Unit)
            } else if (getParamsBuilder().isOriginSameAsDestination()) {
                errorOriginSameAsDestinationObservable.onNext(context.getString(R.string.error_same_flight_departure_arrival))
            } else if (!getParamsBuilder().hasValidDateDuration()) {
                errorMaxDurationObservable.onNext(context.getString(R.string.hotel_search_range_error_TEMPLATE, getMaxSearchDurationDays()))
            }
        }
    }

    val isInfantInLapObserver = endlessObserver<Boolean> { isInfantInLap ->
        getParamsBuilder().infantSeatingInLap(isInfantInLap)
    }

    init {
        Ui.getApplication(context).travelerComponent().inject(this)
        setupFlightSelectionObservables()

        searchParamsObservable.subscribe { params ->
            flightServices.flightSearch(params).subscribe(makeResultsObserver())
        }

        outboundSelected.subscribe { flightLeg ->
            showChargesObFeesSubject.onNext(flightLeg.mayChargeObFees)
        }

        inboundSelected.subscribe { flightLeg ->
            showChargesObFeesSubject.onNext(flightLeg.mayChargeObFees)
        }

        showChargesObFeesSubject.subscribe { hasObFee ->
            if (hasObFee || airlinesChargePaymentFees) {
                val paymentFeeText = context.resources.getString(if (airlinesChargePaymentFees) R.string.airline_fee_notice_payment else R.string.payment_and_baggage_fees_may_apply)
                offerSelectedChargesObFeesSubject.onNext(paymentFeeText)
            }
        }

        isRoundTripSearchObservable.subscribe { isRoundTripSearch ->
            getParamsBuilder().isRoundTrip = isRoundTripSearch
            getParamsBuilder().maxStay = getMaxSearchDurationDays()
            if (datesObservable.value != null && datesObservable.value.first != null) {
                val cachedEndDate = cachedEndDateObservable.value
                if (isRoundTripSearch && cachedEndDate != null && startDate()?.isBefore(cachedEndDate) ?: false) {
                    datesObserver.onNext(Pair(startDate(), cachedEndDate))
                } else {
                    cachedEndDateObservable.onNext(endDate())
                    datesObserver.onNext(Pair(startDate(), null))
                }
            } else {
                dateTextObservable.onNext(context.resources.getString(if (isRoundTripSearch) R.string.select_dates else R.string.select_departure_date))
            }
        }
    }

    override fun sameStartAndEndDateAllowed(): Boolean {
        return true
    }

    override fun getParamsBuilder(): FlightSearchParams.Builder {
        return flightParamsBuilder
    }

    override fun isStartDateOnlyAllowed(): Boolean {
        return true
    }

    override fun getMaxSearchDurationDays(): Int {
        // 0 for one-way searches
        return if (isRoundTripSearchObservable.value) context.resources.getInteger(R.integer.calendar_max_days_flight_search) else 0
    }

    override fun getMaxDateRange(): Int {
        return context.resources.getInteger(R.integer.calendar_max_days_flight_search)
    }

    override fun computeDateInstructionText(start: LocalDate?, end: LocalDate?): CharSequence {
        if (start == null && end == null) {
            return context.getString(R.string.select_departure_date);
        }

        val dateRangeText = computeDateRangeText(start, end)
        val sb = SpannableBuilder()
        sb.append(dateRangeText)

        return sb.build()
    }

    override fun computeDateRangeText(start: LocalDate?, end: LocalDate?): String? {
        if (start == null && end == null) {
            return context.resources.getString(R.string.select_dates)
        } else if (end == null) {
            val stringResId =
                    if (isRoundTripSearchObservable.value)
                        R.string.select_return_date_TEMPLATE
                    else
                        R.string.calendar_instructions_date_range_flight_one_way_TEMPLATE

            return Phrase.from(context.resources, stringResId)
                    .put("startdate", DateUtils.localDateToMMMd(start))
                    .format().toString()
        } else {
            return Phrase.from(context, R.string.calendar_instructions_date_range_TEMPLATE).put("startdate", DateUtils.localDateToMMMd(start)).put("enddate", DateUtils.localDateToMMMd(end)).format().toString()
        }
    }

    override fun computeDateText(start: LocalDate?, end: LocalDate?): CharSequence {
        return computeDateRangeText(start, end).toString()
    }

    override fun computeTooltipText(start: LocalDate?, end: LocalDate?): Pair<String, String> {
        val instructions =
                if (isRoundTripSearchObservable.value) {
                    val instructionStringResId =
                            if (end == null)
                                R.string.calendar_instructions_date_range_flight_select_return_date
                            else
                                R.string.calendar_drag_to_modify
                    context.resources.getString(instructionStringResId)
                }
                else {
                    ""
                }
        return Pair(computeTopTextForToolTip(start, end), instructions)
    }

    fun clearDestinationLocation() {
        getParamsBuilder().destination(null)
        formattedDestinationObservable.onNext("")
        requiredSearchParamsObserver.onNext(Unit)
    }

    fun makeResultsObserver(): Observer<FlightSearchResponse> {
        return object : Observer<FlightSearchResponse> {
            override fun onNext(response: FlightSearchResponse) {
                if (response.hasErrors()) {
                    errorObservable.onNext(response.firstError)
                } else if (response.offers.isEmpty() || response.legs.isEmpty()) {
                    errorObservable.onNext(ApiError(ApiError.Code.FLIGHT_SEARCH_NO_RESULTS))
                } else {
                    createFlightMap(response)
                    obFeeDetailsUrlObservable.onNext(if (airlinesChargePaymentFees) PointOfSale.getPointOfSale().airlineFeeBasedOnPaymentMethodTermsAndConditionsURL else response.obFeesDetails)

                }
            }

            override fun onCompleted() {
                println("flight completed")
            }

            override fun onError(e: Throwable?) {
                println("flight error: " + e?.message)
            }
        }
    }

    fun findInboundFlights(outboundFlightId: String) : List<FlightLeg> {
        val flights = flightMap[outboundFlightId]?.toList() ?: emptyList()
        flights.forEach { inbound ->
            val offer = getFlightOffer(outboundFlightId, inbound.legId)
            if (offer != null) {
                val offerModel = makeOffer(offer)
                inbound.packageOfferModel = offerModel
            }
        }
        return flights
    }

    private fun setupFlightSelectionObservables() {
        confirmedOutboundFlightSelection.subscribe { flight ->
            if (isRoundTripSearchObservable.value) {
                inboundResultsObservable.onNext(findInboundFlights(flight.legId))
            }
            else { // one-way flights
                val outboundLegId = flight.legId
                val inboundLegId = flight.legId // yes, they are the same. It will get us the flight offer
                selectFlightOffer(outboundLegId, inboundLegId)
            }
        }

        // return trip flights
        confirmedInboundFlightSelection.subscribe {
            val inboundLegId = it.legId
            val outboundLegId = confirmedOutboundFlightSelection.value.legId
            selectFlightOffer(outboundLegId, inboundLegId)
        }

        // fires offer selected before flight selection confirmed to lookup terms, fees etc. in offer
        inboundSelected.subscribe {
            val offer = getFlightOffer(outboundSelected.value.legId, it.legId)
            flightOfferSelected.onNext(offer)
        }
    }

    private fun createFlightMap(response: FlightSearchResponse) {
        val outBoundFlights: LinkedHashSet<FlightLeg> = LinkedHashSet()
        val offers = response.offers
        val legs = response.legs
        offers.forEach { offer ->
            val outboundId = offer.legIds.first()
            val inboundId = offer.legIds.last()
            flightOfferModels.put(makeFlightOfferKey(outboundId, inboundId), offer)

            val outboundLeg = legs.find { it.legId == outboundId }

            val inboundLeg = legs.find { it.legId == inboundId }
            if (outboundLeg != null) {
                // assuming all offers are sorted by price by API
                val hasCheapestOffer = !outBoundFlights.contains(outboundLeg)
                if (hasCheapestOffer) {
                    outboundLeg.packageOfferModel = makeOffer(offer)
                }
                outBoundFlights.add(outboundLeg)
            }
            var flights = flightMap[outboundId]
            if (flights == null) {
                flights  = LinkedHashSet()
            }
            if (inboundLeg != null) {
                flights.add(inboundLeg)
            }
            flightMap.put(outboundId, flights)
        }
        outboundResultsObservable.onNext(outBoundFlights.toList())
    }

    private fun makeOffer(offer : FlightTripDetails.FlightOffer) : PackageOfferModel {
        val offerModel = PackageOfferModel()
        val urgencyMessage = PackageOfferModel.UrgencyMessage()
        urgencyMessage.ticketsLeft = offer.seatsRemaining
        val price = PackageOfferModel.PackagePrice()
        price.packageTotalPrice = offer.totalFarePrice
        price.differentialPriceFormatted = offer.totalFarePrice.formattedPrice
        price.packageTotalPriceFormatted = offer.totalFarePrice.formattedPrice
        price.averageTotalPricePerTicket = offer.averageTotalPricePerTicket
        price.pricePerPersonFormatted = offer.averageTotalPricePerTicket.formattedWholePrice
        offerModel.urgencyMessage = urgencyMessage
        offerModel.price = price
        return offerModel
    }

    private fun selectFlightOffer(outboundLegId: String, inboundLegId: String) {
        val offer = getFlightOffer(outboundLegId, inboundLegId)
        if (offer != null) {
            flightProductId.onNext(offer.productKey)
            flightOfferSelected.onNext(offer)
        }
    }

    private fun getFlightOffer(outboundLegId: String, inboundLegId: String): FlightTripDetails.FlightOffer? {
        return flightOfferModels[makeFlightOfferKey(outboundLegId, inboundLegId)]
    }

    private fun makeFlightOfferKey(outboundId: String, inboundId: String): String {
        return outboundId + inboundId
    }

    val deeplinkFlightSearchParamsObserver = endlessObserver<com.expedia.bookings.data.FlightSearchParams> { searchParams ->
        //Setup the viewmodel according to the provided params
        datesObserver.onNext(Pair(searchParams.departureDate, searchParams.returnDate))
        val departureSuggestion = FlightsV2DataUtil.getSuggestionFromDeeplinkLocation(searchParams.departureLocation?.destinationId)
        if (departureSuggestion != null) {
            originLocationObserver.onNext(departureSuggestion)
        }
        val arrivalSuggestion = FlightsV2DataUtil.getSuggestionFromDeeplinkLocation(searchParams.arrivalLocation?.destinationId)
        if (arrivalSuggestion != null) {
            destinationLocationObserver.onNext(arrivalSuggestion)
        }
        travelersObservable.onNext(TravelerParams(searchParams.numAdults, emptyList()))

        if (flightParamsBuilder.areRequiredParamsFilled()) {
            deeplinkDefaultTransitionObservable.onNext(FlightActivity.Screen.RESULTS)
        } else {
            deeplinkDefaultTransitionObservable.onNext(FlightActivity.Screen.SEARCH)
        }
        searchObserver.onNext(Unit)
    }

}
