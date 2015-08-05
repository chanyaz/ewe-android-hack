package com.expedia.vm

import android.content.Context
import android.text.Html
import com.expedia.bookings.R
import com.expedia.bookings.data.hotels.HotelSearchParams
import com.expedia.bookings.data.hotels.SuggestionV4
import com.expedia.bookings.utils.DateUtils
import com.expedia.bookings.utils.StrUtils
import com.expedia.util.endlessObserver
import org.joda.time.LocalDate
import rx.Observer
import rx.subjects.PublishSubject

class HotelSearchViewModel(val context: Context) {
    private val paramsBuilder = HotelSearchParams.Builder()

    // Outputs
    val searchParamsObservable = PublishSubject.create<HotelSearchParams>()
    val dateTextObservable = PublishSubject.create<String>()
    val calendarTooltipTextObservable = PublishSubject.create<Pair<String,String>>()
    val locationTextObservable = PublishSubject.create<String>()
    val errorNoOriginObservable = PublishSubject.create<Unit>()
    val errorNoDatesObservable = PublishSubject.create<Unit>()

    // Inputs
    val datesObserver = endlessObserver<Pair<LocalDate?, LocalDate?>> { data ->
        val (start, end) = data

        paramsBuilder.checkIn(start)
        paramsBuilder.checkOut(end)

        dateTextObservable.onNext(computeDateText(start, end))

        calendarTooltipTextObservable.onNext(computeTooltipText(start, end))
    }

    val travelersObserver = endlessObserver<HotelTravelerParams> { update ->
        paramsBuilder.adults(update.numberOfAdults)
        paramsBuilder.children(update.children)
    }

    val locationObserver = endlessObserver<SuggestionV4> { suggestion ->
        paramsBuilder.city(suggestion)
        locationTextObservable.onNext(Html.fromHtml(StrUtils.formatCityName(suggestion.regionNames.displayName)).toString())
    }

    val searchObserver = endlessObserver<Unit> {
        if (paramsBuilder.areRequiredParamsFilled()) {
            searchParamsObservable.onNext(paramsBuilder.build())
        } else {
            if (!paramsBuilder.hasOrigin()) {
                errorNoOriginObservable.onNext(Unit)
            }
            else if (!paramsBuilder.hasStartAndEndDates()) {
                errorNoDatesObservable.onNext(Unit)
            }
        }
    }

    // Helpers
    private fun computeDateText(start: LocalDate?, end: LocalDate?): String {
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
                if (end == null) R.string.calendar_tooltip_bottom_select_return_date
                else R.string.calendar_tooltip_bottom_drag_to_modify
        val instructions = context.getResources().getString(resource)
        return Pair(computeDateText(start, end), instructions)
    }
}
