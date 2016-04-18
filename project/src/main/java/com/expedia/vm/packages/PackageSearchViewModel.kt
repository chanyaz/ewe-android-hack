package com.expedia.vm.packages

import android.content.Context
import android.text.style.RelativeSizeSpan
import com.expedia.bookings.R
import com.expedia.bookings.data.SuggestionV4
import com.expedia.bookings.data.packages.PackageSearchParams
import com.expedia.bookings.utils.DateUtils
import com.expedia.bookings.utils.SpannableBuilder
import com.expedia.bookings.utils.StrUtils
import com.expedia.util.endlessObserver
import com.expedia.vm.DatedSearchViewModel
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
    val departureTextObservable = BehaviorSubject.create<String>()
    val arrivalTextObservable = PublishSubject.create<String>()
    val errorDepartureSameAsOrigin = PublishSubject.create<String>()

    val maxPackageStay = context.resources.getInteger(R.integer.calendar_max_days_package_stay)

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
        dateInstructionObservable.onNext(computeDateInstructionText(start, end))

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
            if (paramsBuilder.isDepartureSameAsOrigin()) {
                errorDepartureSameAsOrigin.onNext(context.getString(R.string.error_same_flight_departure_arrival))
            } else if (!paramsBuilder.hasValidDates()) {
                errorMaxDatesObservable.onNext(context.getString(R.string.hotel_search_range_error_TEMPLATE, maxPackageStay))
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
    override fun computeDateInstructionText(start: LocalDate?, end: LocalDate?): CharSequence {
        if (start == null && end == null) {
            return context.getString(R.string.select_departure_date);
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
            return context.resources.getString(R.string.select_return_date_TEMPLATE, DateUtils.localDateToMMMd(start))
        } else {
            return context.resources.getString(R.string.calendar_instructions_date_range_TEMPLATE, DateUtils.localDateToMMMd(start), DateUtils.localDateToMMMd(end))
        }
    }

    override fun computeDateText(start: LocalDate?, end: LocalDate?): CharSequence {
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

    override fun computeTooltipText(start: LocalDate?, end: LocalDate?): Pair<String, String> {
        val resource =
                if (end == null) R.string.cars_calendar_start_date_label
                else R.string.calendar_drag_to_modify
        val instructions = context.resources.getString(resource)
        return Pair(computeTopTextForToolTip(start, end), instructions)
    }

}

