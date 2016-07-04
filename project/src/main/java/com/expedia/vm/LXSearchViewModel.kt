package com.expedia.vm

import android.content.Context
import android.text.Html
import com.expedia.bookings.R
import com.expedia.bookings.data.SuggestionV4
import com.expedia.bookings.data.lx.LxSearchParams
import com.expedia.bookings.utils.DateUtils
import com.expedia.util.endlessObserver
import org.joda.time.LocalDate
import rx.subjects.PublishSubject

class LXSearchViewModel(context: Context) : BaseSearchViewModel(context) {

    val lxParamsBuilder = LxSearchParams.Builder()
    val searchParamsObservable = PublishSubject.create<LxSearchParams>()

    // Inputs
    override var requiredSearchParamsObserver = endlessObserver<Unit> {
        searchButtonObservable.onNext(getParamsBuilder().areRequiredParamsFilled())
        destinationValidObservable.onNext(getParamsBuilder().hasDestinationLocation())
    }

    override val destinationLocationObserver = endlessObserver<SuggestionV4> { suggestion ->
        getParamsBuilder().destination(suggestion)
        locationTextObservable.onNext(Html.fromHtml(suggestion.regionNames.displayName).toString())
        requiredSearchParamsObserver.onNext(Unit)
    }

    val suggestionTextChangedObserver = endlessObserver<Unit> {
        getParamsBuilder().destination(null)
        requiredSearchParamsObserver.onNext(Unit)
    }

    val searchObserver = endlessObserver<Unit> {
        if (getParamsBuilder().areRequiredParamsFilled()) {
            val lxSearchParams = getParamsBuilder().build()
            searchParamsObservable.onNext(lxSearchParams)
        } else {
            if (!getParamsBuilder().hasDestinationLocation()) {
                errorNoDestinationObservable.onNext(Unit)
            } else if (!getParamsBuilder().hasStartAndEndDates()) {
                errorNoDatesObservable.onNext(Unit)
            }
        }
    }

    override fun getParamsBuilder(): LxSearchParams.Builder {
        return lxParamsBuilder
    }

    override fun isStartDateOnlyAllowed(): Boolean {
        return true // check-in and out dates required
    }

    override fun getMaxSearchDurationDays(): Int {
        return context.resources.getInteger(R.integer.calendar_max_selection_date_range_lx)
    }

    override fun getMaxDateRange(): Int {
        return context.resources.getInteger(R.integer.calendar_max_days_lx_search)
    }

    override fun onDatesChanged(dates: Pair<LocalDate?, LocalDate?>) {
        val (start, end) = dates

        getParamsBuilder().startDate(start)
        if (start != null && end == null) {
            getParamsBuilder().endDate(start.plusDays(context.resources.getInteger(R.integer.lx_default_search_range)))
        } else {
            getParamsBuilder().endDate(end)
        }

        dateTextObservable.onNext(computeDateText(start, end))
        dateInstructionObservable.onNext(computeDateInstructionText(start, end))

        requiredSearchParamsObserver.onNext(Unit)
        datesObservable.onNext(dates)
    }

    override fun computeDateRangeText(start: LocalDate?, end: LocalDate?, isContentDescription: Boolean): String? {
        if (start == null && end == null) {
            var stringID = if (isContentDescription) R.string.packages_search_dates_cont_desc else R.string.select_dates
            return context.resources.getString(stringID)
        } else {
            return DateUtils.localDateToMMMMd(start)
        }
    }
}
