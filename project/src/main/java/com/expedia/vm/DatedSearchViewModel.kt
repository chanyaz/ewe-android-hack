package com.expedia.vm

import android.content.Context
import com.expedia.bookings.R
import com.expedia.bookings.utils.DateUtils
import com.expedia.util.endlessObserver
import org.joda.time.LocalDate
import rx.subjects.PublishSubject

abstract class DatedSearchViewModel(val context: Context) {

    // Outputs
    val dateTextObservable = PublishSubject.create<CharSequence>()
    val dateInstructionObservable = PublishSubject.create<CharSequence>()
    val calendarTooltipTextObservable = PublishSubject.create<Pair<String, String>>()

    val datesObserver = endlessObserver<Pair<LocalDate?, LocalDate?>> { data ->
        onDatesChanged(data)
    }

    abstract fun onDatesChanged(dates: Pair<LocalDate?, LocalDate?>)
    abstract fun startDate(): LocalDate?
    abstract fun endDate(): LocalDate?

    protected fun computeTopTextForToolTip(start: LocalDate?, end: LocalDate?): String {
        if (start == null && end == null) {
            return context.resources.getString(R.string.select_dates_proper_case)
        } else if (end == null) {
            return DateUtils.localDateToMMMd(start)
        } else {
            return context.resources.getString(R.string.calendar_instructions_date_range_TEMPLATE, DateUtils.localDateToMMMd(start), DateUtils.localDateToMMMd(end))
        }
    }
}

