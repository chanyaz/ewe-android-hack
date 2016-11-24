package com.expedia.vm.rail

import android.content.Context
import android.support.v4.content.ContextCompat
import com.expedia.bookings.R
import com.expedia.bookings.data.SuggestionV4
import com.expedia.bookings.data.rail.requests.RailSearchRequest
import com.expedia.bookings.text.HtmlCompat
import com.expedia.bookings.utils.DateFormatUtils
import com.expedia.bookings.utils.DateUtils
import com.expedia.bookings.utils.SpannableBuilder
import com.expedia.bookings.widget.TimeSlider
import com.expedia.util.endlessObserver
import com.expedia.vm.SearchViewModelWithTimeSliderCalendar
import com.mobiata.android.time.util.JodaUtils
import org.joda.time.DateTime
import org.joda.time.LocalDate
import rx.subjects.BehaviorSubject
import rx.subjects.PublishSubject

class RailSearchViewModel(context: Context) : SearchViewModelWithTimeSliderCalendar(context) {

    val searchParamsObservable = PublishSubject.create<RailSearchRequest>()
    val railOriginObservable = BehaviorSubject.create<SuggestionV4>()
    val railDestinationObservable = BehaviorSubject.create<SuggestionV4>()
    val railRequestBuilder = RailSearchRequest.Builder(getMaxSearchDurationDays(), getMaxDateRange())
    val errorInvalidCardsCountObservable = PublishSubject.create<String>()

    val defaultTimeTooltipColor = ContextCompat.getColor(context, R.color.rail_primary_color)
    val errorTimeTooltipColor = ContextCompat.getColor(context, R.color.cars_tooltip_disabled_color)

    init {
        updateTraveler()
        departTimeSubject.subscribe {
            val isValid = !isStartTimeBeforeNow()
            departTimeSliderTooltipColor.onNext(if (isValid) defaultTimeTooltipColor else errorTimeTooltipColor)
        }

        returnTimeSubject.subscribe {
            val isValid = !isEndTimeBeforeStartTime()
            returnTimeSliderTooltipColor.onNext(if (isValid) defaultTimeTooltipColor else errorTimeTooltipColor)
        }

        setUpTimeSliderSubject.onNext(Pair(null,null))
    }

    override fun updateTraveler() {
        travelersObservable.subscribe { update ->
            getParamsBuilder().adults(update.numberOfAdults)
            getParamsBuilder().children(update.childrenAges)
            getParamsBuilder().youths(update.youthAges)
            getParamsBuilder().seniors(update.seniorAges)
        }
    }

    override val originLocationObserver = endlessObserver<SuggestionV4> { suggestion ->
        getParamsBuilder().origin(suggestion)
        railOriginObservable.onNext(suggestion)
        val origin = HtmlCompat.stripHtml(suggestion.regionNames.shortName)
        formattedOriginObservable.onNext(origin)
        requiredSearchParamsObserver.onNext(Unit)
    }

    override val destinationLocationObserver = endlessObserver<SuggestionV4> { suggestion ->
        getParamsBuilder().destination(suggestion)
        railDestinationObservable.onNext(suggestion)
        val destination = HtmlCompat.stripHtml(suggestion.regionNames.shortName)
        formattedDestinationObservable.onNext(destination)
        requiredSearchParamsObserver.onNext(Unit)
    }

    val searchObserver = endlessObserver<Unit> {
        getParamsBuilder().maxStay = getMaxSearchDurationDays()
        getParamsBuilder().origin(railOriginObservable.value)
        getParamsBuilder().destination(railDestinationObservable.value)
        getParamsBuilder().departDateTimeMillis(departTimeSubject.value)
        getParamsBuilder().returnDateTimeMillis(returnTimeSubject.value)
        getParamsBuilder().searchType(isRoundTripSearchObservable.value)

        if (getParamsBuilder().areRequiredParamsFilled()) {
            if (getParamsBuilder().isOriginSameAsDestination()) {
                errorOriginSameAsDestinationObservable.onNext(context.getString(R.string.error_same_station_departure_arrival))
            } else if (isRoundTripSearchObservable.value && !getParamsBuilder().hasValidDateDuration()) {
                errorMaxDurationObservable.onNext(context.getString(R.string.rail_search_range_error_TEMPLATE, getMaxSearchDurationDays()))
            } else if (!getParamsBuilder().isWithinDateRange()) {
                errorMaxRangeObservable.onNext(context.getString(R.string.error_date_too_far))
            } else if (getParamsBuilder().isRailCardsCountInvalid()) {
                errorInvalidCardsCountObservable.onNext(context.getString(R.string.error_rail_cards_greater_than_number_travelers))
            } else {
                var searchParams = getParamsBuilder().build()
                searchParamsObservable.onNext(searchParams)
            }
        } else {
            if (!getParamsBuilder().hasOriginAndDestination()) {
                errorNoDestinationObservable.onNext(Unit)
            } else if (!getParamsBuilder().hasStartAndOrEndDates()) {
                errorNoDatesObservable.onNext(Unit)
            }
        }
    }

    fun swapLocations() {
        val oldOrigin = railOriginObservable.value
        originLocationObserver.onNext(railDestinationObservable.value)
        destinationLocationObserver.onNext(oldOrigin)
    }

