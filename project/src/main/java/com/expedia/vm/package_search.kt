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

class PackageSearchViewModel(val context: Context) {
    private val paramsBuilder = PackageSearchParams.Builder(context.resources.getInteger(R.integer.calendar_max_days_package_stay))

    // Outputs
    val searchParamsObservable = PublishSubject.create<PackageSearchParams>()
    val originObservable = BehaviorSubject.create<Boolean>(false)
    val destinationObservable = BehaviorSubject.create<Boolean>(false)
    val arrivalObservable = BehaviorSubject.create<Boolean>(false)
    val dateTextObservable = PublishSubject.create<CharSequence>()
    val calendarTooltipTextObservable = PublishSubject.create<Pair<String, String>>()
    val originTextObservable = PublishSubject.create<String>()
    val destinationTextObservable = PublishSubject.create<String>()
    val searchButtonObservable = PublishSubject.create<Boolean>()
    val errorNoOriginObservable = PublishSubject.create<Boolean>()
    val errorNoDatesObservable = PublishSubject.create<Unit>()
    val errorMaxDatesObservable = PublishSubject.create<Unit>()
    val enableDateObservable = PublishSubject.create<Boolean>()
    val enableTravelerObservable = PublishSubject.create<Boolean>()

    val enableDateObserver = endlessObserver<Unit> {
        enableDateObservable.onNext(paramsBuilder.hasOriginAndDestination())
    }

    val enableTravelerObserver = endlessObserver<Unit> {
        enableTravelerObservable.onNext(paramsBuilder.hasOriginAndDestination())
    }

    // Inputs
    val datesObserver = endlessObserver<Pair<LocalDate?, LocalDate?>> { data ->
        val (start, end) = data

        paramsBuilder.checkIn(start)
        if (start != null && end == null) {
            paramsBuilder.checkOut(start.plusDays(1))
        } else {
            paramsBuilder.checkOut(end)
        }

        dateTextObservable.onNext(computeDateText(start, end))

        calendarTooltipTextObservable.onNext(computeTooltipText(start, end))

        requiredSearchParamsObserver.onNext(Unit)
    }

    var requiredSearchParamsObserver = endlessObserver<Unit> {
        searchButtonObservable.onNext(paramsBuilder.areRequiredParamsFilled())
        destinationObservable.onNext(paramsBuilder.hasOrigin())
        arrivalObservable.onNext(paramsBuilder.hasDestination())
        originObservable.onNext(paramsBuilder.hasOriginAndDestination())
    }

    val travelersObserver = endlessObserver<HotelTravelerParams> { update ->
        paramsBuilder.adults(update.numberOfAdults)
        paramsBuilder.children(update.children)
    }

    val originObserver = endlessObserver<SuggestionV4> { suggestion ->
        paramsBuilder.origin(suggestion)
        originTextObservable.onNext(StrUtils.formatAirport(suggestion))
        requiredSearchParamsObserver.onNext(Unit)
    }

    val destinationObserver = endlessObserver<SuggestionV4> { suggestion ->
        paramsBuilder.destination(suggestion)
        destinationTextObservable.onNext(StrUtils.formatAirport(suggestion))
        requiredSearchParamsObserver.onNext(Unit)
    }

    val suggestionTextChangedObserver = endlessObserver<Boolean> {
        if (it) paramsBuilder.origin(null) else paramsBuilder.destination(null)
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
            if (!paramsBuilder.hasOriginAndDestination()) {
                errorNoOriginObservable.onNext(paramsBuilder.hasOrigin())
            } else if (!paramsBuilder.hasStartAndEndDates()) {
                errorNoDatesObservable.onNext(Unit)
            }
        }
    }

    // Helpers
    private fun computeDateRangeText(start: LocalDate?, end: LocalDate?): String? {
        if (start == null && end == null) {
            return context.getResources().getString(R.string.select_dates)
        } else if (end == null) {
            return context.getResources().getString(R.string.select_checkout_date_TEMPLATE, DateUtils.localDateToMMMd(start))
        } else {
            return context.getResources().getString(R.string.calendar_instructions_date_range_TEMPLATE, DateUtils.localDateToMMMd(start), DateUtils.localDateToMMMd(end))
        }
    }

    private fun computeDateText(start: LocalDate?, end: LocalDate?): CharSequence {
        val dateRangeText = computeDateRangeText(start, end)
        val sb = SpannableBuilder()
        sb.append(dateRangeText)

        if (start != null && end != null) {
            val nightCount = JodaUtils.daysBetween(start, end)
            val nightsString = context.getResources().getQuantityString(R.plurals.length_of_stay, nightCount, nightCount)
            sb.append(" ");
            sb.append(context.getResources().getString(R.string.nights_count_TEMPLATE, nightsString), RelativeSizeSpan(0.8f))
        }
        return sb.build()
    }

    private fun computeTopTextForToolTip(start: LocalDate?, end: LocalDate?): String {
        if (start == null && end == null) {
            return context.getResources().getString(R.string.select_dates_proper_case)
        } else if (end == null) {
            return DateUtils.localDateToMMMd(start)
        } else {
            return context.getResources().getString(R.string.calendar_instructions_date_range_TEMPLATE, DateUtils.localDateToMMMd(start), DateUtils.localDateToMMMd(end))
        }
    }

    private fun computeTooltipText(start: LocalDate?, end: LocalDate?): Pair<String, String> {
        val resource =
                if (end == null) R.string.hotel_calendar_tooltip_bottom
                else R.string.hotel_calendar_bottom_drag_to_modify
        val instructions = context.getResources().getString(resource)
        return Pair(computeTopTextForToolTip(start, end), instructions)
    }
}

