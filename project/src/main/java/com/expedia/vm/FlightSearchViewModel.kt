package com.expedia.vm

import android.content.Context
import com.expedia.bookings.R
import com.expedia.bookings.data.ApiError
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.TravelerParams
import com.expedia.bookings.data.flights.FlightSearchParams
import com.expedia.bookings.data.flights.FlightSearchResponse
import com.expedia.bookings.dialog.DialogFactory
import com.expedia.bookings.services.FlightServices
import com.expedia.bookings.utils.DateUtils
import com.expedia.bookings.utils.FlightsV2DataUtil
import com.expedia.bookings.utils.RetrofitUtils
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
import javax.inject.Inject

class FlightSearchViewModel(context: Context, val flightServices: FlightServices) : BaseSearchViewModel(context) {

    lateinit var travelerValidator: TravelerValidator
        @Inject set

    val errorObservable = PublishSubject.create<ApiError>()
    val noNetworkObservable = PublishSubject.create<Unit>()

    // Outputs
    val searchParamsObservable = BehaviorSubject.create<FlightSearchParams>()
    val cachedEndDateObservable = BehaviorSubject.create<LocalDate?>()
    val isRoundTripSearchObservable = BehaviorSubject.create<Boolean>(true)
    val deeplinkDefaultTransitionObservable = PublishSubject.create<FlightActivity.Screen>()
    val flightSearchResponseSubject = PublishSubject.create<FlightSearchResponse>()

    private val flightParamsBuilder = FlightSearchParams.Builder(getMaxSearchDurationDays(), getMaxDateRange())

    init {
        Ui.getApplication(context).travelerComponent().inject(this)

        searchParamsObservable.subscribe { params ->
            flightServices.flightSearch(params).subscribe(makeResultsObserver())
        }

        isRoundTripSearchObservable.subscribe { isRoundTripSearch ->
            getParamsBuilder().roundTrip(isRoundTripSearch)
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

    val performSearchObserver = endlessObserver<Unit> {
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

    val deeplinkFlightSearchParamsObserver = endlessObserver<com.expedia.bookings.data.FlightSearchParams> { searchParams ->
        //Setup the viewmodel according to the provided params
        isRoundTripSearchObservable.onNext(searchParams.isRoundTrip)
        datesObserver.onNext(Pair(searchParams.departureDate, searchParams.returnDate))
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

    private fun makeResultsObserver(): Observer<FlightSearchResponse> {

        return object: Observer<FlightSearchResponse> {

            override fun onNext(response: FlightSearchResponse) {
                if (response.hasErrors()) {
                    errorObservable.onNext(response.firstError)
                } else if (response.offers.isEmpty() || response.legs.isEmpty()) {
                    errorObservable.onNext(ApiError(ApiError.Code.FLIGHT_SEARCH_NO_RESULTS))
                } else {
                    flightSearchResponseSubject.onNext(response)
                }
            }

            override fun onError(e: Throwable) {
                if (RetrofitUtils.isNetworkError(e)) {
                    val retryFun = fun() {
                        flightServices.flightSearch(searchParamsObservable.value).subscribe(makeResultsObserver())
                    }
                    val cancelFun = fun() {
                        noNetworkObservable.onNext(Unit)
                    }
                    DialogFactory.showNoInternetRetryDialog(context, retryFun, cancelFun)
                }
            }

            override fun onCompleted() {}
        }
    }
}
