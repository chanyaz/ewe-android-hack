package com.expedia.vm.rail

import android.content.Context
import com.expedia.bookings.R
import android.support.v4.content.ContextCompat
import org.joda.time.DateTime
import com.expedia.bookings.data.SuggestionV4
import com.expedia.bookings.data.rail.requests.RailSearchRequest
import com.expedia.bookings.utils.DateUtils
import com.expedia.bookings.utils.SpannableBuilder
import com.expedia.util.endlessObserver
import com.expedia.vm.BaseSearchViewModel
import com.mobiata.android.time.util.JodaUtils
import com.squareup.phrase.Phrase
import org.joda.time.LocalDate
import rx.subjects.BehaviorSubject
import rx.subjects.PublishSubject

class RailSearchViewModel(context: Context) : BaseSearchViewModel(context) {

    val railRequestBuilder = RailSearchRequest.Builder()

    // Outputs
    val searchParamsObservable = PublishSubject.create<RailSearchRequest>()
    val railOriginObservable = BehaviorSubject.create<SuggestionV4>()
    val railDestinationObservable = BehaviorSubject.create<SuggestionV4>()

    val departTimeSubject = BehaviorSubject.create<Int>()
    val returnTimeSubject = BehaviorSubject.create<Int>()
    val departTimeSliderTooltipColor = BehaviorSubject.create<Int>()
    val returnTimeSliderTooltipColor = BehaviorSubject.create<Int>()

    val railErrorNoLocationsObservable = PublishSubject.create<Unit>()

    val defaultTimeTooltipColor = ContextCompat.getColor(context, R.color.rail_primary_color)
    val errorTimeTooltipColor = ContextCompat.getColor(context, R.color.cars_tooltip_disabled_color)

    init {
        railOriginObservable.onNext(buildFakeOrigin())
        railDestinationObservable.onNext(buildFakeDestination())

        departTimeSubject.subscribe {
            val valid = it < DateTime.now().millisOfDay //todo more logic

            departTimeSliderTooltipColor.onNext(if (valid) defaultTimeTooltipColor else errorTimeTooltipColor)
        }

        returnTimeSubject.subscribe {
            val valid = it < DateTime.now().millisOfDay //todo more logic

            returnTimeSliderTooltipColor.onNext(if (valid) defaultTimeTooltipColor else errorTimeTooltipColor)
        }
    }

    val searchObserver = endlessObserver<Unit> {
        getParamsBuilder().origin(railOriginObservable.value)
        getParamsBuilder().destination(railDestinationObservable.value)
        getParamsBuilder().startDate(datesObservable.value?.first)
        getParamsBuilder().endDate(datesObservable.value?.second)

        if (getParamsBuilder().areRequiredParamsFilled()) {
            var searchParams = getParamsBuilder().build()
            searchParamsObservable.onNext(searchParams)
        } else {
            if (!getParamsBuilder().hasOriginAndDestination()) {
                railErrorNoLocationsObservable.onNext(Unit)
            }
            if (!getParamsBuilder().hasValidDateDuration()) {
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
        return true // one way train journeys possible
    }

    override fun onDatesChanged(dates: Pair<LocalDate?, LocalDate?>) {
        super.onDatesChanged(dates)

        val (start, end) = dates
        getParamsBuilder().startDate(start)
        getParamsBuilder().endDate(end)
    }

    override fun computeDateText(start: LocalDate?, end: LocalDate?): CharSequence {
        return computeDateRangeText(start, end).toString()
    }

    override fun computeTooltipText(start: LocalDate?, end: LocalDate?): Pair<String, String> {
        val resource =
                if (end == null) R.string.hotel_calendar_tooltip_bottom
                else R.string.calendar_drag_to_modify
        val instructions = context.resources.getString(resource)
        return Pair(computeTopTextForToolTip(start, end), instructions)
    }

    override fun computeDateInstructionText(start: LocalDate?, end: LocalDate?): CharSequence {
        if (start == null && end == null) {
            return context.getString(R.string.select_checkin_date);
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
            return context.resources.getString(R.string.select_dates)
        } else if (end == null) {
            return context.resources.getString(R.string.select_checkout_date_TEMPLATE, DateUtils.localDateToMMMd(start))
        } else {
            return Phrase.from(context, R.string.calendar_instructions_date_range_TEMPLATE).put("startdate", DateUtils.localDateToMMMd(start)).put("enddate", DateUtils.localDateToMMMd(end)).format().toString()
        }
    }

    override fun getMaxSearchDurationDays(): Int {
        return context.resources.getInteger(R.integer.calendar_max_days_rail_search)
    }

    override fun getMaxDateRange(): Int {
        return context.resources.getInteger(R.integer.calendar_max_selectable_date_range)
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
}
