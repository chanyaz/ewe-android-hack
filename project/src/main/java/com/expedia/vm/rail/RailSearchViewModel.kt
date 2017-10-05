package com.expedia.vm.rail

import android.content.Context
import android.support.v4.content.ContextCompat
import com.expedia.bookings.R
import com.expedia.bookings.data.SuggestionV4
import com.expedia.bookings.data.rail.requests.RailSearchRequest
import com.expedia.bookings.rail.util.RailCalendarRules
import com.expedia.bookings.shared.CalendarRules
import com.expedia.bookings.text.HtmlCompat
import com.expedia.bookings.utils.DateFormatUtils
import com.expedia.bookings.utils.LocaleBasedDateFormatUtils
import com.expedia.bookings.widget.TimeSlider
import com.expedia.util.endlessObserver
import com.expedia.vm.SearchViewModelWithTimeSliderCalendar
import com.squareup.phrase.Phrase
import org.joda.time.DateTime
import org.joda.time.LocalDate
import rx.subjects.BehaviorSubject
import rx.subjects.PublishSubject

class RailSearchViewModel(context: Context) : SearchViewModelWithTimeSliderCalendar(context) {

    private val oneWayRules = RailCalendarRules(context, roundTrip = false)
    private val roundTripRules = RailCalendarRules(context, roundTrip = true)

    val searchParamsObservable = PublishSubject.create<RailSearchRequest>()
    val railOriginObservable = BehaviorSubject.create<SuggestionV4>()
    val railDestinationObservable = BehaviorSubject.create<SuggestionV4>()
    val railRequestBuilder = RailSearchRequest.Builder(getCalendarRules().getMaxSearchDurationDays(), getCalendarRules().getMaxDateRange())
    val errorInvalidCardsCountObservable = PublishSubject.create<String>()

