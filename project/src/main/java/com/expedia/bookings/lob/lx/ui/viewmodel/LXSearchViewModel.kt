package com.expedia.bookings.lob.lx.ui.viewmodel

import android.content.Context
import com.expedia.bookings.R
import com.expedia.bookings.data.SuggestionV4
import com.expedia.bookings.data.lx.LxSearchParams
import com.expedia.bookings.text.HtmlCompat
import com.expedia.bookings.utils.DateUtils
import com.expedia.bookings.utils.SpannableBuilder
import com.expedia.util.endlessObserver
import com.expedia.vm.BaseSearchViewModel
import com.squareup.phrase.Phrase
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
        locationTextObservable.onNext(HtmlCompat.stripHtml(suggestion.regionNames.displayName))
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
        dateAccessibilityObservable.onNext(computeDateText(start, end, true))
        dateInstructionObservable.onNext(computeDateInstructionText(start, end))

        requiredSearchParamsObserver.onNext(Unit)
        datesObservable.onNext(dates)
    }

    override fun computeDateText(start: LocalDate?, end: LocalDate?, isContentDescription: Boolean): CharSequence {
        val dateRangeText = if (isContentDescription) computeDateRangeText(start, end, true) else computeDateRangeText(start, end)
        val sb = SpannableBuilder()

        if (start != null && isContentDescription) {
            sb.append(Phrase.from(context, R.string.lx_search_date_range_cont_desc_TEMPLATE)
                    .put("date_range", dateRangeText)
                    .format().toString())

        } else {
            sb.append(dateRangeText)
        }
        return sb.build()
    }

    override fun computeDateInstructionText(start: LocalDate?, end: LocalDate?): CharSequence {
        if (start == null && end == null) {
            return context.getString(R.string.select_lx_search_dates)
        }
        return DateUtils.localDateToMMMd(start)
    }

    override fun computeDateRangeText(start: LocalDate?, end: LocalDate?, isContentDescription: Boolean): String? {
        if (start == null && end == null) {
            val stringID = if (isContentDescription) R.string.packages_search_dates_cont_desc else R.string.select_start_date
            return context.getString(stringID)
        } else {
            return DateUtils.localDateToMMMMd(start)
        }
    }

    override fun sameStartAndEndDateAllowed(): Boolean {
        return false
    }

}
