package com.expedia.vm

import android.content.Context
import com.expedia.bookings.R
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.SuggestionV4
import com.expedia.bookings.data.TravelerParams
import com.expedia.bookings.data.abacus.AbacusUtils
import com.expedia.bookings.data.flights.FlightSearchParams
import com.expedia.bookings.data.flights.FlightServiceClassType
import com.expedia.bookings.tracking.OmnitureTracking
import com.expedia.bookings.tracking.flight.FlightsV2Tracking
import com.expedia.bookings.tracking.hotel.ControlPageUsableData
import com.expedia.bookings.utils.DateUtils
import com.expedia.bookings.utils.FlightSearchParamsHistoryUtil
import com.expedia.bookings.utils.FlightsV2DataUtil
import com.expedia.bookings.utils.FeatureToggleUtil
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.utils.validation.TravelerValidator
import com.expedia.ui.FlightActivity
import com.expedia.util.endlessObserver
import com.expedia.vm.flights.AdvanceSearchFilter
import com.squareup.phrase.Phrase
import org.joda.time.LocalDate
import rx.Observable
import rx.Subscription
import rx.subjects.BehaviorSubject
import rx.subjects.PublishSubject
import javax.inject.Inject

class FlightSearchViewModel(context: Context) : BaseSearchViewModel(context) {

    lateinit var travelerValidator: TravelerValidator
        @Inject set

    val controlPageUsableData = ControlPageUsableData()

    // Outputs
    val searchParamsObservable = BehaviorSubject.create<FlightSearchParams>()
    val cachedEndDateObservable = BehaviorSubject.create<LocalDate?>()
    val isRoundTripSearchObservable = BehaviorSubject.create<Boolean>(true)
    val deeplinkDefaultTransitionObservable = PublishSubject.create<FlightActivity.Screen>()
    val previousSearchParamsObservable = PublishSubject.create<FlightSearchParams>()
    var hasPreviousSearchParams = false
    val flightsSourceObservable = PublishSubject.create<SuggestionV4>()
    val flightsDestinationObservable = PublishSubject.create<SuggestionV4>()
    val swapToFromFieldsObservable = PublishSubject.create<Unit>()
    val showDaywithDate = Db.getAbacusResponse().isUserBucketedForTest(AbacusUtils.EBAndroidAppFlightDayPlusDateSearchForm)
    val isReadyForInteractionTracking = PublishSubject.create<Unit>()

    private val flightParamsBuilder = FlightSearchParams.Builder(getMaxSearchDurationDays(), getMaxDateRange())

    val isInfantInLapObserver = endlessObserver<Boolean> { isInfantInLap ->
        getParamsBuilder().infantSeatingInLap(isInfantInLap)
    }

    val flightCabinClassObserver = endlessObserver<FlightServiceClassType.CabinCode> { cabinCode ->
        getParamsBuilder().flightCabinClass(cabinCode.name)
    }

    var searchSubscription: Subscription? = null
    var toAndFromFlightFieldsSwitched = false
    val advanceSearchObserver = endlessObserver<AdvanceSearchFilter> {
        when (it) {
            AdvanceSearchFilter.NonStop -> {
                getParamsBuilder().nonStopFlight(it.isChecked)
                FlightsV2Tracking.trackAdvanceSearchFilterClick("NonStop", it.isChecked)
            }
            AdvanceSearchFilter.Refundable -> {
                getParamsBuilder().showRefundableFlight(it.isChecked)
                FlightsV2Tracking.trackAdvanceSearchFilterClick("Refundable", it.isChecked)
            }
        }
    }

    override val originLocationObserver = endlessObserver<SuggestionV4> { suggestion ->
        setOriginText(suggestion)
        flightsSourceObservable.onNext(suggestion)
    }

    override val destinationLocationObserver = endlessObserver<SuggestionV4> { suggestion ->
        setDestinationText(suggestion)
        flightsDestinationObservable.onNext(suggestion)
    }

