package com.expedia.vm

import android.content.Context
import com.expedia.bookings.BuildConfig
import com.expedia.bookings.R
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.SuggestionV4
import com.expedia.bookings.data.TravelerParams
import com.expedia.bookings.data.abacus.AbacusUtils
import com.expedia.bookings.data.flights.FlightSearchParams
import com.expedia.bookings.data.flights.FlightServiceClassType
import com.expedia.bookings.extensions.ObservableOld
import com.expedia.bookings.extensions.subscribeObserver
import com.expedia.bookings.extensions.withLatestFrom
import com.expedia.bookings.featureconfig.AbacusFeatureConfigManager
import com.expedia.bookings.shared.CalendarRules
import com.expedia.bookings.tracking.OmnitureTracking
import com.expedia.bookings.tracking.flight.FlightsV2Tracking
import com.expedia.bookings.utils.Constants
import com.expedia.bookings.utils.FlightsV2DataUtil
import com.expedia.bookings.utils.LocaleBasedDateFormatUtils
import com.expedia.bookings.utils.SearchParamsHistoryUtil
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.utils.isFlightGreedySearchEnabled
import com.expedia.bookings.utils.validation.TravelerValidator
import com.expedia.ui.FlightActivity
import com.expedia.util.FlightCalendarRules
import com.expedia.util.Optional
import com.expedia.util.endlessObserver
import com.expedia.vm.flights.AdvanceSearchFilter
import com.mobiata.android.util.SettingUtils
import com.squareup.phrase.Phrase
import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject
import org.joda.time.LocalDate
import javax.inject.Inject

class FlightSearchViewModel(context: Context) : BaseSearchViewModel(context) {
    override fun getCalendarRules(): CalendarRules {
        return if (isRoundTripSearchObservable.value) roundTripRules else oneWayRules
    }

    lateinit var travelerValidator: TravelerValidator
        @Inject set

    private val oneWayRules = FlightCalendarRules(context, false)
    private val roundTripRules = FlightCalendarRules(context, true)

    // Outputs
    val searchParamsObservable = BehaviorSubject.create<FlightSearchParams>()
    val cachedSearchParamsObservable = PublishSubject.create<FlightSearchParams>()
    val cachedEndDateObservable = BehaviorSubject.create<Optional<LocalDate>>()
    val isRoundTripSearchObservable = BehaviorSubject.createDefault<Boolean>(true)
    val deeplinkDefaultTransitionObservable = PublishSubject.create<FlightActivity.Screen>()
    val previousSearchParamsObservable = PublishSubject.create<FlightSearchParams>()
    var hasPreviousSearchParams = false
    val flightsSourceObservable = PublishSubject.create<SuggestionV4>()
    val flightsDestinationObservable = PublishSubject.create<SuggestionV4>()
    val swapToFromFieldsObservable = PublishSubject.create<Unit>()
    val isReadyForInteractionTracking = PublishSubject.create<Unit>()
    val searchTravelerParamsObservable = PublishSubject.create<com.expedia.bookings.data.FlightSearchParams>()
    val greedySearchParamsObservable = PublishSubject.create<FlightSearchParams>()
    val greedyCachedSearchParamsObservable = PublishSubject.create<FlightSearchParams>()
    val abortGreedyCallObservable = PublishSubject.create<Unit>()
    val cancelGreedyCallObservable = PublishSubject.create<Unit>()
    val validDateSetObservable = PublishSubject.create<Unit>()
    val trackSearchClicked = PublishSubject.create<Unit>()

    val EBAndroidAppFlightSubpubChange = AbacusFeatureConfigManager.isBucketedForTest(context, AbacusUtils.EBAndroidAppFlightSubpubChange)
    val isUserEvolableBucketed = AbacusFeatureConfigManager.isBucketedForTest(context, AbacusUtils.EBAndroidAppFlightsEvolable)
    var toAndFromFlightFieldsSwitched = false
    var isGreedyCallStarted = false

    protected var flightGreedySearchSubscription: Disposable? = null

    val flightParamsBuilder = FlightSearchParams.Builder(getCalendarRules().getMaxSearchDurationDays(),
            getCalendarRules().getMaxDateRange())

    val isInfantInLapObserver = endlessObserver<Boolean> { isInfantInLap ->
        getParamsBuilder().infantSeatingInLap(isInfantInLap)
    }

