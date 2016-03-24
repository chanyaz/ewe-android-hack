package com.expedia.vm

import android.content.Context
import android.text.style.RelativeSizeSpan
import com.expedia.bookings.R
import com.expedia.bookings.data.SuggestionV4
import com.expedia.bookings.data.packages.PackageSearchParams
import com.expedia.bookings.utils.DateUtils
import com.expedia.bookings.utils.SpannableBuilder
import com.expedia.bookings.utils.StrUtils
import com.expedia.util.endlessObserver
import com.mobiata.android.time.util.JodaUtils
import org.joda.time.LocalDate
import rx.subjects.BehaviorSubject
import rx.subjects.PublishSubject

class PackageSearchViewModel(context: Context) : DatedSearchViewModel(context) {
    override val paramsBuilder = PackageSearchParams.Builder(context.resources.getInteger(R.integer.calendar_max_days_package_stay))

    // Outputs
    val searchParamsObservable = PublishSubject.create<PackageSearchParams>()
    val destinationObservable = BehaviorSubject.create<Boolean>(false)
    val arrivalObservable = BehaviorSubject.create<Boolean>(false)
    val departureTextObservable = PublishSubject.create<String>()
    val arrivalTextObservable = PublishSubject.create<String>()

    // Inputs
    override fun onDatesChanged(dates: Pair<LocalDate?, LocalDate?>) {
        val (start, end) = dates

        paramsBuilder.startDate(start)
        if (start != null && end == null) {
            paramsBuilder.endDate(start.plusDays(1))
        } else {
            paramsBuilder.endDate(end)
        }

        dateTextObservable.onNext(computeDateText(start, end))

        calendarTooltipTextObservable.onNext(computeTooltipText(start, end))

        requiredSearchParamsObserver.onNext(Unit)
    }

    var requiredSearchParamsObserver = endlessObserver<Unit> {
        searchButtonObservable.onNext(paramsBuilder.areRequiredParamsFilled())
        destinationObservable.onNext(paramsBuilder.hasDeparture())
        arrivalObservable.onNext(paramsBuilder.hasArrival())
        originObservable.onNext(paramsBuilder.hasDepartureAndArrival())
    }

    val isInfantInLapObserver = endlessObserver<Boolean> { isInfantInLap ->
        paramsBuilder.infantSeatingInLap(isInfantInLap)
    }

    val departureObserver = endlessObserver<SuggestionV4> { suggestion ->
        paramsBuilder.departure(suggestion)
        departureTextObservable.onNext(StrUtils.formatAirport(suggestion))
        requiredSearchParamsObserver.onNext(Unit)
    }

    val arrivalObserver = endlessObserver<SuggestionV4> { suggestion ->
        paramsBuilder.arrival(suggestion)
        arrivalTextObservable.onNext(StrUtils.formatAirport(suggestion))
        requiredSearchParamsObserver.onNext(Unit)
    }

    val suggestionTextChangedObserver = endlessObserver<Boolean> {
        if (it) paramsBuilder.departure(null) else paramsBuilder.arrival(null)
        requiredSearchParamsObserver.onNext(Unit)
    }

    val searchObserver = endlessObserver<Unit> {
        if (paramsBuilder.areRequiredParamsFilled()) {
            if (!paramsBuilder.hasValidDates()) {
                errorMaxDatesObservable.onNext(Unit)
            } else {
                val packageSearchParams = paramsBuilder.build()
                searchParamsObservable.onNext(packageSearchParams)
            }
        } else {
            if (!paramsBuilder.hasDepartureAndArrival()) {
                errorNoOriginObservable.onNext(Unit)
            } else if (!paramsBuilder.hasStartAndEndDates()) {
                errorNoDatesObservable.onNext(Unit)
            }
        }
    }

    // Helpers
    private fun computeDateRangeText(start: LocalDate?, end: LocalDate?): String? {
        if (start == null && end == null) {
            return context.resources.getString(R.string.select_dates)
        } else if (end == null) {
            return context.resources.getString(R.string.select_checkout_date_TEMPLATE, DateUtils.localDateToMMMd(start))
        } else {
            return context.resources.getString(R.string.calendar_instructions_date_range_TEMPLATE, DateUtils.localDateToMMMd(start), DateUtils.localDateToMMMd(end))
        }
    }

    private fun computeDateText(start: LocalDate?, end: LocalDate?): CharSequence {
        val dateRangeText = computeDateRangeText(start, end)
        val sb = SpannableBuilder()
        sb.append(dateRangeText)

        if (start != null && end != null) {
            val nightCount = JodaUtils.daysBetween(start, end)
            val nightsString = context.resources.getQuantityString(R.plurals.length_of_stay, nightCount, nightCount)
            sb.append(" ");
            sb.append(context.resources.getString(R.string.nights_count_TEMPLATE, nightsString), RelativeSizeSpan(0.8f))
        }
        return sb.build()
    }

    private fun computeTooltipText(start: LocalDate?, end: LocalDate?): Pair<String, String> {
        val resource =
                if (end == null) R.string.hotel_calendar_tooltip_bottom
                else R.string.calendar_drag_to_modify
        val instructions = context.resources.getString(resource)
        return Pair(computeTopTextForToolTip(start, end), instructions)
    }
}