    init {
        Ui.getApplication(context).travelerComponent().inject(this)

        isRoundTripSearchObservable.subscribe { isRoundTripSearch ->
            getParamsBuilder().roundTrip(isRoundTripSearch)
            getParamsBuilder().maxStay = getMaxSearchDurationDays()
            if (Db.getAbacusResponse().isUserBucketedForTest(AbacusUtils.EBAndroidAppFlightByotSearch)) {
                getParamsBuilder().legNo(if (isRoundTripSearch) 0 else null)
            }
            if (selectedDates.first != null) {
                val cachedEndDate = cachedEndDateObservable.value
                if (isRoundTripSearch && cachedEndDate != null && startDate()?.isBefore(cachedEndDate) ?: false) {
                    datesUpdated(startDate(), cachedEndDate)
                } else {
                    cachedEndDateObservable.onNext(endDate())
                    datesUpdated(startDate(), null)
                }
            } else {
                dateTextObservable.onNext(context.resources.getString(if (isRoundTripSearch) R.string.select_dates else R.string.select_departure_date))
            }
        }

        if (!((FeatureToggleUtil.isUserBucketedAndFeatureEnabled(context, AbacusUtils.EBAndroidAppFlightRetainSearchParams, R.string.preference_flight_retain_search_params)) ||
                (Db.getAbacusResponse().isUserBucketedForTest(AbacusUtils.EBAndroidAppFlightAdvanceSearch)))) {
            Observable.combineLatest(formattedOriginObservable, formattedDestinationObservable, dateSetObservable, {flyFrom, flyTo, date ->
                object {
                    val flyingFrom = flyFrom
                    val flyingTo = flyTo
                    val travelDate = date
                }
            }).subscribe {
                if (!controlPageUsableData.isTimerAborted()) {
                    if (controlPageUsableData.hasTimerStarted()) {
                        controlPageUsableData.abortTimer()
                    } else {
                        controlPageUsableData.markPageLoadStarted(System.currentTimeMillis())
                    }
                }
            }

            abortTimerObservable.subscribe{
                if (!controlPageUsableData.isTimerAborted()) {
                    controlPageUsableData.abortTimer()
                }
            }

            searchSubscription = searchParamsObservable.subscribe {
                if (controlPageUsableData.isTimerAborted()) {
                    FlightsV2Tracking.trackFlightsSearchFieldsChanged()
                } else {
                    controlPageUsableData.markAllViewsLoaded(System.currentTimeMillis())
                    FlightsV2Tracking.trackFlightsTimeToClick(controlPageUsableData.getLoadTimeInSeconds())
                }
            }
        }

        isReadyForInteractionTracking.subscribe {
            Observable.merge(formattedOriginObservable, formattedDestinationObservable, dateSetObservable).take(1).subscribe {
                OmnitureTracking.trackFlightSearchFormInteracted()
            }
        }

        previousSearchParamsObservable.subscribe { params ->
            hasPreviousSearchParams = true
            setupViewModelFromPastSearch(params)
        }

        swapToFromFieldsObservable.withLatestFrom(flightsSourceObservable, flightsDestinationObservable, {
            _, source, destination ->
            object {
                val source = source
                val destination = destination
            }
        }).subscribe {
            originLocationObserver.onNext(it.destination)
            destinationLocationObserver.onNext(it.source)
            FlightsV2Tracking.trackFlightLocationSwapViewClick()
        }
    }

    val performSearchObserver = endlessObserver<Unit> {
        getParamsBuilder().maxStay = getMaxSearchDurationDays()
        if (getParamsBuilder().areRequiredParamsFilled() && !getParamsBuilder().isOriginSameAsDestination()) {
            val flightSearchParams = getParamsBuilder().build()
            travelerValidator.updateForNewSearch(flightSearchParams)
            Db.setFlightSearchParams(flightSearchParams)
            searchParamsObservable.onNext(flightSearchParams)
            FlightsV2Tracking.trackSearchClick()
            if (Db.getAbacusResponse().isUserBucketedForTest(AbacusUtils.EBAndroidAppFlightRetainSearchParams)) {
                FlightSearchParamsHistoryUtil.saveFlightParams(context, flightSearchParams)
            }
            searchSubscription?.unsubscribe()
        } else {
            if (!Db.getAbacusResponse().isUserBucketedForTest(AbacusUtils.EBAndroidAppFlightSearchFormValidation)) {
                stepByStepSearchFormValidation()
            }
            else {
                concurrentSearchFormValidation()
            }
        }
    }