    val defaultTimeTooltipColor = ContextCompat.getColor(context, R.color.rail_primary_color)
    val errorTimeTooltipColor = ContextCompat.getColor(context, R.color.rail_tooltip_disabled_color)

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
        isRoundTripSearchObservable.subscribe { isRoundTrip ->
            getParamsBuilder().searchType(isRoundTrip)
        }
    }

    val searchObserver = endlessObserver<Unit> {
        getParamsBuilder().maxStay = getCalendarRules().getMaxSearchDurationDays()
        getParamsBuilder().origin(railOriginObservable.value)
        getParamsBuilder().destination(railDestinationObservable.value)
        getParamsBuilder().departDateTimeMillis(departTimeSubject.value)
        getParamsBuilder().returnDateTimeMillis(returnTimeSubject.value)

        if (getParamsBuilder().areRequiredParamsFilled()) {
            if (getParamsBuilder().isOriginSameAsDestination()) {
                errorOriginSameAsDestinationObservable.onNext(context.getString(R.string.error_same_station_departure_arrival))
            } else if (isRoundTripSearchObservable.value && !getParamsBuilder().hasValidDateDuration()) {
                errorMaxDurationObservable.onNext(context.getString(R.string.rail_search_range_error_TEMPLATE, getCalendarRules().getMaxSearchDurationDays()))
            } else if (!getParamsBuilder().isWithinDateRange()) {
                errorMaxRangeObservable.onNext(context.getString(R.string.error_date_too_far))
            } else if (getParamsBuilder().isRailCardsCountInvalid()) {
                errorInvalidCardsCountObservable.onNext(context.getString(R.string.error_rail_cards_greater_than_number_travelers))
            } else {
                val searchParams = getParamsBuilder().build()
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

    fun resetDatesAndTimes() {
        resetDates()
        departTimeSubject.onNext(0)
        returnTimeSubject.onNext(0)
        onTimesChanged(Pair(0, 0))
    }

    fun swapLocations() {
        val oldOrigin = railOriginObservable.value
        originLocationObserver.onNext(railDestinationObservable.value)
        destinationLocationObserver.onNext(oldOrigin)
    }

    override fun getCalendarRules(): CalendarRules {
        return if (isRoundTripSearchObservable.value) roundTripRules else oneWayRules
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

    override fun getParamsBuilder(): RailSearchRequest.Builder {
        return railRequestBuilder
    }

    override fun onDatesChanged(dates: Pair<LocalDate?, LocalDate?>) {
        val (start, end) = dates
        dateInstructionObservable.onNext(getDateInstructionText(start, end))
        calendarTooltipTextObservable.onNext(getToolTipText(start, end))
        calendarTooltipContDescObservable.onNext(getToolTipContentDescription(start, end, isRoundTripSearchObservable.value))
        setUpTimeSliderSubject.onNext(dates)

        super.onDatesChanged(dates)
    }

    override fun onTimesChanged(times: Pair<Int, Int>){
        val (startMillis, endMillis) = times
        getParamsBuilder().departDateTimeMillis(startMillis)
        getParamsBuilder().returnDateTimeMillis(endMillis)
        dateTextObservable.onNext(computeCalendarCardViewText(startMillis, endMillis, false))
        dateAccessibilityObservable.onNext(computeCalendarCardViewText(startMillis, endMillis, true))
    }

    // Reset times if the start is equal to today and the selected time is before the current time
    // or if the end time is earlier or equal to the start time and its the same day.
    override fun validateTimes() {
        val now = DateTime.now()
        if (isStartTimeBeforeNow()) {
            // Adding min search hours to current time for same day search
            // TODO update this with minimum search out time and handle end of day case
            departTimeSubject.onNext(now.plusHours(R.integer.calendar_min_search_time_rail).millisOfDay)
        }
        if (isEndTimeBeforeStartTime() && isRoundTripSearchObservable.value) {
            returnTimeSubject.onNext(getStartDateTimeAsMillis() + DateTime().withHourOfDay(2).withMinuteOfHour(0).millisOfDay)
        }

    }

    override fun getAllowedMinProgress(now: DateTime): Int {
        return TimeSlider.convertMillisToProgress(now.millisOfDay) + R.integer.calendar_min_search_time_rail
    }

    override fun getDateInstructionText(start: LocalDate?, end: LocalDate?): CharSequence {
        if (start == null && end == null) {
            return getCalendarDateLabel()
        } else if (end == null && isRoundTripSearchObservable.value) {
            return Phrase.from(context.resources, R.string.select_return_date_TEMPLATE).put("startdate", LocaleBasedDateFormatUtils.localDateToMMMd(start!!)).format().toString()
        }
        return DateFormatUtils.formatRailDateRange(context, start, end)
    }

    override fun getCalendarSliderTooltipStartTimeLabel(): String{
        return context.resources.getString(R.string.rail_departing_at)
    }

    override fun getCalendarSliderTooltipEndTimeLabel(): String{
        return context.resources.getString(R.string.rail_returning_at)
    }

    override fun getCalendarToolTipInstructions(start: LocalDate?, end: LocalDate?): String {
        if (isRoundTripSearchObservable.value && end == null) {
            return context.getString(R.string.calendar_instructions_date_range_flight_select_return_date)
        }
        return context.getString(R.string.calendar_drag_to_modify)
    }

    override fun getEmptyDateText(forContentDescription: Boolean): String {
        val label = getCalendarDateLabel()
        if (forContentDescription)  {
            return getDateAccessibilityText(label, "")
        }
        return label
    }

    override fun getNoEndDateText(start: LocalDate?, forContentDescription: Boolean): String {
        return "" //no op, rail doesn't update until time is selected.
    }

    override fun getCompleteDateText(start: LocalDate, end: LocalDate, forContentDescription: Boolean): String {
        return "" //no op, rail doesn't update until time is selected.
    }

    private fun computeCalendarCardViewText(startMillis: Int, endMillis: Int, isContentDescription: Boolean): String? {
        if (startDate() == null) {
            return getEmptyDateText(isContentDescription)
        }

        val dateTimeRange = DateFormatUtils.formatRailDateTimeRange(context, startDate(), startMillis,
                endDate(), endMillis, isRoundTripSearchObservable.value)
        if (isContentDescription) {
            return getDateAccessibilityText(getCalendarDateLabel(), dateTimeRange)
        }
        return dateTimeRange

    }

    private fun getCalendarDateLabel() : String {
        val resId = if (isRoundTripSearchObservable.value) R.string.select_dates else R.string.select_departure_date
        return context.getString(resId)
    }

    private fun resetDates() {
        onDatesChanged(Pair(null, null))
    }

    override fun getStartTimeContDesc(time: String): String {
        return Phrase.from(context, R.string.rail_depart_time_cont_desc_TEMPLATE).put("time", time).format().toString()
    }

    override fun getEndTimeContDesc(time: String): String {
        return Phrase.from(context, R.string.rail_return_time_cont_desc_TEMPLATE).put("time", time).format().toString()
    }
}
