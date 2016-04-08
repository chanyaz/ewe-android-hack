package com.expedia.vm

import android.content.Context
import com.expedia.bookings.R
import com.expedia.bookings.data.SuggestionV4
import com.expedia.bookings.data.rail.requests.RailSearchRequest
import com.expedia.bookings.utils.DateUtils
import com.expedia.bookings.utils.SpannableBuilder
import com.expedia.util.endlessObserver
import com.mobiata.android.time.util.JodaUtils
import org.joda.time.LocalDate
import rx.subjects.BehaviorSubject
import rx.subjects.PublishSubject

class RailSearchViewModel(context: Context) : DatedSearchViewModel(context) {
    override val paramsBuilder = RailSearchRequest.Builder()

    // Outputs
    val searchParamsObservable = PublishSubject.create<RailSearchRequest>()
    val railOriginObservable = BehaviorSubject.create<SuggestionV4>()
    val railDestinationObservable = BehaviorSubject.create<SuggestionV4>()

    val railErrorNoLocationsObservable = PublishSubject.create<Unit>()

    init {
        railOriginObservable.onNext(buildFakeOrigin())
        railDestinationObservable.onNext(buildFakeDestination())
    }

    val searchObserver = endlessObserver<Unit> {
        paramsBuilder.departure(railOriginObservable.value)
        paramsBuilder.arrival(railDestinationObservable.value)
        paramsBuilder.startDate(datesObservable.value?.first)
        paramsBuilder.endDate(datesObservable.value?.second)

        if (paramsBuilder.areRequiredParamsFilled()) {
            var searchParams = paramsBuilder.build()
            searchParamsObservable.onNext(searchParams)
        } else {
            if (!paramsBuilder.hasOriginAndDestination()) {
                railErrorNoLocationsObservable.onNext(Unit)
            }
            if (!paramsBuilder.hasValidDates()) {
                errorNoDatesObservable.onNext(Unit)
            }
        }
    }

    fun swapLocations() {
        val oldOrigin = railOriginObservable.value
        railOriginObservable.onNext(railDestinationObservable.value)
        railDestinationObservable.onNext(oldOrigin)
    }

    override fun onDatesChanged(dates: Pair<LocalDate?, LocalDate?>) {
        val (start, end) = dates
        datesObservable.onNext(dates)

        paramsBuilder.startDate(start)
        paramsBuilder.endDate(end)

        dateTextObservable.onNext(computeDateRangeText(context, start, end))

        dateInstructionObservable.onNext(computeDateInstructionText(context, start, end))

        calendarTooltipTextObservable.onNext(computeTooltipText(start, end))
    }

    private fun computeTooltipText(start: LocalDate?, end: LocalDate?): Pair<String, String> {
        val resource =
                if (end == null) R.string.hotel_calendar_tooltip_bottom
                else R.string.calendar_drag_to_modify
        val instructions = context.resources.getString(resource)
        return Pair(computeTopTextForToolTip(start, end), instructions)
    }

    private fun computeDateInstructionText(context: Context, start: LocalDate?, end: LocalDate?): CharSequence {
        if (start == null && end == null) {
            return context.getString(R.string.select_checkin_date);
        }

        val dateRangeText = computeDateRangeText(context, start, end)
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

    private fun computeDateRangeText(context: Context, start: LocalDate?, end: LocalDate?): String? {
        if (start == null && end == null) {
            return context.resources.getString(R.string.select_dates)
        } else if (end == null) {
            return context.resources.getString(R.string.select_checkout_date_TEMPLATE, DateUtils.localDateToMMMd(start))
        } else {
            return context.resources.getString(R.string.calendar_instructions_date_range_TEMPLATE, DateUtils.localDateToMMMd(start), DateUtils.localDateToMMMd(end))
        }
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
