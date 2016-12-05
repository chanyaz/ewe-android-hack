package com.expedia.vm.packages

import android.content.Context
import android.text.style.RelativeSizeSpan
import com.expedia.bookings.R
import com.expedia.bookings.data.SuggestionV4
import com.expedia.bookings.data.packages.PackageSearchParams
import com.expedia.bookings.text.HtmlCompat
import com.expedia.bookings.utils.DateUtils
import com.expedia.bookings.utils.SpannableBuilder
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.utils.validation.TravelerValidator
import com.expedia.util.endlessObserver
import com.expedia.vm.BaseSearchViewModel
import com.mobiata.android.time.util.JodaUtils
import com.squareup.phrase.Phrase
import org.joda.time.LocalDate
import rx.subjects.PublishSubject
import javax.inject.Inject

class PackageSearchViewModel(context: Context) : BaseSearchViewModel(context) {
    lateinit var travelerValidator: TravelerValidator
        @Inject set

    // Inputs
    val isInfantInLapObserver = endlessObserver<Boolean> { isInfantInLap ->
        getParamsBuilder().infantSeatingInLap(isInfantInLap)
    }
    // Outputs
    val searchParamsObservable = PublishSubject.create<PackageSearchParams>()

    val packageParamsBuilder = PackageSearchParams.Builder(getMaxSearchDurationDays(), getMaxDateRange())

    init {
        Ui.getApplication(context).travelerComponent().inject(this)
    }

    val searchObserver = endlessObserver<Unit> {
        if (getParamsBuilder().areRequiredParamsFilled()) {
            if (getParamsBuilder().isOriginSameAsDestination()) {
                errorOriginSameAsDestinationObservable.onNext(context.getString(R.string.error_same_flight_departure_arrival))
            } else if (!getParamsBuilder().hasValidDateDuration()) {
                errorMaxDurationObservable.onNext(context.getString(R.string.hotel_search_range_error_TEMPLATE, getMaxSearchDurationDays()))
            } else if (!getParamsBuilder().isWithinDateRange()) {
                errorMaxRangeObservable.onNext(context.getString(R.string.error_date_too_far, getMaxSearchDurationDays()))
            } else {
                val packageSearchParams = getParamsBuilder().build()
                travelerValidator.updateForNewSearch(packageSearchParams)
                searchParamsObservable.onNext(packageSearchParams)
            }
        } else {
            if (!getParamsBuilder().hasOriginAndDestination()) {
                errorNoDestinationObservable.onNext(Unit)
            } else if (!getParamsBuilder().hasStartAndEndDates()) {
                errorNoDatesObservable.onNext(Unit)
            }
        }
    }

    override fun getMaxSearchDurationDays(): Int {
        return context.resources.getInteger(R.integer.calendar_max_days_package_stay);
    }

    override fun getMaxDateRange(): Int {
        return context.resources.getInteger(R.integer.max_calendar_selectable_date_range)
    }

    override val destinationLocationObserver = endlessObserver<SuggestionV4> { suggestion ->
        getParamsBuilder().destination(suggestion)
        formattedDestinationObservable.onNext(HtmlCompat.stripHtml(suggestion.regionNames.displayName))
        requiredSearchParamsObserver.onNext(Unit)
    }

    override fun getParamsBuilder(): PackageSearchParams.Builder {
        return packageParamsBuilder
    }

    override fun isStartDateOnlyAllowed(): Boolean {
        return false
    }

    override fun onDatesChanged(dates: Pair<LocalDate?, LocalDate?>) {
        var (start, end) = dates
        //dates cant be the same, shift end by 1 day
        if (start != null && (end == null || end.isEqual(start))) {
            end = start.plusDays(1)
        }

        dateTextObservable.onNext(computeDateText(start, end))
        dateAccessibilityObservable.onNext(computeDateText(start, end, true))
        dateInstructionObservable.onNext(computeDateInstructionText(start, end))
        calendarTooltipTextObservable.onNext(computeTooltipText(start, end))

        super.onDatesChanged(Pair(start, end))
    }

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
            return Phrase.from(context.resources, R.string.select_return_date_TEMPLATE).put("startdate", DateUtils.localDateToMMMd(start)).format().toString()
        } else {
            return Phrase.from(context, R.string.calendar_instructions_date_range_TEMPLATE).put("startdate", DateUtils.localDateToMMMd(start)).put("enddate", DateUtils.localDateToMMMd(end)).format().toString()
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

    override fun sameStartAndEndDateAllowed(): Boolean {
        return false
    }
}
