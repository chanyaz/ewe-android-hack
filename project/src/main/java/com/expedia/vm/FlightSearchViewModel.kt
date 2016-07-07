package com.expedia.vm

import android.content.Context
import com.expedia.bookings.R
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.flights.FlightLeg
import com.expedia.bookings.data.flights.FlightSearchParams
import com.expedia.bookings.data.flights.FlightSearchResponse
import com.expedia.bookings.data.flights.FlightTripDetails
import com.expedia.bookings.data.packages.PackageOfferModel
import com.expedia.bookings.services.FlightServices
import com.expedia.bookings.utils.DateUtils
import com.expedia.bookings.utils.SpannableBuilder
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.utils.validation.TravelerValidator
import com.expedia.util.endlessObserver
import com.squareup.phrase.Phrase
import org.joda.time.LocalDate
import rx.Observable
import rx.Observer
import rx.Subscription
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

    // Outputs
    val searchParamsObservable = PublishSubject.create<FlightSearchParams>()
    val cachedEndDateObservable = BehaviorSubject.create<LocalDate?>()
    val outboundResultsObservable = PublishSubject.create<List<FlightLeg>>()
    val inboundResultsObservable = PublishSubject.create<List<FlightLeg>>()
    val flightProductId = PublishSubject.create<String>()
    val confirmedOutboundFlightSelection = PublishSubject.create<FlightLeg>()
    val confirmedInboundFlightSelection = PublishSubject.create<FlightLeg>()
    val outboundSelected = PublishSubject.create<FlightLeg>()
    val inboundSelected = PublishSubject.create<FlightLeg>()
    val offerSelectedChargesObFeesSubject = PublishSubject.create<Boolean>()
    val flightOfferSelected = PublishSubject.create<FlightTripDetails.FlightOffer>()
    val obFeeDetailsUrlObservable = PublishSubject.create<String>()
    val isRoundTripSearchObservable = BehaviorSubject.create<Boolean>(true)
    val flightParamsBuilder = FlightSearchParams.Builder(getMaxSearchDurationDays(), getMaxDateRange())

    val searchObserver = endlessObserver<Unit> {
        getParamsBuilder().maxStay = getMaxSearchDurationDays()
        if (getParamsBuilder().areRequiredParamsFilled()) {
            if (getParamsBuilder().isOriginSameAsDestination()) {
                errorOriginSameAsDestinationObservable.onNext(context.getString(R.string.error_same_flight_departure_arrival))
            } else if (!getParamsBuilder().hasValidDateDuration()) {
                errorMaxDurationObservable.onNext(context.getString(R.string.hotel_search_range_error_TEMPLATE, getMaxSearchDurationDays()))
            } else {
                val flightSearchParams = getParamsBuilder().build()
                travelerValidator.updateForNewSearch(flightSearchParams)
                Db.setFlightSearchParams(flightSearchParams)
                searchParamsObservable.onNext(flightSearchParams)
            }
        } else {
            if (!getParamsBuilder().hasOriginAndDestination()) {
                errorNoDestinationObservable.onNext(Unit)
            } else if (!getParamsBuilder().hasStartAndEndDates()) {
                errorNoDatesObservable.onNext(Unit)
            }
        }
    }

    private var confirmedOutInSubscription: Subscription? = null
    private var inboundOutboundSelectSubscription: Subscription? = null
    private val resetFlightSelectionsSubject = PublishSubject.create<Unit>()

    init {
        Ui.getApplication(context).travelerComponent().inject(this)

        searchParamsObservable.subscribe { params ->
            flightServices.flightSearch(params).subscribe(makeResultsObserver())
        }
        searchParamsObservable.map { Unit }.subscribe(resetFlightSelectionsSubject)

        confirmedOutboundFlightSelection.subscribe { flight ->
            if (isRoundTripSearchObservable.value) {
                inboundResultsObservable.onNext(findInboundFlights(flight.legId))
            }
            else {
                val offer = flightOfferModels[makeFlightOfferKey(flight.legId,flight.legId)]
                if (offer != null) {
                    flightProductId.onNext(offer.productKey)
                    flightOfferSelected.onNext(offer)
                }
            }
        }

        resetFlightSelectionsSubject.subscribe {
            setupFlightSelectionObservables()
        }

        flightOfferSelected.map { it.mayChargeOBFees }.subscribe(offerSelectedChargesObFeesSubject)

        isRoundTripSearchObservable.subscribe { isRoundTripSearch ->
            if (datesObservable.value != null) {
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

    fun setupFlightSelectionObservables() {
        confirmedOutInSubscription?.unsubscribe()
        inboundOutboundSelectSubscription?.unsubscribe()

        confirmedOutInSubscription = Observable.combineLatest(confirmedOutboundFlightSelection, confirmedInboundFlightSelection, { outbound, inbound ->
            val offer = flightOfferModels[makeFlightOfferKey(outbound.legId, inbound.legId)]
            if (offer != null) {
                flightProductId.onNext(offer.productKey)
            }
        }).subscribe()

        inboundOutboundSelectSubscription = Observable.combineLatest(outboundSelected, inboundSelected, { outbound, inbound ->
            val offer = flightOfferModels[makeFlightOfferKey(outbound.legId, inbound.legId)]
            if (offer != null) {
                flightOfferSelected.onNext(offer)
            }
        }).subscribe()
    }

    fun clearDestinationLocation() {
        getParamsBuilder().destination(null)
        formattedDestinationObservable.onNext("")
        requiredSearchParamsObserver.onNext(Unit)
    }

    val isInfantInLapObserver = endlessObserver<Boolean> { isInfantInLap ->
        getParamsBuilder().infantSeatingInLap(isInfantInLap)
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

    fun makeResultsObserver(): Observer<FlightSearchResponse> {
        return object : Observer<FlightSearchResponse> {
            override fun onNext(response: FlightSearchResponse) {
                createFlightMap(response)
                obFeeDetailsUrlObservable.onNext(response.obFeesDetails)
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
            val offer = flightOfferModels[makeFlightOfferKey(outboundFlightId, inbound.legId)]
            if (offer != null) {
                val offerModel = makeOffer(offer)
                inbound.packageOfferModel = offerModel
            }
        }
        return flights
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
                    outboundLeg?.packageOfferModel = makeOffer(offer)
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
        price.pricePerPersonFormatted = offer.averageTotalPricePerTicket.formattedPrice
        offerModel.urgencyMessage = urgencyMessage
        offerModel.price = price
        return offerModel
    }

    private fun makeFlightOfferKey(outboundId: String, inboundId: String): String {
        return outboundId + inboundId
    }

    fun resetFlightSelections() {
        resetFlightSelectionsSubject.onNext(Unit)
    }
}