    fun stepByStepSearchFormValidation() {
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

    fun concurrentSearchFormValidation() {
        if (!getParamsBuilder().areRequiredParamsFilled()) {
            if (!getParamsBuilder().hasOriginLocation()) {
                errorNoOriginObservable.onNext(Unit)
            }
            if (!getParamsBuilder().hasDestinationLocation()) {
                errorNoDestinationObservable.onNext(Unit)
            }
            if (!getParamsBuilder().hasValidDates()) {
                errorNoDatesObservable.onNext(Unit)
            }
        } else if (getParamsBuilder().isOriginSameAsDestination()) {
            errorOriginSameAsDestinationObservable.onNext(context.getString(R.string.error_same_flight_departure_arrival))
        } else if (!getParamsBuilder().hasValidDateDuration()) {
            errorMaxDurationObservable.onNext(context.getString(R.string.hotel_search_range_error_TEMPLATE, getMaxSearchDurationDays()))
        }
    }

    fun performDeepLinkFlightSearch(searchParams: com.expedia.bookings.data.FlightSearchParams) {
        //Setup the viewmodel according to the provided params
        val oneWay = searchParams.departureDate != null && searchParams.returnDate == null
        isRoundTripSearchObservable.onNext(!oneWay)
        datesUpdated(searchParams.departureDate, searchParams.returnDate)
        val departureSuggestion = FlightsV2DataUtil.getSuggestionFromDeeplinkLocation(searchParams.departureLocation?.destinationId)
        if (departureSuggestion != null) {
            originLocationObserver.onNext(departureSuggestion)
        }
        val arrivalSuggestion = FlightsV2DataUtil.getSuggestionFromDeeplinkLocation(searchParams.arrivalLocation?.destinationId)
        if (arrivalSuggestion != null) {
            destinationLocationObserver.onNext(arrivalSuggestion)
        }
        travelersObservable.onNext(TravelerParams(searchParams.numAdults, emptyList(), emptyList(), emptyList()))

        if (flightParamsBuilder.areRequiredParamsFilled()) {
            deeplinkDefaultTransitionObservable.onNext(FlightActivity.Screen.RESULTS)
        } else {
            deeplinkDefaultTransitionObservable.onNext(FlightActivity.Screen.SEARCH)
        }
        performSearchObserver.onNext(Unit)
    }

    private fun setupViewModelFromPastSearch(pastSearchParams: FlightSearchParams) {
        isRoundTripSearchObservable.onNext(pastSearchParams.isRoundTrip())
        val currentDate = LocalDate.now()
        val invalidDates = pastSearchParams.departureDate.isBefore(currentDate) || pastSearchParams.returnDate?.isBefore(currentDate) ?: false
        if (!invalidDates) {
            datesUpdated(pastSearchParams.departureDate, pastSearchParams.returnDate)
        }
        originLocationObserver.onNext(pastSearchParams.departureAirport)
        destinationLocationObserver.onNext(pastSearchParams.arrivalAirport)
        isReadyForInteractionTracking.onNext(Unit)
    }

    fun clearDestinationLocation() {
        getParamsBuilder().destination(null)
        formattedDestinationObservable.onNext("")
        requiredSearchParamsObserver.onNext(Unit)
    }

    override  fun onDatesChanged(dates: Pair<LocalDate?, LocalDate?>) {
        var (start, end) = dates

        dateTextObservable.onNext(getCalendarCardDateText(start, end, false))
        dateAccessibilityObservable.onNext(getCalendarCardDateText(start, end, true))
        dateInstructionObservable.onNext(getDateInstructionText(start, end))
        calendarTooltipTextObservable.onNext(getToolTipText(start, end))
        calendarTooltipContDescObservable.onNext(getToolTipContentDescription(start, end, isRoundTripSearchObservable.value))

        if (!isStartDateOnlyAllowed()) {
            if (start != null && end == null) {
                end = start.plusDays(1)
            }
        }
        val hasValidDates = when (isRoundTripSearchObservable.value) {
            true -> start != null && end != null
            false -> start != null
        }
        hasValidDatesObservable.onNext(hasValidDates)

        super.onDatesChanged(Pair(start, end))
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

    override fun getDateInstructionText(start: LocalDate?, end: LocalDate?): CharSequence {
        if (start == null && end == null) {
            return context.getString(R.string.select_departure_date);
        } else if (end == null) {
            return getNoEndDateText(start, false)
        }
        return getCompleteDateText(start!!, end, false)
    }

    override fun getCalendarToolTipInstructions(start: LocalDate?, end: LocalDate?): String {
        if (isRoundTripSearchObservable.value && end == null) {
            return context.getString(R.string.calendar_instructions_date_range_flight_select_return_date)
        }
        return context.getString(R.string.calendar_drag_to_modify)
    }

    override fun getEmptyDateText(forContentDescription: Boolean): String {
        if (forContentDescription) {
            return context.getString(R.string.select_travel_dates_cont_desc)
        }
        return context.getString(R.string.select_dates)
    }

    override fun getNoEndDateText(start: LocalDate?, forContentDescription: Boolean): String {
        if (isRoundTripSearchObservable.value) {
            val dateString = Phrase.from(context.resources, R.string.select_return_date_TEMPLATE)
                    .put("startdate", getFormattedDate(start))
                    .format().toString()
            if (forContentDescription) {
                return getDateAccessibilityText(context.getString(R.string.select_dates), dateString)
            }
            return dateString
        } else {
            val dateString = Phrase.from(context.resources, R.string.calendar_instructions_date_range_flight_one_way_TEMPLATE)
                    .put("startdate", getFormattedDate(start))
                    .format().toString()
            if (forContentDescription) {
                return getDateAccessibilityText(context.getString(R.string.select_dates), dateString)
            }
            return dateString
        }
    }

    override fun getCompleteDateText(start: LocalDate, end: LocalDate, forContentDescription: Boolean): String {
        if (forContentDescription) {
            val formattedDate = if (showDaywithDate) getStartToEndDateWithDayString(start, end) else getStartToEndDateString(start, end)
            return getDateAccessibilityText(context.getString(R.string.select_dates), formattedDate)
        }
        if (showDaywithDate) {
            return getStartDashEndDateWithDayString(start, end)
        }
        else {
            return getStartDashEndDateString(start, end)
        }
    }

    fun getStartDashEndDateWithDayString(start: LocalDate, end: LocalDate) : String {
        return Phrase.from(context, R.string.calendar_instructions_date_range_flight_extra_spacing_TEMPLATE)
                .put("startdate", DateUtils.localDateToEEEMMMd(start))
                .put("enddate", DateUtils.localDateToEEEMMMd(end))
                .format().toString()
    }

    fun getFormattedDate(date: LocalDate?): String {
        if (showDaywithDate) {
            return DateUtils.localDateToEEEMMMd(date)
        }
        return DateUtils.localDateToMMMd(date)
    }

    protected fun getStartToEndDateWithDayString(start: LocalDate, end: LocalDate) : String {
        // need to explicitly use "to" for screen readers
        return Phrase.from(context, R.string.search_date_range_cont_desc_TEMPLATE)
                .put("startdate", DateUtils.localDateToEEEMMMd(start))
                .put("enddate", DateUtils.localDateToEEEMMMd(end))
                .format().toString()
    }

}
