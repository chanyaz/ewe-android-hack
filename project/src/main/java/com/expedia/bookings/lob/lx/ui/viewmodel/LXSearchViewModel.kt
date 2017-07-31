package com.expedia.bookings.lob.lx.ui.viewmodel

import android.content.Context
import com.expedia.bookings.R
import com.expedia.bookings.data.SuggestionV4
import com.expedia.bookings.data.lx.LxSearchParams
import com.expedia.bookings.text.HtmlCompat
import com.expedia.bookings.utils.DateUtils
import com.expedia.bookings.utils.LocaleBasedDateFormatUtils
import com.expedia.util.endlessObserver
import com.expedia.vm.BaseSearchViewModel
import org.joda.time.LocalDate
import rx.subjects.PublishSubject

class LXSearchViewModel(context: Context) : BaseSearchViewModel(context) {

    val lxParamsBuilder = LxSearchParams.Builder()
    val searchParamsObservable = PublishSubject.create<LxSearchParams>()

    // Inputs
    override var requiredSearchParamsObserver = endlessObserver<Unit> {
        searchButtonObservable.onNext(getParamsBuilder().areRequiredParamsFilled())
    }

    override val destinationLocationObserver = endlessObserver<SuggestionV4> { suggestion ->
        getParamsBuilder().destination(suggestion)
        locationTextObservable.onNext(HtmlCompat.stripHtml(suggestion.regionNames.displayName))
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
        var (start, end) = dates

        dateTextObservable.onNext(getCalendarCardDateText(start, end, false))
        dateAccessibilityObservable.onNext(getCalendarCardDateText(start, end, true))
        dateInstructionObservable.onNext(getDateInstructionText(start, end))

        if (start != null && end == null) {
            end = start.plusDays(context.resources.getInteger(R.integer.lx_default_search_range))
        }

        super.onDatesChanged(Pair(start, end))
    }

    override fun getDateInstructionText(start: LocalDate?, end: LocalDate?): CharSequence {
        if (start == null && end == null) {
            return context.getString(R.string.select_lx_search_dates)
        }
        return LocaleBasedDateFormatUtils.localDateToMMMd(start!!)
    }

    override fun sameStartAndEndDateAllowed(): Boolean {
        return false
    }

    override fun getCalendarToolTipInstructions(start: LocalDate?, end: LocalDate?): String {
        return context.getString(R.string.calendar_drag_to_modify)
    }

    override fun getEmptyDateText(forContentDescription: Boolean): String {
        if (forContentDescription) {
           return getDateAccessibilityText(context.getString(R.string.select_start_date), "")
        } else {
            return context.getString(R.string.select_start_date)
        }
    }

    override fun getNoEndDateText(start: LocalDate?, forContentDescription: Boolean): String {
        if (forContentDescription) {
            return getDateAccessibilityText(context.getString(R.string.select_start_date), LocaleBasedDateFormatUtils.localDateToMMMMd(start!!))
        } else {
            return LocaleBasedDateFormatUtils.localDateToMMMMd(start!!)
        }
    }

    override fun getCompleteDateText(start: LocalDate, end: LocalDate, forContentDescription: Boolean): String {
        if (forContentDescription) {
            return getDateAccessibilityText(context.getString(R.string.select_start_date), LocaleBasedDateFormatUtils.localDateToMMMMd(start))
        } else {
            return LocaleBasedDateFormatUtils.localDateToMMMMd(start)
        }
    }
}