    val flightCabinClassObserver = endlessObserver<FlightServiceClassType.CabinCode> { cabinCode ->
        getParamsBuilder().flightCabinClass(cabinCode.name)
        if (isFlightGreedySearchEnabled(context) && !cabinCode.equals(FlightServiceClassType.CabinCode.COACH)) {
            abortGreedyCallObservable.onNext(Unit)
        }
    }

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
        if (isFlightGreedySearchEnabled(context)) {
            abortGreedyCallObservable.onNext(Unit)
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

    val performGreedyCallSearchObserver = endlessObserver<Unit> {
        val maxStay = getCalendarRules().getMaxSearchDurationDays()
        getParamsBuilder().maxStay = maxStay
        val flightSearchParams = getParamsBuilder().build()
        greedySearchParamsObservable.onNext(flightSearchParams)
        if (AbacusFeatureConfigManager.isBucketedForTest(context, AbacusUtils.EBAndroidAppFlightsSearchResultCaching)
                && shouldPerformCacheCalls(flightSearchParams)) {
            val cachedSearchParams = flightSearchParams.buildParamsForCachedSearch(maxStay, getCalendarRules().getMaxDateRange())
            greedyCachedSearchParamsObservable.onNext(cachedSearchParams)
        }
        isGreedyCallStarted = true
        flightGreedySearchSubscription?.dispose()
    }

    init {
        Ui.getApplication(context).travelerComponent().inject(this)

        isRoundTripSearchObservable.subscribe { isRoundTripSearch ->
            getParamsBuilder().roundTrip(isRoundTripSearch)
            getParamsBuilder().maxStay = getCalendarRules().getMaxSearchDurationDays()
            if (AbacusFeatureConfigManager.isBucketedForTest(context, AbacusUtils.EBAndroidAppFlightByotSearch)) {
                getParamsBuilder().legNo(if (isRoundTripSearch) 0 else null)
            }
            if (selectedDates.first != null) {
                val cachedEndDate = cachedEndDateObservable.value?.value
                if (isRoundTripSearch && cachedEndDate != null && startDate()?.isBefore(cachedEndDate) ?: false) {
                    datesUpdated(startDate(), cachedEndDate)
                } else {
                    cachedEndDateObservable.onNext(Optional(endDate()))
                    datesUpdated(startDate(), null)
                }
            } else {
                dateTextObservable.onNext(context.resources.getString(if (isRoundTripSearch) R.string.select_dates else R.string.select_departure_date))
            }
            searchTravelerParamsObservable.subscribe { searchParam ->
                performDeepLinkFlightSearch(searchParam)
            }
        }

        if (EBAndroidAppFlightSubpubChange) {
            flightParamsBuilder.setFeatureOverride(Constants.FEATURE_SUBPUB)
        }

        if (isUserEvolableBucketed) {
            flightParamsBuilder.setFeatureOverride(Constants.FEATURE_EVOLABLE)
        }

        if (isFlightGreedySearchEnabled(context)) {
            Observable.merge(dateSetObservable, isRoundTripSearchObservable).filter { getParamsBuilder().hasValidDates() }
                    .map { Unit }.subscribe(validDateSetObservable)

            flightGreedySearchSubscription = ObservableOld.combineLatest(formattedOriginObservable, formattedDestinationObservable,
                    validDateSetObservable, { _, _, _ -> Unit })
                    .filter { isReadyToFireSearchCall() }
                    .subscribeObserver(performGreedyCallSearchObserver)

            ObservableOld.combineLatest(greedySearchParamsObservable, flightsSourceObservable, flightsDestinationObservable, dateSetObservable,
                    { searchParams, origin, destination, _ ->
                        object {
                            val searchParams = searchParams
                            val origin = origin
                            val destination = destination
                        }
                    }).filter { isGreedyCallStarted }
                    .subscribe { combination ->
                        var isStartDateSelectionChanged = false
                        var isEndDateSelectionChanged = false
                        if (selectedDates.first != null) {
                            isStartDateSelectionChanged = !selectedDates.first!!.isEqual(combination.searchParams.startDate)
                        }
                        if (isRoundTripSearchObservable.value && combination.searchParams.endDate != null && selectedDates.second != null) {
                            isEndDateSelectionChanged = !selectedDates.second!!.isEqual(combination.searchParams.endDate)
                        }
                        if ((isStartDateSelectionChanged || isEndDateSelectionChanged) || (!combination.searchParams.departureAirport.equals(combination.origin)) ||
                                (!combination.searchParams.arrivalAirport.equals(combination.destination))) {
                            abortGreedyCallObservable.onNext(Unit)
                        }
                    }
            abortGreedyCallObservable.subscribe {
                flightGreedySearchSubscription?.dispose()
                cancelGreedyCallObservable.onNext(Unit)
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

    private fun isReadyToFireSearchCall(): Boolean {
        return getParamsBuilder().areRequiredParamsFilled() && !getParamsBuilder().isOriginSameAsDestination() && getParamsBuilder().hasValidDateDuration()
    }

    val performSearchObserver = endlessObserver<Unit> {
        val maxStay = getCalendarRules().getMaxSearchDurationDays()
        getParamsBuilder().maxStay = maxStay
        if (isReadyToFireSearchCall()) {
            val flightSearchParams = getParamsBuilder().build()
            travelerValidator.updateForNewSearch(flightSearchParams)
            Db.setFlightSearchParams(flightSearchParams)
            searchParamsObservable.onNext(flightSearchParams)
            if (isFlightGreedySearchEnabled(context) && isGreedyCallStarted) {
                trackSearchClicked.onNext(Unit)
                isGreedyCallStarted = false
            } else {
                    FlightsV2Tracking.trackSearchClick(flightSearchParams)
            }
            if (BuildConfig.DEBUG && SettingUtils.get(context, R.string.preference_enable_retain_prev_flight_search_params, false)) {
                SearchParamsHistoryUtil.saveFlightParams(context, flightSearchParams)
            }
            if (AbacusFeatureConfigManager.isBucketedForTest(context, AbacusUtils.EBAndroidAppFlightsSearchResultCaching)
                    && shouldPerformCacheCalls(flightSearchParams)) {
                val cachedSearchParams = flightSearchParams.buildParamsForCachedSearch(maxStay, getCalendarRules().getMaxDateRange())
                cachedSearchParamsObservable.onNext(cachedSearchParams)
            }
        } else {
            concurrentSearchFormValidation()
        }
    }

    private fun shouldPerformCacheCalls(flightSearchParams: FlightSearchParams): Boolean {
        return !flightSearchParams.hasAdvanceSearchOption() && flightSearchParams.flightCabinClass.equals(FlightServiceClassType.CabinCode.COACH.name) && flightSearchParams.children.isEmpty() && flightSearchParams.adults == 1
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
            errorMaxDurationObservable.onNext(context.getString(R.string.hotel_search_range_error_TEMPLATE,
                    getCalendarRules().getMaxSearchDurationDays()))
        }
    }

    fun performDeepLinkFlightSearch(searchParams: com.expedia.bookings.data.FlightSearchParams) {
        //Setup the viewmodel according to the provided params
        if (isFlightGreedySearchEnabled(context)) {
            abortGreedyCallObservable.onNext(Unit)
        }
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

        if (flightParamsBuilder.areRequiredParamsFilled() && !flightParamsBuilder.isOriginSameAsDestination() && flightParamsBuilder.hasValidDateDuration()) {
            deeplinkDefaultTransitionObservable.onNext(FlightActivity.Screen.RESULTS)
        } else {
            deeplinkDefaultTransitionObservable.onNext(FlightActivity.Screen.SEARCH)
        }
        performSearchObserver.onNext(Unit)
    }

    private fun setupViewModelFromPastSearch(pastSearchParams: FlightSearchParams) {
        isRoundTripSearchObservable.onNext(pastSearchParams.isRoundTrip())
        val currentDate = LocalDate.now()
        val isStartDateInvalid = pastSearchParams.departureDate.isBefore(currentDate)
        val isEndDateInvalid = pastSearchParams.returnDate?.isBefore(currentDate) ?: false

        if (isStartDateInvalid && isEndDateInvalid) {
            datesUpdated(null, null)
            highlightCalendarObservable.onNext(true)
        } else if (isStartDateInvalid && !isEndDateInvalid) {
            if (pastSearchParams.isRoundTrip()) {
                datesUpdated(currentDate, pastSearchParams.returnDate)
                highlightCalendarObservable.onNext(false)
            } else {
                datesUpdated(null, null)
                highlightCalendarObservable.onNext(true)
            }
        } else {
            datesUpdated(pastSearchParams.departureDate, pastSearchParams.returnDate)
            highlightCalendarObservable.onNext(false)
        }

        originLocationObserver.onNext(pastSearchParams.departureAirport)
        destinationLocationObserver.onNext(pastSearchParams.arrivalAirport)
    }

    override fun onDatesChanged(dates: Pair<LocalDate?, LocalDate?>) {
        var (start, end) = dates

        dateTextObservable.onNext(getCalendarCardDateText(start, end, false))
        dateAccessibilityObservable.onNext(getCalendarCardDateText(start, end, true))
        dateInstructionObservable.onNext(getDateInstructionText(start, end))
        calendarTooltipTextObservable.onNext(getToolTipText(start, end))
        calendarTooltipContDescObservable.onNext(getToolTipContentDescription(start, end, isRoundTripSearchObservable.value))

        if (!getCalendarRules().isStartDateOnlyAllowed()) {
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

    override fun getParamsBuilder(): FlightSearchParams.Builder {
        return flightParamsBuilder
    }

    override fun getDateInstructionText(start: LocalDate?, end: LocalDate?): CharSequence {
        if (start == null && end == null) {
            return context.getString(R.string.select_departure_date)
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
            val formattedDate = getStartToEndDateWithDayString(start, end)
            return getDateAccessibilityText(context.getString(R.string.select_dates), formattedDate)
        }
        return getStartDashEndDateWithDayString(start, end)
    }

    fun getFormattedDate(date: LocalDate?): String {
        if (date == null) {
            return ""
        }
        return LocaleBasedDateFormatUtils.localDateToEEEMMMd(date)
    }
}
