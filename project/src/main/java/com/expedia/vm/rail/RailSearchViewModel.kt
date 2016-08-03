package com.expedia.vm.rail

import android.content.Context
import android.support.v4.content.ContextCompat
import com.expedia.bookings.R
import com.expedia.bookings.data.SuggestionV4
import com.expedia.bookings.data.rail.requests.RailSearchRequest
import com.expedia.bookings.utils.DateUtils
import com.expedia.bookings.utils.SpannableBuilder
import com.expedia.bookings.widget.TimeSlider
import com.expedia.util.endlessObserver
import com.expedia.vm.BaseSearchViewModel
import com.mobiata.android.time.util.JodaUtils
import com.squareup.phrase.Phrase
import org.joda.time.DateTime
import org.joda.time.Days
import org.joda.time.LocalDate
import rx.subjects.BehaviorSubject
import rx.subjects.PublishSubject

class RailSearchViewModel(context: Context) : BaseSearchViewModel(context) {

    val searchParamsObservable = PublishSubject.create<RailSearchRequest>()
    val railOriginObservable = BehaviorSubject.create<SuggestionV4>()
    val railDestinationObservable = BehaviorSubject.create<SuggestionV4>()

    val departTimeSubject = BehaviorSubject.create<Int>()
    val returnTimeSubject = BehaviorSubject.create<Int>()
    val departTimeSliderTooltipColor = BehaviorSubject.create<Int>()
    val returnTimeSliderTooltipColor = BehaviorSubject.create<Int>()
    val isRoundTripSearchObservable = BehaviorSubject.create<Boolean>(false)
    val timesObservable = BehaviorSubject.create<Pair<Long, Long?>>()
    val railRequestBuilder = RailSearchRequest.Builder(getMaxSearchDurationDays(), getMaxDateRange())

    val railErrorNoLocationsObservable = PublishSubject.create<Unit>()

    val defaultTimeTooltipColor = ContextCompat.getColor(context, R.color.rail_primary_color)
    val errorTimeTooltipColor = ContextCompat.getColor(context, R.color.cars_tooltip_disabled_color)

    init {
        railOriginObservable.onNext(buildFakeOrigin())
        railDestinationObservable.onNext(buildFakeDestination())

        departTimeSubject.subscribe {
            val valid = it > DateTime.now().millisOfDay //todo more logic
            departTimeSliderTooltipColor.onNext(if (valid) defaultTimeTooltipColor else errorTimeTooltipColor)
        }

        returnTimeSubject.subscribe {
            val valid = it > DateTime.now().millisOfDay //todo more logic
            returnTimeSliderTooltipColor.onNext(if (valid) defaultTimeTooltipColor else errorTimeTooltipColor)
        }
    }

    val searchObserver = endlessObserver<Unit> {
        getParamsBuilder().maxStay = getMaxSearchDurationDays()
        getParamsBuilder().origin(railOriginObservable.value)
        getParamsBuilder().destination(railDestinationObservable.value)
        getParamsBuilder().startDate(datesObservable.value?.first)
        getParamsBuilder().departTime(timesObservable.value?.first)
        getParamsBuilder().searchType(isRoundTripSearchObservable.value)
       if (isRoundTripSearchObservable.value) {
            getParamsBuilder().endDate(datesObservable.value?.second)
            getParamsBuilder().returnTime(timesObservable.value?.second)
        }

        if (getParamsBuilder().areRequiredParamsFilled()) {
            if (isRoundTripSearchObservable.value && !getParamsBuilder().hasValidDateDuration()) {
                errorMaxDurationObservable.onNext(context.getString(R.string.rail_search_range_error_TEMPLATE, getMaxSearchDurationDays()))
            } else if (!getParamsBuilder().isWithinDateRange()) {
                errorMaxRangeObservable.onNext(context.getString(R.string.error_date_too_far))
            } else {
                var searchParams = getParamsBuilder().build()
                searchParamsObservable.onNext(searchParams)
            }
        } else {
            if (!getParamsBuilder().hasOriginAndOrDestination()) {
                errorNoDestinationObservable.onNext(Unit)
            } else if (!getParamsBuilder().hasStartAndOrEndDates()) {
                errorNoDatesObservable.onNext(Unit)
            }
        }
    }

    fun swapLocations() {
        val oldOrigin = railOriginObservable.value
        railOriginObservable.onNext(railDestinationObservable.value)
        railDestinationObservable.onNext(oldOrigin)
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
        onTimesChanged(Pair(0, 0))
    }

    fun onTimesChanged(times: Pair<Int , Int>) {
        val (departProgress, returnProgress) = times

        departTimeSubject.onNext(TimeSlider.convertProgressToMillis(departProgress))
        if (isRoundTripSearchObservable.value) {
            returnTimeSubject.onNext(TimeSlider.convertProgressToMillis(returnProgress))
        }

        requiredSearchParamsObserver.onNext(Unit)
        timesObservable.onNext(Pair(TimeSlider.getDateTime(departProgress).millis, TimeSlider.getDateTime(returnProgress).millis))
    }

    override fun onDatesChanged(dates: Pair<LocalDate?, LocalDate?>) {
        super.onDatesChanged(dates)

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
            val datesText = if (isRoundTripSearchObservable.value)
                                Phrase.from(context, R.string.calendar_instructions_date_range_TEMPLATE)
                                        .put("startdate", DateUtils.localDateToMMMd(start))
                                        .put("enddate", DateUtils.localDateToMMMd(end)).format().toString()
                            else context.getString(R.string.one_way_TEMPLATE, DateUtils.localDateToMMMd(start))
            return datesText
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

    //TODO - rip these out once we have an ESS service that works for Rail
    private fun buildFakeOrigin(): SuggestionV4 {
        val suggestion = SuggestionV4()
        suggestion.gaiaId = ""
        suggestion.regionNames = SuggestionV4.RegionNames()
        suggestion.regionNames.displayName = "Manchester, UK"
        suggestion.regionNames.fullName = "Manchester, UK"
        suggestion.regionNames.shortName = "Manchester"
        suggestion.hierarchyInfo = SuggestionV4.HierarchyInfo()
        suggestion.hierarchyInfo!!.airport = SuggestionV4.Airport()
        suggestion.hierarchyInfo!!.airport!!.airportCode = ""
        return suggestion
    }

    private fun buildFakeDestination(): SuggestionV4 {
        val suggestion = SuggestionV4()
        suggestion.gaiaId = ""
        suggestion.regionNames = SuggestionV4.RegionNames()
        suggestion.regionNames.displayName = "London, UK"
        suggestion.regionNames.fullName = "London, UK"
        suggestion.regionNames.shortName = "London"
        suggestion.hierarchyInfo = SuggestionV4.HierarchyInfo()
        suggestion.hierarchyInfo!!.airport = SuggestionV4.Airport()
        suggestion.hierarchyInfo!!.airport!!.airportCode = ""
        return suggestion
    }

    override fun sameStartAndEndDateAllowed(): Boolean {
        return false
    }

    fun isStartDateEqualToToday(): Boolean {
        return if (startDate() != null) startDate()!!.isEqual(LocalDate.now()) else false
    }

    fun isStartEqualToEnd(): Boolean {
        return if (startDate() != null && endDate() != null) startDate()!!.isEqual(endDate()) else false
    }
}
