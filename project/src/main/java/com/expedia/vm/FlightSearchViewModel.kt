package com.expedia.vm

import android.content.Context
import com.expedia.bookings.R
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.TravelerParams
import com.expedia.bookings.data.abacus.AbacusUtils
import com.expedia.bookings.data.flights.FlightSearchParams
import com.expedia.bookings.data.flights.FlightServiceClassType
import com.expedia.bookings.tracking.flight.FlightsV2Tracking
import com.expedia.bookings.tracking.hotel.ControlPageUsableData
import com.expedia.bookings.utils.DateUtils
import com.expedia.bookings.utils.FlightsV2DataUtil
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.utils.validation.TravelerValidator
import com.expedia.ui.FlightActivity
import com.expedia.util.endlessObserver
import com.squareup.phrase.Phrase
import org.joda.time.LocalDate
import rx.Observable
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

    private val flightParamsBuilder = FlightSearchParams.Builder(getMaxSearchDurationDays(), getMaxDateRange())

    val isInfantInLapObserver = endlessObserver<Boolean> { isInfantInLap ->
        getParamsBuilder().infantSeatingInLap(isInfantInLap)
    }

    val flightCabinClassObserver = endlessObserver<FlightServiceClassType.CabinCode> { cabinCode ->
        getParamsBuilder().flightCabinClass(cabinCode.name)
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

        searchParamsObservable.subscribe {
            if (controlPageUsableData.isTimerAborted()) {
                FlightsV2Tracking.trackFlightsSearchFieldsChanged()
            } else {
                controlPageUsableData.markAllViewsLoaded(System.currentTimeMillis())
                FlightsV2Tracking.trackFlightsTimeToClick(controlPageUsableData.getLoadTimeInSeconds())
            }
        }
    }

    val performSearchObserver = endlessObserver<Unit> {
        getParamsBuilder().maxStay = getMaxSearchDurationDays()
        if (getParamsBuilder().areRequiredParamsFilled() && !getParamsBuilder().isOriginSameAsDestination()) {
            val flightSearchParams = getParamsBuilder().build()
            travelerValidator.updateForNewSearch(flightSearchParams)
            Db.setFlightSearchParams(flightSearchParams)
            searchParamsObservable.onNext(flightSearchParams)
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
                    .put("startdate", DateUtils.localDateToMMMd(start))
                    .format().toString()
            if (forContentDescription) {
                return getDateAccessibilityText(context.getString(R.string.select_dates), dateString)
            }
            return dateString
        } else {
            val dateString = Phrase.from(context.resources, R.string.calendar_instructions_date_range_flight_one_way_TEMPLATE)
                    .put("startdate", DateUtils.localDateToMMMd(start))
                    .format().toString()
            if (forContentDescription) {
                return getDateAccessibilityText(context.getString(R.string.select_dates), dateString)
            }
            return dateString
        }
    }

    override fun getCompleteDateText(start: LocalDate, end: LocalDate, forContentDescription: Boolean): String {
        if (forContentDescription) {
            return getDateAccessibilityText(context.getString(R.string.select_dates), getStartToEndDateString(start, end))
        }
        return getStartDashEndDateString(start, end)
    }
}
