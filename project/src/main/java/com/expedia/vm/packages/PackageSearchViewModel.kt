package com.expedia.vm.packages

import android.content.Context
import android.text.style.RelativeSizeSpan
import com.expedia.bookings.R
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.SuggestionV4
import com.expedia.bookings.data.Traveler
import com.expedia.bookings.data.packages.PackageSearchParams
import com.expedia.bookings.enums.PassengerCategory
import com.expedia.bookings.utils.DateUtils
import com.expedia.bookings.utils.SpannableBuilder
import com.expedia.bookings.utils.StrUtils
import com.expedia.util.endlessObserver
import com.expedia.vm.BaseSearchViewModel
import com.mobiata.android.time.util.JodaUtils
import com.squareup.phrase.Phrase
import org.joda.time.LocalDate
import rx.subjects.BehaviorSubject
import rx.subjects.PublishSubject

class PackageSearchViewModel(context: Context) : BaseSearchViewModel(context) {
    override val paramsBuilder = PackageSearchParams.Builder(getMaxSearchDurationDays(), getMaxDateRange())

    // Outputs
    val searchParamsObservable = PublishSubject.create<PackageSearchParams>()


    override fun getMaxSearchDurationDays(): Int {
        return context.resources.getInteger(R.integer.calendar_max_days_package_stay);
    }

    override fun getMaxDateRange(): Int {
        return context.resources.getInteger(R.integer.calendar_max_package_selectable_date_range)
    }

    // Inputs

    val isInfantInLapObserver = endlessObserver<Boolean> { isInfantInLap ->
        paramsBuilder.infantSeatingInLap(isInfantInLap)
    }

    val suggestionTextChangedObserver = endlessObserver<Boolean> {
        if (it) paramsBuilder.origin(null) else paramsBuilder.destination(null)
        requiredSearchParamsObserver.onNext(Unit)
    }

    val searchObserver = endlessObserver<Unit> {
        if (paramsBuilder.areRequiredParamsFilled()) {
            if (paramsBuilder.isOriginSameAsDestination()) {
                errorOriginSameAsDestinationObservable.onNext(context.getString(R.string.error_same_flight_departure_arrival))
            } else if (!paramsBuilder.hasValidDateDuration()) {
                errorMaxDurationObservable.onNext(context.getString(R.string.hotel_search_range_error_TEMPLATE, getMaxSearchDurationDays()))
            } else if (!paramsBuilder.isWithinDateRange()) {
               errorMaxRangeObservable.onNext(context.getString(R.string.error_date_too_far, getMaxSearchDurationDays()))
            } else {
                val packageSearchParams = paramsBuilder.build()
                updateDbTravelers(packageSearchParams) // This is required for the checkout screen to correctly populate traveler entry screen.
                searchParamsObservable.onNext(packageSearchParams)
            }
        } else {
            if (!paramsBuilder.hasOriginAndDestination()) {
                errorNoDestinationObservable.onNext(Unit)
            } else if (!paramsBuilder.hasStartAndEndDates()) {
                errorNoDatesObservable.onNext(Unit)
            }
        }
    }

    override fun isStartDateOnlyAllowed(): Boolean {
        return false
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
            return Phrase.from(context.resources, R.string.select_return_date_TEMPLATE).put("startdate", DateUtils.localDateToMMMd(start)).format().toString()
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

    private fun updateDbTravelers(params: PackageSearchParams) {
        // This is required for the checkout screen to correctly populate traveler entry screen.
        val travelerList = Db.getTravelers()
        if (travelerList.isNotEmpty()) {
            travelerList.clear()
        }

        for (i in 1..params.adults) {
            val traveler = Traveler()
            traveler.setPassengerCategory(PassengerCategory.ADULT)
            travelerList.add(traveler)
        }
        for (child in params.children) {
            val traveler = Traveler()
            var category = PassengerCategory.CHILD
            if (child < 2) {
                category = if (params.infantSeatingInLap) PassengerCategory.INFANT_IN_LAP else PassengerCategory.INFANT_IN_SEAT
            }
            traveler.setPassengerCategory(category)
            travelerList.add(traveler)
        }
        Db.setTravelers(travelerList)
    }
}

