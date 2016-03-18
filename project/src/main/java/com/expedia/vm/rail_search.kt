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
    private val paramsBuilder = RailSearchRequest.Builder()

    // Outputs
    val searchParamsObservable = PublishSubject.create<RailSearchRequest>()
    val datesObservable = BehaviorSubject.create<Pair<LocalDate?, LocalDate?>>()

    val searchObserver = endlessObserver<Unit> {
        paramsBuilder.origin(SuggestionV4())
        paramsBuilder.destination(SuggestionV4())

        var searchParams = paramsBuilder.build()
        searchParamsObservable.onNext(searchParams)
    }

    override fun onDatesChanged(dates: Pair<LocalDate?, LocalDate?>) {
        val (start, end) = dates
        datesObservable.onNext(dates)

        paramsBuilder.departDate(start)
        paramsBuilder.returnDate(end)

        dateTextObservable.onNext(computeDateRangeText(context, start, end))
        dateInstructionObservable.onNext(computeDateInstructionText(context, start, end))

        calendarTooltipTextObservable.onNext(computeTooltipText(start, end))
    }

    override fun startDate(): LocalDate? {
        return datesObservable?.value?.first
    }

    override fun endDate(): LocalDate? {
        return datesObservable?.value?.second
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
}