    override fun getParamsBuilder(): RailSearchRequest.Builder {
        return railRequestBuilder
    }

    override fun isStartDateOnlyAllowed(): Boolean {
        // return true if one-way
        return !isRoundTripSearchObservable.value
    }

    fun resetDatesAndTimes() {
        resetDates()
        departTimeSubject.onNext(0)
        returnTimeSubject.onNext(0)
        onTimesChanged(Pair(0, 0))
    }

    override fun onDatesChanged(dates: Pair<LocalDate?, LocalDate?>) {
        super.onDatesChanged(dates)
        setUpTimeSliderSubject.onNext(dates)
        requiredSearchParamsObserver.onNext(Unit)
    }

    override fun onTimesChanged(times: Pair<Int, Int>){
        val (startMillis, endMillis) = times
        getParamsBuilder().departDateTimeMillis(startMillis)
        getParamsBuilder().returnDateTimeMillis(endMillis)
        dateTextObservable.onNext(computeCalendarCardViewText(startMillis, endMillis))
    }

    fun computeCalendarCardViewText(startMillis: Int, endMillis: Int): String? {
        if (startDate() == null) {
            val resId = if (isRoundTripSearchObservable.value) R.string.select_dates else R.string.select_departure_date
            return context.resources.getString(resId)
        } else {
            return DateFormatUtils.formatRailDateTimeRange(context, startDate(), startMillis, endDate(), endMillis, isRoundTripSearchObservable.value);
        }
    }

    // Reset times if the start is equal to today and the selected time is before the current time
    // or if the end time is earlier or equal to the start time and its the same day.
    override fun validateTimes() {
        val now = DateTime.now()
        if (isStartTimeBeforeNow()) {
            // Adding min search hours to current time for same day search
            // TODO update this with minimum search out time and handle end of day case
            departTimeSubject.onNext(now.plusHours(R.integer.calendar_min_search_time_rail).millisOfDay);
        }
        if (isEndTimeBeforeStartTime() && isRoundTripSearchObservable.value) {
            returnTimeSubject.onNext(getStartDateTimeAsMillis() + DateTime().withHourOfDay(2).withMinuteOfHour(0).millisOfDay);
        }

    }

    override fun getAllowedMinProgress(now: DateTime): Int {
        return TimeSlider.convertMillisToProgress(now.millisOfDay) + R.integer.calendar_min_search_time_rail
    }

    override fun computeDateText(start: LocalDate?, end: LocalDate?): CharSequence {
        return computeDateRangeText(start, end).toString()
    }

    override fun computeTooltipText(start: LocalDate?, end: LocalDate?): Pair<String, String> {
        val resource = if (isRoundTripSearchObservable.value) {
            val instructionStringResId =
                    if (end == null)
                        R.string.calendar_instructions_date_range_flight_select_return_date
                    else
                        R.string.calendar_drag_to_modify
            context.resources.getString(instructionStringResId)
        } else {
            ""
        }
        return Pair(computeTopTextForToolTip(start, end), resource)
    }

    override fun computeDateInstructionText(start: LocalDate?, end: LocalDate?): CharSequence {
        if (start == null && end == null) {
            val resId = if (isRoundTripSearchObservable.value) R.string.select_dates else R.string.select_departure_date
            return context.resources.getString(resId)
        }

        val dateRangeText = computeDateRangeText(start, end)
        val sb = SpannableBuilder()
        sb.append(dateRangeText)

        if (start != null && end != null) {
            val nightCount = JodaUtils.daysBetween(start, end)
            val nightsString = context.resources.getQuantityString(R.plurals.length_of_stay, nightCount, nightCount)
            sb.append(" ");
            sb.append(context.resources.getString(R.string.nights_count_TEMPLATE, nightsString))
        }
        return sb.build()
    }

    override fun computeDateRangeText(start: LocalDate?, end: LocalDate?): String? {
        if (start == null && end == null) {
            val resId = if (isRoundTripSearchObservable.value) R.string.select_dates else R.string.select_departure_date
            return context.getString(resId)
        } else if (end == null && isRoundTripSearchObservable.value) {
            return context.getString(R.string.select_checkout_date_TEMPLATE, DateUtils.localDateToMMMd(start))
        } else {
            return DateFormatUtils.formatRailDateRange(context, start, end)
        }
    }

    override fun getMaxSearchDurationDays(): Int {
        // 0 for one-way searches
        // TODO update this with correct max duration
        return if (isRoundTripSearchObservable.value) context.resources.getInteger(R.integer.calendar_max_days_rail_return) else 0
    }

    override fun getMaxDateRange(): Int {
        return context.resources.getInteger(R.integer.calendar_max_days_rail_search)
    }

    override fun sameStartAndEndDateAllowed(): Boolean {
        return false
    }

    override fun getStartDate(): LocalDate {
        return LocalDate.now().plusDays(1)
    }

    override fun getCalendarSliderTooltipStartTimeLabel(): String{
        return context.resources.getString(R.string.rail_departing_at)
    }

    override fun getCalendarSliderTooltipEndTimeLabel(): String{
        return context.resources.getString(R.string.rail_returning_at)
    }
}
